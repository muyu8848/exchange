package com.exchange.background.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.background.domain.AccountRole;
import com.exchange.background.domain.BackgroundAccount;
import com.exchange.background.domain.Menu;
import com.exchange.background.domain.RoleMenu;
import com.exchange.background.param.AddBackgroundAccountParam;
import com.exchange.background.param.AssignRoleParam;
import com.exchange.background.param.BackgroundAccountQueryCondParam;
import com.exchange.background.param.LoginParam;
import com.exchange.background.param.UpdateBackgroundAccountParam;
import com.exchange.background.param.UpdateLoginPwdParam;
import com.exchange.background.repo.AccountRoleRepo;
import com.exchange.background.repo.BackgroundAccountRepo;
import com.exchange.background.repo.MenuRepo;
import com.exchange.background.repo.RoleMenuRepo;
import com.exchange.background.vo.BackgroundAccountVO;
import com.exchange.background.vo.MenuRouteVO;
import com.exchange.background.vo.RoleVO;
import com.exchange.background.vo.SuperAdminVO;
import com.exchange.common.exception.BizException;
import com.exchange.common.googleauth.GoogleAuthenticator;
import com.exchange.security.service.LoginAttemptService;
import com.exchange.common.utils.PwdUtils;
import com.exchange.common.vo.LoginResult;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.TokenInfo;
import com.exchange.constants.Constant;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class BackgroundAccountService {

	@Autowired
	private MenuRepo menuRepo;

	@Autowired
	private RoleMenuRepo roleMenuRepo;

	@Autowired
	private AccountRoleRepo accountRoleRepo;

	@Autowired
	private BackgroundAccountRepo backgroundAccountRepo;

	@Autowired
	private LoginAttemptService loginAttemptService;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<SuperAdminVO> findSuperAdmin() {
		return SuperAdminVO.convertFor(backgroundAccountRepo.findBySuperAdminFlagTrueAndDeletedFlagIsFalse());
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public LoginResult login(@Valid LoginParam param) {
		String accountKey = "login:fail:admin:" + param.getUserName();
		String ipKey = "login:fail:ip:" + param.getIp();

		if (loginAttemptService.isIpLocked(ipKey)) {
			return LoginResult.fail(null, "当前网络登录异常，请稍后再试");
		}
		if (loginAttemptService.isAccountLocked(accountKey)) {
			return LoginResult.fail(null, "登录失败次数过多，请稍后再试");
		}

		BackgroundAccount account = backgroundAccountRepo.findByUserNameAndDeletedFlagIsFalse(param.getUserName());

		// 账号不存在 → accountId = null，只记 IP
		if (account == null) {
			loginAttemptService.recordIpLoginFailure(ipKey);
			return LoginResult.fail(null, "账号或密码不正确");
		}

		// 账号存在，密码错误
		if (!PwdUtils.pwdVerify(param.getLoginPwd(), account.getLoginPwd())) {
			loginAttemptService.recordIpLoginFailure(ipKey);
			loginAttemptService.recordAccountLoginFailure(accountKey);
			return LoginResult.fail(account.getId(), "账号或密码不正确");
		}

		if (Constant.启用状态_停用.equals(account.getStatus())) {
			return LoginResult.fail(account.getId(), "账号已被停用");
		}

		if (!account.getSuperAdminFlag()) {
			List<AccountRole> accountRoles = accountRoleRepo.findByAccountId(account.getId());
			if (CollUtil.isEmpty(accountRoles)) {
				return LoginResult.fail(account.getId(), "请联系管理员授予角色");
			}
			List<String> roleIds = accountRoles.stream().map(AccountRole::getRoleId).collect(Collectors.toList());
			List<RoleMenu> roleMenus = roleMenuRepo.findByRoleIdIn(roleIds);
			if (CollUtil.isEmpty(roleMenus)) {
				return LoginResult.fail(account.getId(), "请联系管理员授予菜单权限");
			}
		}

		if (StrUtil.isNotBlank(account.getGoogleSecretKey())) {
			if (StrUtil.isBlank(param.getGoogleVerCode())) {
				loginAttemptService.recordIpLoginFailure(ipKey);
				loginAttemptService.recordAccountLoginFailure(accountKey);
				return LoginResult.fail(account.getId(), "请输入谷歌验证码");
			}
			if (!GoogleAuthenticator.checkCode(account.getGoogleSecretKey(), param.getGoogleVerCode(),
					System.currentTimeMillis())) {
				loginAttemptService.recordIpLoginFailure(ipKey);
				loginAttemptService.recordAccountLoginFailure(accountKey);
				return LoginResult.fail(account.getId(), "谷歌验证码不正确");
			}
		}

		loginAttemptService.clear(accountKey);
		loginAttemptService.clear(ipKey);

		account.setLatelyLoginTime(LocalDateTime.now());
		backgroundAccountRepo.save(account);
		return LoginResult.success(account.getId(), buildLoginTokenInfo(account));
	}

	public TokenInfo buildLoginTokenInfo(BackgroundAccount backgroundAccount) {
		List<String> permissions = findPermissionByAccountId(backgroundAccount.getId());
		Map<String, Object> extra = new HashMap<>();
		extra.put("accountType", Constant.登录会话账号类型_后台);
		extra.put("accountName", backgroundAccount.getUserName());
		StpUtil.login(backgroundAccount.getId(), new SaLoginParameter().setIsLastingCookie(false)
				.setDeviceType(Constant.登录设备类型_WEB).setExtraData(extra));
		StpUtil.getSession().set("permissions", permissions);
		StpUtil.getSession().set("accountName", backgroundAccount.getUserName());
		StpUtil.getSession().set("subSystem", Constant.子系统_后台管理);

		TokenInfo tokenInfo = TokenInfo.build();
		tokenInfo.setPermissions(permissions);
		return tokenInfo;
	}

	public void logout() {
		if (StpUtil.isLogin()) {
			StpUtil.logout();
		}
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateBackgroundAccount(@Valid UpdateBackgroundAccountParam param) {
		BackgroundAccount existAccount = backgroundAccountRepo.findByUserNameAndDeletedFlagIsFalse(param.getUserName());
		if (existAccount != null && !existAccount.getId().equals(param.getId())) {
			throw new BizException("账号已存在");
		}
		if (existAccount != null && existAccount.getSuperAdminFlag()) {
			throw new BizException("无权操作超级管理员");
		}
		BackgroundAccount userAccount = backgroundAccountRepo.getOne(param.getId());
		BeanUtils.copyProperties(param, userAccount);
		backgroundAccountRepo.save(userAccount);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<BackgroundAccountVO> findBackgroundAccountByPage(BackgroundAccountQueryCondParam param,
			String currentAccountId) {
		Page<BackgroundAccount> result = backgroundAccountRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("createTime"))));
		PageResult<BackgroundAccountVO> pageResult = new PageResult<>(
				BackgroundAccountVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
		return pageResult;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateCurrentAccountLoginPwd(@Valid UpdateLoginPwdParam param) {
		BackgroundAccount backgroundAccount = backgroundAccountRepo.getOne(param.getId());
		if (!PwdUtils.pwdVerify(param.getOldPwd(), backgroundAccount.getLoginPwd())) {
			throw new BizException("旧密码不正确");
		}

		backgroundAccount.setLoginPwd(PwdUtils.pwdEncrypt(param.getNewPwd()));
		backgroundAccountRepo.save(backgroundAccount);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateLoginPwd(@NotBlank String id, @NotBlank String newPwd) {
		BackgroundAccount account = backgroundAccountRepo.getOne(id);
		if (account.getSuperAdminFlag()) {
			throw new BizException("无权操作超级管理员");
		}
		account.setLoginPwd(PwdUtils.pwdEncrypt(newPwd));
		backgroundAccountRepo.save(account);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public BackgroundAccountVO findBackgroundAccountById(@NotBlank String accountId) {
		return BackgroundAccountVO.convertFor(backgroundAccountRepo.getOne(accountId));
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void addBackgroundAccount(@Valid AddBackgroundAccountParam param) {
		BackgroundAccount backgroundAccount = backgroundAccountRepo
				.findByUserNameAndDeletedFlagIsFalse(param.getUserName());
		if (backgroundAccount != null) {
			throw new BizException("账号已存在");
		}

		BackgroundAccount newAccount = BackgroundAccount.build(param.getUserName(),
				PwdUtils.pwdEncrypt(param.getLoginPwd()));
		backgroundAccountRepo.save(newAccount);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void delBackgroundAccount(@NotBlank String accountId) {
		BackgroundAccount account = backgroundAccountRepo.getOne(accountId);
		if (account.getSuperAdminFlag()) {
			throw new BizException("无权操作超级管理员");
		}
		account.deleted();
		backgroundAccountRepo.save(account);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<String> findPermissionByAccountId(String accountId) {
		BackgroundAccount account = backgroundAccountRepo.getOne(accountId);
		// 超管下发通配符：前端指令与 Sa-Token 的 @SaCheckPermission 都认 `*`，
		// 避免后台尚未配置任何权限节点时超管自己也被锁死
		if (Boolean.TRUE.equals(account.getSuperAdminFlag())) {
			List<String> wildcard = new ArrayList<>();
			wildcard.add("*");
			return wildcard;
		}
		List<String> roleIds = new ArrayList<>();
		List<AccountRole> accountRoles = accountRoleRepo.findByAccountIdAndRoleDeletedFlagFalse(accountId);
		// 无角色 = 无权限。原实现此处返回全部权限，等同于「删光角色即可越权」，必须堵住
		if (CollectionUtil.isEmpty(accountRoles)) {
			return new ArrayList<String>();
		}
		for (AccountRole accountRole : accountRoles) {
			roleIds.add(accountRole.getRoleId());
		}
		List<String> menuIds = new ArrayList<>();
		List<RoleMenu> roleMenus = roleMenuRepo.findByRoleIdIn(roleIds);
		for (RoleMenu roleMenu : roleMenus) {
			menuIds.add(roleMenu.getMenuId());
		}
		List<Menu> menus = menuRepo.findByIdInAndDeletedFlagFalseOrderBySortNum(menuIds);
		return buildPermission(menus);
	}

	/**
	 * 严格按已分配的权限节点（menu_3）下发按钮权限：
	 * 只分配了菜单、未勾选任何权限时，该菜单下不会自动获得按钮权限。
	 */
	public List<String> buildPermission(List<Menu> menus) {
		Set<String> permissionSet = new HashSet<>();
		for (Menu menu : menus) {
			if (Constant.菜单类型_权限.equals(menu.getType())) {
				permissionSet.add(menu.getUrl());
			}
		}
		return new ArrayList<String>(permissionSet);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<MenuRouteVO> findMenuRouteByAccountId(String accountId) {
		BackgroundAccount backgroundAccount = backgroundAccountRepo.getOne(accountId);
		if (backgroundAccount.getSuperAdminFlag()) {
			List<Menu> menus = menuRepo.findByDeletedFlagFalseOrderBySortNum();
			return buildMenuRoute(menus);
		}
		List<String> roleIds = new ArrayList<>();
		List<AccountRole> accountRoles = accountRoleRepo.findByAccountIdAndRoleDeletedFlagFalse(accountId);
		if (CollectionUtil.isEmpty(accountRoles)) {
			return new ArrayList<>();
		}
		for (AccountRole accountRole : accountRoles) {
			roleIds.add(accountRole.getRoleId());
		}
		Map<String, Menu> menuMap = new HashMap<>();
		List<String> menuIds = new ArrayList<>();
		List<RoleMenu> roleMenus = roleMenuRepo.findByRoleIdIn(roleIds);
		for (RoleMenu roleMenu : roleMenus) {
			menuIds.add(roleMenu.getMenuId());
		}
		List<Menu> menus = menuRepo.findByIdInAndDeletedFlagFalseOrderBySortNum(menuIds);
		List<String> menu1And2Ids = new ArrayList<>();
		for (Menu menu : menus) {
			menuMap.put(menu.getId(), menu);
			if (Constant.菜单类型_权限.equals(menu.getType())) {
				menu1And2Ids.add(menu.getParentId());
				Menu menu2 = menuRepo.getOne(menu.getParentId());
				if (menu2 != null && StrUtil.isNotBlank(menu2.getParentId())) {
					menu1And2Ids.add(menu2.getParentId());
				}
			} else if (Constant.菜单类型_二级菜单.equals(menu.getType())) {
				if (StrUtil.isNotBlank(menu.getParentId())) {
					menu1And2Ids.add(menu.getParentId());
				}
			}
		}
		List<Menu> menu1And2s = menuRepo.findByIdInAndDeletedFlagFalseOrderBySortNum(new ArrayList<>(menu1And2Ids));
		for (Menu menu : menu1And2s) {
			menuMap.put(menu.getId(), menu);
		}
		List<Menu> allMenus = new ArrayList<>(menuMap.values());
		Collections.sort(allMenus, new Comparator<Menu>() {

			@Override
			public int compare(Menu o1, Menu o2) {
				int s1 = o1.getSortNum() != null ? o1.getSortNum() : 0;
				int s2 = o2.getSortNum() != null ? o2.getSortNum() : 0;
				return Integer.compare(s1, s2);
			}
		});
		return buildMenuRoute(allMenus);
	}

	public List<MenuRouteVO> buildMenuRoute(List<Menu> menus) {
		List<MenuRouteVO> vos = new ArrayList<>();
		List<Menu> menu1s = new ArrayList<>();
		List<Menu> menu2s = new ArrayList<>();
		for (Menu m : menus) {
			if (Constant.菜单类型_一级菜单.equals(m.getType())) {
				menu1s.add(m);
			}
			if (Constant.菜单类型_二级菜单.equals(m.getType())) {
				menu2s.add(m);
			}
		}
		for (Menu menu1 : menu1s) {
			MenuRouteVO vo = MenuRouteVO.convertFor(menu1);
			for (Menu menu2 : menu2s) {
				if (menu1.getId().equals(menu2.getParentId())) {
					vo.getChildren().add(MenuRouteVO.convertFor(menu2));
				}
			}
			if (CollUtil.isNotEmpty(vo.getChildren())) {
				vo.getChildren().sort(Comparator.comparingInt(m -> {
					Integer sortNum = m.getSortNum();
					return sortNum != null ? sortNum : 0;
				}));
				vo.setComponent(null);
				vo.setRedirect("/page/" + vo.getPath() + "/" + vo.getChildren().get(0).getPath());
			}
			vos.add(vo);
		}
		return vos;
	}

	/**
	 * 保存账号的角色分配。
	 *
	 * @return 角色分配是否发生变化（用于决定是否需要强制该账号重新登录）
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public boolean assignRole(@Valid AssignRoleParam param) {
		List<AccountRole> assignRoles = accountRoleRepo.findByAccountId(param.getAccountId());
		Set<String> oldRoleIds = assignRoles.stream().map(AccountRole::getRoleId).collect(Collectors.toSet());
		List<String> roleIds = CollUtil.emptyIfNull(param.getRoleIds());
		boolean changed = !oldRoleIds.equals(new HashSet<>(roleIds));
		accountRoleRepo.deleteAll(assignRoles);
		for (String roleId : roleIds) {
			accountRoleRepo.save(AccountRole.build(param.getAccountId(), roleId));
		}
		return changed;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<RoleVO> findRoleByAccountId(@NotBlank String accountId) {
		List<RoleVO> roleVOs = new ArrayList<>();
		List<AccountRole> accountRoles = accountRoleRepo.findByAccountId(accountId);
		for (AccountRole accountRole : accountRoles) {
			roleVOs.add(RoleVO.convertFor(accountRole.getRole()));
		}
		return roleVOs;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void bindGoogleAuth(@NotBlank String id, @NotBlank String googleSecretKey, @NotBlank String googleVerCode) {
		if (!GoogleAuthenticator.checkCode(googleSecretKey, googleVerCode, System.currentTimeMillis())) {
			throw new BizException("谷歌验证码不正确");
		}
		BackgroundAccount account = backgroundAccountRepo.getOne(id);
		if (account.getGoogleAuthBindTime() != null) {
			throw new BizException("该账号已绑定谷歌验证器");
		}
		account.bindGoogleAuth(googleSecretKey);
		backgroundAccountRepo.save(account);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void unBindGoogleAuth(@NotBlank String id, @NotBlank String operatorId) {
		BackgroundAccount operator = backgroundAccountRepo.getOne(operatorId);
		if (!Boolean.TRUE.equals(operator.getSuperAdminFlag())) {
			throw new BizException("无权操作");
		}
		BackgroundAccount account = backgroundAccountRepo.getOne(id);
		if (Boolean.TRUE.equals(account.getSuperAdminFlag())) {
			throw new BizException("不能解绑超级管理员的谷歌验证器");
		}
		account.unBindGoogleAuth();
		backgroundAccountRepo.save(account);
	}

}

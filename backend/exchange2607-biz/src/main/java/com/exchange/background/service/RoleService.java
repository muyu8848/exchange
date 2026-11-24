package com.exchange.background.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.background.domain.Role;
import com.exchange.background.domain.RoleMenu;
import com.exchange.background.param.AssignMenuParam;
import com.exchange.background.param.RoleParam;
import com.exchange.background.repo.RoleMenuRepo;
import com.exchange.background.repo.RoleRepo;
import com.exchange.background.vo.MenuVO;
import com.exchange.background.vo.RoleVO;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class RoleService {

	@Autowired
	private RoleRepo roleRepo;

	@Autowired
	private RoleMenuRepo roleMenuRepo;

	/**
	 * 保存角色的菜单分配。
	 *
	 * @return 菜单分配是否发生变化（用于决定是否需要强制相关账号重新登录）
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public boolean assignMenu(@Valid AssignMenuParam param) {
		List<RoleMenu> roleMenus = roleMenuRepo.findByRoleId(param.getRoleId());
		Set<String> oldMenuIds = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toSet());
		List<String> menuIds = CollUtil.emptyIfNull(param.getMenuIds());
		boolean changed = !oldMenuIds.equals(new HashSet<>(menuIds));
		roleMenuRepo.deleteAll(roleMenus);
		for (String menuId : menuIds) {
			roleMenuRepo.save(RoleMenu.build(param.getRoleId(), menuId));
		}
		return changed;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<MenuVO> findMenuByRoleId(@NotBlank String roleId) {
		List<MenuVO> menuVOs = new ArrayList<>();
		List<RoleMenu> roleMenus = roleMenuRepo.findByRoleId(roleId);
		for (RoleMenu roleMenu : roleMenus) {
			menuVOs.add(MenuVO.convertFor(roleMenu.getMenu()));
		}
		return menuVOs;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<RoleVO> findAll() {
		return RoleVO.convertFor(roleRepo.findByDeletedFlagFalse());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public RoleVO findById(@NotBlank String id) {
		return RoleVO.convertFor(roleRepo.getOne(id));
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void del(@NotBlank String id) {
		Role role = roleRepo.getOne(id);
		role.deleted();
		roleRepo.save(role);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void addOrUpdate(@Valid RoleParam param) {
		if (StrUtil.isBlank(param.getId())) {
			Role role = param.convertToPo();
			roleRepo.save(role);
		} else {
			Role role = roleRepo.getOne(param.getId());
			BeanUtils.copyProperties(param, role);
			roleRepo.save(role);
		}
	}
}

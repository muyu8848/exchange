package com.exchange.security.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.exchange.background.domain.AccountRole;
import com.exchange.background.repo.AccountRoleRepo;
import com.exchange.constants.Constant;
import com.exchange.security.domain.AccountLoginSession;
import com.exchange.security.repo.AccountLoginSessionRepo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 后台登录会话：面向运营后台，只查 ADMIN + ONLINE。
 * <p>
 * 提供按账号、按角色踢出在线会话的能力，供权限/角色/密码变更后强制相关账号重新登录。
 * 踢出原因统一由 {@link AccountLoginSessionService#markKickoutReason} 记录。
 */
@Validated
@Service
public class BackgroundAccountLoginSessionService {

	private static final String ACCOUNT_TYPE_ADMIN = Constant.登录会话账号类型_后台;

	private static final String STATUS_ONLINE = Constant.登录会话状态_在线;

	@Autowired
	private AccountLoginSessionRepo accountLoginSessionRepo;

	@Autowired
	private AccountLoginSessionService accountLoginSessionService;

	@Autowired
	private AccountRoleRepo accountRoleRepo;

	/** 踢出单个后台账号的所有在线会话，返回实际踢出的账号数 */
	public int kickoutByAccountId(String accountId, String reason) {
		return kickoutByAccountIds(Collections.singletonList(accountId), reason);
	}

	/** 踢出拥有指定角色的所有后台账号，返回实际踢出的账号数 */
	public int kickoutByRoleId(String roleId, String reason) {
		if (StrUtil.isBlank(roleId)) {
			return 0;
		}
		List<String> accountIds = accountRoleRepo.findByRoleId(roleId).stream()
				.map(AccountRole::getAccountId)
				.filter(StrUtil::isNotBlank)
				.distinct()
				.collect(Collectors.toList());
		return kickoutByAccountIds(accountIds, reason);
	}

	/** 批量踢出后台账号的所有在线会话，返回实际踢出的账号数 */
	public int kickoutByAccountIds(Collection<String> accountIds, String reason) {
		if (CollUtil.isEmpty(accountIds)) {
			return 0;
		}
		Set<String> distinct = new HashSet<>();
		int count = 0;
		for (String accountId : accountIds) {
			if (StrUtil.isBlank(accountId) || !distinct.add(accountId)) {
				continue;
			}
			List<AccountLoginSession> sessions = accountLoginSessionRepo
					.findByAccountIdAndAccountTypeAndStatusOrderByLastActiveTimeDesc(accountId, ACCOUNT_TYPE_ADMIN,
							STATUS_ONLINE);
			if (sessions.isEmpty()) {
				continue;
			}
			for (AccountLoginSession session : sessions) {
				accountLoginSessionService.markKickoutReason(session.getToken(), reason);
				accountLoginSessionService.kickoutByToken(session.getToken(), Constant.登录会话状态_已踢出);
			}
			count++;
		}
		return count;
	}

	/** 按会话 id 踢出（校验后台账号类型；会话页手动操作，沿用默认提示） */
	public void kickout(String id) {
		accountLoginSessionService.kickout(id, ACCOUNT_TYPE_ADMIN);
	}

}

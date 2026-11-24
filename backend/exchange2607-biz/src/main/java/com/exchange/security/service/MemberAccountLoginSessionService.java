package com.exchange.security.service;

import java.util.List;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.exception.BizException;
import com.exchange.constants.Constant;
import com.exchange.security.domain.AccountLoginSession;
import com.exchange.security.repo.AccountLoginSessionRepo;
import com.exchange.security.vo.MemberAccountLoginSessionVO;

import cn.hutool.core.util.StrUtil;

/**
 * 会员登录会话：面向客户端，只查 MEMBER + ONLINE。
 */
@Validated
@Service
public class MemberAccountLoginSessionService {

	@Autowired
	private AccountLoginSessionRepo accountLoginSessionRepo;

	@Autowired
	private AccountLoginSessionService accountLoginSessionService;

	public List<MemberAccountLoginSessionVO> findByMemberId(@NotBlank String memberId, @NotBlank String currentToken) {
		List<AccountLoginSession> sessions = accountLoginSessionRepo
				.findByAccountIdAndAccountTypeAndStatusOrderByLastActiveTimeDesc(memberId, Constant.登录会话账号类型_会员,
						Constant.登录会话状态_在线);
		return MemberAccountLoginSessionVO.convertFor(sessions, currentToken);
	}

	public void kickout(String memberId, String id) {
		AccountLoginSession session = accountLoginSessionRepo.findById(id).orElseThrow(() -> new BizException("会话不存在"));
		if (!memberId.equals(session.getAccountId())) {
			throw new BizException("会话不存在");
		}
		accountLoginSessionService.kickoutByToken(session.getToken(), Constant.登录会话状态_已踢出);
	}

	public void kickoutOthers(@NotBlank String memberId, @NotBlank String currentToken) {
		List<AccountLoginSession> sessions = accountLoginSessionRepo
				.findByAccountIdAndAccountTypeAndStatusOrderByLastActiveTimeDesc(memberId, Constant.登录会话账号类型_会员,
						Constant.登录会话状态_在线);
		for (AccountLoginSession session : sessions) {
			if (!session.getToken().equals(currentToken)) {
				accountLoginSessionService.kickoutByToken(session.getToken(), Constant.登录会话状态_已踢出);
			}
		}
	}

	/** 踢出指定会员的所有在线会话，返回实际踢出的会话数 */
	public int kickoutByMemberId(String memberId, String reason) {
		if (StrUtil.isBlank(memberId)) {
			return 0;
		}
		List<AccountLoginSession> sessions = accountLoginSessionRepo
				.findByAccountIdAndAccountTypeAndStatusOrderByLastActiveTimeDesc(memberId, Constant.登录会话账号类型_会员,
						Constant.登录会话状态_在线);
		for (AccountLoginSession session : sessions) {
			accountLoginSessionService.markKickoutReason(session.getToken(), reason);
			accountLoginSessionService.kickoutByToken(session.getToken(), Constant.登录会话状态_已踢出);
		}
		return sessions.size();
	}

}

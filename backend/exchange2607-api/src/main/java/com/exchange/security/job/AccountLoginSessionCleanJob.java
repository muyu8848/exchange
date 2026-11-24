package com.exchange.security.job;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.constants.Constant;
import com.exchange.security.domain.AccountLoginSession;
import com.exchange.security.repo.AccountLoginSessionRepo;
import com.exchange.security.service.AccountLoginSessionService;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 会话过期清理：阈值读 active-timeout，未配置兜底 7 天。
 */
@Slf4j
@Component
public class AccountLoginSessionCleanJob {

	private static final long DEFAULT_ACTIVE_SECONDS = 7 * 24 * 3600;

	@Autowired
	private SaTokenConfig saTokenConfig;

	@Autowired
	private AccountLoginSessionRepo accountLoginSessionRepo;

	@Autowired
	private AccountLoginSessionService accountLoginSessionService;

	@Scheduled(fixedRate = 3600000)
	public void execute() {
		try {
			long activeSeconds = saTokenConfig.getActiveTimeout();
			if (activeSeconds <= 0) {
				activeSeconds = DEFAULT_ACTIVE_SECONDS;
			}
			LocalDateTime threshold = LocalDateTime.now().minusSeconds(activeSeconds);
			List<AccountLoginSession> list = accountLoginSessionRepo
					.findByStatusAndLastActiveTimeBefore(Constant.登录会话状态_在线, threshold);
			for (AccountLoginSession session : list) {
				try {
					if (StpUtil.getStpLogic().getTokenLastActiveTime(session.getToken()) <= 0) {
						accountLoginSessionService.markStatus(session.getToken(), Constant.登录会话状态_已过期);
					}
				} catch (Exception e) {
					log.error("清理过期会话失败:{}", session.getToken(), e);
				}
			}
		} catch (Exception e) {
			log.error("会话过期清理任务异常", e);
		}
	}
}

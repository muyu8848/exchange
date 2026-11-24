package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.contract.service.PerpetualOrderMatchService;

import lombok.extern.slf4j.Slf4j;

/**
 * 永续合约限价委托撮合任务，每3秒检查一次待成交挂单。
 */
@Slf4j
@Component
public class PerpetualLimitOrderMatchJob {

	@Autowired
	private PerpetualOrderMatchService orderMatchService;

	@Scheduled(fixedRate = 3000)
	public void execute() {
		try {
			orderMatchService.matchLimitOrders();
		} catch (Exception e) {
			log.error("永续合约限价撮合异常", e);
		}
	}

}

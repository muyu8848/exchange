package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.contract.service.PerpetualOrderMatchService;

import lombok.extern.slf4j.Slf4j;

/**
 * 永续合约计划委托触发任务，每3秒检查一次委托中的计划单。
 */
@Slf4j
@Component
public class PerpetualPlanOrderMatchJob {

	@Autowired
	private PerpetualOrderMatchService orderMatchService;

	@Scheduled(fixedRate = 3000)
	public void execute() {
		try {
			orderMatchService.matchPlanOrders();
		} catch (Exception e) {
			log.error("永续合约计划委托触发异常", e);
		}
	}

}

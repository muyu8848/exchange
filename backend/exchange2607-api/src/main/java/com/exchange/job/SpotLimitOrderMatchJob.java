package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.spot.service.SpotOrderService;
import com.exchange.spot.service.SpotPlanOrderService;

import lombok.extern.slf4j.Slf4j;

/**
 * 现货委托撮合任务，每3秒检查一次待成交订单。
 */
@Slf4j
@Component
public class SpotLimitOrderMatchJob {

	@Autowired
	private SpotOrderService spotOrderService;

	@Autowired
	private SpotPlanOrderService spotPlanOrderService;

	@Scheduled(fixedRate = 3000)
	public void execute() {
		try {
			spotOrderService.matchLimitOrders();
			spotPlanOrderService.matchPlanOrders();
		} catch (Exception e) {
			log.error("现货委托撮合异常", e);
		}
	}

}

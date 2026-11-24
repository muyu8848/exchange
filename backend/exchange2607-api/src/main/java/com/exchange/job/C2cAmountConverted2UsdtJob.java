package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.c2c.service.C2cOrderService;

import lombok.extern.slf4j.Slf4j;

/**
 * C2C 订单 USDT 折算补偿任务：每分钟扫描已完成但未折算的订单并补折算。
 */
@Slf4j
@Component
public class C2cAmountConverted2UsdtJob {

	@Autowired
	private C2cOrderService c2cOrderService;

	@Scheduled(fixedRate = 60000)
	public void execute() {
		try {
			c2cOrderService.c2cAmountConverted2Usdt();
		} catch (Exception e) {
			log.error("C2C订单折算USDT任务异常", e);
		}
	}
}

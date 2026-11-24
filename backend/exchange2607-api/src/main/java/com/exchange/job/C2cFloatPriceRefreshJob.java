package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.c2c.service.C2cFloatPriceRefreshService;

/**
 * 定时刷新 C2C 浮动定价挂单的价格（每 30 秒）。
 */
@Component
public class C2cFloatPriceRefreshJob {

	@Autowired
	private C2cFloatPriceRefreshService floatPriceRefreshService;

	@Scheduled(fixedRate = 30000)
	public void execute() {
		floatPriceRefreshService.refreshFloatPrices();
	}

}

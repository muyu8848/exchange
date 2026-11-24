package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.c2c.service.C2cTradeService;

/**
 * C2C 买家已付款后，卖家超时未确认放行，系统自动放行。
 */
@Component
public class C2cTimeoutUnReleaseJob {

	@Autowired
	private C2cTradeService tradeService;

	@Scheduled(fixedRate = 30000)
	public void execute() {
		tradeService.c2cTimeoutUnRelease();
	}

}

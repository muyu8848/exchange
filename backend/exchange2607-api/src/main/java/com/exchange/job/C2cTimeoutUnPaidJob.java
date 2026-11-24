package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.c2c.service.C2cTradeService;

@Component
public class C2cTimeoutUnPaidJob {

	@Autowired
	private C2cTradeService tradeService;

	@Scheduled(fixedRate = 9000)
	public void execute() {
		tradeService.c2cTimeoutUnPaid();
	}

}

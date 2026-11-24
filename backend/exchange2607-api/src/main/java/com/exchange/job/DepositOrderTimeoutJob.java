package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.deposit.service.DepositOrderService;

@Component
public class DepositOrderTimeoutJob {

	@Autowired
	private DepositOrderService depositOrderService;

	@Scheduled(fixedRate = 10000)
	public void execute() {
		depositOrderService.depositOrderTimeout();
	}

}

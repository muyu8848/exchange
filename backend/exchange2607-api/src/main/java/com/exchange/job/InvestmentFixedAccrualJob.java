package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.investment.service.InvestmentAccrualService;

@Component
public class InvestmentFixedAccrualJob {

	@Autowired
	private InvestmentAccrualService investmentAccrualService;

	@Scheduled(fixedRate = 25000)
	public void execute() {
		investmentAccrualService.processFixedAccrualJob();
	}

}

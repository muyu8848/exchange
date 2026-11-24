package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.contract.service.PerpetualOrderMatchService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PerpetualFundingJob {

	@Autowired
	private PerpetualOrderMatchService orderMatchService;

	/** 每小时整点触发，由 Service 按各交易对的 fundingInterval 判断是否到期 */
	@Scheduled(cron = "0 0 * * * ?")
	public void settle() {
		try {
			orderMatchService.settleFunding();
		} catch (Exception e) {
			log.error("永续资金费结算异常", e);
		}
	}

}

package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.contract.service.PerpetualOrderMatchService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PerpetualLiquidationJob {

	@Autowired
	private PerpetualOrderMatchService orderMatchService;

	@Scheduled(fixedRate = 5000)
	public void check() {
		try {
			orderMatchService.liquidationCheck();
		} catch (Exception e) {
			log.error("永续强平检查异常", e);
		}
	}

}

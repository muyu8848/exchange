package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.investment.service.InvestmentUsdtConvertService;

import lombok.extern.slf4j.Slf4j;

/**
 * 赚币流水 USDT 折算补偿任务：每分钟扫描未折算的申购/赎回/日利息/小时利息并补折算。
 */
@Slf4j
@Component
public class InvestmentAmountConverted2UsdtJob {

	@Autowired
	private InvestmentUsdtConvertService investmentUsdtConvertService;

	@Scheduled(fixedRate = 60000)
	public void execute() {
		try {
			investmentUsdtConvertService.convertPending();
		} catch (Exception e) {
			log.error("赚币USDT折算补偿异常:" + e.getMessage());
		}
	}
}

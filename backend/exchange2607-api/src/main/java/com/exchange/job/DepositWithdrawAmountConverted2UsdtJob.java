package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.deposit.service.DepositOrderService;
import com.exchange.withdraw.service.WithdrawOrderService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DepositWithdrawAmountConverted2UsdtJob {

	@Autowired
	private DepositOrderService depositOrderService;

	@Autowired
	private WithdrawOrderService withdrawOrderService;

	@Scheduled(fixedRate = 60000)
	public void execute() {
		try {
			depositOrderService.depositAmountConverted2Usdt();
		} catch (Exception e) {
			log.error("充币USDT折算补偿异常:" + e.getMessage());
		}
		try {
			withdrawOrderService.withdrawAmountConverted2Usdt();
		} catch (Exception e) {
			log.error("提币USDT折算补偿异常:" + e.getMessage());
		}
	}

}

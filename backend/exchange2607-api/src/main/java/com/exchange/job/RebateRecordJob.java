package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.rebate.service.RebateRecordService;

import lombok.extern.slf4j.Slf4j;

/**
 * 返佣生成补偿任务：定时扫描仍未返佣的订单重新发布 Topic 信号，
 * 由 RedisMqListener 消费处理（参考 DepositWithdrawAmountConverted2Usdt）。
 */
@Slf4j
@Component
public class RebateRecordJob {

	@Autowired
	private RebateRecordService rebateRecordService;

	@Scheduled(fixedRate = 60000)
	public void compensate() {
		try {
			rebateRecordService.compensateSpotRebate();
		} catch (Exception e) {
			log.error("现货返佣补偿异常:" + e.getMessage());
		}
		try {
			rebateRecordService.compensatePerpRebate();
		} catch (Exception e) {
			log.error("合约返佣补偿异常:" + e.getMessage());
		}
	}
}

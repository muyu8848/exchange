package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.c2c.service.C2cTradeService;

/**
 * 协议取消超时兜底：扫描超时未响应的协议取消申请并投递 MQ，由消费者单笔异步处理（无责取消）
 */
@Component
public class C2cProtocolCancelTimeoutJob {

	@Autowired
	private C2cTradeService tradeService;

	@Scheduled(fixedRate = 90000)
	public void execute() {
		tradeService.c2cProtocolCancelTimeout();
	}

}

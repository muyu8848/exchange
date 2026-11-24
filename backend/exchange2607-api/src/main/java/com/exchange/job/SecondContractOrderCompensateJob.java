package com.exchange.job;

import java.util.List;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.constants.Constant;
import com.exchange.contract.domain.SecondContractOrder;
import com.exchange.contract.service.SecondContractOrderService;

import lombok.extern.slf4j.Slf4j;

/**
 * 秒合约订单兜底补偿：每 10 秒扫描「已到期但仍进行中」的订单重新投递平仓。
 * <p>
 * 正常情况由 {@code SecondContractOrderExpiryDispatcher} 实时派发； 本 Job 兜住「消息丢失 /
 * 平仓异常（余额并发、行情缺失等）/ 应用重启」等场景。 重复投递安全：closePosition 有分布式锁 + 状态检查 + 乐观锁。
 */
@Slf4j
@Component
public class SecondContractOrderCompensateJob {

	@Autowired
	private RedissonClient redissonClient;

	@Autowired
	private SecondContractOrderService secondContractOrderService;

	@Scheduled(fixedDelay = 10000)
	public void execute() {
		try {
			List<SecondContractOrder> orders = secondContractOrderService.findExpiredUnclosedOrders();
			for (SecondContractOrder order : orders) {
				redissonClient.getTopic(Constant.秒合约平仓).publish(order.getId());
			}
		} catch (Exception e) {
			log.error("秒合约订单兜底补偿异常", e);
		}
	}

}

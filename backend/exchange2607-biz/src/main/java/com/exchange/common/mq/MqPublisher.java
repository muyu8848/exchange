package com.exchange.common.mq;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Redis 消息统一发布入口。
 * <p>
 * 提供两类能力：<br>
 * 1. Topic 广播（{@link #publishAfterCommit}）；<br>
 * 2. 延迟队列投递（{@link #offerDelayedAfterCommit}），到期后转入阻塞队列，由轮询任务消费。
 */
@Component
public class MqPublisher {

	@Autowired
	private RedissonClient redissonClient;

	/** 事务提交后广播 Topic；当前无事务时立即发送 */
	public void publishAfterCommit(String topic, String value) {
		runAfterCommit(() -> redissonClient.getTopic(topic).publish(value));
	}

	/** 立即广播 Topic */
	public void publish(String topic, String value) {
		redissonClient.getTopic(topic).publish(value);
	}

	/** 事务提交后投递延迟队列（到期后转入阻塞队列），用于秒合约到期平仓等延时任务 */
	public void offerDelayedAfterCommit(String dequeName, String value, long delay, TimeUnit unit) {
		runAfterCommit(() -> {
			RDelayedQueue<String> queue = redissonClient
					.getDelayedQueue(redissonClient.getBlockingDeque(dequeName));
			queue.offer(value, delay, unit);
		});
	}

	/** 有事务则提交后执行，否则立即执行 */
	private void runAfterCommit(Runnable action) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			action.run();
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				action.run();
			}
		});
	}

}

package com.exchange.runner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.exchange.constants.Constant;

import lombok.extern.slf4j.Slf4j;

/**
 * 秒合约订单到期派发器：容器启动后拉起常驻线程，阻塞等待延迟队列到期消息，
 * 转发到「秒合约平仓」Topic（保持与 RedisMqListener 解耦）。
 * <p>
 * 派发失败或平仓异常由 {@code SecondContractOrderCompensateJob} 定时兜底补偿。
 */
@Slf4j
@Component
public class SecondContractOrderExpiryDispatcher implements CommandLineRunner {

	@Autowired
	private RedissonClient redissonClient;

	private ExecutorService executor;

	@Override
	public void run(String... args) {
		executor = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "second-contract-expiry");
			t.setDaemon(true);
			return t;
		});
		executor.submit(this::dispatchLoop);
		log.info("秒合约订单到期派发线程已启动");
	}

	@PreDestroy
	public void stop() {
		executor.shutdownNow();
	}

	private void dispatchLoop() {
		RBlockingQueue<String> queue = redissonClient.getBlockingDeque(Constant.秒合约订单队列);
		while (!Thread.currentThread().isInterrupted()) {
			String message = null;
			try {
				// 阻塞等待：无消息时线程挂起，不消耗 CPU
				message = queue.take();
				redissonClient.getTopic(Constant.秒合约平仓).publish(message);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) {
				log.error("秒合约到期派发异常, orderId={}", message, e);
			}
		}
	}

}

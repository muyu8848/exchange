package com.exchange.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.constants.Constant;
import com.exchange.kline.service.KlineService;

import lombok.extern.slf4j.Slf4j;

/**
 * 低频清理过期K线任务：按周期保留不同时长，每天凌晨执行，分批删除避免大事务。
 */
@Slf4j
@Component
public class KlineCleanJob {

	private static class Retention {
		final String period;
		final int days;

		Retention(String period, int days) {
			this.period = period;
			this.days = days;
		}
	}

	private static final int BATCH_SIZE = 10000;

	private static final Retention[] RETENTIONS = {
			new Retention(Constant.K线时间粒度_1分, 3),
			new Retention(Constant.K线时间粒度_15分, 7),
			new Retention(Constant.K线时间粒度_1时, 30),
			new Retention(Constant.K线时间粒度_4时, 180),
			new Retention(Constant.K线时间粒度_1日, 360) };

	@Autowired
	private KlineService klineService;

	@Scheduled(cron = "0 0 3 * * ?")
	public void cleanExpiredKlines() {
		long start = System.currentTimeMillis();
		int totalDeleted = 0;
		for (Retention r : RETENTIONS) {
			try {
				long cutoff = System.currentTimeMillis() - (long) r.days * 24 * 60 * 60 * 1000;
				int deleted = 0;
				int batch;
				do {
					batch = klineService.deleteExpiredBatch(r.period, cutoff, BATCH_SIZE);
					deleted += batch;
				} while (batch == BATCH_SIZE);
				totalDeleted += deleted;
				log.info("清理K线完成: period={}, 保留{}天, 删除{}条", r.period, r.days, deleted);
			} catch (Exception e) {
				log.error("清理K线异常: period={}", r.period, e);
			}
		}
		log.info("清理K线汇总: 删除共{}条, 耗时={}ms", totalDeleted, System.currentTimeMillis() - start);
	}

}

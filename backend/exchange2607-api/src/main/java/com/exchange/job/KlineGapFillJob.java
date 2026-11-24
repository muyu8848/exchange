package com.exchange.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.common.utils.SymbolPriceUtils;
import com.exchange.constants.Constant;
import com.exchange.contract.service.PerpetualContractSymbolService;
import com.exchange.contract.service.SecondContractSymbolService;
import com.exchange.kline.service.KlineService;
import com.exchange.kline.service.KlineService.KlineGap;
import com.exchange.spot.service.SpotSymbolService;

import lombok.extern.slf4j.Slf4j;

/**
 * 低频补K线空洞任务：兜底近期窗口内的中间空洞 + 尾部整段缺失，历史大缺口不处理。
 */
@Slf4j
@Component
public class KlineGapFillJob {

	private static class PeriodWindow {
		final String period;
		final long windowMs;

		PeriodWindow(String period, long windowMs) {
			this.period = period;
			this.windowMs = windowMs;
		}
	}

	private static final List<PeriodWindow> PERIOD_WINDOWS = Arrays.asList(
			new PeriodWindow(Constant.K线时间粒度_1分, 4L * 60 * 60 * 1000),
			new PeriodWindow(Constant.K线时间粒度_15分, 24L * 60 * 60 * 1000),
			new PeriodWindow(Constant.K线时间粒度_1时, 72L * 60 * 60 * 1000),
			new PeriodWindow(Constant.K线时间粒度_4时, 7L * 24 * 60 * 60 * 1000),
			new PeriodWindow(Constant.K线时间粒度_1日, 14L * 24 * 60 * 60 * 1000));

	@Autowired
	private SpotSymbolService spotSymbolService;

	@Autowired
	private SecondContractSymbolService secondContractSymbolService;

	@Autowired
	private PerpetualContractSymbolService perpetualContractSymbolService;

	@Autowired
	private KlineService klineService;

	/** 合约交易对：秒合约 + 永续合约合并去重（两者共用 -SWAP 行情），与 KlineSyncJob 保持一致。 */
	private List<String> getAllContractSymbols() {
		List<String> symbols = new ArrayList<>(secondContractSymbolService.findEnableSymbolCode());
		for (String s : perpetualContractSymbolService.findEnableSymbolCode()) {
			if (!symbols.contains(s)) {
				symbols.add(s);
			}
		}
		return symbols;
	}

	/** 现货 + 合约各自的 instId（现货无后缀，合约 -SWAP），两组天然不重复。 */
	private List<String> getAllInstIds() {
		List<String> instIds = new ArrayList<>();
		for (String s : spotSymbolService.findEnableSymbolCode()) {
			instIds.add(SymbolPriceUtils.getInstId(Constant.行情业务类型_现货, s));
		}
		for (String s : getAllContractSymbols()) {
			instIds.add(SymbolPriceUtils.getInstId(Constant.行情业务类型_合约, s));
		}
		return instIds;
	}

	@Scheduled(cron = "0 5 * * * ?")
	public void fillKlineGaps() {
		long start = System.currentTimeMillis();
		List<String> instIds = getAllInstIds();
		int scanned = 0;
		int gapCount = 0;
		int filled = 0;
		int failed = 0;
		for (String instId : instIds) {
			for (PeriodWindow pw : PERIOD_WINDOWS) {
				scanned++;
				try {
					long to = System.currentTimeMillis();
					List<KlineGap> gaps = klineService.detectGaps(instId, pw.period, to - pw.windowMs, to);
					for (KlineGap gap : gaps) {
						gapCount++;
						filled += klineService.fillGap(instId, pw.period, gap);
					}
				} catch (Exception e) {
					failed++;
					log.error("补K线缺口异常, instId={}, period={}", instId, pw.period, e);
				}
			}
		}
		log.info("补K线缺口完成: 扫描={}组, 检出空洞={}个, 拉取={}条, 失败={}组, 耗时={}ms", scanned, gapCount, filled,
				failed, System.currentTimeMillis() - start);
	}

}

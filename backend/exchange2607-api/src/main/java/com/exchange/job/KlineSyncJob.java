package com.exchange.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.common.utils.SymbolPriceUtils;
import com.exchange.constants.Constant;
import com.exchange.contract.service.PerpetualContractSymbolService;
import com.exchange.contract.service.SecondContractSymbolService;
import com.exchange.kline.domain.Kline;
import com.exchange.kline.service.KlineService;
import com.exchange.kline.support.OkxKlineApi;
import com.exchange.spot.service.SpotSymbolService;

@Component
public class KlineSyncJob {

	@Autowired
	private OkxKlineApi okxKlineApi;

	@Autowired
	private KlineService klineService;

	@Autowired
	private SpotSymbolService spotSymbolService;

	@Autowired
	private SecondContractSymbolService secondContractSymbolService;

	@Autowired
	private PerpetualContractSymbolService perpetualContractSymbolService;

	private List<String> getAllContractSymbols() {
		List<String> symbols = new java.util.ArrayList<>(secondContractSymbolService.findEnableSymbolCode());
		for (String s : perpetualContractSymbolService.findEnableSymbolCode()) {
			if (!symbols.contains(s)) {
				symbols.add(s);
			}
		}
		return symbols;
	}

	@Scheduled(fixedRate = 35000)
	public void sync1mSpotKline() {
		syncKlines(spotSymbolService.findEnableSymbolCode(), Constant.行情业务类型_现货, Constant.K线时间粒度_1分);
	}

	@Scheduled(fixedRate = 60000)
	public void sync15mSpotKline() {
		syncKlines(spotSymbolService.findEnableSymbolCode(), Constant.行情业务类型_现货, Constant.K线时间粒度_15分);
	}

	@Scheduled(fixedRate = 70000)
	public void sync1hSpotKline() {
		syncKlines(spotSymbolService.findEnableSymbolCode(), Constant.行情业务类型_现货, Constant.K线时间粒度_1时);
	}

	@Scheduled(fixedRate = 80000)
	public void sync4hSpotKline() {
		syncKlines(spotSymbolService.findEnableSymbolCode(), Constant.行情业务类型_现货, Constant.K线时间粒度_4时);
	}

	@Scheduled(fixedRate = 90000)
	public void sync1dSpotKline() {
		syncKlines(spotSymbolService.findEnableSymbolCode(), Constant.行情业务类型_现货, Constant.K线时间粒度_1日);
	}

	@Scheduled(fixedRate = 35000)
	public void sync1mContractKline() {
		syncKlines(getAllContractSymbols(), Constant.行情业务类型_合约, Constant.K线时间粒度_1分);
	}

	@Scheduled(fixedRate = 60000)
	public void sync15mContractKline() {
		syncKlines(getAllContractSymbols(), Constant.行情业务类型_合约, Constant.K线时间粒度_15分);
	}

	@Scheduled(fixedRate = 70000)
	public void sync1hContractKline() {
		syncKlines(getAllContractSymbols(), Constant.行情业务类型_合约, Constant.K线时间粒度_1时);
	}

	@Scheduled(fixedRate = 80000)
	public void sync4hContractKline() {
		syncKlines(getAllContractSymbols(), Constant.行情业务类型_合约, Constant.K线时间粒度_4时);
	}

	@Scheduled(fixedRate = 90000)
	public void sync1dContractKline() {
		syncKlines(getAllContractSymbols(), Constant.行情业务类型_合约, Constant.K线时间粒度_1日);
	}

	/**
	 * 同步某业务类型下所有交易对指定周期的K线： 数据库已同步到最新则跳过，无需调用OKX接口；落后/初始化时拉取补齐（saveKline 幂等去重）。
	 */
	private void syncKlines(List<String> symbols, String bizType, String period) {
		for (String symbol : symbols) {
			String instId = SymbolPriceUtils.getInstId(bizType, symbol);
			if (klineService.isKlineUpToDate(instId, period)) {
				continue;
			}
			List<Kline> klines = okxKlineApi.getKline(instId, period, Constant.默认同步K线数量, null, null);
			klineService.saveKline(klines);
		}
	}

}

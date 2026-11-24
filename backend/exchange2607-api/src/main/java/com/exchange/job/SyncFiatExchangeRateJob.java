package com.exchange.job;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.exchange.c2c.domain.C2cFiat;
import com.exchange.c2c.repo.C2cFiatRepo;
import com.exchange.constants.Constant;
import com.exchange.setting.domain.FiatRateSyncSetting;
import com.exchange.setting.repo.FiatRateSyncSettingRepo;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SyncFiatExchangeRateJob {

	@Autowired
	private FiatRateSyncSettingRepo fiatRateSyncSettingRepo;

	@Autowired
	private C2cFiatRepo c2cFiatRepo;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Value("${useProxy:false}")
	private Boolean useProxy;

	@Value("${proxyHost:}")
	private String proxyHost;

	@Value("${proxyPort:0}")
	private Integer proxyPort;

	@Scheduled(fixedRate = 30000)
	public void execute() {
		FiatRateSyncSetting setting = fiatRateSyncSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null || Boolean.FALSE.equals(setting.getAutoSyncExchangeRate())) {
			return;
		}
		String source = setting.getExchangeRateSource();
		List<C2cFiat> fiats = c2cFiatRepo
				.findByStatusAndDeletedFlagFalseOrderBySortNumAscCreateTimeAsc(Constant.启用状态_启用);
		for (C2cFiat fiat : fiats) {
			try {
				BigDecimal rate = fetchRate(source, fiat.getFiatCode());
				if (rate != null && rate.compareTo(BigDecimal.ZERO) > 0) {
					redisTemplate.opsForValue().set(Constant.C2C法币汇率 + fiat.getFiatCode(),
							rate.setScale(4, RoundingMode.HALF_UP).toPlainString(), 120, TimeUnit.SECONDS);
				}
			} catch (Exception e) {
				log.error("法币汇率同步失败 {}:{}", fiat.getFiatCode(), e.getMessage());
			}
		}
	}

	private BigDecimal fetchRate(String source, String fiatCode) {
		if (Constant.汇率数据源_Binance.equals(source)) {
			return fetchFromBinanceP2P(fiatCode);
		}
		if (Constant.汇率数据源_OKX.equals(source)) {
			return fetchFromOKX(fiatCode);
		}
		return null;
	}

	/**
	 * 币安 P2P 市场价：拉取指定法币买入 USDT 的 P2P 广告，取中间价。 API: POST
	 * https://p2p.binance.com/bapi/c2c/v2/friendly/c2c/adv/search
	 */
	private BigDecimal fetchFromBinanceP2P(String fiatCode) {
		String url = "https://p2p.binance.com/bapi/c2c/v2/friendly/c2c/adv/search";
		String body = "{\"fiat\":\"" + fiatCode
				+ "\",\"page\":1,\"rows\":10,\"tradeType\":\"BUY\",\"asset\":\"USDT\",\"countries\":[],\"payTypes\":[]}";
		String response = doHttpPost(url, body);
		JSONObject json = JSONUtil.parseObj(response);
		cn.hutool.json.JSONArray data = json.getJSONArray("data");
		if (data == null || data.isEmpty()) {
			return null;
		}
		List<BigDecimal> prices = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			JSONObject adv = data.getJSONObject(i);
			if (adv == null) {
				continue;
			}
			JSONObject advDetail = adv.getJSONObject("adv");
			if (advDetail == null) {
				continue;
			}
			BigDecimal price = advDetail.getBigDecimal("price");
			if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
				prices.add(price);
			}
		}
		if (prices.isEmpty()) {
			return null;
		}
		// 取中间价
		prices.sort(BigDecimal::compareTo);
		return prices.get(prices.size() / 2).setScale(4, RoundingMode.HALF_UP);
	}

	/**
	 * OKX C2C 市场价：拉取 USDT/{fiatCode} 的 C2C 广告簿，取中间价。
	 */
	private BigDecimal fetchFromOKX(String fiatCode) {
		String url = MessageFormat.format(
				"https://www.okx.com/v3/c2c/tradingOrders/books?quoteCurrency={0}&baseCurrency=USDT&side=buy&userType=all&t={1}",
				fiatCode, String.valueOf(System.currentTimeMillis()));
		String response = doHttpGet(url);
		if (!JSONUtil.isTypeJSONObject(response)) {
			log.warn("OKX C2C接口异常:{}", response);
			return null;
		}
		JSONObject jsonObject = JSONUtil.parseObj(response);
		Integer code = jsonObject.getInt("code");
		if (code != 0) {
			log.warn("OKX C2C接口异常:{}", response);
			return null;
		}
		JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("buy");
		List<BigDecimal> prices = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject item = jsonArray.getJSONObject(i);
			if (item == null) {
				continue;
			}
			BigDecimal price = item.getBigDecimal("price");
			if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
				prices.add(price);
			}
		}
		if (prices.isEmpty()) {
			return null;
		}
		// 取中间价
		prices.sort(BigDecimal::compareTo);
		return prices.get(prices.size() / 2).setScale(2, RoundingMode.HALF_UP);
	}

	private String doHttpGet(String url) {
		HttpRequest request = HttpUtil.createGet(url);
		return doRequest(request);
	}

	private String doHttpPost(String url, String body) {
		HttpRequest request = HttpUtil.createPost(url).body(body);
		request.header("Content-Type", "application/json");
		return doRequest(request);
	}

	private String doRequest(HttpRequest request) {
		if (Boolean.TRUE.equals(useProxy) && proxyHost != null && !proxyHost.isEmpty() && proxyPort != null
				&& proxyPort > 0) {
			request.setProxy(
					new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(proxyHost, proxyPort)));
		}
		return request.timeout(10000).execute().body();
	}

}

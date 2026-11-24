package com.exchange.runner;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.exchange.common.utils.IdUtils;
import com.exchange.common.utils.SymbolPriceUtils;
import com.exchange.constants.Constant;
import com.exchange.kline.domain.Kline;
import com.exchange.kline.support.KlineRealTimeCache;
import com.exchange.spot.service.SpotSymbolService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

@Slf4j
@Component
public class SpotKlineMonitor implements CommandLineRunner {

	private Set<String> symbolSet = new HashSet<>();

	private WebSocket businessWebSocket;

	private Integer reconnectCount = 0;

	private Integer maxReconnectCount = 10000;

	@Value("${useProxy:false}")
	private Boolean useProxy;

	@Value("${proxyHost:}")
	private String proxyHost;

	@Value("${proxyPort:0}")
	private Integer proxyPort;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private SpotSymbolService spotSymbolService;

	public void monitorNewSymbol(String symbolCode) {
		monitorCrypto(Arrays.asList(symbolCode));
	}

	public void monitorCrypto(List<String> symbolCodes) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("op", "subscribe");
		List<Map<String, Object>> args = new ArrayList<>();
		for (String symbolCode : symbolCodes) {
			if (symbolSet.contains(symbolCode)) {
				continue;
			}
			String instId = SymbolPriceUtils.getInstId(Constant.行情业务类型_现货, symbolCode);
			Map<String, Object> arg = new HashMap<>();

			// 1分
			arg = new HashMap<>();
			arg.put("channel", "candle1m");
			arg.put("instId", instId);
			args.add(arg);

			// 15分
			arg = new HashMap<>();
			arg.put("channel", "candle15m");
			arg.put("instId", instId);
			args.add(arg);

			// 1时
			arg = new HashMap<>();
			arg.put("channel", "candle1H");
			arg.put("instId", instId);
			args.add(arg);

			// 4时
			arg = new HashMap<>();
			arg.put("channel", "candle4H");
			arg.put("instId", instId);
			args.add(arg);

			// 1日
			arg.put("channel", "candle1D");
			arg.put("instId", instId);
			args.add(arg);
		}
		paramMap.put("args", args);
		businessWebSocket.send(JSONUtil.toJsonStr(paramMap));
		symbolSet.addAll(symbolCodes);
	}

	public void reconnect() {
		if (reconnectCount < maxReconnectCount) {
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.info("reconnect count: {}", reconnectCount);
			symbolSet = new HashSet<>();
			connect();
			reconnectCount++;
		} else {
			log.error("Reconnect over {} times, please check network", maxReconnectCount);
		}
	}

	public void connect() {
		Proxy proxy = null;
		if (useProxy) {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
		}
		Builder builder = new OkHttpClient.Builder();
		if (proxy != null) {
			builder.proxy(proxy);
		}
		OkHttpClient okHttpClient = builder.build();
		Request request = new Request.Builder().url("wss://ws.okx.com:8443/ws/v5/business").build();
		businessWebSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
			@Override
			public void onOpen(WebSocket webSocket, Response response) {
			}

			@Override
			public void onMessage(WebSocket webSocket, String text) {
				if (!text.startsWith("{\"arg\"")) {
					return;
				}
				JSONObject jsonObject = JSONUtil.parseObj(text);
				String symbol = jsonObject.getByPath("arg.instId", String.class);
				String period = jsonObject.getByPath("arg.channel", String.class).split("candle")[1];
				String closePrice = jsonObject.getByPath("data[0][4]", String.class);
				String openPrice = jsonObject.getByPath("data[0][1]", String.class);
				String highPrice = jsonObject.getByPath("data[0][2]", String.class);
				String lowPrice = jsonObject.getByPath("data[0][3]", String.class);
				Long timestamp = jsonObject.getByPath("data[0][0]", Long.class);
				Long volume = jsonObject.getByPath("data[0][7]", Long.class);
				Long klineCompleteFlag = jsonObject.getByPath("data[0][8]", Long.class);
				if ("1D".equals(period)) {
					redisTemplate.opsForValue().set(Constant.收盘价 + symbol, closePrice, 60, TimeUnit.SECONDS);
					redisTemplate.opsForValue().set(Constant.最高价 + symbol, highPrice, 60, TimeUnit.SECONDS);
					redisTemplate.opsForValue().set(Constant.最低价 + symbol, lowPrice, 60, TimeUnit.SECONDS);
					redisTemplate.opsForValue().set(Constant.最后更新价格时间 + symbol, timestamp + "", 60, TimeUnit.SECONDS);
					String dayOpenPriceLockKey = DateUtil.date(timestamp).dayOfMonth() + Constant.开盘价 + symbol;
					String dayOpenPriceLockVal = redisTemplate.opsForValue().get(dayOpenPriceLockKey);
					if (StrUtil.isBlank(dayOpenPriceLockVal)) {
						redisTemplate.opsForValue().set(dayOpenPriceLockKey, "1", 2, TimeUnit.DAYS);
						redisTemplate.opsForValue().set(Constant.开盘价 + symbol, openPrice, 2, TimeUnit.DAYS);
					}
				}
				String klineCacheKey = symbol + ":" + period;
				Kline kline = Kline.builder().id(IdUtils.getId()).symbol(symbol).period(period).openTime(timestamp)
						.openPrice(new BigDecimal(openPrice)).highPrice(new BigDecimal(highPrice))
						.lowPrice(new BigDecimal(lowPrice)).closePrice(new BigDecimal(closePrice))
						.volume(new BigDecimal(volume)).build();
				if (klineCompleteFlag == 0) {
					KlineRealTimeCache.K线缓存.put(klineCacheKey, kline);
				} else {
					KlineRealTimeCache.K线缓存.remove(klineCacheKey);
				}
			}

			@Override
			public void onFailure(WebSocket webSocket, Throwable t, Response response) {
				log.warn("SpotKlineMonitor onFailure");
				reconnect();
			}

			@Override
			public void onClosed(WebSocket webSocket, int code, String reason) {
				log.warn("SpotKlineMonitor onClosed");
			}
		});
		monitorCrypto(spotSymbolService.findEnableSymbolCode());
	}

	@Override
	public void run(String... args) throws Exception {
		connect();
	}

}

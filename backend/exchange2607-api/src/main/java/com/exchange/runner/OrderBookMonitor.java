package com.exchange.runner;

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
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.exchange.common.utils.SymbolPriceUtils;
import com.exchange.constants.Constant;
import com.exchange.contract.service.PerpetualContractSymbolService;
import com.exchange.spot.service.SpotSymbolService;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * 盘口数据监控（现货 + 永续合约）
 * <p>
 * 连接 OKX 公共 WebSocket（/ws/v5/public），订阅 books5 频道，
 * 将5档买卖盘口数据存入 Redis。
 * <p>
 * 现货 instId = BTC-USDT，合约 instId = BTC-USDT-SWAP，Redis Key 天然隔离。
 */
@Slf4j
@Component
@Order(2)
public class OrderBookMonitor implements CommandLineRunner {

	private Set<String> symbolSet = new HashSet<>();

	private WebSocket orderBookWebSocket;

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
	private PerpetualContractSymbolService perpetualContractSymbolService;

	@Autowired
	private SpotSymbolService spotSymbolService;

	/** 新增合约交易对时调用（MQ监听） */
	public void monitorNewContractSymbol(String symbolCode) {
		subscribeBooks5(Arrays.asList(symbolCode), Constant.行情业务类型_合约);
	}

	/** 新增现货交易对时调用（MQ监听） */
	public void monitorNewSpotSymbol(String symbolCode) {
		subscribeBooks5(Arrays.asList(symbolCode), Constant.行情业务类型_现货);
	}

	public void subscribeBooks5(List<String> symbolCodes, String bizType) {
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("op", "subscribe");
		List<Map<String, Object>> args = new ArrayList<>();
		for (String symbolCode : symbolCodes) {
			String instId = SymbolPriceUtils.getInstId(bizType, symbolCode);
			if (symbolSet.contains(instId)) {
				continue;
			}
			Map<String, Object> arg = new HashMap<>();
			arg.put("channel", "books5");
			arg.put("instId", instId);
			args.add(arg);
			symbolSet.add(instId);
		}
		if (args.isEmpty()) {
			return;
		}
		paramMap.put("args", args);
		orderBookWebSocket.send(JSONUtil.toJsonStr(paramMap));
	}

	public void reconnect() {
		if (reconnectCount < maxReconnectCount) {
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.info("OrderBookMonitor reconnect count: {}", reconnectCount);
			symbolSet = new HashSet<>();
			connect();
			reconnectCount++;
		} else {
			log.error("OrderBookMonitor reconnect over {} times", maxReconnectCount);
		}
	}

	public void connect() {
		Proxy proxy = null;
		if (useProxy) {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
		}
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		if (proxy != null) {
			builder.proxy(proxy);
		}
		OkHttpClient okHttpClient = builder.build();
		Request request = new Request.Builder()
				.url("wss://ws.okx.com:8443/ws/v5/public")
				.build();
		orderBookWebSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {
			@Override
			public void onOpen(WebSocket webSocket, Response response) {
				log.info("OrderBookMonitor WebSocket connected");
			}

			@Override
			public void onMessage(WebSocket webSocket, String text) {
				if (!text.startsWith("{")) {
					return;
				}
				JSONObject jsonObject = JSONUtil.parseObj(text);
				String channel = jsonObject.getByPath("arg.channel", String.class);
				if (channel == null || !"books5".equals(channel)) {
					return;
				}

				String instId = jsonObject.getByPath("arg.instId", String.class);
				Object dataObj = jsonObject.getByPath("data[0]", Object.class);
				if (instId != null && dataObj != null) {
					redisTemplate.opsForValue().set(
							Constant.盘口数据 + instId,
							JSONUtil.toJsonStr(dataObj),
							2, TimeUnit.SECONDS);
				}
			}

			@Override
			public void onFailure(WebSocket webSocket, Throwable t, Response response) {
				log.warn("OrderBookMonitor onFailure: {}", t.getMessage());
				reconnect();
			}

			@Override
			public void onClosed(WebSocket webSocket, int code, String reason) {
				log.warn("OrderBookMonitor onClosed: code={}, reason={}", code, reason);
			}
		});

		// 订阅所有启用的现货 + 永续合约交易对
		List<String> spotSymbols = spotSymbolService.findEnableSymbolCode();
		if (spotSymbols != null && !spotSymbols.isEmpty()) {
			subscribeBooks5(spotSymbols, Constant.行情业务类型_现货);
		}
		List<String> perpSymbols = perpetualContractSymbolService.findEnableSymbolCode();
		if (perpSymbols != null && !perpSymbols.isEmpty()) {
			subscribeBooks5(perpSymbols, Constant.行情业务类型_合约);
		}
	}

	@Override
	public void run(String... args) throws Exception {
		connect();
	}

}

package com.exchange.dashboard.param;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class SpotDashboardQueryParam {

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	/** 交易对，如 BTC-USDT */
	private String symbolCode;

	/** 方向：buy / sell */
	private String side;

	/** 趋势粒度：hour / day，空则按时间跨度自动选择 */
	private String granularity;
}

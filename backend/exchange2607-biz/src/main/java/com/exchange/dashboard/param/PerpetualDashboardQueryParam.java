package com.exchange.dashboard.param;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class PerpetualDashboardQueryParam {

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	/** 交易对 */
	private String symbolCode;

	/** 方向：long / short */
	private String direction;

	/** 订单类型：open / close / liquidate */
	private String orderType;

	/** hour / day */
	private String granularity;
}

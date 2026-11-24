package com.exchange.dashboard.param;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class DepositWithdrawDashboardQueryParam {

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	private String assetCode;

	private String chainType;

	/** 趋势粒度：hour / day，留空按时间跨度自动判断 */
	private String granularity;
}

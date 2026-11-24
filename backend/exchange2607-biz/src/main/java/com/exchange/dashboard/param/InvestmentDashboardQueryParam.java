package com.exchange.dashboard.param;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class InvestmentDashboardQueryParam {

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	/** 产品类型：FLEXIBLE=活期 / FIXED=定期 */
	private String productType;

	private String assetCode;

	private String productId;

	/** 趋势粒度：P1 统一按天，保留字段便于扩展 */
	private String granularity;
}

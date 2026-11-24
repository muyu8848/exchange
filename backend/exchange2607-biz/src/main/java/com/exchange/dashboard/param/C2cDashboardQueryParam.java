package com.exchange.dashboard.param;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class C2cDashboardQueryParam {

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	/** 法币 */
	private String fiatCode;

	/** 币种 */
	private String assetCode;

	/** 支付方式编码 */
	private String paymentTypeCode;

	/** hour / day */
	private String granularity;
}

package com.exchange.dashboard.param;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class ConvertDashboardQueryParam {

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	/** 消耗币种 */
	private String fromAssetCode;

	/** 获得币种 */
	private String toAssetCode;

	/** 趋势粒度：hour / day，空则按时间跨度自动选择 */
	private String granularity;
}

package com.exchange.dashboard.param;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

/**
 * 秒合约看板查询条件。
 * <p>
 * 注意两套时间口径：下单量按 openTime 统计、结算结果（盈亏/胜负）按 closeTime 统计，
 * 跨天订单会分别计入两天，看板需注明。
 */
@Data
public class SecondContractDashboardQueryParam {

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	/** 交易对 */
	private String symbolCode;

	/** 方向：long / short */
	private String direction;

	/** 周期（秒） */
	private Integer period;

	/** hour / day，留空按时间跨度自动判断 */
	private String granularity;
}

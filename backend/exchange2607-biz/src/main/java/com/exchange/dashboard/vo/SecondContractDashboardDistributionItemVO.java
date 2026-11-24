package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * 秒合约分布项（周期分布 / 方向分布 / 交易对 Top 共用）。
 */
@Data
public class SecondContractDashboardDistributionItemVO {

	/** 维度编码（period 字符串 / long|short / symbolCode） */
	private String key;

	/** 展示名称（周期如「60 秒」、方向如「买涨」、交易对原样） */
	private String name;

	private Long orderCount = 0L;

	/** 下注额合计（USDT） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal amount = BigDecimal.ZERO;

	/** 占比（%，按下注额） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal ratio = BigDecimal.ZERO;
}

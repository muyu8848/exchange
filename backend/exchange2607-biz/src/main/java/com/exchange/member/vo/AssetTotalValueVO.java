package com.exchange.member.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 资产总估值：totalUsdt 为折算基准，totalFiat 为按请求法币折算后的参考估值。
 * <p>
 * totalFiat 为 null 表示该法币汇率暂不可用（不抛异常，由前端显示占位符）。
 */
@Data
public class AssetTotalValueVO {

	/** 折算后的法币金额（按该法币精度舍入）；汇率为 null 时本字段为 null */
	private BigDecimal totalFiat;

	private BigDecimal totalUsdt;

	/** 实际使用的法币代码（入参非法或缺失时回落 CNY） */
	private String fiatCode;

	/** 该法币兑 USDT 的汇率；不可用时为 null */
	private BigDecimal fiatRate;

}

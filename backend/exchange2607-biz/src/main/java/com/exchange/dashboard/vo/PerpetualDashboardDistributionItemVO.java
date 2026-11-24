package com.exchange.dashboard.vo;

import lombok.Data;

@Data
public class PerpetualDashboardDistributionItemVO {

	/** 类型编码：open / close / liquidate */
	private String type;

	/** 类型名称 */
	private String typeName;

	private Long orderCount = 0L;
}

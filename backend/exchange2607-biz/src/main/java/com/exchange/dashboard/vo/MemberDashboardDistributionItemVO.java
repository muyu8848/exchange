package com.exchange.dashboard.vo;

import lombok.Data;

@Data
public class MemberDashboardDistributionItemVO {

	/** 展示名称：邮箱 / 手机号 / 已认证 等 */
	private String name;

	private Long value = 0L;
}

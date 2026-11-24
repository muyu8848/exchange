package com.exchange.dashboard.vo;

import lombok.Data;

@Data
public class MemberDashboardTrendItemVO {

	/** 时间点：yyyy-MM-dd 或 yyyy-MM-dd HH:00 */
	private String date;

	/** 新增注册 */
	private Long newMemberCount = 0L;

	/** 活跃会员 */
	private Long activeMemberCount = 0L;
}

package com.exchange.dashboard.vo;

import lombok.Data;

@Data
public class MemberDashboardDailyItemVO {

	/** 时间点：yyyy-MM-dd 或 yyyy-MM-dd HH:00 */
	private String date;

	/** 新增注册 */
	private Long newMemberCount = 0L;

	/** 活跃会员 */
	private Long activeMemberCount = 0L;

	/** 实名新增 */
	private Long newVerifiedCount = 0L;
}

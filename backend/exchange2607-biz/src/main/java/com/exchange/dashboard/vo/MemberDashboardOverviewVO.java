package com.exchange.dashboard.vo;

import lombok.Data;

@Data
public class MemberDashboardOverviewVO {

	/** 新增注册（时间范围内） */
	private Long newMemberCount = 0L;

	/** 累计注册（快照，含已注销） */
	private Long totalRegisteredCount = 0L;

	/** 有效会员（快照，不含已注销） */
	private Long totalMemberCount = 0L;

	/** 活跃会员（login_log 时间范围内客户端成功登录，去重） */
	private Long activeMemberCount = 0L;

	/** 实名会员（快照，已认证） */
	private Long verifiedMemberCount = 0L;

	/** 实名新增（authenticationPassedTime 时间范围内） */
	private Long newVerifiedCount = 0L;

	/** 停用会员（快照） */
	private Long disabledMemberCount = 0L;
}

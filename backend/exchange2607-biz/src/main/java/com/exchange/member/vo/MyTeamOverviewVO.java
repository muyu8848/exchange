package com.exchange.member.vo;

import java.util.List;

import lombok.Data;

@Data
public class MyTeamOverviewVO {

	/**
	 * 直推人数（一级下级）
	 */
	private long directCount;

	/**
	 * 团队总人数（所有下级）
	 */
	private long teamTotalCount;

	/**
	 * 各层级人数统计（按层级升序）
	 */
	private List<MyTeamLevelCountVO> levelCounts;

	/**
	 * 今日新增人数（团队全量后代，即所有下级中注册时间在本日 00:00 之后的成员数；
	 * 注意：运营后台统计全量，不受 maxLevel 截断，与客户端按 depth<=maxLevel 截断的口径不同）
	 */
	private long todayNewCount;

	/**
	 * 当前配置的返佣最大层级（运营后台用于区分返佣范围内外的层级）
	 */
	private Integer rebateMaxLevel;

}

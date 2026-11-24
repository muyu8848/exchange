package com.exchange.member.vo;

import lombok.Data;

@Data
public class MyDirectTeamOverviewVO {

	/** 直推人数（一级下级） */
	private long directCount;

	/** 今日新增直推（注册时间在本日 00:00 之后的直推成员数） */
	private long todayNewDirect;

}

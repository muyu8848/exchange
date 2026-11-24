package com.exchange.member.vo;

import lombok.Data;

@Data
public class MyTeamLevelCountVO {

	/**
	 * 层级（1=直推，2=二级，以此类推）
	 */
	private int level;

	/**
	 * 该层级人数
	 */
	private long count;

}

package com.exchange.dashboard.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 秒合约看板聚合返回。
 */
@Data
public class SecondContractDashboardSummaryVO {

	private SecondContractDashboardOverviewVO overview = new SecondContractDashboardOverviewVO();

	private List<SecondContractDashboardTrendItemVO> trend = new ArrayList<>();

	/** 周期分布（按 period） */
	private List<SecondContractDashboardDistributionItemVO> periodDistribution = new ArrayList<>();

	/** 方向分布（买涨 / 买跌） */
	private List<SecondContractDashboardDistributionItemVO> directionDistribution = new ArrayList<>();

	/** 交易对 Top 10（按下注额） */
	private List<SecondContractDashboardDistributionItemVO> symbolTop = new ArrayList<>();

	/** 盈利会员 Top 10（按已结算盈亏降序） */
	private List<SecondContractDashboardMemberItemVO> topProfitMembers = new ArrayList<>();

	/** 亏损会员 Top 10（按已结算盈亏升序） */
	private List<SecondContractDashboardMemberItemVO> topLossMembers = new ArrayList<>();
}

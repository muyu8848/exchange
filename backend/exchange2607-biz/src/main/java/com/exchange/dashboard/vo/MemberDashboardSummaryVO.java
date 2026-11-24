package com.exchange.dashboard.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MemberDashboardSummaryVO {

	private MemberDashboardOverviewVO overview = new MemberDashboardOverviewVO();

	private List<MemberDashboardTrendItemVO> trend = new ArrayList<>();

	/** 注册方式分布：邮箱 / 手机号 */
	private List<MemberDashboardDistributionItemVO> contactTypeDistribution = new ArrayList<>();

	/** 认证状态分布 */
	private List<MemberDashboardDistributionItemVO> authenticationStatusDistribution = new ArrayList<>();
}

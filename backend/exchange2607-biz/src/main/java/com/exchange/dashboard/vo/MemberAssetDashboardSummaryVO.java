package com.exchange.dashboard.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MemberAssetDashboardSummaryVO {

	private MemberAssetDashboardOverviewVO overview = new MemberAssetDashboardOverviewVO();

	/** 币种分布 */
	private List<MemberAssetDashboardAssetItemVO> assetDistribution = new ArrayList<>();

	/** 账户类型分布 */
	private List<MemberAssetDashboardTypeItemVO> accountTypeDistribution = new ArrayList<>();
}

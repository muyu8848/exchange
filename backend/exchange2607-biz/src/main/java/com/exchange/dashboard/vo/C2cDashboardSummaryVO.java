package com.exchange.dashboard.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class C2cDashboardSummaryVO {

	private C2cDashboardOverviewVO overview = new C2cDashboardOverviewVO();

	private List<C2cDashboardTrendItemVO> trend = new ArrayList<>();

	/** 法币分布 */
	private List<C2cDashboardDistributionItemVO> fiatDistribution = new ArrayList<>();

	/** 支付方式分布 */
	private List<C2cDashboardDistributionItemVO> paymentDistribution = new ArrayList<>();

	/** 币种分布 */
	private List<C2cDashboardDistributionItemVO> assetDistribution = new ArrayList<>();

	/** 买卖方向分布（广告方视角） */
	private List<C2cDashboardDistributionItemVO> directionDistribution = new ArrayList<>();
}

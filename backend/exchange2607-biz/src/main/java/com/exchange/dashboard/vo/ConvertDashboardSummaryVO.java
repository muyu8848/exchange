package com.exchange.dashboard.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ConvertDashboardSummaryVO {

	private ConvertDashboardOverviewVO overview = new ConvertDashboardOverviewVO();

	private List<ConvertDashboardTrendItemVO> trend = new ArrayList<>();

	/** 币种流向 Top 10 */
	private List<ConvertDashboardPairItemVO> pairTop = new ArrayList<>();
}

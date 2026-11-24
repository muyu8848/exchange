package com.exchange.dashboard.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class InvestmentDashboardSummaryVO {

	private InvestmentDashboardOverviewVO overview = new InvestmentDashboardOverviewVO();

	private List<InvestmentDashboardTrendItemVO> trend = new ArrayList<>();
}

package com.exchange.dashboard.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SpotDashboardSummaryVO {

	private SpotDashboardOverviewVO overview = new SpotDashboardOverviewVO();

	private List<SpotDashboardTrendItemVO> trend = new ArrayList<>();
}

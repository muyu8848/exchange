package com.exchange.dashboard.vo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DepositWithdrawSummaryVO {

	private DepositWithdrawOverviewVO overview = new DepositWithdrawOverviewVO();

	private List<DepositWithdrawTrendItemVO> trend = new ArrayList<>();
}

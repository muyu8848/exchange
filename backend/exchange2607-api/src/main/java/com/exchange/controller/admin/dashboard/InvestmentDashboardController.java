package com.exchange.controller.admin.dashboard;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.dashboard.param.InvestmentDashboardQueryParam;
import com.exchange.dashboard.service.InvestmentDashboardService;
import com.exchange.dashboard.vo.InvestmentDashboardDailyItemVO;
import com.exchange.dashboard.vo.InvestmentDashboardSummaryVO;

@Validated
@RestController
@RequestMapping("/investmentDashboard")
public class InvestmentDashboardController {

	@Autowired
	private InvestmentDashboardService investmentDashboardService;

	@PostMapping("/summary")
	public Result<InvestmentDashboardSummaryVO> summary(InvestmentDashboardQueryParam param) {
		return Result.success(investmentDashboardService.summary(param));
	}

	@PostMapping("/daily")
	public Result<List<InvestmentDashboardDailyItemVO>> daily(InvestmentDashboardQueryParam param) {
		return Result.success(investmentDashboardService.daily(param));
	}
}

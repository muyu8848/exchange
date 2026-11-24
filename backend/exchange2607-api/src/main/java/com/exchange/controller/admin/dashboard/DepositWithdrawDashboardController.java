package com.exchange.controller.admin.dashboard;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.dashboard.param.DepositWithdrawDashboardQueryParam;
import com.exchange.dashboard.service.DepositWithdrawDashboardService;
import com.exchange.dashboard.vo.DepositWithdrawDailyItemVO;
import com.exchange.dashboard.vo.DepositWithdrawSummaryVO;

@Validated
@RestController
@RequestMapping("/depositWithdrawDashboard")
public class DepositWithdrawDashboardController {

	@Autowired
	private DepositWithdrawDashboardService depositWithdrawDashboardService;

	@PostMapping("/summary")
	public Result<DepositWithdrawSummaryVO> summary(DepositWithdrawDashboardQueryParam param) {
		return Result.success(depositWithdrawDashboardService.summary(param));
	}

	@PostMapping("/daily")
	public Result<List<DepositWithdrawDailyItemVO>> daily(DepositWithdrawDashboardQueryParam param) {
		return Result.success(depositWithdrawDashboardService.daily(param));
	}
}

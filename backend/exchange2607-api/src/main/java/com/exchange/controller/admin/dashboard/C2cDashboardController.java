package com.exchange.controller.admin.dashboard;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.dashboard.param.C2cDashboardQueryParam;
import com.exchange.dashboard.service.C2cDashboardService;
import com.exchange.dashboard.vo.C2cDashboardDailyItemVO;
import com.exchange.dashboard.vo.C2cDashboardSummaryVO;

@RestController
@RequestMapping("/c2cDashboard")
public class C2cDashboardController {

	@Autowired
	private C2cDashboardService c2cDashboardService;

	@PostMapping("/summary")
	public Result<C2cDashboardSummaryVO> summary(C2cDashboardQueryParam param) {
		return Result.success(c2cDashboardService.summary(param));
	}

	@PostMapping("/daily")
	public Result<List<C2cDashboardDailyItemVO>> daily(C2cDashboardQueryParam param) {
		return Result.success(c2cDashboardService.daily(param));
	}
}

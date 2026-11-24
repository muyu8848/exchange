package com.exchange.controller.admin.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.dashboard.param.SpotDashboardQueryParam;
import com.exchange.dashboard.service.SpotDashboardService;
import com.exchange.dashboard.vo.SpotDashboardDailyItemVO;
import com.exchange.dashboard.vo.SpotDashboardSummaryVO;

import java.util.List;

@RestController
@RequestMapping("/spotDashboard")
public class SpotDashboardController {

	@Autowired
	private SpotDashboardService spotDashboardService;

	@PostMapping("/summary")
	public Result<SpotDashboardSummaryVO> summary(SpotDashboardQueryParam param) {
		return Result.success(spotDashboardService.summary(param));
	}

	@PostMapping("/daily")
	public Result<List<SpotDashboardDailyItemVO>> daily(SpotDashboardQueryParam param) {
		return Result.success(spotDashboardService.daily(param));
	}
}

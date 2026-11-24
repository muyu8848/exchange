package com.exchange.controller.admin.dashboard;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.dashboard.param.ConvertDashboardQueryParam;
import com.exchange.dashboard.service.ConvertDashboardService;
import com.exchange.dashboard.vo.ConvertDashboardDailyItemVO;
import com.exchange.dashboard.vo.ConvertDashboardSummaryVO;

@RestController
@RequestMapping("/convertDashboard")
public class ConvertDashboardController {

	@Autowired
	private ConvertDashboardService convertDashboardService;

	@PostMapping("/summary")
	public Result<ConvertDashboardSummaryVO> summary(ConvertDashboardQueryParam param) {
		return Result.success(convertDashboardService.summary(param));
	}

	@PostMapping("/daily")
	public Result<List<ConvertDashboardDailyItemVO>> daily(ConvertDashboardQueryParam param) {
		return Result.success(convertDashboardService.daily(param));
	}
}

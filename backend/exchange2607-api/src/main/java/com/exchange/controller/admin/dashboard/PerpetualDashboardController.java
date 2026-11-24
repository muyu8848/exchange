package com.exchange.controller.admin.dashboard;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.dashboard.param.PerpetualDashboardQueryParam;
import com.exchange.dashboard.service.PerpetualDashboardService;
import com.exchange.dashboard.vo.PerpetualDashboardDailyItemVO;
import com.exchange.dashboard.vo.PerpetualDashboardSummaryVO;

@RestController
@RequestMapping("/perpetualDashboard")
public class PerpetualDashboardController {

	@Autowired
	private PerpetualDashboardService perpetualDashboardService;

	@PostMapping("/summary")
	public Result<PerpetualDashboardSummaryVO> summary(PerpetualDashboardQueryParam param) {
		return Result.success(perpetualDashboardService.summary(param));
	}

	@PostMapping("/daily")
	public Result<List<PerpetualDashboardDailyItemVO>> daily(PerpetualDashboardQueryParam param) {
		return Result.success(perpetualDashboardService.daily(param));
	}
}

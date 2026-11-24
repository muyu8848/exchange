package com.exchange.controller.admin.dashboard;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.dashboard.param.MemberDashboardQueryParam;
import com.exchange.dashboard.service.MemberDashboardService;
import com.exchange.dashboard.vo.MemberDashboardDailyItemVO;
import com.exchange.dashboard.vo.MemberDashboardSummaryVO;

@RestController
@RequestMapping("/memberDashboard")
public class MemberDashboardController {

	@Autowired
	private MemberDashboardService memberDashboardService;

	@PostMapping("/summary")
	public Result<MemberDashboardSummaryVO> summary(MemberDashboardQueryParam param) {
		return Result.success(memberDashboardService.summary(param));
	}

	@PostMapping("/daily")
	public Result<List<MemberDashboardDailyItemVO>> daily(MemberDashboardQueryParam param) {
		return Result.success(memberDashboardService.daily(param));
	}
}

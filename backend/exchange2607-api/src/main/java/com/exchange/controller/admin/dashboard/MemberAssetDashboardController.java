package com.exchange.controller.admin.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.dashboard.param.MemberAssetDashboardQueryParam;
import com.exchange.dashboard.service.MemberAssetDashboardService;
import com.exchange.dashboard.vo.MemberAssetDashboardSummaryVO;

@RestController
@RequestMapping("/memberAssetDashboard")
public class MemberAssetDashboardController {

	@Autowired
	private MemberAssetDashboardService memberAssetDashboardService;

	@PostMapping("/summary")
	public Result<MemberAssetDashboardSummaryVO> summary(MemberAssetDashboardQueryParam param) {
		return Result.success(memberAssetDashboardService.summary(param));
	}
}

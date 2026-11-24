package com.exchange.controller.admin.dashboard;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.dashboard.param.SecondContractDashboardQueryParam;
import com.exchange.dashboard.service.SecondContractDashboardService;
import com.exchange.dashboard.vo.SecondContractDashboardSummaryVO;
import com.exchange.dashboard.vo.SecondContractDashboardTrendItemVO;

/**
 * 秒合约看板。
 */
@Validated
@RestController
@RequestMapping("/secondContractDashboard")
public class SecondContractDashboardController {

	@Autowired
	private SecondContractDashboardService secondContractDashboardService;

	@PostMapping("/summary")
	public Result<SecondContractDashboardSummaryVO> summary(SecondContractDashboardQueryParam param) {
		return Result.success(secondContractDashboardService.summary(param));
	}

	@PostMapping("/daily")
	public Result<List<SecondContractDashboardTrendItemVO>> daily(SecondContractDashboardQueryParam param) {
		return Result.success(secondContractDashboardService.daily(param));
	}
}

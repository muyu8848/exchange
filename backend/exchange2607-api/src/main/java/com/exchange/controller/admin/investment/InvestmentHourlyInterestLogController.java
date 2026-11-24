package com.exchange.controller.admin.investment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.investment.param.InvestmentHourlyInterestLogQueryCondParam;
import com.exchange.investment.service.InvestmentInterestLogQueryService;
import com.exchange.investment.vo.InvestmentHourlyInterestLogVO;
import com.exchange.investment.vo.InvestmentHourlyInterestLogSubtotalVO;

@RestController
@RequestMapping("/investmentHourlyInterestLog")
public class InvestmentHourlyInterestLogController {

	@Autowired
	private InvestmentInterestLogQueryService investmentInterestLogQueryService;

	@PostMapping("/findByPage")
	public Result<PageResult<InvestmentHourlyInterestLogVO>> findByPage(
			InvestmentHourlyInterestLogQueryCondParam param) {
		return Result.success(investmentInterestLogQueryService.findInvestmentHourlyInterestLogByPage(param));
	}

	@PostMapping("/subtotal")
	public Result<InvestmentHourlyInterestLogSubtotalVO> subtotal(InvestmentHourlyInterestLogQueryCondParam param) {
		return Result.success(investmentInterestLogQueryService.hourlyInterestLogSubtotal(param));
	}
}

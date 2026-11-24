package com.exchange.controller.admin.investment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.investment.param.InvestmentRedeemRecordQueryCondParam;
import com.exchange.investment.service.InvestmentInterestLogQueryService;
import com.exchange.investment.vo.InvestmentRedeemRecordVO;
import com.exchange.investment.vo.InvestmentRedeemRecordSubtotalVO;

@RestController
@RequestMapping("/investmentRedeemRecord")
public class InvestmentRedeemRecordController {

	@Autowired
	private InvestmentInterestLogQueryService investmentInterestLogQueryService;

	@PostMapping("/findByPage")
	public Result<PageResult<InvestmentRedeemRecordVO>> findByPage(InvestmentRedeemRecordQueryCondParam param) {
		return Result.success(investmentInterestLogQueryService.findInvestmentRedeemRecordPage(param));
	}

	@PostMapping("/findDetail")
	public Result<InvestmentRedeemRecordVO> findDetail(String id) {
		return Result.success(investmentInterestLogQueryService.findRedeemLogDetail(id));
	}

	@PostMapping("/subtotal")
	public Result<InvestmentRedeemRecordSubtotalVO> subtotal(InvestmentRedeemRecordQueryCondParam param) {
		return Result.success(investmentInterestLogQueryService.redeemLogSubtotal(param));
	}

}

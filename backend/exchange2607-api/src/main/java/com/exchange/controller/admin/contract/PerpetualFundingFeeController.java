package com.exchange.controller.admin.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.contract.param.PerpetualFundingFeeQueryCondParam;
import com.exchange.contract.service.PerpetualFundingFeeService;
import com.exchange.contract.vo.PerpetualFundingFeeSubtotalVO;
import com.exchange.contract.vo.PerpetualFundingFeeVO;

@RestController
@RequestMapping("/perpetualFundingFee")
public class PerpetualFundingFeeController {

	@Autowired
	private PerpetualFundingFeeService perpetualFundingFeeService;

	@PostMapping("/findByPage")
	public Result<PageResult<PerpetualFundingFeeVO>> findByPage(PerpetualFundingFeeQueryCondParam param) {
		return Result.success(perpetualFundingFeeService.findByPage(param));
	}

	@PostMapping("/subtotal")
	public Result<PerpetualFundingFeeSubtotalVO> subtotal(PerpetualFundingFeeQueryCondParam param) {
		return Result.success(perpetualFundingFeeService.subtotal(param));
	}
}

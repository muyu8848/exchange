package com.exchange.controller.admin.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.contract.param.FundingRateHistoryQueryCondParam;
import com.exchange.contract.service.PerpetualContractQueryService;
import com.exchange.contract.vo.FundingRateHistoryVO;

@Validated
@RestController
@RequestMapping("/perpetualFundingRateHistory")
public class PerpetualFundingRateHistoryController {

	@Autowired
	private PerpetualContractQueryService queryService;

	/** 资金费结算记录分页（后台） */
	@PostMapping("/findByPage")
	public Result<PageResult<FundingRateHistoryVO>> findByPage(FundingRateHistoryQueryCondParam param) {
		return Result.success(queryService.findFundingRateHistory(param));
	}

}

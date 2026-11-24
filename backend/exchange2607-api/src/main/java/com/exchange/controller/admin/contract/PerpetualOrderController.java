package com.exchange.controller.admin.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.contract.param.PerpetualOrderQueryCondParam;
import com.exchange.contract.service.PerpetualContractQueryService;
import com.exchange.contract.vo.PerpetualOrderSubtotalVO;
import com.exchange.contract.vo.PerpetualOrderVO;

@Validated
@RestController
@RequestMapping("/perpetualOrder")
public class PerpetualOrderController {

	@Autowired
	private PerpetualContractQueryService queryService;

	@PostMapping("/findByPage")
	public Result<PageResult<PerpetualOrderVO>> findByPage(PerpetualOrderQueryCondParam param) {
		return Result.success(queryService.findPerpetualOrderByPage(param));
	}

	@PostMapping("/findById")
	public Result<PerpetualOrderVO> findById(String id) {
		return Result.success(queryService.findPerpetualOrderById(id));
	}

	@PostMapping("/subtotal")
	public Result<PerpetualOrderSubtotalVO> subtotal(PerpetualOrderQueryCondParam param) {
		return Result.success(queryService.subtotal(param));
	}

}

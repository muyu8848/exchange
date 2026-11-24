package com.exchange.controller.client.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.contract.param.SecondContractOpenPositionParam;
import com.exchange.contract.param.SecondContractOrderQueryCondParam;
import com.exchange.contract.service.SecondContractOrderService;
import com.exchange.contract.vo.MySecondContractOrderVO;

import cn.dev33.satoken.stp.StpUtil;

@Validated
@RestController
@RequestMapping("/secondContractOrder")
public class SecondContractOrderController {

	@Autowired
	private SecondContractOrderService secondContractOrderService;

	@PostMapping("/findMyOrderByPage")
	public Result<PageResult<MySecondContractOrderVO>> findMyOrderByPage(SecondContractOrderQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(secondContractOrderService.findMySecondContractOrderByPage(param));
	}

	@PostMapping("/findMyOrderById")
	public Result<MySecondContractOrderVO> findMyOrderById(String id) {
		return Result.success(
				secondContractOrderService.findMyOrderById(StpUtil.getLoginIdAsString(), id));
	}

	@PostMapping("/openPosition")
	public Result<String> openPosition(SecondContractOpenPositionParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		String orderId = secondContractOrderService.openPosition(param);
		return Result.success(orderId);
	}

}

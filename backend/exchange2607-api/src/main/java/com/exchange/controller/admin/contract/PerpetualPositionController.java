package com.exchange.controller.admin.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.contract.param.PerpetualPositionQueryCondParam;
import com.exchange.contract.service.PerpetualContractQueryService;
import com.exchange.contract.service.PerpetualContractTradeService;
import com.exchange.contract.vo.PerpetualPositionSubtotalVO;
import com.exchange.contract.vo.PerpetualPositionVO;

import com.exchange.common.operlog.OperLog;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

@Validated
@RestController
@RequestMapping("/perpetualPosition")
public class PerpetualPositionController {

	@Autowired
	private PerpetualContractQueryService queryService;

	@Autowired
	private PerpetualContractTradeService tradeService;

	@PostMapping("/findByPage")
	public Result<PageResult<PerpetualPositionVO>> findByPage(PerpetualPositionQueryCondParam param) {
		return Result.success(queryService.findPerpetualPositionByPage(param));
	}

	@PostMapping("/findById")
	public Result<PerpetualPositionVO> findById(String id) {
		return Result.success(queryService.findPerpetualPositionById(id));
	}

	@PostMapping("/subtotal")
	public Result<PerpetualPositionSubtotalVO> subtotal(PerpetualPositionQueryCondParam param) {
		return Result.success(queryService.subtotal(param));
	}

	@SaCheckPermission("perpetualPosition.close")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_永续持仓, operate = "后台平仓")
	@PostMapping("/adminClose")
	public Result<String> adminClose(String positionId) {
		tradeService.adminClose(positionId);
		return Result.success();
	}

}

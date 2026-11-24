package com.exchange.controller.admin.spot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.spot.param.SpotPlanOrderQueryCondParam;
import com.exchange.spot.service.SpotPlanOrderService;
import com.exchange.spot.vo.SpotPlanOrderSubtotalVO;
import com.exchange.spot.vo.SpotPlanOrderVO;

import com.exchange.common.operlog.OperLog;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

@Validated
@RestController
@RequestMapping("/spotPlanOrder")
public class SpotPlanOrderController {

	@Autowired
	private SpotPlanOrderService spotPlanOrderService;

	@PostMapping("findByPage")
	public Result<PageResult<SpotPlanOrderVO>> findByPage(SpotPlanOrderQueryCondParam param) {
		return Result.success(spotPlanOrderService.findAdminByPage(param));
	}

	@PostMapping("findById")
	public Result<SpotPlanOrderVO> findById(String id) {
		return Result.success(spotPlanOrderService.findAdminById(id));
	}

	@PostMapping("subtotal")
	public Result<SpotPlanOrderSubtotalVO> subtotal(SpotPlanOrderQueryCondParam param) {
		return Result.success(spotPlanOrderService.subtotal(param));
	}

	@SaCheckPermission("spotPlanOrder.cancel")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_现货计划委托, operate = "撤销")
	@PostMapping("cancel")
	public Result<String> cancel(String id) {
		spotPlanOrderService.adminCancelOrder(id);
		return Result.success();
	}

}

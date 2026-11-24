package com.exchange.controller.admin.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.contract.param.PerpetualPlanOrderQueryCondParam;
import com.exchange.contract.service.PerpetualPlanOrderQueryService;
import com.exchange.contract.service.PerpetualPlanOrderService;
import com.exchange.contract.vo.PerpetualPlanOrderSubtotalVO;
import com.exchange.contract.vo.PerpetualPlanOrderVO;

import com.exchange.common.operlog.OperLog;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/perpetualPlanOrder")
public class PerpetualPlanOrderController {

	@Autowired
	private PerpetualPlanOrderService perpetualPlanOrderService;

	@Autowired
	private PerpetualPlanOrderQueryService perpetualPlanOrderQueryService;

	@PostMapping("findByPage")
	public Result<PageResult<PerpetualPlanOrderVO>> findByPage(PerpetualPlanOrderQueryCondParam param) {
		return Result.success(perpetualPlanOrderQueryService.findAdminByPage(param));
	}

	@PostMapping("findById")
	public Result<PerpetualPlanOrderVO> findById(String id) {
		return Result.success(perpetualPlanOrderQueryService.findAdminById(id));
	}

	@PostMapping("subtotal")
	public Result<PerpetualPlanOrderSubtotalVO> subtotal(PerpetualPlanOrderQueryCondParam param) {
		return Result.success(perpetualPlanOrderQueryService.subtotal(param));
	}

	@SaCheckPermission("perpetualPlanOrder.cancel")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_永续计划委托, operate = "撤销")
	@PostMapping("cancel")
	public Result<String> cancel(String id) {
		perpetualPlanOrderService.adminCancelOrder(id);
		return Result.success();
	}

}

package com.exchange.controller.client.contract;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.contract.param.PerpetualPlanOrderQueryCondParam;
import com.exchange.contract.param.PerpetualPlanTradeParam;
import com.exchange.contract.param.PerpetualSetTpslParam;
import com.exchange.contract.service.PerpetualPlanOrderQueryService;
import com.exchange.contract.service.PerpetualPlanOrderService;
import com.exchange.contract.vo.MyHistoryPlanOrderVO;
import com.exchange.contract.vo.MyPendingPlanOrderVO;
import com.exchange.contract.vo.PerpetualPlanOrderVO;

import cn.dev33.satoken.stp.StpUtil;

@Validated
@RestController
@RequestMapping("/perpetualPlanOrder")
public class PerpetualPlanOrderController {

	@Autowired
	private PerpetualPlanOrderService perpetualPlanOrderService;

	@Autowired
	private PerpetualPlanOrderQueryService perpetualPlanOrderQueryService;

	@PostMapping("/trade")
	public Result<String> trade(PerpetualPlanTradeParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(perpetualPlanOrderService.placePlanOrder(param));
	}

	/** 设置持仓止盈止损（止盈/止损两边独立设置，互不影响） */
	@PostMapping("/setPositionTpsl")
	public Result<String> setPositionTpsl(PerpetualSetTpslParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		perpetualPlanOrderService.setPositionTpsl(param);
		return Result.success();
	}

	@PostMapping("/cancel")
	public Result<String> cancel(String orderId) {
		perpetualPlanOrderService.cancelOrder(StpUtil.getLoginIdAsString(), orderId);
		return Result.success();
	}

	@PostMapping("/findMyPlanOrderByPage")
	public Result<PageResult<PerpetualPlanOrderVO>> findMyPlanOrderByPage(PerpetualPlanOrderQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(perpetualPlanOrderQueryService.findMyOrderByPage(param));
	}

	@PostMapping("/findMyPendingPlanOrder")
	public Result<List<MyPendingPlanOrderVO>> findMyPendingPlanOrder(
			PerpetualPlanOrderQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		param.setStatus(Constant.永续计划委托状态_委托中);
		return Result.success(perpetualPlanOrderQueryService.findMyPendingPlanOrder(param));
	}

	/** 历史委托-计划委托：已委托 + 已撤销 + 委托失败 */
	@PostMapping("/findMyHistoryPlanOrderByPage")
	public Result<PageResult<MyHistoryPlanOrderVO>> findMyHistoryPlanOrderByPage(
			PerpetualPlanOrderQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		param.setStatus(Constant.永续计划委托状态_已委托 + "," + Constant.永续计划委托状态_已撤销 + "," + Constant.永续计划委托状态_委托失败 + ","
				+ Constant.永续计划委托状态_已失效);
		return Result.success(perpetualPlanOrderQueryService.findMyHistoryPlanOrderByPage(param));
	}

}

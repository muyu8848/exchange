package com.exchange.controller.client.contract;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.contract.param.AdjustPositionMarginParam;
import com.exchange.contract.param.FundingRateHistoryQueryCondParam;
import com.exchange.contract.param.PerpetualMarketClosePartPositionParam;
import com.exchange.contract.param.PerpetualOpenPositionParam;
import com.exchange.contract.param.PerpetualOrderQueryCondParam;
import com.exchange.contract.param.PerpetualPlaceCloseLimitOrderParam;
import com.exchange.contract.param.PerpetualPositionQueryCondParam;
import com.exchange.contract.param.PreviewMarginLiqPriceParam;
import com.exchange.contract.service.PerpetualContractQueryService;
import com.exchange.contract.service.PerpetualContractTradeService;
import com.exchange.contract.vo.FundingRateHistoryVO;
import com.exchange.contract.vo.MyClosedPositionVO;
import com.exchange.contract.vo.MyHistoryPerpetualContractOrderVO;
import com.exchange.contract.vo.MyPendingPerpetualContractOrderVO;
import com.exchange.contract.vo.MyPerpetualPositionVO;
import com.exchange.contract.vo.PerpetualLimitModifyOrderVO;
import com.exchange.contract.vo.PerpetualOrderDetailVO;
import com.exchange.contract.vo.PreviewMarginLiqPriceVO;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/perpetualContract")
public class PerpetualContractController {

	@Autowired
	private PerpetualContractQueryService queryService;

	@Autowired
	private PerpetualContractTradeService tradeService;

	@PostMapping("/openPosition")
	public Result<String> openPosition(PerpetualOpenPositionParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(tradeService.openPosition(param));
	}

	/** 市价全平 */
	@PostMapping("/marketCloseAll")
	public Result<String> marketCloseAll(String positionId) {
		tradeService.marketCloseAll(StpUtil.getLoginIdAsString(), positionId);
		return Result.success();
	}

	/** 市价部分平仓 */
	@PostMapping("/marketClosePart")
	public Result<String> marketClosePart(PerpetualMarketClosePartPositionParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		tradeService.marketClosePart(param);
		return Result.success();
	}

	@PostMapping("/reversePosition")
	public Result<String> reversePosition(String positionId) {
		return Result.success(tradeService.reversePosition(StpUtil.getLoginIdAsString(), positionId));
	}

	@PostMapping("/adjustMargin")
	public Result<String> adjustMargin(AdjustPositionMarginParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(tradeService.adjustMargin(param));
	}

	/** 资金费率历史（客户端：按交易对分页） */
	@PostMapping("/findFundingRateHistory")
	public Result<PageResult<FundingRateHistoryVO>> findFundingRateHistory(FundingRateHistoryQueryCondParam param) {
		return Result.success(queryService.findFundingRateHistory(param));
	}

	/** 调整保证金预览：返回调整后的参考强平价与保证金率（只读，不落库） */
	@PostMapping("/previewMarginLiqPrice")
	public Result<PreviewMarginLiqPriceVO> previewMarginLiqPrice(PreviewMarginLiqPriceParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(queryService.previewMarginLiqPrice(param));
	}

	@PostMapping("/placeCloseLimitOrder")
	public Result<String> placeCloseLimitOrder(PerpetualPlaceCloseLimitOrderParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(tradeService.placeCloseLimitOrder(param.getMemberAccountId(), param.getPositionId(),
				param.getPrice(), param.getSize()));
	}

	@PostMapping("/cancelOrder")
	public Result<String> cancelOrder(String orderId) {
		tradeService.cancelOrder(StpUtil.getLoginIdAsString(), orderId);
		return Result.success();
	}

	/** 修改限价开仓挂单：调整价格/数量，保证金与手续费差额多退少补 */
	@PostMapping("/changeOrder")
	public Result<String> changeOrder(String orderId, BigDecimal price, BigDecimal size) {
		return Result.success(tradeService.changeOrder(StpUtil.getLoginIdAsString(), orderId, price, size));
	}

	/** 我的持仓：一次返回全部 */
	@PostMapping("/findMyPositions")
	public Result<List<MyPerpetualPositionVO>> findMyPositions(PerpetualPositionQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(queryService.findMyPositions(param));
	}

	/** 历史仓位：已平仓/已强平持仓分页记录 */
	@PostMapping("/findMyClosedPositionByPage")
	public Result<PageResult<MyClosedPositionVO>> findMyClosedPositionByPage(PerpetualPositionQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		param.setStatus(Constant.永续持仓状态_已平仓 + "," + Constant.永续持仓状态_已强平);
		return Result.success(queryService.findMyClosedPositionByPage(param));
	}

	@PostMapping("/findMyOrderById")
	public Result<PerpetualOrderDetailVO> findMyOrderById(String id) {
		return Result.success(queryService.findMyOrderById(StpUtil.getLoginIdAsString(), id));
	}

	/** 限价修改订单弹窗数据（含实时最新价） */
	@PostMapping("/findPerpetualLimitModifyOrderById")
	public Result<PerpetualLimitModifyOrderVO> findPerpetualLimitModifyOrderById(String id) {
		return Result.success(queryService.findPerpetualLimitModifyOrderById(StpUtil.getLoginIdAsString(), id));
	}

	@PostMapping("/findMyPendingOrder")
	public Result<List<MyPendingPerpetualContractOrderVO>> findMyPendingOrder(PerpetualOrderQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		param.setStatus(Constant.现货订单状态_待成交);
		return Result.success(queryService.findMyPendingOrder(param));
	}

	/** 历史委托-限价委托/市价委托：已成交 + 已撤销，排除强平、资金费单 */
	@PostMapping("/findMyHistoryOrderByPage")
	public Result<PageResult<MyHistoryPerpetualContractOrderVO>> findMyHistoryOrderByPage(
			PerpetualOrderQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		param.setStatus(Constant.现货订单状态_已完成 + "," + Constant.现货订单状态_已撤销);
		param.setType(Constant.永续订单类型_开仓 + "," + Constant.永续订单类型_平仓);
		return Result.success(queryService.findMyHistoryOrderByPage(param));
	}

}

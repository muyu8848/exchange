package com.exchange.contract.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.exception.BizException;
import com.exchange.common.utils.IdUtils;
import com.exchange.common.utils.SymbolPriceUtils;
import com.exchange.constants.Constant;
import com.exchange.contract.domain.PerpetualContractSymbol;
import com.exchange.contract.domain.PerpetualPlanOrder;
import com.exchange.contract.domain.PerpetualPosition;
import com.exchange.contract.param.PerpetualOpenPositionParam;
import com.exchange.contract.param.PerpetualPlanTradeParam;
import com.exchange.contract.param.PerpetualSetTpslParam;
import com.exchange.contract.repo.PerpetualContractSymbolRepo;
import com.exchange.contract.repo.PerpetualPlanOrderRepo;
import com.exchange.contract.repo.PerpetualPositionRepo;
import com.zengtengpeng.annotation.Lock;

import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class PerpetualPlanOrderService {

	@Autowired
	private PerpetualPlanOrderRepo planOrderRepo;

	@Autowired
	private PerpetualContractSymbolRepo symbolRepo;

	@Autowired
	private PerpetualPositionRepo positionRepo;

	@Autowired
	private PerpetualContractTradeService tradeService;

	@Lock(keys = "'perpPlanTrade' + #param.memberAccountId")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public String placePlanOrder(@Valid PerpetualPlanTradeParam param) {
		if (Constant.永续订单类型_开仓.equals(param.getPlanType())) {
			return placeOpenPlanOrder(param);
		}
		if (Constant.永续订单类型_平仓.equals(param.getPlanType())) {
			return placeClosePlanOrder(param);
		}
		throw new BizException("未知的计划类型");
	}

	private String placeOpenPlanOrder(PerpetualPlanTradeParam param) {
		if (StrUtil.isBlank(param.getSymbolCode()) || StrUtil.isBlank(param.getDirection())
				|| param.getLeverage() == null || param.getMargin() == null) {
			throw new BizException("参数不完整");
		}
		PerpetualContractSymbol symbol = symbolRepo.findTopBySymbolCodeAndDeletedFlagFalse(param.getSymbolCode())
				.orElse(null);
		if (symbol == null) {
			throw new BizException("未知的交易对");
		}
		if (!Constant.启用状态_启用.equals(symbol.getStatus())) {
			throw new BizException("交易对不可用");
		}
		if (param.getLeverage() < 1 || param.getLeverage() > symbol.getMaxLeverage()) {
			throw new BizException("杠杆倍数范围为1~{0}", symbol.getMaxLeverage());
		}
		if (!Constant.永续合约方向_多.equals(param.getDirection()) && !Constant.永续合约方向_空.equals(param.getDirection())) {
			throw new BizException("未知的方向");
		}
		if (symbol.getMinOrderValue() != null && param.getMargin().multiply(BigDecimal.valueOf(param.getLeverage()))
				.compareTo(symbol.getMinOrderValue()) < 0) {
			throw new BizException("最小名义价值为{0}", symbol.getMinOrderValue());
		}
		PerpetualPlanOrder order = buildPlanOrder(param, symbol.getPriceScale());
		order.setType(Constant.永续订单类型_开仓);
		order.setSource(Constant.永续计划来源_普通);
		order.setDirection(param.getDirection());
		order.setLeverage(param.getLeverage());
		order.setMargin(param.getMargin());
		planOrderRepo.save(order);
		return order.getId();
	}

	private String placeClosePlanOrder(PerpetualPlanTradeParam param) {
		if (StrUtil.isBlank(param.getPositionId())) {
			throw new BizException("参数不完整");
		}
		PerpetualPosition position = positionRepo.findById(param.getPositionId()).orElse(null);
		if (position == null || !param.getMemberAccountId().equals(position.getMemberAccountId())) {
			throw new BizException("持仓不存在");
		}
		if (!Constant.永续持仓状态_持仓中.equals(position.getStatus())) {
			throw new BizException("当前持仓不可平仓");
		}
		PerpetualContractSymbol symbol = symbolRepo.findTopBySymbolCodeAndDeletedFlagFalse(position.getSymbolCode())
				.orElse(null);
		int priceScale = symbol != null && symbol.getPriceScale() != null ? symbol.getPriceScale() : 2;

		PerpetualPlanOrder order = buildPlanOrder(param, priceScale);
		order.setType(Constant.永续订单类型_平仓);
		order.setSource(Constant.永续计划来源_普通);
		order.setPositionId(position.getId());
		order.setDirection(position.getDirection());
		order.setLeverage(position.getLeverage());
		order.setMargin(position.getMargin());
		planOrderRepo.save(order);
		return order.getId();
	}

	private PerpetualPlanOrder buildPlanOrder(PerpetualPlanTradeParam param, int priceScale) {
		// 自动推断触发条件
		String marketPriceStr = SymbolPriceUtils.getClosePrice(Constant.行情业务类型_合约, symbolCodeOf(param));
		if (StrUtil.isBlank(marketPriceStr)) {
			throw new BizException("未同步最新币价");
		}
		BigDecimal marketPrice = new BigDecimal(marketPriceStr);
		String triggerType = param.getTriggerPrice().compareTo(marketPrice) >= 0 ? Constant.计划触发_大于等于
				: Constant.计划触发_小于等于;

		// 触发价精度显式校验：超出交易对价格精度直接报错，不再静默四舍五入
		if (param.getTriggerPrice().stripTrailingZeros().scale() > priceScale) {
			throw new BizException("触发价精度不能超过{0}位", priceScale);
		}
		BigDecimal triggerPrice = param.getTriggerPrice().setScale(priceScale, RoundingMode.DOWN);
		BigDecimal limitPrice = null;
		if (Constant.计划执行_限价.equals(param.getExecType())) {
			if (param.getPrice() == null || param.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
				throw new BizException("请输入委托价");
			}
			// 委托价精度显式校验
			if (param.getPrice().stripTrailingZeros().scale() > priceScale) {
				throw new BizException("委托价精度不能超过{0}位", priceScale);
			}
			limitPrice = param.getPrice().setScale(priceScale, RoundingMode.DOWN);
		} else if (!Constant.计划执行_市价.equals(param.getExecType())) {
			throw new BizException("未知的执行方式");
		}

		return PerpetualPlanOrder.buildFromTradeParam(param, symbolCodeOf(param), triggerType, triggerPrice,
				limitPrice);
	}

	private String symbolCodeOf(PerpetualPlanTradeParam param) {
		if (StrUtil.isNotBlank(param.getSymbolCode())) {
			return param.getSymbolCode();
		}
		if (StrUtil.isNotBlank(param.getPositionId())) {
			PerpetualPosition position = positionRepo.findById(param.getPositionId()).orElse(null);
			return position != null ? position.getSymbolCode() : null;
		}
		return null;
	}

	/** 运营后台：撤销计划委托（与触发任务并发安全） */
	@Lock(keys = "'perpPlanOrder' + #orderId")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void adminCancelOrder(String orderId) {
		PerpetualPlanOrder order = planOrderRepo.findById(orderId).orElse(null);
		if (order == null) {
			throw new BizException("订单不存在");
		}
		if (!Constant.永续计划委托状态_委托中.equals(order.getStatus())) {
			throw new BizException("订单状态不可撤销");
		}
		order.setStatus(Constant.永续计划委托状态_已撤销);
		planOrderRepo.save(order);
	}

	/** 撤销计划委托（未触发时直接改状态） */
	@Lock(keys = "'perpPlanCancel' + #memberAccountId")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void cancelOrder(String memberAccountId, String orderId) {
		PerpetualPlanOrder order = planOrderRepo.findById(orderId).orElse(null);
		if (order == null) {
			throw new BizException("订单不存在");
		}
		if (!memberAccountId.equals(order.getMemberAccountId())) {
			throw new BizException("无权操作");
		}
		if (!Constant.永续计划委托状态_委托中.equals(order.getStatus())) {
			throw new BizException("订单状态不可撤销");
		}
		order.setStatus(Constant.永续计划委托状态_已撤销);
		planOrderRepo.save(order);
	}

	// ============ 止盈止损（TP/SL） ============

	/**
	 * 设置持仓止盈止损：止盈/止损两边独立取消+重建，互不影响；并回显到持仓。
	 */
	@Lock(keys = "'perpTpsl' + #param.memberAccountId")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void setPositionTpsl(PerpetualSetTpslParam param) {
		PerpetualPosition position = positionRepo.findById(param.getPositionId()).orElse(null);
		if (position == null || !param.getMemberAccountId().equals(position.getMemberAccountId())) {
			throw new BizException("持仓不存在");
		}
		if (!Constant.永续持仓状态_持仓中.equals(position.getStatus())) {
			throw new BizException("当前持仓不可设置止盈止损");
		}
		boolean hasTp = param.getTakeProfitPrice() != null && param.getTakeProfitPrice().compareTo(BigDecimal.ZERO) > 0;
		boolean hasSl = param.getStopLossPrice() != null && param.getStopLossPrice().compareTo(BigDecimal.ZERO) > 0;
		if (!hasTp && !hasSl) {
			throw new BizException("请至少设置止盈价或止损价");
		}
		// 方向校验：避免设置「立即触发」的止盈止损（多头止盈须高于现价、止损须低于现价；空头反之）
		String priceStr = SymbolPriceUtils.getClosePrice(Constant.行情业务类型_合约, position.getSymbolCode());
		if (StrUtil.isBlank(priceStr)) {
			throw new BizException("未同步最新币价");
		}
		BigDecimal marketPrice = new BigDecimal(priceStr);
		boolean isLong = Constant.永续合约方向_多.equals(position.getDirection());
		if (hasTp) {
			boolean valid = isLong ? param.getTakeProfitPrice().compareTo(marketPrice) > 0
					: param.getTakeProfitPrice().compareTo(marketPrice) < 0;
			if (!valid) {
				throw new BizException(isLong ? "多头止盈价必须高于当前价" : "空头止盈价必须低于当前价");
			}
		}
		if (hasSl) {
			boolean valid = isLong ? param.getStopLossPrice().compareTo(marketPrice) < 0
					: param.getStopLossPrice().compareTo(marketPrice) > 0;
			if (!valid) {
				throw new BizException(isLong ? "多头止损价必须低于当前价" : "空头止损价必须高于当前价");
			}
		}
		// 止盈边：独立取消旧的再重建（不波止损边）
		cancelPlanOrderBySource(param.getPositionId(), param.getMemberAccountId(), Constant.永续计划来源_止盈);
		if (hasTp) {
			placeTpslPlanOrder(position, param.getTakeProfitPrice(), true);
		}
		// 止损边：独立取消旧的再重建（不波止盈边）
		cancelPlanOrderBySource(param.getPositionId(), param.getMemberAccountId(), Constant.永续计划来源_止损);
		if (hasSl) {
			placeTpslPlanOrder(position, param.getStopLossPrice(), false);
		}
		// 回显到持仓，便于列表展示与弹窗回填
		position.setTakeProfitPrice(hasTp ? param.getTakeProfitPrice() : null);
		position.setStopLossPrice(hasSl ? param.getStopLossPrice() : null);
		positionRepo.save(position);
	}

	/** 构建并落库单条止盈/止损平仓计划单（source 区分 tp/sl） */
	private void placeTpslPlanOrder(PerpetualPosition position, BigDecimal triggerPrice, boolean isTakeProfit) {
		PerpetualContractSymbol symbol = symbolRepo.findTopBySymbolCodeAndDeletedFlagFalse(position.getSymbolCode())
				.orElse(null);
		int priceScale = symbol != null && symbol.getPriceScale() != null ? symbol.getPriceScale() : 2;
		if (triggerPrice.stripTrailingZeros().scale() > priceScale) {
			throw new BizException("触发价精度不能超过{0}位", priceScale);
		}
		BigDecimal tp = triggerPrice.setScale(priceScale, RoundingMode.DOWN);
		String triggerType = tpslTriggerType(position.getDirection(), isTakeProfit);
		String source = isTakeProfit ? Constant.永续计划来源_止盈 : Constant.永续计划来源_止损;

		PerpetualPlanOrder order = new PerpetualPlanOrder();
		order.setId(IdUtils.getId());
		order.setOrderNo(order.getId());
		order.setMemberAccountId(position.getMemberAccountId());
		order.setSymbolCode(position.getSymbolCode());
		order.setType(Constant.永续订单类型_平仓);
		order.setPositionId(position.getId());
		order.setDirection(position.getDirection());
		order.setLeverage(position.getLeverage());
		order.setMargin(position.getMargin());
		order.setTriggerPrice(tp);
		order.setTriggerType(triggerType);
		order.setExecType(Constant.计划执行_市价);
		order.setPrice(null);
		order.setSource(source);
		order.setStatus(Constant.永续计划委托状态_委托中);
		order.setCreateTime(LocalDateTime.now());
		planOrderRepo.save(order);
	}

	/** 按持仓方向显式推导触发条件（绝不走自动推断）：long 止盈=价涨到(ge)/止损=价跌到(le)；short 反之 */
	private String tpslTriggerType(String direction, boolean isTakeProfit) {
		if (Constant.永续合约方向_多.equals(direction)) {
			return isTakeProfit ? Constant.计划触发_大于等于 : Constant.计划触发_小于等于;
		}
		return isTakeProfit ? Constant.计划触发_小于等于 : Constant.计划触发_大于等于;
	}

	/** 取消某持仓指定来源的委托中计划单（单边重设用） */
	private void cancelPlanOrderBySource(String positionId, String memberAccountId, String source) {
		List<PerpetualPlanOrder> orders = planOrderRepo.findByPositionIdAndMemberAccountIdAndStatusAndSource(positionId,
				memberAccountId, Constant.永续计划委托状态_委托中, source);
		for (PerpetualPlanOrder o : orders) {
			o.setStatus(Constant.永续计划委托状态_已失效);
		}
		if (!orders.isEmpty()) {
			planOrderRepo.saveAll(orders);
		}
	}

	/**
	 * 计划委托触发失败：用独立新事务记录失败状态，避免业务事务已回滚导致状态存不进去、下一轮反复重试。
	 * <p>
	 * 必须由其它 Bean 调用（{@code PerpetualOrderMatchService}）；自调用会让 {@code REQUIRES_NEW} 失效。
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
	public void markPlanOrderFailed(String planOrderId) {
		PerpetualPlanOrder order = planOrderRepo.findById(planOrderId).orElse(null);
		if (order == null || !Constant.永续计划委托状态_委托中.equals(order.getStatus())) {
			return;
		}
		order.setStatus(Constant.永续计划委托状态_委托失败);
		order.setTriggerTime(LocalDateTime.now());
		planOrderRepo.save(order);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void executePlan(PerpetualPlanOrder order) {
		String priceStr = SymbolPriceUtils.getClosePrice(Constant.行情业务类型_合约, order.getSymbolCode());
		if (StrUtil.isBlank(priceStr)) {
			return;
		}
		BigDecimal marketPrice = new BigDecimal(priceStr);

		// 判断是否触发
		if (Constant.计划触发_大于等于.equals(order.getTriggerType())) {
			if (marketPrice.compareTo(order.getTriggerPrice()) < 0) {
				return;
			}
		} else if (Constant.计划触发_小于等于.equals(order.getTriggerType())) {
			if (marketPrice.compareTo(order.getTriggerPrice()) > 0) {
				return;
			}
		} else {
			return;
		}

		// 触发！执行委托（业务失败直接抛出，由调用方用独立事务记录失败状态，避免事务已废导致状态丢失）
		String resultOrderId = Constant.永续订单类型_开仓.equals(order.getType()) ? executeOpenPlan(order)
				: executeClosePlan(order);
		order.setStatus(Constant.永续计划委托状态_已委托);
		order.setPerpetualOrderId(resultOrderId);
		order.setTriggerTime(LocalDateTime.now());
		planOrderRepo.save(order);
	}

	/** 计划委托开仓执行（包级可见：供 PerpetualOrderMatchService 在独立事务内调用） */
	String executeOpenPlan(PerpetualPlanOrder order) {
		PerpetualOpenPositionParam param = new PerpetualOpenPositionParam();
		param.setMemberAccountId(order.getMemberAccountId());
		param.setSymbolCode(order.getSymbolCode());
		param.setDirection(order.getDirection());
		param.setLeverage(order.getLeverage());
		param.setMargin(order.getMargin());
		param.setEntrustType(Constant.永续委托类型_限价.equals(order.getExecType()) ? Constant.永续委托类型_限价 : Constant.永续委托类型_市价);
		if (Constant.永续委托类型_限价.equals(param.getEntrustType())) {
			param.setPrice(order.getPrice());
		}
		return tradeService.openPosition(param);
	}

	/** 计划委托平仓执行（包级可见：供 PerpetualOrderMatchService 在独立事务内调用） */
	String executeClosePlan(PerpetualPlanOrder order) {
		// 持仓已平/强平：竞争兜底，撤销该计划单，避免误触
		PerpetualPosition position = positionRepo.findById(order.getPositionId()).orElse(null);
		if (position == null || !Constant.永续持仓状态_持仓中.equals(position.getStatus())) {
			order.setStatus(Constant.永续计划委托状态_已失效);
			planOrderRepo.save(order);
			return null;
		}
		// 触发后清除持仓对应止盈/止损回显（仅 tp/sl 来源）
		if (Constant.永续计划来源_止盈.equals(order.getSource()) || Constant.永续计划来源_止损.equals(order.getSource())) {
			if (Constant.永续计划来源_止盈.equals(order.getSource())) {
				position.setTakeProfitPrice(null);
			} else {
				position.setStopLossPrice(null);
			}
			positionRepo.save(position);
		}
		if (Constant.计划执行_限价.equals(order.getExecType())) {
			return tradeService.placeCloseLimitOrder(order.getMemberAccountId(), order.getPositionId(),
					order.getPrice(), null);
		}
		tradeService.marketCloseAll(order.getMemberAccountId(), order.getPositionId());
		return null;
	}

}

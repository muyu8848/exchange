package com.exchange.contract.param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exchange.common.exception.BizException;
import com.exchange.common.utils.IdUtils;
import com.exchange.constants.Constant;
import com.exchange.contract.domain.PerpetualContractSymbol;
import com.exchange.contract.domain.PerpetualOrder;
import com.exchange.contract.domain.PerpetualPosition;

import lombok.Data;

@Data
public class PerpetualOpenPositionParam {

	private String memberAccountId;

	@NotBlank
	private String symbolCode;

	@NotBlank
	private String direction;

	/** 委托类型：market=市价, limit=限价，默认市价 */
	private String entrustType;

	/** 限价委托价格（限价时必填） */
	private BigDecimal price;

	@NotNull
	private Integer leverage;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal margin;

	public PerpetualContractSymbol validate(PerpetualContractSymbol symbol) {
		if (symbol == null) {
			throw new BizException("未知的交易对");
		}
		if (!Constant.启用状态_启用.equals(symbol.getStatus())) {
			throw new BizException("交易对不可用");
		}
		if (leverage < 1 || leverage > symbol.getMaxLeverage()) {
			throw new BizException("杠杆倍数范围为1~{0}", symbol.getMaxLeverage());
		}
		if (!Constant.永续合约方向_多.equals(direction) && !Constant.永续合约方向_空.equals(direction)) {
			throw new BizException("未知的方向");
		}
		if (symbol.getMinOrderValue() != null
				&& margin.multiply(BigDecimal.valueOf(leverage)).compareTo(symbol.getMinOrderValue()) < 0) {
			throw new BizException("最小名义价值为{0}", symbol.getMinOrderValue());
		}
		return symbol;
	}

	public PerpetualPosition buildPosition(
			BigDecimal openPrice, BigDecimal size, BigDecimal liquidationPrice) {
		PerpetualPosition po = new PerpetualPosition();
		po.setId(IdUtils.getId());
		po.setOrderNo(po.getId());
		po.setMemberAccountId(memberAccountId);
		po.setSymbolCode(symbolCode);
		po.setDirection(direction);
		po.setLeverage(leverage);
		po.setMargin(margin);
		po.setSize(size);
		po.setOpenPrice(openPrice);
		po.setLiquidationPrice(liquidationPrice);
		po.setAccumulatedFunding(BigDecimal.ZERO);
		po.setStatus(Constant.永续持仓状态_持仓中);
		po.setOpenTime(LocalDateTime.now());
		po.setRealizedPnl(BigDecimal.ZERO);
		return po;
	}

	public PerpetualOrder buildOpenOrder(
			String positionId, BigDecimal price, BigDecimal size, BigDecimal fee) {
		PerpetualOrder po = new PerpetualOrder();
		po.setId(IdUtils.getId());
		po.setOrderNo(po.getId());
		po.setMemberAccountId(memberAccountId);
		po.setPositionId(positionId);
		po.setSymbolCode(symbolCode);
		po.setType(Constant.永续订单类型_开仓);
		po.setEntrustType(Constant.永续委托类型_市价.equals(entrustType) ? Constant.永续委托类型_市价 : Constant.永续委托类型_限价);
		po.setStatus(Constant.永续订单状态_已成交);
		po.setDirection(direction);
		po.setLeverage(leverage);
		po.setMargin(margin);
		po.setSize(size);
		po.setPrice(price);
		po.setFee(fee);
		po.setCreateTime(LocalDateTime.now());
		return po;
	}

}

package com.exchange.contract.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.exchange.constants.Constant;
import com.exchange.contract.domain.PerpetualOrder;
import com.exchange.dictconfig.DictHolder;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 永续合约-历史委托成交详情 VO（单表版，仅取 perpetual_order 自身数据）。
 */
@Data
public class PerpetualOrderDetailVO {

	private String id;

	private String orderNo;

	private String positionId;

	private String symbolCode;

	/** 订单类型：open / close / liquidate */
	private String type;

	/** 订单类型文案：开仓 / 平仓 / 强平 */
	private String typeName;

	/** 委托类型：market / limit */
	private String entrustType;

	/** 委托类型文案：限价 / 市价 */
	private String entrustTypeName;

	/** 订单状态：0=待成交, 1=已成交, 2=已撤销 */
	private String status;

	/** 状态文案：待成交 / 已成交 / 已撤销 */
	private String statusName;

	private String direction;

	/** 开平方向文案：开多/开空（开仓）、平多/平空（平仓） */
	private String orderDirectionName;

	private Integer leverage;

	/** 保证金(USDT) */
	private BigDecimal margin;

	/** 委托量(基础币种) */
	private BigDecimal size;

	/** 委托价(USDT计价) */
	private BigDecimal price;

	/** 成交量（已成交=委托量，已撤销=0） */
	private BigDecimal filledSize;

	/** 成交均价（已成交取委托价） */
	private BigDecimal avgPrice;

	/** 手续费(USDT) */
	private BigDecimal fee;

	/** 已实现盈亏(USDT，平仓/强平单有值) */
	private BigDecimal pnl;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	/** 成交时间（已成交单有值） */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime fillTime;

	public static PerpetualOrderDetailVO convertFor(PerpetualOrder po) {
		if (po == null) {
			return null;
		}
		PerpetualOrderDetailVO vo = new PerpetualOrderDetailVO();
		vo.setId(po.getId());
		vo.setOrderNo(po.getOrderNo());
		vo.setPositionId(po.getPositionId());
		vo.setSymbolCode(po.getSymbolCode());
		vo.setType(po.getType());
		vo.setEntrustType(po.getEntrustType());
		vo.setStatus(po.getStatus());
		vo.setDirection(po.getDirection());
		vo.setLeverage(po.getLeverage());
		vo.setMargin(po.getMargin());
		vo.setSize(po.getSize());
		vo.setPrice(po.getPrice());
		vo.setFee(po.getFee());
		vo.setPnl(po.getPnl());
		vo.setCreateTime(po.getCreateTime());
		vo.setFillTime(po.getFillTime());

		vo.setTypeName(DictHolder.getDictItemName("perpetualOrderType", po.getType()));
		vo.setOrderDirectionName(
				DictHolder.getDictItemName("perpetualOrderDirection", po.getType() + "-" + po.getDirection()));
		vo.setEntrustTypeName(DictHolder.getDictItemName("perpetualEntrustType", po.getEntrustType()));
		vo.setStatusName(DictHolder.getDictItemName("perpetualOrderStatus", po.getStatus()));

		if (Constant.永续订单状态_已成交.equals(po.getStatus())) {
			vo.setFilledSize(po.getSize());
			vo.setAvgPrice(po.getPrice());
		} else if (Constant.永续订单状态_待成交.equals(po.getStatus())) {
			vo.setFilledSize(BigDecimal.ZERO);
		} else if (Constant.永续订单状态_已撤销.equals(po.getStatus())) {
			vo.setFilledSize(BigDecimal.ZERO);
		}
		return vo;
	}

}

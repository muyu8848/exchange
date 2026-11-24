package com.exchange.contract.vo;

import java.math.BigDecimal;

import com.exchange.common.utils.SymbolPriceUtils;
import com.exchange.constants.Constant;
import com.exchange.contract.domain.PerpetualOrder;
import com.exchange.dictconfig.DictHolder;

import lombok.Data;

/**
 * 修改订单弹窗数据：仅返回弹窗所需字段（含实时最新价）。
 */
@Data
public class PerpetualLimitModifyOrderVO {

	private String id;

	private String symbolCode;

	/** 方向：long=多, short=空 */
	private String direction;

	/** 订单类型：open=开仓, close=平仓, liquidate=强平 */
	private String type;

	/** 开平方向文案：开多/开空 */
	private String orderDirectionName;

	/** 委托类型：market=市价, limit=限价 */
	private String entrustType;

	private Integer leverage;

	/** 委托价 */
	private BigDecimal price;

	/** 委托量 */
	private BigDecimal size;

	/** 订单状态：0=待成交 */
	private String status;

	/** 最新价（接口查询时刻实时获取） */
	private String latestPrice;

	public static PerpetualLimitModifyOrderVO convertFor(PerpetualOrder po) {
		if (po == null) {
			return null;
		}
		PerpetualLimitModifyOrderVO vo = new PerpetualLimitModifyOrderVO();
		vo.setId(po.getId());
		vo.setSymbolCode(po.getSymbolCode());
		vo.setDirection(po.getDirection());
		vo.setType(po.getType());
		vo.setOrderDirectionName(
				DictHolder.getDictItemName("perpetualOrderDirection", po.getType() + "-" + po.getDirection()));
		vo.setEntrustType(po.getEntrustType());
		vo.setLeverage(po.getLeverage());
		vo.setPrice(po.getPrice());
		vo.setSize(po.getSize());
		vo.setStatus(po.getStatus());
		vo.setLatestPrice(SymbolPriceUtils.getClosePrice(Constant.行情业务类型_合约, po.getSymbolCode()));
		return vo;
	}

}

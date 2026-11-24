package com.exchange.contract.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.exchange.contract.domain.PerpetualOrder;
import com.exchange.dictconfig.DictHolder;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 当前委托-限价|市价子 tab 展示 VO（待成交挂单）。
 */
@Data
public class MyPendingPerpetualContractOrderVO {

	private String id;

	private String symbolCode;

	/** 订单类型：open / close / liquidate */
	private String type;

	/** 开平方向文案：开多/开空（开仓挂单）、平多/平空（平仓挂单） */
	private String orderDirectionName;

	/** 方向：long=多, short=空 */
	private String direction;

	/** 委托类型文案：限价 */
	private String entrustTypeName;

	/** 委托类型：market=市价, limit=限价 */
	private String entrustType;

	private Integer leverage;

	/** 委托价 */
	private BigDecimal price;

	/** 委托量 */
	private BigDecimal size;

	/** 成交量（永续挂单无部分成交，固定 0） */
	private BigDecimal filledSize;

	@JsonFormat(pattern = "MM/dd HH:mm")
	private LocalDateTime createTime;

	public static MyPendingPerpetualContractOrderVO convertFor(PerpetualOrder po) {
		if (po == null) {
			return null;
		}
		MyPendingPerpetualContractOrderVO vo = new MyPendingPerpetualContractOrderVO();
		vo.setId(po.getId());
		vo.setSymbolCode(po.getSymbolCode());
		vo.setType(po.getType());
		vo.setOrderDirectionName(
				DictHolder.getDictItemName("perpetualOrderDirection", po.getType() + "-" + po.getDirection()));
		vo.setDirection(po.getDirection());
		vo.setEntrustTypeName(DictHolder.getDictItemName("perpetualEntrustType", po.getEntrustType()));
		vo.setEntrustType(po.getEntrustType());
		vo.setLeverage(po.getLeverage());
		vo.setPrice(po.getPrice());
		vo.setSize(po.getSize());
		vo.setFilledSize(BigDecimal.ZERO);
		vo.setCreateTime(po.getCreateTime());
		return vo;
	}

	public static List<MyPendingPerpetualContractOrderVO> convertFor(List<PerpetualOrder> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		return pos.stream().map(MyPendingPerpetualContractOrderVO::convertFor).collect(Collectors.toList());
	}

}

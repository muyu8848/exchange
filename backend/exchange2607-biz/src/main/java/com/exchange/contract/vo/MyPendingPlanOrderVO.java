package com.exchange.contract.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.exchange.constants.Constant;
import com.exchange.contract.domain.PerpetualPlanOrder;
import com.exchange.dictconfig.DictHolder;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 当前委托-计划委托子 tab 展示 VO。
 */
@Data
public class MyPendingPlanOrderVO {

	private String id;

	private String symbolCode;

	/** open=开仓计划, close=平仓计划 */
	private String type;

	/** 开平方向文案：开多/开空（开仓计划）、平多/平空（平仓计划） */
	private String orderDirectionName;

	/** 方向：long=多, short=空 */
	private String direction;

	/** 委托类型文案：计划委托 */
	private String entrustTypeName;

	private Integer leverage;

	/** 委托价（限价执行时） */
	private BigDecimal price;

	/** 委托量（预估：保证金×杠杆÷执行价） */
	private BigDecimal size;

	/** 触发价 */
	private BigDecimal triggerPrice;

	/** 触发条件：ge=最新价≥触发价触发, le=最新价≤触发价触发 */
	private String triggerType;

	/** 来源：plan=普通条件单, tp=止盈单, sl=止损单 */
	private String source;

	/** 来源文案（字典 perpetualPlanSource） */
	private String sourceName;

	@JsonFormat(pattern = "MM/dd HH:mm")
	private LocalDateTime createTime;

	public static MyPendingPlanOrderVO convertFor(PerpetualPlanOrder po) {
		if (po == null) {
			return null;
		}
		MyPendingPlanOrderVO vo = new MyPendingPlanOrderVO();
		vo.setId(po.getId());
		vo.setSymbolCode(po.getSymbolCode());
		vo.setType(po.getType());
		vo.setOrderDirectionName(
				DictHolder.getDictItemName("perpetualOrderDirection", po.getType() + "-" + po.getDirection()));
		vo.setDirection(po.getDirection());
		vo.setEntrustTypeName(DictHolder.getDictItemName("perpetualEntrustType", Constant.永续委托类型_计划委托));
		vo.setLeverage(po.getLeverage());
		vo.setPrice(po.getPrice());
		vo.setSize(calcSize(po));
		vo.setTriggerPrice(po.getTriggerPrice());
		vo.setTriggerType(po.getTriggerType());
		vo.setCreateTime(po.getCreateTime());
		vo.setSource(po.getSource());
		vo.setSourceName(DictHolder.getDictItemName("perpetualPlanSource", po.getSource()));
		return vo;
	}

	/** 预估委托量：保证金×杠杆÷执行价（限价用委托价，市价用触发价） */
	private static BigDecimal calcSize(PerpetualPlanOrder po) {
		BigDecimal margin = po.getMargin();
		Integer leverage = po.getLeverage();
		if (margin == null || leverage == null || leverage <= 0) {
			return null;
		}
		BigDecimal execPrice = Constant.永续委托类型_限价.equals(po.getExecType()) ? po.getPrice() : po.getTriggerPrice();
		if (execPrice == null || execPrice.compareTo(BigDecimal.ZERO) <= 0) {
			return null;
		}
		return margin.multiply(BigDecimal.valueOf(leverage)).divide(execPrice, 6, RoundingMode.HALF_UP);
	}

	public static List<MyPendingPlanOrderVO> convertFor(List<PerpetualPlanOrder> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		return pos.stream().map(MyPendingPlanOrderVO::convertFor).collect(Collectors.toList());
	}

}

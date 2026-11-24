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
 * 历史委托-计划委托 展示 VO（已委托、已撤销、委托失败）。
 */
@Data
public class MyHistoryPlanOrderVO {

	private String id;

	private String symbolCode;

	/** open=开仓计划, close=平仓计划 */
	private String type;

	/** 开平方向文案：开多/开空（开仓计划）、平多/平空（平仓计划） */
	private String orderDirectionName;

	/** 方向：long=做多, short=做空 */
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

	/** 执行方式：limit=限价, market=市价 */
	private String execType;

	/** 状态：0=委托中, 1=已委托, 2=已撤销, 3=委托失败, 4=已失效 */
	private String status;

	/** 状态文案：已委托 / 已撤销 / 委托失败 */
	private String statusName;

	@JsonFormat(pattern = "MM/dd HH:mm")
	private LocalDateTime createTime;

	public static MyHistoryPlanOrderVO convertFor(PerpetualPlanOrder po) {
		if (po == null) {
			return null;
		}
		MyHistoryPlanOrderVO vo = new MyHistoryPlanOrderVO();
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
		vo.setExecType(po.getExecType());
		vo.setStatus(po.getStatus());
		vo.setStatusName(DictHolder.getDictItemName("perpetualPlanOrderStatus", po.getStatus()));
		vo.setSource(po.getSource());
		vo.setSourceName(DictHolder.getDictItemName("perpetualPlanSource", po.getSource()));
		vo.setCreateTime(po.getCreateTime());
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

	public static List<MyHistoryPlanOrderVO> convertFor(List<PerpetualPlanOrder> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		return pos.stream().map(MyHistoryPlanOrderVO::convertFor).collect(Collectors.toList());
	}

}

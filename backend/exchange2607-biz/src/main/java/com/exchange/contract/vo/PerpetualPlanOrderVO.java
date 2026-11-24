package com.exchange.contract.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.exchange.contract.domain.PerpetualPlanOrder;
import com.exchange.dictconfig.DictHolder;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class PerpetualPlanOrderVO {

	private String id;
	
	private String memberAccountId;
	
	private Long memberUid;
	
	private String orderNo;
	
	private String symbolCode;
	
	private String type;
	
	private String typeName;
	
	private String direction;
	
	private String directionName;
	
	private Integer leverage;
	
	private BigDecimal margin;
	
	private String positionId;
	
	private BigDecimal triggerPrice;
	
	private String triggerType;
	
	private String execType;
	
	private BigDecimal price;
	
	private String status;
	
	private String statusName;

	/** 来源：plan=普通条件单, tp=止盈单, sl=止损单 */
	private String source;

	/** 来源文案（字典 perpetualPlanSource） */
	private String sourceName;

	private String perpetualOrderId;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime triggerTime;
	
	public static List<PerpetualPlanOrderVO> convertFor(List<PerpetualPlanOrder> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<PerpetualPlanOrderVO> vos = new ArrayList<>();
		for (PerpetualPlanOrder po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static PerpetualPlanOrderVO convertFor(PerpetualPlanOrder po) {
		if (po == null) {
			return null;
		}
		PerpetualPlanOrderVO vo = new PerpetualPlanOrderVO();
		vo.setId(po.getId());
		vo.setOrderNo(po.getOrderNo());
		vo.setSymbolCode(po.getSymbolCode());
		vo.setType(po.getType());
		vo.setTypeName(DictHolder.getDictItemName("perpetualOrderType", po.getType()));
		vo.setDirection(po.getDirection());
		vo.setDirectionName(DictHolder.getDictItemName("perpetualDirection", po.getDirection()));
		vo.setLeverage(po.getLeverage());
		vo.setMargin(po.getMargin());
		vo.setPositionId(po.getPositionId());
		vo.setTriggerPrice(po.getTriggerPrice());
		vo.setTriggerType(po.getTriggerType());
		vo.setExecType(po.getExecType());
		vo.setPrice(po.getPrice());
		vo.setStatus(po.getStatus());
		vo.setStatusName(DictHolder.getDictItemName("perpetualPlanOrderStatus", po.getStatus()));
		vo.setSource(po.getSource());
		vo.setSourceName(DictHolder.getDictItemName("perpetualPlanSource", po.getSource()));
		vo.setPerpetualOrderId(po.getPerpetualOrderId());
		vo.setMemberAccountId(po.getMemberAccountId());
		if (po.getMemberAccount() != null) {
			vo.setMemberUid(po.getMemberAccount().getUid());
		}
		vo.setCreateTime(po.getCreateTime());
		vo.setTriggerTime(po.getTriggerTime());
		return vo;
	}

}

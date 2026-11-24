package com.exchange.contract.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.contract.domain.SecondContractOrder;
import com.exchange.dictconfig.DictHolder;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class SecondContractOrderVO {

	private String id;

	private String memberAccountId;

	private String orderNo;

	private BigDecimal amount;

	private BigDecimal openPrice;

	private BigDecimal closePrice;

	private Integer period;

	private String direction;
	
	private String directionName;

	private BigDecimal odds;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime openTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime expectCloseTime;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime closeTime;

	private String status;

	private String statusName;

	private BigDecimal pnl;

	private String symbolCode;

	private Long memberUid;

	public static List<SecondContractOrderVO> convertFor(List<SecondContractOrder> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<SecondContractOrderVO> vos = new ArrayList<>();
		for (SecondContractOrder po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static SecondContractOrderVO convertFor(SecondContractOrder po) {
		if (po == null) {
			return null;
		}
		SecondContractOrderVO vo = new SecondContractOrderVO();
		BeanUtils.copyProperties(po, vo);
		vo.setDirectionName(DictHolder.getDictItemName("secondContractDirection", po.getDirection()));
		vo.setStatusName(DictHolder.getDictItemName("secondContractOrderStatus", po.getStatus()));
		if (po.getMemberAccount() != null) {
			vo.setMemberUid(po.getMemberAccount().getUid());
		}
		return vo;
	}

}

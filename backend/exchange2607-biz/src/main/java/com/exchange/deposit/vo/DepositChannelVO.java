package com.exchange.deposit.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.deposit.domain.DepositChannel;
import com.exchange.dictconfig.DictHolder;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class DepositChannelVO {

	private String id;

	private String chainType;

	private String assetCode;

	private Integer orderExpireMinutes;

	private BigDecimal minAmount;

	private BigDecimal maxAmount;

	private Integer sortNum;

	private String status;

	private String statusName;

	private Long idleAddressCount;

	public static List<DepositChannelVO> convertFor(List<DepositChannel> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<DepositChannelVO> vos = new ArrayList<>();
		for (DepositChannel po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static DepositChannelVO convertFor(DepositChannel po) {
		if (po == null) {
			return null;
		}
		DepositChannelVO vo = new DepositChannelVO();
		BeanUtils.copyProperties(po, vo);
		vo.setStatusName(DictHolder.getDictItemName("enableStatus", vo.getStatus()));
		return vo;
	}

}

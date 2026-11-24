package com.exchange.withdraw.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.withdraw.domain.WithdrawChannel;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class WithdrawChannelVO {

	private String id;

	private String chainType;

	private String assetCode;

	private BigDecimal minAmount;

	private BigDecimal fixedFee;

	private Integer withdrawScale;

	private String arrivalTimeDesc;

	private Integer sortNum;

	private String status;

	private String statusName;

	public static List<WithdrawChannelVO> convertFor(List<WithdrawChannel> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<WithdrawChannelVO> vos = new ArrayList<>();
		for (WithdrawChannel po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static WithdrawChannelVO convertFor(WithdrawChannel po) {
		if (po == null) {
			return null;
		}
		WithdrawChannelVO vo = new WithdrawChannelVO();
		BeanUtils.copyProperties(po, vo);
		vo.setStatusName(DictHolder.getDictItemName("enableStatus", vo.getStatus()));
		return vo;
	}

}

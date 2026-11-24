package com.exchange.contract.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.contract.domain.SecondContractPeriodConfig;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class SecondContractPeriodConfigVO {

	private Integer period;

	private BigDecimal minAmount;

	private BigDecimal maxAmount;

	private BigDecimal odds;

	public static List<SecondContractPeriodConfigVO> convertFor(List<SecondContractPeriodConfig> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<SecondContractPeriodConfigVO> vos = new ArrayList<>();
		for (SecondContractPeriodConfig po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static SecondContractPeriodConfigVO convertFor(SecondContractPeriodConfig po) {
		if (po == null) {
			return null;
		}
		SecondContractPeriodConfigVO vo = new SecondContractPeriodConfigVO();
		BeanUtils.copyProperties(po, vo);
		return vo;
	}

}

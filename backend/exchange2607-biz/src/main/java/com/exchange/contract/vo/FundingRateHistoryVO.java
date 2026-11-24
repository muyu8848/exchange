package com.exchange.contract.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.exchange.contract.domain.PerpetualFundingRateHistory;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class FundingRateHistoryVO {

	private String id;

	private String symbolCode;

	/** 资金费率（小数，前端 ×100 展示为百分比） */
	private BigDecimal fundingRate;

	/** 结算时间（整点） */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime fundingTime;

	public static List<FundingRateHistoryVO> convertFor(List<PerpetualFundingRateHistory> list) {
		if (CollectionUtil.isEmpty(list)) {
			return new ArrayList<>();
		}
		return list.stream().map(po -> {
			FundingRateHistoryVO vo = new FundingRateHistoryVO();
			vo.setId(po.getId());
			vo.setSymbolCode(po.getSymbolCode());
			vo.setFundingRate(po.getFundingRate());
			vo.setFundingTime(po.getFundingTime());
			return vo;
		}).collect(Collectors.toList());
	}

}

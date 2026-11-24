package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.investment.domain.InvestmentHourlyInterestLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class InvestmentHourlyInterestLogVO {

	private String id;

	private String flexibleAccountId;

	private String memberAccountId;

	private Long memberUid;

	private String assetCode;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate interestDate;

	private Integer interestHour;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal principal;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal interestAmount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	public static List<InvestmentHourlyInterestLogVO> convertFor(List<InvestmentHourlyInterestLog> logs) {
		if (CollectionUtil.isEmpty(logs)) {
			return new ArrayList<>();
		}
		List<InvestmentHourlyInterestLogVO> vos = new ArrayList<>();
		for (InvestmentHourlyInterestLog log : logs) {
			vos.add(convertFor(log));
		}
		return vos;
	}

	public static InvestmentHourlyInterestLogVO convertFor(InvestmentHourlyInterestLog po) {
		if (po == null) {
			return null;
		}
		InvestmentHourlyInterestLogVO vo = new InvestmentHourlyInterestLogVO();
		BeanUtils.copyProperties(po, vo);
		if (po.getMemberAccount() != null) {
			vo.setMemberUid(po.getMemberAccount().getUid());
		}
		return vo;
	}

}

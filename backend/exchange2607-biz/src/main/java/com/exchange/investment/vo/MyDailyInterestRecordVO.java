package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.investment.domain.InvestmentInterestLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 客户端赚币-日收益记录VO
 */
@Data
public class MyDailyInterestRecordVO {

	private String id;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal interestAmount;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate interestDate;

	/** 是否为当天实时汇总数据（尚未跨天结算） */
	private Boolean realtime;

	public static List<MyDailyInterestRecordVO> convertFor(List<InvestmentInterestLog> logs) {
		if (CollectionUtil.isEmpty(logs)) {
			return new ArrayList<>();
		}
		List<MyDailyInterestRecordVO> vos = new ArrayList<>();
		for (InvestmentInterestLog log : logs) {
			vos.add(convertFor(log));
		}
		return vos;
	}

	public static MyDailyInterestRecordVO convertFor(InvestmentInterestLog po) {
		if (po == null) {
			return null;
		}
		MyDailyInterestRecordVO vo = new MyDailyInterestRecordVO();
		BeanUtils.copyProperties(po, vo);
		return vo;
	}

}

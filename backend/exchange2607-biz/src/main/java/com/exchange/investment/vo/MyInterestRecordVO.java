package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.dictconfig.DictHolder;
import com.exchange.investment.domain.InvestmentInterestLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 客户端赚币订单-收益tab列表VO
 * 仅组装前端列表展示所需的最小字段集，避免过度传输
 */
@Data
public class MyInterestRecordVO {

	private String id;

	private String assetCode;

	private String productType;

	private String productTypeName;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal principal;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal interestAmount;

	/** 收益类型（COMPOUND=收益复投 DISTRIBUTE=收益发放） */
	private String interestType;

	/** 收益类型名称（收益发放/收益复投） */
	private String interestTypeName;

	@JsonFormat(pattern = "MM/dd HH:mm")
	private LocalDateTime createTime;

	public static List<MyInterestRecordVO> convertFor(List<InvestmentInterestLog> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MyInterestRecordVO> vos = new ArrayList<>();
		for (InvestmentInterestLog po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static MyInterestRecordVO convertFor(InvestmentInterestLog po) {
		if (po == null) {
			return null;
		}
		MyInterestRecordVO vo = new MyInterestRecordVO();
		BeanUtils.copyProperties(po, vo);
		vo.setProductTypeName(DictHolder.getDictItemName("investmentProductType", po.getProductType()));
		vo.setInterestTypeName(DictHolder.getDictItemName("investmentInterestType", po.getInterestType()));
		return vo;
	}

}

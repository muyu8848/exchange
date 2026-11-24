package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.dictconfig.DictHolder;
import com.exchange.investment.domain.InvestmentRedeemRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 客户端赚币订单-赎回tab列表VO
 */
@Data
public class MyRedeemRecordVO {

	private String id;

	private String assetCode;

	private String productType;

	private String productTypeName;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal redeemAmount;

	/** 赎回方式（1=到期赎回 2=手动赎回） */
	private String redeemType;

	private String redeemTypeName;

	@JsonFormat(pattern = "MM/dd HH:mm")
	private LocalDateTime createTime;

	public static List<MyRedeemRecordVO> convertFor(List<InvestmentRedeemRecord> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MyRedeemRecordVO> vos = new ArrayList<>();
		for (InvestmentRedeemRecord log : pos) {
			vos.add(convertFor(log));
		}
		return vos;
	}

	public static MyRedeemRecordVO convertFor(InvestmentRedeemRecord po) {
		if (po == null) {
			return null;
		}
		MyRedeemRecordVO vo = new MyRedeemRecordVO();
		BeanUtils.copyProperties(po, vo);
		vo.setProductTypeName(DictHolder.getDictItemName("investmentProductType", po.getProductType()));
		vo.setRedeemTypeName(DictHolder.getDictItemName("investmentRedeemType", po.getRedeemType()));
		return vo;
	}

}

package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.dictconfig.DictHolder;
import com.exchange.investment.domain.InvestmentRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 客户端赚币订单-投资tab列表VO
 */
@Data
public class MyInvestmentRecordVO {

	private String id;

	private String orderNo;

	private String assetCode;

	private String productType;

	private String productTypeName;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal principal;

	@JsonFormat(pattern = "MM/dd HH:mm")
	private LocalDateTime createTime;

	public static List<MyInvestmentRecordVO> convertFor(List<InvestmentRecord> records) {
		if (CollectionUtil.isEmpty(records)) {
			return new ArrayList<>();
		}
		List<MyInvestmentRecordVO> vos = new ArrayList<>();
		for (InvestmentRecord record : records) {
			vos.add(convertFor(record));
		}
		return vos;
	}

	public static MyInvestmentRecordVO convertFor(InvestmentRecord record) {
		if (record == null) {
			return null;
		}
		MyInvestmentRecordVO vo = new MyInvestmentRecordVO();
		BeanUtils.copyProperties(record, vo);
		vo.setProductTypeName(DictHolder.getDictItemName("investmentProductType", record.getProductType()));
		return vo;
	}

}

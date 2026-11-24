package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
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

@Data
public class InvestmentInterestLogVO {

	private String id;

	private String positionId;

	private String flexibleAccountId;

	private String memberAccountId;

	private String assetCode;

	private String productType;

	private String productId;

	private String productTypeName;

	private String interestType;

	private String interestTypeName;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate interestDate;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal principal;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal interestAmount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	private Long memberUid;

	public static List<InvestmentInterestLogVO> convertFor(List<InvestmentInterestLog> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<InvestmentInterestLogVO> vos = new ArrayList<>();
		for (InvestmentInterestLog po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static InvestmentInterestLogVO convertFor(InvestmentInterestLog po) {
		if (po == null) {
			return null;
		}
		InvestmentInterestLogVO vo = new InvestmentInterestLogVO();
		BeanUtils.copyProperties(po, vo);
		vo.setProductTypeName(DictHolder.getDictItemName("investmentProductType", po.getProductType()));
		vo.setInterestTypeName(DictHolder.getDictItemName("investmentInterestType", po.getInterestType()));
		if (po.getMemberAccount() != null) {
			vo.setMemberUid(po.getMemberAccount().getUid());
		}
		return vo;
	}

}

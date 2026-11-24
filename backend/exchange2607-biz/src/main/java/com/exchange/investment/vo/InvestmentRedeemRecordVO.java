package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.dictconfig.DictHolder;
import com.exchange.investment.domain.InvestmentRedeemRecord;
import com.exchange.member.domain.MemberAccount;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class InvestmentRedeemRecordVO {

	private String id;

	private String orderNo;

	private String memberAccountId;

	private Long memberUid;

	private String assetCode;

	private Integer lockDays;

	private String productType;

	private String productId;

	/** 所属活期账户（在投周期）；定期到期赎回为空 */
	private String flexibleAccountId;

	private String productTypeName;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal redeemAmount;

	private String redeemType;

	private String redeemTypeName;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	public static List<InvestmentRedeemRecordVO> convertFor(List<InvestmentRedeemRecord> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<InvestmentRedeemRecordVO> vos = new ArrayList<>();
		for (InvestmentRedeemRecord log : pos) {
			vos.add(convertFor(log));
		}
		return vos;
	}

	public static InvestmentRedeemRecordVO convertFor(InvestmentRedeemRecord po) {
		if (po == null) {
			return null;
		}
		InvestmentRedeemRecordVO vo = new InvestmentRedeemRecordVO();
		BeanUtils.copyProperties(po, vo);
		vo.setProductTypeName(DictHolder.getDictItemName("investmentProductType", po.getProductType()));
		vo.setRedeemTypeName(DictHolder.getDictItemName("investmentRedeemType", po.getRedeemType()));
		MemberAccount memberAccount = po.getMemberAccount();
		if (memberAccount != null) {
			vo.setMemberUid(memberAccount.getUid());
		}
		return vo;
	}

}

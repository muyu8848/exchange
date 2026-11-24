package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.dictconfig.DictHolder;
import com.exchange.investment.domain.InvestmentRecord;
import com.exchange.member.domain.MemberAccount;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class InvestmentRecordVO {

	private String id;

	private String orderNo;

	private String memberAccountId;

	private Long memberUid;

	private String productId;

	private String assetCode;

	/** 所属活期账户（在投周期）；定期记录为空 */
	private String flexibleAccountId;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal principal;

	private String productType;

	private String productTypeName;

	private BigDecimal annualRate;

	private BigDecimal subsidizedRate;

	private BigDecimal rewardLimit;

	private String accrualFrequency;

	private String accrualFrequencyName;

	private Integer lockDays;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	public static List<InvestmentRecordVO> convertFor(List<InvestmentRecord> records) {
		if (CollectionUtil.isEmpty(records)) {
			return new ArrayList<>();
		}
		List<InvestmentRecordVO> vos = new ArrayList<>();
		for (InvestmentRecord record : records) {
			vos.add(convertFor(record));
		}
		return vos;
	}

	public static InvestmentRecordVO convertFor(InvestmentRecord record) {
		if (record == null) {
			return null;
		}
		InvestmentRecordVO vo = new InvestmentRecordVO();
		BeanUtils.copyProperties(record, vo);
		vo.setProductTypeName(DictHolder.getDictItemName("investmentProductType", record.getProductType()));
		vo.setAccrualFrequencyName(
				DictHolder.getDictItemName("investmentAccrualFrequency", record.getAccrualFrequency()));
		MemberAccount memberAccount = record.getMemberAccount();
		if (memberAccount != null) {
			vo.setMemberUid(memberAccount.getUid());
		}
		return vo;
	}

}

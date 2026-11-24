package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.dictconfig.DictHolder;
import com.exchange.investment.domain.InvestmentFixedPosition;
import com.exchange.member.domain.MemberAccount;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class InvestmentFixedPositionVO {

	private String id;

	private String orderNo;

	private String memberAccountId;

	private String productId;

	private String assetCode;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal principal;

	private BigDecimal annualRate;

	private String accrualFrequency;

	private String accrualFrequencyName;

	private Integer lockDays;

	private BigDecimal accruedInterest;

	/** 估算总收益 */
	private BigDecimal estimatedTotalInterest;

	/** 已确认收益 */
	private BigDecimal confirmedInterest;

	private String status;

	private String statusName;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime investTime;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate valueDate;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate maturityDate;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate arrivalDate;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate lastAccrualDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime nextAccrualTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime redeemTime;

	private Long memberUid;

	public static List<InvestmentFixedPositionVO> convertFor(List<InvestmentFixedPosition> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<InvestmentFixedPositionVO> vos = new ArrayList<>();
		for (InvestmentFixedPosition po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static InvestmentFixedPositionVO convertFor(InvestmentFixedPosition po) {
		if (po == null) {
			return null;
		}
		InvestmentFixedPositionVO vo = new InvestmentFixedPositionVO();
		BeanUtils.copyProperties(po, vo);
		vo.setStatusName(DictHolder.getDictItemName("investmentPositionStatus", po.getStatus()));
		vo.setAccrualFrequencyName(DictHolder.getDictItemName("investmentAccrualFrequency", po.getAccrualFrequency()));
		vo.setEstimatedTotalInterest(po.getEstimatedTotalInterest());
		vo.setConfirmedInterest(po.getConfirmedInterest());
		MemberAccount memberAccount = po.getMemberAccount();
		if (memberAccount != null) {
			vo.setMemberUid(memberAccount.getUid());
		}
		return vo;
	}

}

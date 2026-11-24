package com.exchange.investment.param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.investment.domain.InvestmentProduct;

import lombok.Data;

@Data
public class AddOrUpdateInvestmentProductParam {

	private String id;

	@NotBlank
	private String assetCode;

	@NotBlank
	private String productType;

	/** 年化收益率(%): 定期为产品年化；活期为基础年化 */
	private BigDecimal annualRate;

	/** 补贴后年化收益率(%), 仅FLEXIBLE */
	private BigDecimal subsidizedRate;

	/** 奖励限额, 仅FLEXIBLE */
	private BigDecimal rewardLimit;

	/** 计息频率: HOURLY / DAILY */
	private String accrualFrequency;

	private Boolean newUserExclusive;

	private Integer lockDays;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal minAmount;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal totalQuota;

	/** 项目剩余额度 */
	private BigDecimal remainingQuota;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private BigDecimal maxHoldPerUser;

	@NotBlank
	private String status;

	@NotNull
	@DecimalMin(value = "0", inclusive = true)
	private Integer sortNum;

	public InvestmentProduct convertToPo() {
		InvestmentProduct po = new InvestmentProduct();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		LocalDateTime now = LocalDateTime.now();
		po.setCreateTime(now);
		po.setLatelyUpdateTime(now);
		po.setDeletedFlag(false);
		return po;
	}

}

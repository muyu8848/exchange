package com.exchange.investment.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.exchange.common.exception.BizException;
import com.exchange.constants.Constant;

import lombok.Getter;
import lombok.Setter;

/**
 * 赚币产品配置（活期 FLEXIBLE / 定期 FIXED）
 * <p>
 * 年化、补贴年化、奖励限额、总额度均为产品级配置；剩余额度随申购扣减、并随总额度调整同步。
 */
@Getter
@Setter
@Entity
@Table(name = "investment_product")
@DynamicInsert(true)
@DynamicUpdate(true)
public class InvestmentProduct implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	private String assetCode;

	/** 产品类型: FLEXIBLE(活期) / FIXED(定期)；新人专享由 newUserExclusive 标识，不是产品类型 */
	private String productType;

	/** 年化收益率(%): 定期为产品年化；活期为基础年化（补贴后年化见 subsidizedRate） */
	private BigDecimal annualRate;

	/** 补贴后年化收益率(%), 仅FLEXIBLE */
	private BigDecimal subsidizedRate;

	/** 奖励限额, 仅FLEXIBLE, 限额内按补贴后年化计息 */
	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal rewardLimit;

	/** 计息频率: HOURLY / DAILY */
	private String accrualFrequency;

	/** 是否新人专享 */
	private Boolean newUserExclusive;

	@Column(name = "lock_days")
	private Integer lockDays;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal minAmount;

	/** 项目总额度 */
	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal totalQuota;

	/** 项目剩余额度 */
	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal remainingQuota;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal maxHoldPerUser;

	private String status;

	@Column(name = "sort_num")
	private Integer sortNum;

	@Column(name = "deleted_flag")
	private Boolean deletedFlag;

	@Column(name = "deleted_time")
	private LocalDateTime deletedTime;

	@Column(name = "create_time")
	private LocalDateTime createTime;

	@Column(name = "lately_update_time")
	private LocalDateTime latelyUpdateTime;

	public void validateEnable() {
		if (!Constant.启用状态_启用.equals(this.getStatus())) {
			throw new BizException("该赚币产品已停用");
		}
	}

	public void deleted() {
		if (Constant.启用状态_启用.equals(this.getStatus())) {
			this.setStatus(Constant.启用状态_停用);
		}
		this.setDeletedFlag(true);
		this.setDeletedTime(LocalDateTime.now());
	}

}

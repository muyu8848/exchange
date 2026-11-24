package com.exchange.investment.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.exchange.common.utils.IdUtils;
import com.exchange.constants.Constant;
import com.exchange.member.domain.MemberAccount;

import lombok.Getter;
import lombok.Setter;

/**
 * 活期按小时计息流水（每个活期账户每小时一条）
 */
@Getter
@Setter
@Entity
@Table(name = "investment_hourly_interest_log")
@DynamicInsert(true)
@DynamicUpdate(true)
public class InvestmentHourlyInterestLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	@Column(name = "flexible_account_id", length = 32)
	private String flexibleAccountId;

	@Column(name = "member_account_id", length = 32)
	private String memberAccountId;

	private String assetCode;

	private LocalDate interestDate;

	/** 计息小时(0-23) */
	private Integer interestHour;

	/** 计息本金快照（该小时计息基数） */
	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal principal;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal interestAmount;

	/** 利息折合USDT（发放时价格快照） */
	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal interestUsdtAmount;

	/** USDT折算状态：0=未折算 1=已折算 */
	private String usdtConvertStatus;

	private LocalDateTime createTime;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_account_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAccount memberAccount;

	/**
	 * 静态工厂：创建小时收益日志
	 */
	public static InvestmentHourlyInterestLog create(String flexibleAccountId, String memberAccountId,
			String assetCode, LocalDate interestDate, Integer interestHour,
			BigDecimal principal, BigDecimal interestAmount, LocalDateTime now) {
		InvestmentHourlyInterestLog log = new InvestmentHourlyInterestLog();
		log.setId(IdUtils.getId());
		log.setFlexibleAccountId(flexibleAccountId);
		log.setMemberAccountId(memberAccountId);
		log.setAssetCode(assetCode);
		log.setInterestDate(interestDate);
		log.setInterestHour(interestHour);
		log.setPrincipal(principal);
		log.setInterestAmount(interestAmount);
		log.setCreateTime(now);
		return log;
	}

}

package com.exchange.investment.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

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
 * 活期账户（会员 + 币种维度唯一）
 * <p>
 * 本金与每小时复投的利息都计入 position，nextAccrualTime 驱动计息调度。
 * 赎回支持按金额部分赎回：从 position 扣减，并按申购时间 FIFO 逐笔冲销持仓记录。
 */
@Getter
@Setter
@Entity
@Table(name = "investment_flexible_account")
@DynamicInsert(true)
@DynamicUpdate(true)
public class InvestmentFlexibleAccount implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	/** 乐观锁版本号 */
	@Version
	private Long version;

	@Column(name = "member_account_id", length = 32)
	private String memberAccountId;

	private String assetCode;

	@Column(name = "product_id", length = 32)
	private String productId;

	/** 持仓金额（含每小时复投的利息） */
	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal position;

	/** 累计总收益（历史全部） */
	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal accruedInterest;

	/** 下次发放时间（整点） */
	private LocalDateTime nextAccrualTime;

	/** 上次计息日期 */
	private LocalDate lastAccrualDate;

	private LocalDateTime redeemTime;

	private LocalDateTime createTime;

	private LocalDateTime latelyUpdateTime;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_account_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAccount memberAccount;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private InvestmentProduct product;

	/**
	 * 起息口径：下一个整点起息，下下个整点首次发放。
	 * <p>
	 * 首次开户与「赎空后重新申购」共用同一口径，避免两处公式各写一份导致漂移。
	 */
	public static LocalDateTime firstAccrualTime(LocalDateTime now) {
		LocalDateTime valueTime = now.plusHours(1).truncatedTo(ChronoUnit.HOURS);
		return valueTime.plusHours(1);
	}

	/**
	 * 静态工厂：首次申购创建活期账户
	 */
	public static InvestmentFlexibleAccount create(InvestmentProduct product, String memberAccountId,
			BigDecimal amount, LocalDateTime now) {
		InvestmentFlexibleAccount acc = new InvestmentFlexibleAccount();
		acc.setId(IdUtils.getId());
		acc.setMemberAccountId(memberAccountId);
		acc.setAssetCode(product.getAssetCode());
		acc.setProductId(product.getId());
		acc.setPosition(amount);
		acc.setAccruedInterest(BigDecimal.ZERO);
		acc.setNextAccrualTime(firstAccrualTime(now));
		acc.setCreateTime(now);
		acc.setLatelyUpdateTime(now);
		return acc;
	}

}

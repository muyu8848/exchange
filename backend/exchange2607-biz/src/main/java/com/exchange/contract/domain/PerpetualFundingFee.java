package com.exchange.contract.domain;

import java.io.Serializable;
import java.math.BigDecimal;
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
 * 用户永续合约资金费记录。
 * <p>
 * 每次资金费结算为每个持仓写一条，fundingAmount 为正表示用户收取，为负表示用户支付。
 */
@Getter
@Setter
@Entity
@Table(name = "perpetual_funding_fee")
@DynamicInsert(true)
@DynamicUpdate(true)
public class PerpetualFundingFee implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	@Column(name = "member_account_id", length = 32)
	private String memberAccountId;

	/** 持仓ID */
	@Column(name = "position_id", length = 32)
	private String positionId;

	/** 持仓订单号 */
	private String orderNo;

	private String symbolCode;

	/** 方向：long / short */
	private String direction;

	/** 名义价值（仓位数量 × 结算价） */
	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal notional;

	/** 本次资金费率 */
	@Column(precision = 18, scale = 8)
	private BigDecimal fundingRate;

	/** 资金费金额：正=收取，负=支付 */
	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal fundingAmount;

	/** 结算时间（对齐整点） */
	private LocalDateTime fundingTime;

	private LocalDateTime createTime;

	@Version
	private Long version;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_account_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAccount memberAccount;

	/**
	 * 构建资金费记录。
	 *
	 * @param position      持仓
	 * @param notional      名义价值
	 * @param fundingRate   资金费率
	 * @param fundingAmount 资金费金额（正=收取，负=支付）
	 * @param fundingTime   结算时间（对齐整点）
	 */
	public static PerpetualFundingFee build(PerpetualPosition position, BigDecimal notional,
			BigDecimal fundingRate, BigDecimal fundingAmount, LocalDateTime fundingTime) {
		PerpetualFundingFee fee = new PerpetualFundingFee();
		fee.setId(IdUtils.getId());
		fee.setMemberAccountId(position.getMemberAccountId());
		fee.setPositionId(position.getId());
		fee.setOrderNo(position.getOrderNo());
		fee.setSymbolCode(position.getSymbolCode());
		fee.setDirection(position.getDirection());
		fee.setNotional(notional);
		fee.setFundingRate(fundingRate);
		fee.setFundingAmount(fundingAmount);
		fee.setFundingTime(fundingTime);
		fee.setCreateTime(LocalDateTime.now());
		return fee;
	}
}

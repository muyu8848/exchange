package com.exchange.rebate.domain;

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

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.exchange.constants.Constant;
import com.exchange.member.domain.MemberAccount;

import lombok.Getter;
import lombok.Setter;

/**
 * 邀请返佣记录（HTX 式：推荐人从被推荐人的现货/合约交易手续费中获得返佣）
 */
@Getter
@Setter
@Entity
@Table(name = "rebate_record")
@DynamicInsert(true)
@DynamicUpdate(true)
public class RebateRecord implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	/** 来源订单号（现货/合约订单ID） */
	@Column(name = "source_order_id", length = 32)
	private String sourceOrderId;

	/** 交易对 */
	@Column(name = "symbol_code", length = 32)
	private String symbolCode;

	/** 返佣层级（直推=1） */
	private Integer level;

	/** 返佣金额 */
	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal amount;

	/** 返佣币种 */
	@Column(name = "asset_code", length = 16)
	private String assetCode;

	/** 业务类型：1=现货返佣 2=合约返佣（见 Constant.返佣业务类型_*） */
	@Column(name = "biz_type", length = 8)
	private String bizType;

	/** 状态（1=已发放，预留） */
	@Column(name = "status", length = 8)
	private String status;

	private LocalDateTime createTime;
	
	/** 收佣人（推荐人）会员ID */
	@Column(name = "beneficiary_id", length = 32)
	private String beneficiaryId;
	
	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "beneficiary_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAccount beneficiary;

	/** 来源会员（产生手续费的交易者）会员ID */
	@Column(name = "source_member_id", length = 32)
	private String sourceMemberId;
	
	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "source_member_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAccount sourceMember;
	

}

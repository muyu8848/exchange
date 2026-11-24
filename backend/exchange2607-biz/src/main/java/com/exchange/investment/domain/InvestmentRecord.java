package com.exchange.investment.domain;

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

import com.exchange.common.utils.IdUtils;
import com.exchange.constants.Constant;
import com.exchange.member.domain.MemberAccount;

import lombok.Getter;
import lombok.Setter;

/**
 * 投资记录（不可变，记录用户每次申购的原始数据）
 */
@Getter
@Setter
@Entity
@Table(name = "investment_record")
@DynamicInsert(true)
@DynamicUpdate(true)
public class InvestmentRecord implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	/** 订单号（本记录自身生成的唯一编号，与持仓记录的 orderNo 不同源，不能用于关联） */
	@Column(name = "order_no")
	private String orderNo;

	@Column(name = "member_account_id", length = 32)
	private String memberAccountId;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_account_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAccount memberAccount;

	@Column(name = "product_id", length = 32)
	private String productId;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private InvestmentProduct product;

	private String assetCode;

	/** 所属活期账户（在投周期）；定期记录为空 */
	@Column(name = "flexible_account_id", length = 32)
	private String flexibleAccountId;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal principal;

	/** 申购本金折合USDT（申购时价格快照） */
	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal principalUsdtAmount;

	/** USDT折算状态：0=未折算 1=已折算 */
	private String usdtConvertStatus;

	/** 产品类型快照 */
	private String productType;

	/** 年化收益率快照 */
	private BigDecimal annualRate;

	/** 补贴后年化快照(活期) */
	private BigDecimal subsidizedRate;

	/** 奖励限额快照(活期) */
	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal rewardLimit;

	/** 计息频率快照 */
	private String accrualFrequency;

	/** 封闭天数快照(定期) */
	private Integer lockDays;

	/** 申购时间 */
	private LocalDateTime createTime;

	/**
	 * 创建投资记录，快照产品信息
	 */
	public static InvestmentRecord create(InvestmentProduct product, String memberAccountId, BigDecimal amount,
			LocalDateTime now) {
		InvestmentRecord record = new InvestmentRecord();
		record.setId(IdUtils.getId());
		record.setOrderNo(record.getId());
		record.setMemberAccountId(memberAccountId);
		record.setProductId(product.getId());
		record.setAssetCode(product.getAssetCode());
		record.setPrincipal(amount);
		record.setProductType(product.getProductType());
		record.setAnnualRate(product.getAnnualRate());
		record.setSubsidizedRate(product.getSubsidizedRate());
		record.setRewardLimit(product.getRewardLimit());
		record.setAccrualFrequency(product.getAccrualFrequency());
		record.setLockDays(product.getLockDays());
		record.setCreateTime(now);
		return record;
	}

}

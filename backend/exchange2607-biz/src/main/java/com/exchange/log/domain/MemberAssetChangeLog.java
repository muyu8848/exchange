package com.exchange.log.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

import com.exchange.c2c.domain.C2cOrder;
import com.exchange.common.utils.IdUtils;
import com.exchange.constants.Constant;
import com.exchange.contract.domain.SecondContractOrder;
import com.exchange.member.domain.MemberAccount;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "member_asset_change_log")
@DynamicInsert(true)
@DynamicUpdate(true)
public class MemberAssetChangeLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	private String bizOrderNo;

	private LocalDateTime changeTime;

	private String changeType;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal balanceChange;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal balanceBefore;

	@Column(precision = Constant.数值总位数, scale = Constant.小数部分位数)
	private BigDecimal balanceAfter;

	private String note;

	@Column(name = "member_account_id", length = 32)
	private String memberAccountId;

	@Column(name = "account_type", length = 16)
	private String accountType;

	@Column(name = "asset_code", length = 16)
	private String assetCode;

	/** 交易对（永续合约相关流水记录） */
	private String symbolCode;

	@NotFound(action = NotFoundAction.IGNORE)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_account_id", updatable = false, insertable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private MemberAccount memberAccount;

	public static MemberAssetChangeLog buildWithDeposit(String memberAccountId, String accountType, String assetCode,
			BigDecimal balanceBefore, BigDecimal credited, String orderNo) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_充币);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		log.setBalanceChange(credited);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(credited).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		return log;
	}

	public static MemberAssetChangeLog buildWithWithdraw(String memberAccountId, String accountType, String assetCode,
			BigDecimal balanceBefore, BigDecimal deducted, String orderNo) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_提币);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		log.setBalanceChange(deducted.negate());
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.subtract(deducted).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		return log;
	}

	/** C2C 买币：买家收到币，余额增加 */
	public static MemberAssetChangeLog buildWithBuy(String memberAccountId, String accountType, String assetCode,
			BigDecimal balanceBefore, C2cOrder order) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(order.getOrderNo());
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_C2C买币);
		log.setBalanceChange(order.getAmount());
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(order.getAmount()).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

	/** C2C 卖币：卖家付出币，余额减少 */
	public static MemberAssetChangeLog buildWithSell(String memberAccountId, String accountType, String assetCode,
			BigDecimal balanceBefore, C2cOrder order) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(order.getOrderNo());
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_C2C卖币);
		log.setBalanceChange(order.getAmount().negate());
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.subtract(order.getAmount()).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithInvestmentSubscribe(String memberAccountId, String accountType,
			String assetCode, BigDecimal balanceBefore, BigDecimal amount, String orderNo) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_理财申购);
		log.setBalanceChange(amount.negate());
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.subtract(amount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithInvestmentInterest(String memberAccountId, String accountType,
			String assetCode, BigDecimal balanceBefore, BigDecimal amount, String orderNo, String interestDate) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_理财收益);
		log.setBalanceChange(amount);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(amount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithInvestmentRedeem(String memberAccountId, String accountType,
			String assetCode, BigDecimal balanceBefore, BigDecimal amount, String orderNo) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_理财赎回);
		log.setBalanceChange(amount);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(amount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithSecondContractOpenPosition(String memberAccountId, String accountType,
			String assetCode, BigDecimal balanceBefore, SecondContractOrder order) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(order.getOrderNo());
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_秒合约开仓);
		BigDecimal change = order.getAmount().negate();
		log.setBalanceChange(change);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(change).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

	/**
	 * 秒合约平仓：本金返还流水。
	 * <p>
	 * 与 {@link #buildWithSecondContractClosePosition} 配对使用：先记本金返还（+amount），
	 * 再记盈亏结算（正/负），两条流水合计即账户实际变动，亏损单因此可被审计。
	 */
	public static MemberAssetChangeLog buildWithSecondContractRefund(String memberAccountId, String accountType,
			String assetCode, BigDecimal balanceBefore, BigDecimal refundAmount, SecondContractOrder order) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(order.getOrderNo());
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_秒合约本金返还);
		log.setBalanceChange(refundAmount);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(refundAmount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithSecondContractClosePosition(String memberAccountId, String accountType,
			String assetCode, BigDecimal balanceBefore, BigDecimal settleAmount, SecondContractOrder order) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(order.getOrderNo());
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_秒合约平仓);
		log.setBalanceChange(settleAmount);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(settleAmount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithAssetTransferOut(String memberAccountId, String accountType,
			String assetCode, BigDecimal balanceBefore, BigDecimal amount, String orderNo, String toAccountType) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_资金划转);
		log.setBalanceChange(amount.negate());
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.subtract(amount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithAssetTransferIn(String memberAccountId, String accountType,
			String assetCode, BigDecimal balanceBefore, BigDecimal amount, String orderNo, String fromAccountType) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_资金划转入);
		log.setBalanceChange(amount);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(amount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithSpotBuy(String memberAccountId, String accountType, String assetCode,
			BigDecimal balanceBefore, BigDecimal changeAmount, String orderNo) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_现货买入);
		log.setBalanceChange(changeAmount);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(changeAmount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithSpotSell(String memberAccountId, String accountType, String assetCode,
			BigDecimal balanceBefore, BigDecimal changeAmount, String orderNo) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_现货卖出);
		log.setBalanceChange(changeAmount);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(changeAmount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithAdminAdjust(String memberAccountId, String accountType,
			String assetCode, BigDecimal balanceBefore, BigDecimal changeAmount, String changeType, String note) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(changeType);
		log.setBalanceChange(changeAmount);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(changeAmount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		// 仅保留管理员自定义备注；其余场景不再写入中文固定文案（改由前端按 changeType 渲染）
		if (StrUtil.isNotBlank(note)) {
			log.setNote(note);
		}
		return log;
	}

	// ===== 永续合约 =====

	public static MemberAssetChangeLog buildWithPerpClosePosition(String memberAccountId, String accountType,
			String assetCode, BigDecimal balanceBefore, BigDecimal returnAmount, String orderNo, boolean isLiquidation,
			String symbolCode) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(isLiquidation ? Constant.会员资产变动类型_永续强平 : Constant.会员资产变动类型_永续平仓);
		log.setBalanceChange(returnAmount);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(returnAmount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		log.setSymbolCode(symbolCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithPerpFunding(String memberAccountId, String accountType,
			String assetCode, BigDecimal balanceBefore, BigDecimal fundingAmount, String orderNo, String symbolCode) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员资产变动类型_永续资金费);
		log.setBalanceChange(fundingAmount);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(fundingAmount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		log.setSymbolCode(symbolCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithPerpFee(String memberAccountId, String accountType, String assetCode,
			BigDecimal balanceBefore, BigDecimal fee, String orderNo, String symbolCode) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员资产变动类型_永续手续费);
		log.setBalanceChange(fee);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(fee).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		log.setSymbolCode(symbolCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithRebate(String memberAccountId, String accountType, String assetCode,
			BigDecimal balanceBefore, BigDecimal rebateAmount, String orderNo, String symbolCode) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员资产变动类型_返佣);
		log.setBalanceChange(rebateAmount);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(rebateAmount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		log.setSymbolCode(symbolCode);
		if (Constant.账户类型_合约.equals(accountType)) {
		} else {
		}
		return log;
	}

	// ===== 闪兑 =====

	public static MemberAssetChangeLog buildWithConvertOut(String memberAccountId, String accountType, String assetCode,
			BigDecimal balanceBefore, BigDecimal amount, String orderNo, String toAssetCode) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_闪兑);
		log.setBalanceChange(amount.negate());
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.subtract(amount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

	public static MemberAssetChangeLog buildWithConvertIn(String memberAccountId, String accountType, String assetCode,
			BigDecimal balanceBefore, BigDecimal amount, String orderNo, String fromAssetCode) {
		MemberAssetChangeLog log = new MemberAssetChangeLog();
		log.setId(IdUtils.getId());
		log.setBizOrderNo(orderNo);
		log.setChangeTime(LocalDateTime.now());
		log.setChangeType(Constant.会员余额变动类型_闪兑);
		log.setBalanceChange(amount);
		log.setBalanceBefore(balanceBefore);
		log.setBalanceAfter(balanceBefore.add(amount).setScale(Constant.小数部分位数, RoundingMode.HALF_UP));
		log.setMemberAccountId(memberAccountId);
		log.setAccountType(accountType);
		log.setAssetCode(assetCode);
		return log;
	}

}

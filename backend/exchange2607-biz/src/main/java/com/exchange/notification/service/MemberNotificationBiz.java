package com.exchange.notification.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.c2c.domain.C2cOrder;
import com.exchange.c2c.domain.C2cRiskRecord;
import com.exchange.c2c.repo.C2cOrderRepo;
import com.exchange.c2c.support.C2cChatEventRenderer;
import com.exchange.common.utils.IdUtils;

import cn.hutool.json.JSONUtil;
import com.exchange.constants.Constant;
import com.exchange.contract.domain.PerpetualPosition;
import com.exchange.deposit.domain.DepositOrder;
import com.exchange.investment.domain.InvestmentFixedPosition;
import com.exchange.member.domain.MemberAccount;
import com.exchange.member.repo.MemberAccountRepo;
import com.exchange.notification.domain.MemberNotification;
import com.exchange.notification.repo.MemberNotificationRepo;
import com.exchange.notification.template.NotificationTemplate;
import com.exchange.withdraw.domain.WithdrawOrder;
import com.exchange.withdraw.repo.WithdrawOrderRepo;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@Component
public class MemberNotificationBiz {

	@Autowired
	private MemberNotificationRepo memberNotificationRepo;

	@Autowired
	private MemberAccountRepo memberAccountRepo;

	@Autowired
	private C2cOrderRepo c2cOrderRepo;

	@Autowired
	private WithdrawOrderRepo withdrawOrderRepo;

	@Autowired
	private C2cChatEventRenderer c2cChatEventRenderer;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void sendSystemMessage(@NotBlank String memberAccountId, @NotBlank String title, @NotBlank String content) {
		dispatch(memberAccountId, Constant.会员消息类型_系统, title, content, null, false, null, null);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void notifyInvestSubscribe(InvestmentFixedPosition position) {
		NotificationTemplate template = NotificationTemplate.INVEST_SUBSCRIBE;
		dispatch(position.getMemberAccountId(), Constant.会员消息类型_理财申购, template,
				new Object[] { formatAmount(position.getPrincipal(), position.getAssetCode()),
						position.getValueDate(), position.getMaturityDate() },
				position.getId(), false);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void notifyInvestRedeemed(InvestmentFixedPosition position, boolean autoRedeem) {
		NotificationTemplate template = autoRedeem ? NotificationTemplate.INVEST_AUTO_REDEEMED
				: NotificationTemplate.INVEST_REDEEMED;
		dispatch(position.getMemberAccountId(), Constant.会员消息类型_理财赎回, template,
				new Object[] { formatAmount(position.getPrincipal(), position.getAssetCode()) },
				position.getId(), false);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void notifyAuthPassed(@NotBlank String memberAccountId) {
		dispatch(memberAccountId, Constant.会员消息类型_实名通过, NotificationTemplate.AUTH_PASSED, null, null, false);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void notifyAuthRejected(@NotBlank String memberAccountId, String failReason) {
		boolean hasReason = StrUtil.isNotBlank(failReason);
		NotificationTemplate template = hasReason ? NotificationTemplate.AUTH_REJECTED
				: NotificationTemplate.AUTH_REJECTED_NO_REASON;
		dispatch(memberAccountId, Constant.会员消息类型_实名未通过, template,
				hasReason ? new Object[] { failReason } : null, null, false);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void notifyPerpetualLiquidation(PerpetualPosition position, BigDecimal closePrice,
			BigDecimal closePnl) {
		String langCode = langCodeOf(position.getMemberAccountId());
		String directionText = NotificationTemplate.direction(langCode,
				Constant.永续合约方向_多.equals(position.getDirection()));
		dispatch(position.getMemberAccountId(), Constant.会员消息类型_永续强平,
				NotificationTemplate.PERPETUAL_LIQUIDATION,
				new Object[] { position.getSymbolCode(), directionText,
						closePrice.stripTrailingZeros().toPlainString(),
						formatAmount(closePnl, Constant.数字货币_USDT) },
				position.getId(), false);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void notifyDepositCredited(DepositOrder order) {
		BigDecimal creditedNet = order.getActualAmount() != null ? order.getActualAmount() : BigDecimal.ZERO;
		dispatch(order.getMemberAccountId(), Constant.会员消息类型_充币, NotificationTemplate.DEPOSIT_CREDITED,
				new Object[] { order.getAssetCode(), order.getOrderNo(),
						formatAmount(creditedNet, order.getAssetCode()) },
				order.getId(), false);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void tipBuyerPayment(@NotBlank String orderId, boolean popupFlag) {
		C2cOrder order = c2cOrderRepo.getOne(orderId);
		MemberAccount buyer = order.getBuyer();
		if (buyer == null) {
			return;
		}
		dispatch(buyer.getId(), Constant.会员消息类型_C2C交易, NotificationTemplate.C2C_BUYER_PAYMENT, null,
				order.getId(), popupFlag);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void notifyOrderCanceled(@NotBlank String orderId, String cancelReason) {
		// 申诉结案取消由 notifyAppealResult 单独通知，避免重复
		if (Constant.C2C取消原因_申诉取消.equals(cancelReason)) {
			return;
		}
		C2cOrder order = c2cOrderRepo.getOne(orderId);
		if (order == null) {
			return;
		}
		NotificationTemplate template = NotificationTemplate.C2C_ORDER_CANCELED;

		MemberAccount buyer = order.getBuyer();
		if (buyer != null) {
			String reason = c2cChatEventRenderer.cancelReasonName(cancelReason, langCodeOf(buyer.getId()));
			dispatch(buyer.getId(), Constant.会员消息类型_C2C交易, template, new Object[] { reason }, orderId, false);
		}

		MemberAccount seller = order.getSeller();
		if (seller != null) {
			String reason = c2cChatEventRenderer.cancelReasonName(cancelReason, langCodeOf(seller.getId()));
			dispatch(seller.getId(), Constant.会员消息类型_C2C交易, template, new Object[] { reason }, orderId, false);
		}
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void notifyAppealResult(@NotBlank String orderId, boolean approved) {
		C2cOrder order = c2cOrderRepo.getOne(orderId);
		if (order == null) {
			return;
		}
		NotificationTemplate template = approved ? NotificationTemplate.C2C_APPEAL_APPROVED
				: NotificationTemplate.C2C_APPEAL_REJECTED;
		MemberAccount buyer = order.getBuyer();
		if (buyer != null) {
			dispatch(buyer.getId(), Constant.会员消息类型_C2C交易, template, null, orderId, false);
		}

		MemberAccount seller = order.getSeller();
		if (seller != null) {
			dispatch(seller.getId(), Constant.会员消息类型_C2C交易, template, null, orderId, false);
		}
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void tipSellerBuyerPaid(@NotBlank String orderId) {
		C2cOrder order = c2cOrderRepo.getOne(orderId);
		MemberAccount seller = order.getSeller();
		dispatch(seller.getId(), Constant.会员消息类型_C2C交易, NotificationTemplate.C2C_SELLER_PAID, null,
				order.getId(), true);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void tipTradeCompleted(@NotBlank String orderId) {
		C2cOrder order = c2cOrderRepo.getOne(orderId);
		if (!Constant.C2C订单状态_已完成.equals(order.getStatus())) {
			return;
		}
		String amountText = formatAmount(order.getAmount(), order.getAssetCode());
		MemberAccount seller = order.getSeller();
		dispatch(seller.getId(), Constant.会员消息类型_C2C交易,
				NotificationTemplate.C2C_TRADE_COMPLETED_SELLER, new Object[] { amountText }, order.getId(), false);

		if (StrUtil.isNotBlank(order.getBuyerId())) {
			MemberAccount buyer = order.getBuyer();
			dispatch(buyer.getId(), Constant.会员消息类型_C2C交易,
					NotificationTemplate.C2C_TRADE_COMPLETED_BUYER, new Object[] { amountText }, order.getId(), false);
		}
	}

	private String langCodeOf(String memberAccountId) {
		MemberAccount member = memberAccountRepo.findById(memberAccountId).orElse(null);
		return member != null && StrUtil.isNotBlank(member.getNotifyLangCode())
				? member.getNotifyLangCode() : Constant.默认语言_简体中文;
	}

	private String formatAmount(BigDecimal amount, String currency) {
		String cur = StrUtil.isBlank(currency) ? "" : currency;
		if (amount == null) {
			return cur;
		}
		return amount.stripTrailingZeros().toPlainString() + " " + cur;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void notifyWithdrawCompleted(@NotBlank String orderId) {
		WithdrawOrder order = withdrawOrderRepo.getOne(orderId);
		if (order == null) {
			return;
		}
		String amountText = formatAmount(
				order.getActualAmount() != null ? order.getActualAmount() : order.getAmount(),
				order.getAssetCode());
		dispatch(order.getMemberAccountId(), Constant.会员消息类型_提币, NotificationTemplate.WITHDRAW_COMPLETED,
				new Object[] { amountText }, orderId, false);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void notifyWithdrawRejected(@NotBlank String orderId) {
		WithdrawOrder order = withdrawOrderRepo.getOne(orderId);
		if (order == null) {
			return;
		}
		String amountText = formatAmount(order.getAmount(), order.getAssetCode());
		dispatch(order.getMemberAccountId(), Constant.会员消息类型_提币, NotificationTemplate.WITHDRAW_REJECTED,
				new Object[] { amountText }, orderId, false);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void notifyRiskControl(C2cRiskRecord riskRecord) {
		String finishTimeText = formatRiskFinishTime(riskRecord.getRiskFinishTime());
		dispatch(riskRecord.getMemberAccountId(), Constant.会员消息类型_C2C风控,
				NotificationTemplate.C2C_RISK_CONTROL,
				new Object[] { riskRecord.getRiskCause(), riskRecord.getRiskPunish(), finishTimeText },
				riskRecord.getId(), false);
	}

	private String formatRiskFinishTime(LocalDateTime finishTime) {
		if (finishTime == null) {
			return "";
		}
		return finishTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}

	private void dispatch(String memberAccountId, String type, String title, String content, String bizId,
			boolean popupFlag, String templateCode, String templateParams) {
		MemberNotification notification = new MemberNotification();
		notification.setId(IdUtils.getId());
		notification.setMemberAccountId(memberAccountId);
		notification.setType(type);
		notification.setTitle(title);
		notification.setContent(content);
		notification.setBizId(bizId);
		notification.setTemplateCode(templateCode);
		notification.setTemplateParams(templateParams);
		notification.setReadFlag(false);
		notification.setPopupFlag(popupFlag);
		notification.setCreateTime(LocalDateTime.now());
		memberNotificationRepo.save(notification);
	}

	private void dispatch(String memberAccountId, String type, NotificationTemplate template, Object[] args,
			String bizId, boolean popupFlag) {
		String langCode = langCodeOf(memberAccountId);
		Object[] localized = template.localizeArgs(langCode, args);
		dispatch(memberAccountId, type, template.title(langCode), template.content(langCode, localized), bizId,
				popupFlag, template.name(), args == null ? null : JSONUtil.toJsonStr(args));
	}

}

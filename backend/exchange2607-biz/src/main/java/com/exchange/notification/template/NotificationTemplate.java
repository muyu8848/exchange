package com.exchange.notification.template;

import java.util.Locale;

import com.exchange.common.i18n.I18nUtils;

import cn.hutool.core.util.StrUtil;

/**
 * 会员通知模板：标题、内容文案统一维护在 i18n/messages*.properties，按会员 langCode 渲染。
 * <p>
 * key 不带空格，避免 java.util.Properties 把空格当作 key/value 分隔符。
 */
public enum NotificationTemplate {

	INVEST_SUBSCRIBE("理财申购成功", "申购成功，产品本金{0}，起息日{1}，到期日{2}。"),

	INVEST_AUTO_REDEEMED("理财自动赎回", "产品已到期，本金{0}已自动赎回至现货账户。"),

	INVEST_REDEEMED("理财赎回成功", "产品本金{0}赎回成功，已退回现货账户。"),

	AUTH_PASSED("实名认证通过", "您的实名认证已通过，相关交易功能已解锁，欢迎使用。"),

	AUTH_REJECTED("实名认证未通过", "您的实名认证未通过，原因：{0}。请修改信息后重新提交。"),

	AUTH_REJECTED_NO_REASON("实名认证未通过", "您的实名认证未通过，请修改信息后重新提交。"),

	PERPETUAL_LIQUIDATION("仓位已强制平仓", "您的{0}永续{1}已强平，平仓价{2}，盈亏{3}。"),

	DEPOSIT_CREDITED("充币到账", "{0}充币订单{1}已确认到账，到账{2}。"),

	C2C_BUYER_PAYMENT("C2C订单待付款", "您的订单已创建，请尽快向卖家付款，超时订单将自动取消。"),

	C2C_SELLER_PAID("C2C买家已付款", "买家已付款，请尽快核对收款并放行。"),

	C2C_TRADE_COMPLETED_SELLER("C2C卖出成功", "交易完成，您已成功出售{0}。"),

	C2C_TRADE_COMPLETED_BUYER("C2C买入成功", "交易已完成，您购买的{0}已到账。"),

	C2C_ORDER_CANCELED("C2C订单已取消", "您的订单已取消，原因：{0}。"),

	C2C_APPEAL_APPROVED("C2C申诉通过", "您的申诉已通过，订单已放行。"),

	C2C_APPEAL_REJECTED("C2C申诉未通过", "您的申诉未通过，订单已取消。"),

	WITHDRAW_COMPLETED("提币成功", "您提取的{0}已到账，请注意查收。"),

	WITHDRAW_REJECTED("提币被驳回", "您的提币申请被驳回，{0}已退回现货账户。"),

	C2C_RISK_CONTROL("C2C风控提醒", "因「{0}」触发风控，您已被{1}，预计{2}解除。") {

		@Override
		public Object[] localizeArgs(String langCode, Object[] args) {
			if (args == null || args.length < 3) {
				return args;
			}
			Locale locale = Locale.forLanguageTag(langCode);
			return new Object[] {
					I18nUtils.getMessage("riskCause." + args[0], locale),
					I18nUtils.getMessage("riskPunish." + args[1], locale),
					args[2]
			};
		}
	};

	private final String titleKey;

	private final String contentKey;

	NotificationTemplate(String titleKey, String contentKey) {
		this.titleKey = titleKey;
		this.contentKey = contentKey;
	}

	public String title(String langCode) {
		return I18nUtils.getMessage(titleKey, Locale.forLanguageTag(langCode));
	}

	public String content(String langCode, Object... args) {
		return I18nUtils.getMessage(contentKey, Locale.forLanguageTag(langCode), args);
	}

	/** 风控等需要二次翻译的参数，默认原样返回 */
	public Object[] localizeArgs(String langCode, Object[] args) {
		return args;
	}

	public static NotificationTemplate of(String name) {
		if (StrUtil.isBlank(name)) {
			return null;
		}
		for (NotificationTemplate template : values()) {
			if (template.name().equals(name)) {
				return template;
			}
		}
		return null;
	}

	/** 永续方向文案：多单 / Long / 多單，空单 / Short / 空單 */
	public static String direction(String langCode, boolean longPosition) {
		return I18nUtils.getMessage(longPosition ? "多单" : "空单", Locale.forLanguageTag(langCode));
	}
}

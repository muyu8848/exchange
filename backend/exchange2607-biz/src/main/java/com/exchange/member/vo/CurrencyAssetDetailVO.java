package com.exchange.member.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CurrencyAssetDetailVO {

	private String assetCode;

	/** 总余额 */
	private BigDecimal totalBalance;

	/** USDT 折算值 */
	private BigDecimal usdtValue;

	/** 按账户类型分布 */
	private List<AccountDistribution> accounts = new ArrayList<>();

	@Data
	public static class AccountDistribution {

		private String accountType;

		private String accountTypeName;

		private BigDecimal total;

		/** 该账户余额折算的 USDT 值 */
		private BigDecimal usdtValue;

		/** 占总余额的百分比 (0-100) */
		private BigDecimal percentage;

	}

}

package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class InvestmentOverviewVO {

	private BigDecimal totalUsdt = BigDecimal.ZERO;

	private BigDecimal totalAccruedInterestUsdt = BigDecimal.ZERO;

	/** 昨日收益（折合USDT） */
	private BigDecimal yesterdayInterestUsdt = BigDecimal.ZERO;

	private List<CurrencySummary> currencySummaries = new ArrayList<>();

	private List<ProductSummary> productSummaries = new ArrayList<>();

	@Data
	public static class CurrencySummary {
		private String assetCode;
		private BigDecimal totalValue;
		private BigDecimal usdtValue;
		private int positionCount;
		private List<CurrencyDetail> details = new ArrayList<>();
	}

	@Data
	public static class CurrencyDetail {
		private String positionId;
		/** 活期账户 ID（活期行才有值，供跳转活期详情页） */
		private String flexibleAccountId;
		private String productType;
		private Integer lockDays;
		private String productTypeName;
		private BigDecimal totalPrincipal;
		private BigDecimal totalAccruedInterest;
		/** 估算总收益（定期） */
		private BigDecimal estimatedTotalInterest;
		/** 已确认收益（定期） */
		private BigDecimal confirmedInterest;
		/** 年化收益率 */
		private BigDecimal annualRate;
		/** 定期：已过计息天数 */
		private Integer elapsedDays;
		/** 活期：昨日收益 */
		private BigDecimal yesterdayInterest;
	}

	@Data
	public static class ProductSummary {
		private String productType;
		private String productTypeName;
		private BigDecimal usdtValue;
		private int positionCount;
		private List<ProductDetail> details = new ArrayList<>();
	}

	@Data
	public static class ProductDetail {
		private String positionId;
		/** 活期账户 ID（活期行才有值，供跳转活期详情页） */
		private String flexibleAccountId;
		private String assetCode;
		private Integer lockDays;
		private BigDecimal totalPrincipal;
		private BigDecimal totalAccruedInterest;
		/** 估算总收益（定期） */
		private BigDecimal estimatedTotalInterest;
		/** 已确认收益（定期） */
		private BigDecimal confirmedInterest;
		/** 年化收益率 */
		private BigDecimal annualRate;
		/** 定期：已过计息天数 */
		private Integer elapsedDays;
		/** 活期：昨日收益 */
		private BigDecimal yesterdayInterest;
	}

}

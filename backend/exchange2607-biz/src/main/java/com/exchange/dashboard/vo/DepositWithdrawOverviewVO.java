package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class DepositWithdrawOverviewVO {

	/** 充值金额（USDT，按到账时间） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal depositAmount = BigDecimal.ZERO;

	/** 充值提交笔数（按提交时间） */
	private Long depositOrderCount = 0L;

	/** 充币待确认笔数（确认中，当前快照） */
	private Long pendingConfirmDepositCount = 0L;

	/** 提币金额（USDT，按完成时间） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal withdrawAmount = BigDecimal.ZERO;

	/** 提币手续费（USDT，按完成时间） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal withdrawFeeUsdtAmount = BigDecimal.ZERO;

	/** 提币提交笔数（按提交时间） */
	private Long withdrawOrderCount = 0L;

	/** 净流入（充值金额 - 提币金额） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal netInflow = BigDecimal.ZERO;

	/** 待审核提币笔数（快照） */
	private Long pendingAuditCount = 0L;

	/** 在途提币笔数（待审核 + 审核通过，快照） */
	private Long inTransitCount = 0L;
}

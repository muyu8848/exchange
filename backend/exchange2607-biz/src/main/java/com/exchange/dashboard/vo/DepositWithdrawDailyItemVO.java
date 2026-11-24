package com.exchange.dashboard.vo;

import java.math.BigDecimal;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
public class DepositWithdrawDailyItemVO {

	/** 时间点：yyyy-MM-dd 或 yyyy-MM-dd HH:00 */
	private String date;

	/** 充值提交笔数（按提交时间） */
	private Long depositOrderCount = 0L;

	/** 充值成功笔数（按到账时间） */
	private Long depositSuccessCount = 0L;

	/** 充值金额（USDT，按到账时间） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal depositAmount = BigDecimal.ZERO;

	/** 提币提交笔数（按提交时间） */
	private Long withdrawOrderCount = 0L;

	/** 提币成功笔数（按完成时间） */
	private Long withdrawSuccessCount = 0L;

	/** 提币金额（USDT，按完成时间） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal withdrawAmount = BigDecimal.ZERO;

	/** 提币手续费（USDT，按完成时间） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal withdrawFeeUsdtAmount = BigDecimal.ZERO;

	/** 净流入（充值金额 - 提币金额） */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal netInflow = BigDecimal.ZERO;
}

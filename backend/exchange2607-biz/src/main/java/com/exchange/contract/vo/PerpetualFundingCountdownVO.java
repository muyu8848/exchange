package com.exchange.contract.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class PerpetualFundingCountdownVO {

	/** 资金费率(交易对配置) */
	private BigDecimal fundingRate;

	/** 距下次资金费结算的毫秒数 */
	private Long remainingMillis;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime nextSettleTime;

}

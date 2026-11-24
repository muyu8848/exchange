package com.exchange.setting.param;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class C2cRiskSettingParam {

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Long buyerCancelTrade;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Long buyerUnPaid;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Long notMySelfPaid;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Long timeoutUnPaid;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Long buyerProtocolCancel;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Long sellerProtocolCancel;

	/** 违规分级递进 - 普通原因第 1 次违规处罚时长（小时），必填 */
	@NotNull
	@DecimalMin(value = "1")
	private Long tier1Duration;

	/** 违规分级递进 - 普通原因第 2 次违规处罚时长（小时），必填 */
	@NotNull
	@DecimalMin(value = "1")
	private Long tier2Duration;

	/** 违规分级递进 - 普通原因第 3 次违规处罚时长（小时），必填 */
	@NotNull
	@DecimalMin(value = "1")
	private Long tier3Duration;

	/** 违规分级递进 - 普通原因第 4 次及以上违规处罚时长（小时），必填 */
	@NotNull
	@DecimalMin(value = "1")
	private Long tier4Duration;

}

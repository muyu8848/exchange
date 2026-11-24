package com.exchange.setting.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.exchange.common.utils.IdUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "c2c_risk_setting")
@DynamicInsert(true)
@DynamicUpdate(true)
public class C2cRiskSetting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	/** 买家当日主动取消交易次数阈值：达到后限制买入（风控原因：买家取消交易） */
	private Long buyerCancelTrade;

	/** 买家当日未付款（含系统超时自动取消）次数阈值：达到后限制买入（风控原因：买家未付款） */
	private Long buyerUnPaid;

	/** 买家使用非本人支付账户付款的违规次数阈值：达到后限制买入 48 小时（风控原因：使用非本人支付账户付款） */
	private Long notMySelfPaid;

	/** 买家超时未付款次数阈值（独立可配，区别于 buyerUnPaid）：达到后限制买入（风控原因：超时未付款） */
	private Long timeoutUnPaid;

	/** 买家责任协议取消当日次数阈值：达到后限制买入（风控原因：买家原因协议取消）。非空，必须有值。 */
	private Long buyerProtocolCancel;

	/** 卖家责任协议取消当日次数阈值：达到后限制卖出（风控原因：卖家原因协议取消）。非空，必须有值。 */
	private Long sellerProtocolCancel;

	/** 违规分级递进 - 普通原因第 1 次违规处罚时长（小时） */
	private Long tier1Duration;

	/** 违规分级递进 - 普通原因第 2 次违规处罚时长（小时） */
	private Long tier2Duration;

	/** 违规分级递进 - 普通原因第 3 次违规处罚时长（小时） */
	private Long tier3Duration;

	/** 违规分级递进 - 普通原因第 4 次及以上违规处罚时长（小时） */
	private Long tier4Duration;

	/** 配置最后更新时间 */
	private LocalDateTime latelyUpdateTime;

	public static C2cRiskSetting build() {
		C2cRiskSetting setting = new C2cRiskSetting();
		setting.setId(IdUtils.getId());
		return setting;
	}

}

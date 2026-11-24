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
@Table(name = "c2c_setting")
@DynamicInsert(true)
@DynamicUpdate(true)
public class C2cSetting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	/** 发布广告时付款时限的可输入范围下限（分钟） */
	private Integer tradeDeadlineMin;

	/** 发布广告时付款时限的可输入范围上限（分钟） */
	private Integer tradeDeadlineMax;

	/** 买家付款后，卖家未确认放行的自动放行时间（分钟） */
	private Integer autoReleaseMinutes;
	
	private Integer maxActivePreOrders;

	/** 每条广告最多可绑定的支付方式数 */
	private Integer perOrderMaxPaymentAccounts;

	/** 协议取消申请后，对方未响应的自动超时时间（小时），必填 */
	private Integer protocolCancelHours;

	/** 同一订单再次发起申诉需间隔的分钟数（防申诉刷屏），必填 */
	private Integer appealIntervalMinutes;

	private LocalDateTime latelyUpdateTime;

	public static C2cSetting build() {
		C2cSetting setting = new C2cSetting();
		setting.setId(IdUtils.getId());
		return setting;
	}

}

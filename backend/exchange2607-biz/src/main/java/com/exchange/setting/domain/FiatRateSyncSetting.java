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
@Table(name = "fiat_rate_sync_setting")
@DynamicInsert(true)
@DynamicUpdate(true)
public class FiatRateSyncSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 32)
	private String id;

	@Column(name = "exchange_rate_source")
	private String exchangeRateSource;

	@Column(name = "auto_sync_exchange_rate")
	private Boolean autoSyncExchangeRate;

	private LocalDateTime latelyUpdateTime;

	public static FiatRateSyncSetting build() {
		FiatRateSyncSetting setting = new FiatRateSyncSetting();
		setting.setId(IdUtils.getId());
		return setting;
	}

}

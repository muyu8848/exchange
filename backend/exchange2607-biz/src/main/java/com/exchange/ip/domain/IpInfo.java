package com.exchange.ip.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * IP 归属地缓存。
 * <p>
 * 主键直接使用 IP：查过一次即永久保留，避免重复调用第三方接口。
 */
@Getter
@Setter
@Entity
@Table(name = "ip_info")
@DynamicInsert(true)
@DynamicUpdate(true)
public class IpInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 主键：直接使用 IP 地址 */
	@Id
	@Column(name = "id", length = 32)
	private String id;

	private String ip;

	private String country;

	private String province;

	private String city;

	private String isp;

	private LocalDateTime createTime;

	/** 拼成「国家|省|市|运营商」，空值自动跳过 */
	public String getFormatLocation() {
		List<String> infos = new ArrayList<>();
		if (StrUtil.isNotBlank(this.country)) {
			infos.add(this.country);
		}
		if (StrUtil.isNotBlank(this.province)) {
			infos.add(this.province);
		}
		if (StrUtil.isNotBlank(this.city)) {
			infos.add(this.city);
		}
		if (StrUtil.isNotBlank(this.isp)) {
			infos.add(this.isp);
		}
		return String.join("|", infos);
	}

}

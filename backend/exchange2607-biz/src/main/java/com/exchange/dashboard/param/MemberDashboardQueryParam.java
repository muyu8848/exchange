package com.exchange.dashboard.param;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class MemberDashboardQueryParam {

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	/** 账号状态：1=启用 0=停用 */
	private String status;

	/** 认证状态 */
	private String authenticationStatus;

	/** 注册方式：email / phone */
	private String contactType;

	/** 趋势粒度：hour / day，空则按时间跨度自动选择 */
	private String granularity;
}

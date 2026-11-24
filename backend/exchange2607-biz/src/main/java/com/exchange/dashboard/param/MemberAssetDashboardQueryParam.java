package com.exchange.dashboard.param;

import lombok.Data;

@Data
public class MemberAssetDashboardQueryParam {

	/** 币种 */
	private String assetCode;

	/** 会员账号状态：逗号分隔，如 NORMAL,FROZEN */
	private String memberAccountStatus;

	/** 账户类型：SPOT / FUTURES / FIAT */
	private String accountType;
}

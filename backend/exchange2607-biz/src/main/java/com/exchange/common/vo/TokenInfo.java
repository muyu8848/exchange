package com.exchange.common.vo;

import java.util.ArrayList;
import java.util.List;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import lombok.Data;

@Data
public class TokenInfo {

	private String tokenName;

	private String tokenValue;

	private String accountId;

	private String accountType;

	/**
	 * 权限集合
	 */
	private List<String> permissions = new ArrayList<>();

	public static TokenInfo build() {
		SaTokenInfo saTokenInfo = StpUtil.getTokenInfo();
		TokenInfo tokenInfo = new TokenInfo();
		tokenInfo.setTokenName(saTokenInfo.getTokenName());
		tokenInfo.setTokenValue(saTokenInfo.getTokenValue());
		return tokenInfo;
	}

}

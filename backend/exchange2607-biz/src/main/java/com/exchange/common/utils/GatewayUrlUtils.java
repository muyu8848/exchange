package com.exchange.common.utils;

import cn.hutool.core.util.StrUtil;

public final class GatewayUrlUtils {

	private GatewayUrlUtils() {
	}

	public static String buildFullUrl(String apiGateway, String path) {
		if (StrUtil.isBlank(path)) {
			return path;
		}
		if (StrUtil.isBlank(apiGateway)) {
			return path;
		}
		return apiGateway + path;
	}

}

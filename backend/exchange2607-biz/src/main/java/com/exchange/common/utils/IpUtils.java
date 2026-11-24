package com.exchange.common.utils;

import javax.servlet.http.HttpServletRequest;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;

/**
 * 客户端 IP 解析：优先信任 Nginx 覆盖的 X-Real-IP，其次 X-Forwarded-For 最后一个，最后 remoteAddr。
 */
public class IpUtils {

	private static final String UNKNOWN = "unknown";

	private IpUtils() {
	}

	public static String getClientIp(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		String realIp = request.getHeader("X-Real-IP");
		if (isValidIp(realIp)) {
			return realIp.trim();
		}
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (StrUtil.isNotBlank(forwardedFor)) {
			String[] parts = forwardedFor.split(",");
			for (int i = parts.length - 1; i >= 0; i--) {
				String candidate = parts[i].trim();
				if (isValidIp(candidate)) {
					return candidate;
				}
			}
		}
		String remoteAddr = request.getRemoteAddr();
		return isValidIp(remoteAddr) ? remoteAddr : null;
	}

	private static boolean isValidIp(String ip) {
		if (StrUtil.isBlank(ip)) {
			return false;
		}
		String value = ip.trim();
		if (UNKNOWN.equalsIgnoreCase(value)) {
			return false;
		}
		return Validator.isIpv4(value) || Validator.isIpv6(value);
	}
}

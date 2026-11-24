package com.exchange.common.utils;

import java.util.ArrayList;
import java.util.List;

import com.exchange.common.exception.BizException;

import cn.hutool.core.util.StrUtil;

public class IpWhiteList {

	public static void validIpWhiteList(String ipWhiteList, String ipAddr) {
		if (StrUtil.isNotBlank(ipWhiteList)) {
			String[] allIps = ipWhiteList.split("\\r?\\n");
			boolean ipHintFlag = false;
			for (String multiIp : allIps) {
				String[] ips = multiIp.split(",");
				for (String ip : ips) {
					if (StrUtil.trim(ip).equals(ipAddr)) {
						ipHintFlag = true;
						break;
					}
				}
			}
			if (!ipHintFlag) {
				throw new BizException("{0}不在IP白名单内", ipAddr);
			}
		}
	}

	public static String ipSplit(String ipWhiteList) {
		List<String> ips = new ArrayList<String>();
		if (StrUtil.isNotBlank(ipWhiteList)) {
			ipWhiteList = ipWhiteList.replace("，", ",");
			String[] tmpIps = ipWhiteList.split(",");
			for (String ip : tmpIps) {
				ips.add(StrUtil.trim(ip));
			}
		}
		return String.join(",", ips);
	}

}

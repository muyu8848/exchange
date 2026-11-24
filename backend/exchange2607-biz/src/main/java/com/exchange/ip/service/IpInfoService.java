package com.exchange.ip.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.ip.domain.IpInfo;
import com.exchange.ip.repo.IpInfoRepo;
import com.zengtengpeng.annotation.Lock;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * IP 归属地查询（数据源：ip9.com.cn 免费接口）。
 * <p>
 * 设计要点：<br>
 * 1. DB 即缓存：一个 IP 只查一次，之后直接读库；<br>
 * 2. 失败不阻断业务：请求异常直接返回，由调用方兜底；<br>
 * 3. 3 秒超时，避免拖慢调用方。
 */
@Slf4j
@Validated
@Service
public class IpInfoService {

	@Autowired
	private IpInfoRepo ipInfoRepo;

	/**
	 * 查询并记录 IP 归属地（幂等：已缓存则直接返回）。
	 * <p>
	 * 本机/内网 IP、请求异常、返回异常时静默跳过。
	 */
	@Lock(keys = "'logIpInfo' + #ip")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void logIpInfo(String ip) {
		if (StrUtil.isBlank(ip) || isLocalOrInnerIp(ip)) {
			return;
		}
		if (ipInfoRepo.findByIp(ip) != null) {
			return;
		}
		String responseStr;
		try {
			responseStr = HttpUtil.get("https://ip9.com.cn/get?ip=" + ip, 10000);
		} catch (Exception e) {
			log.warn("IP归属地请求异常 ip={} msg={}", ip, e.getMessage());
			return;
		}
		IpInfo ipInfo = parseIpInfo(ip, responseStr);
		if (ipInfo == null) {
			return;
		}
		ipInfoRepo.save(ipInfo);
	}

	/** 解析第三方响应；格式异常返回 null */
	private IpInfo parseIpInfo(String ip, String responseStr) {
		if (!JSONUtil.isTypeJSONObject(responseStr)) {
			log.warn("IP归属地接口返回非JSON ip={} resp={}", ip, StrUtil.subPre(responseStr, 200));
			return null;
		}
		JSONObject jsonObject = JSONUtil.parseObj(responseStr);
		Object ret = jsonObject.get("ret");
		if (!(ret instanceof Number) || ((Number) ret).intValue() != 200) {
			log.warn("IP归属地接口返回异常 ip={} resp={}", ip, StrUtil.subPre(responseStr, 200));
			return null;
		}
		JSONObject data = jsonObject.getJSONObject("data");
		if (data == null) {
			return null;
		}
		IpInfo ipInfo = new IpInfo();
		ipInfo.setId(ip);
		ipInfo.setIp(ip);
		ipInfo.setCountry(data.getStr("country"));
		ipInfo.setProvince(data.getStr("prov"));
		ipInfo.setCity(data.getStr("city"));
		ipInfo.setIsp(data.getStr("isp"));
		ipInfo.setCreateTime(LocalDateTime.now());
		return ipInfo;
	}

	/** 本机回环 / 内网 IP 无需查询（查了也是无意义的局域网信息） */
	private boolean isLocalOrInnerIp(String ip) {
		if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
			return true;
		}
		return NetUtil.isInnerIP(ip);
	}

}

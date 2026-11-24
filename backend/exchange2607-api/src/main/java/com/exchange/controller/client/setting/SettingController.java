package com.exchange.controller.client.setting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.setting.service.SettingService;
import com.exchange.setting.vo.C2cRiskSettingVO;
import com.exchange.setting.vo.C2cSettingVO;
import com.exchange.setting.vo.LatestAppInfoVO;

@RestController
@RequestMapping("/setting")
public class SettingController {

	@Autowired
	private SettingService service;

	@PostMapping("/getC2cRiskSetting")
	public Result<C2cRiskSettingVO> getC2cRiskSetting() {
		return Result.success(service.getC2cRiskSetting());
	}

	@PostMapping("/getC2cSetting")
	public Result<C2cSettingVO> getC2cSetting() {
		return Result.success(service.getC2cSetting());
	}

	@PostMapping("/getLatestAppInfo")
	public Result<LatestAppInfoVO> getLatestAppInfo() {
		return Result.success(service.getLatestAppInfo());
	}

	@PostMapping("/getCustomerServiceUrl")
	public Result<String> getCustomerServiceUrl() {
		return Result.success(service.getSystemSetting().getCustomerServiceUrl());
	}

	@PostMapping("/getH5Url")
	public Result<String> getH5Url() {
		return Result.success(service.getSystemSetting().getH5Url());
	}
}

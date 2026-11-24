package com.exchange.controller.admin.setting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.setting.param.C2cRiskSettingParam;
import com.exchange.setting.param.C2cSettingParam;
import com.exchange.setting.param.ConvertSettingParam;
import com.exchange.setting.param.FiatRateSyncSettingParam;
import com.exchange.setting.param.MailSettingParam;
import com.exchange.setting.param.OssSettingParam;
import com.exchange.setting.param.RebateSettingSaveParam;
import com.exchange.setting.param.RegisterSettingParam;
import com.exchange.setting.param.SecuritySettingParam;
import com.exchange.setting.param.SystemSettingParam;
import com.exchange.setting.service.SettingService;
import com.exchange.setting.vo.C2cRiskSettingVO;
import com.exchange.setting.vo.C2cSettingVO;
import com.exchange.setting.vo.ConvertSettingVO;
import com.exchange.setting.vo.FiatRateSyncSettingVO;
import com.exchange.setting.vo.MailSettingVO;
import com.exchange.setting.vo.OssSettingVO;
import com.exchange.setting.vo.RebateSettingVO;
import com.exchange.setting.vo.RegisterSettingVO;
import com.exchange.setting.vo.SecuritySettingVO;
import com.exchange.setting.vo.SystemSettingVO;

import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 参数配置
 */
@RestController
@RequestMapping("/setting")
public class SettingController {

	@Autowired
	private SettingService service;

	@PostMapping("/getMailSetting")
	public Result<MailSettingVO> getMailSetting() {
		return Result.success(service.getMailSetting());
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_参数配置, operate = "更新邮件设置")
	@SaCheckPermission("setting.mail")
	@PostMapping("/updateMailSetting")
	public Result<String> updateMailSetting(MailSettingParam param) {
		service.updateMailSetting(param);
		return Result.success();
	}

	/**
	 * 获取注册奖励设置
	 * 
	 * @return
	 */
	@PostMapping("/getRegisterSetting")
	public Result<RegisterSettingVO> getRegisterSetting() {
		return Result.success(service.getRegisterSetting());
	}

	/**
	 * 更新注册奖励
	 * 
	 * @param param
	 * @return
	 */
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_参数配置, operate = "更新注册奖励")
	@SaCheckPermission("setting.register")
	@PostMapping("/updateRegisterSetting")
	public Result<String> updateRegisterSetting(RegisterSettingParam param) {
		service.updateRegisterSetting(param);
		return Result.success();
	}

	/**
	 * 获取C2C设置
	 * 
	 * @return
	 */
	@PostMapping("/getC2cSetting")
	public Result<C2cSettingVO> getC2cSetting() {
		return Result.success(service.getC2cSetting());
	}

	/**
	 * 更新C2C设置
	 * 
	 * @param param
	 * @return
	 */
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_参数配置, operate = "更新C2C设置")
	@SaCheckPermission("setting.c2c")
	@PostMapping("/updateC2cSetting")
	public Result<String> updateC2cSetting(C2cSettingParam param) {
		service.updateC2cSetting(param);
		return Result.success();
	}

	/**
	 * 获取C2C风控规则
	 * 
	 * @return
	 */
	@PostMapping("/getC2cRiskSetting")
	public Result<C2cRiskSettingVO> getC2cRiskSetting() {
		return Result.success(service.getC2cRiskSetting());
	}

	/**
	 * 更新C2C风控规则
	 * 
	 * @param param
	 * @return
	 */
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_参数配置, operate = "更新C2C风控规则")
	@SaCheckPermission("setting.c2cRisk")
	@PostMapping("/updateC2cRiskSetting")
	public Result<String> updateC2cRiskSetting(C2cRiskSettingParam param) {
		service.updateC2cRiskSetting(param);
		return Result.success();
	}

	/**
	 * 获取系统设置
	 * 
	 * @return
	 */
	@PostMapping("/getSystemSetting")
	public Result<SystemSettingVO> getSystemSetting() {
		return Result.success(service.getSystemSetting());
	}

	/**
	 * 更新系统设置
	 * 
	 * @param param
	 * @return
	 */
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_参数配置, operate = "更新系统设置")
	@SaCheckPermission("setting.system")
	@PostMapping("/updateSystemSetting")
	public Result<String> updateSystemSetting(SystemSettingParam param) {
		service.updateSystemSetting(param);
		return Result.success();
	}

	/**
	 * 获取文件存储设置
	 */
	@PostMapping("/getOssSetting")
	public Result<OssSettingVO> getOssSetting() {
		return Result.success(service.getOssSetting());
	}

	/**
	 * 更新文件存储设置
	 */
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_参数配置, operate = "更新文件存储设置")
	@SaCheckPermission("setting.oss")
	@PostMapping("/updateOssSetting")
	public Result<String> updateOssSetting(OssSettingParam param) {
		service.updateOssSetting(param);
		return Result.success();
	}

	/**
	 * 刷新缓存
	 * 
	 * @return
	 */
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_参数配置, operate = "刷新缓存")
	@SaCheckPermission("setting.refreshCache")
	@PostMapping("/refreshCache")
	public Result<String> refreshCache() {
		service.refreshCache();
		return Result.success();
	}

	@PostMapping("/getFiatRateSyncSetting")
	public Result<FiatRateSyncSettingVO> getFiatRateSyncSetting() {
		return Result.success(service.getFiatRateSyncSetting());
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_参数配置, operate = "更新法币汇率同步设置")
	@SaCheckPermission("setting.fiatRate")
	@PostMapping("/updateFiatRateSyncSetting")
	public Result<String> updateFiatRateSyncSetting(FiatRateSyncSettingParam param) {
		service.updateFiatRateSyncSetting(param);
		return Result.success();
	}

	/**
	 * 获取闪兑设置
	 */
	@PostMapping("/getConvertSetting")
	public Result<ConvertSettingVO> getConvertSetting() {
		return Result.success(service.getConvertSetting());
	}

	/**
	 * 更新闪兑设置
	 */
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_参数配置, operate = "更新闪兑设置")
	@SaCheckPermission("setting.convert")
	@PostMapping("/updateConvertSetting")
	public Result<String> updateConvertSetting(ConvertSettingParam param) {
		service.updateConvertSetting(param);
		return Result.success();
	}

	@PostMapping("/getRebateSetting")
	public Result<RebateSettingVO> getRebateSetting() {
		return Result.success(service.getRebateSetting());
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_参数配置, operate = "更新返佣比例")
	@SaCheckPermission("setting.rebate")
	@PostMapping("/updateRebateSetting")
	public Result<String> updateRebateSetting(RebateSettingSaveParam param) {
		service.updateRebateSetting(param);
		return Result.success();
	}

	@PostMapping("/getSecuritySetting")
	public Result<SecuritySettingVO> getSecuritySetting() {
		return Result.success(service.getSecuritySetting());
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_参数配置, operate = "更新安全设置")
	@SaCheckPermission("setting.security")
	@PostMapping("/updateSecuritySetting")
	public Result<String> updateSecuritySetting(SecuritySettingParam param) {
		service.updateSecuritySetting(param);
		return Result.success();
	}

}

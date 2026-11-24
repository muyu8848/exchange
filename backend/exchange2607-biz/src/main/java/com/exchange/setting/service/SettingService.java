package com.exchange.setting.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.exception.BizException;
import com.exchange.constants.Constant;
import com.exchange.setting.domain.C2cRiskSetting;
import com.exchange.setting.domain.C2cSetting;
import com.exchange.setting.domain.ConvertSetting;
import com.exchange.setting.domain.FiatRateSyncSetting;
import com.exchange.setting.domain.MailSetting;
import com.exchange.setting.domain.OssSetting;
import com.exchange.setting.domain.RegisterSetting;
import com.exchange.setting.domain.SecuritySetting;
import com.exchange.setting.domain.SystemSetting;
import com.exchange.setting.param.C2cRiskSettingParam;
import com.exchange.setting.param.C2cSettingParam;
import com.exchange.setting.param.ConvertSettingParam;
import com.exchange.setting.param.FiatRateSyncSettingParam;
import com.exchange.setting.param.MailSettingParam;
import com.exchange.setting.param.OssSettingParam;
import com.exchange.setting.param.RegisterSettingParam;
import com.exchange.setting.param.SecuritySettingParam;
import com.exchange.setting.param.SystemSettingParam;
import com.exchange.setting.repo.C2cRiskSettingRepo;
import com.exchange.setting.repo.C2cSettingRepo;
import com.exchange.setting.repo.ConvertSettingRepo;
import com.exchange.setting.repo.FiatRateSyncSettingRepo;
import com.exchange.setting.repo.MailSettingRepo;
import com.exchange.setting.repo.OssSettingRepo;
import com.exchange.setting.repo.RegisterSettingRepo;
import com.exchange.setting.repo.SecuritySettingRepo;
import com.exchange.setting.repo.SystemSettingRepo;
import com.exchange.setting.domain.RebateSetting;
import com.exchange.setting.param.RebateSettingSaveParam;
import com.exchange.setting.repo.RebateSettingRepo;
import com.exchange.setting.vo.RebateSettingVO;
import com.exchange.setting.vo.C2cRiskSettingVO;
import com.exchange.setting.vo.C2cSettingVO;
import com.exchange.setting.vo.ConvertSettingVO;
import com.exchange.setting.vo.FiatRateSyncSettingVO;
import com.exchange.setting.vo.LatestAppInfoVO;
import com.exchange.setting.vo.MailSettingVO;
import com.exchange.setting.vo.OssSettingVO;
import com.exchange.setting.vo.RegisterSettingVO;
import com.exchange.setting.vo.SecuritySettingVO;
import com.exchange.setting.vo.SystemSettingVO;

import lombok.extern.slf4j.Slf4j;

@Validated
@Service
@Slf4j
public class SettingService {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private SystemSettingRepo systemSettingRepo;

	@Autowired
	private C2cRiskSettingRepo c2cRiskSettingRepo;

	@Autowired
	private C2cSettingRepo c2cSettingRepo;

	@Autowired
	private RegisterSettingRepo registerSettingRepo;

	@Autowired
	private OssSettingRepo ossSettingRepo;

	@Autowired
	private FiatRateSyncSettingRepo fiatRateSyncSettingRepo;

	@Autowired
	private ConvertSettingRepo convertSettingRepo;

	@Autowired
	private MailSettingRepo mailSettingRepo;

	@Autowired
	private RebateSettingRepo rebateSettingRepo;

	@Autowired
	private SecuritySettingRepo securitySettingRepo;

	@Transactional(readOnly = true)
	public MailSettingVO getMailSetting() {
		MailSetting setting = mailSettingRepo.findTopByOrderByLatelyUpdateTime();
		return MailSettingVO.convertFor(setting);
	}

	@Transactional
	public void updateMailSetting(@Valid MailSettingParam param) {
		MailSetting setting = mailSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null) {
			setting = MailSetting.build();
		}
		BeanUtils.copyProperties(param, setting);
		setting.setLatelyUpdateTime(new Date());
		mailSettingRepo.save(setting);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public RegisterSettingVO getRegisterSetting() {
		RegisterSetting setting = registerSettingRepo.findTopByOrderByLatelyUpdateTime();
		return RegisterSettingVO.convertFor(setting);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateRegisterSetting(@Valid RegisterSettingParam param) {
		RegisterSetting setting = registerSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null) {
			setting = RegisterSetting.build();
		}
		BeanUtils.copyProperties(param, setting);
		setting.setLatelyUpdateTime(LocalDateTime.now());
		registerSettingRepo.save(setting);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public C2cSettingVO getC2cSetting() {
		C2cSetting setting = c2cSettingRepo.findTopByOrderByLatelyUpdateTime();
		return C2cSettingVO.convertFor(setting);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateC2cSetting(@Valid C2cSettingParam param) {
		Integer deadlineMin = param.getTradeDeadlineMin();
		Integer deadlineMax = param.getTradeDeadlineMax();
		if (deadlineMin == null || deadlineMin <= 0) {
			throw new BizException("付款时限范围下限必须大于0");
		}
		if (deadlineMax == null || deadlineMax < deadlineMin) {
			throw new BizException("付款时限范围上限不能小于下限");
		}
		C2cSetting setting = c2cSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null) {
			setting = C2cSetting.build();
		}
		BeanUtils.copyProperties(param, setting);
		setting.setLatelyUpdateTime(LocalDateTime.now());
		c2cSettingRepo.save(setting);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public C2cRiskSettingVO getC2cRiskSetting() {
		C2cRiskSetting setting = c2cRiskSettingRepo.findTopByOrderByLatelyUpdateTime();
		return C2cRiskSettingVO.convertFor(setting);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateC2cRiskSetting(@Valid C2cRiskSettingParam param) {
		C2cRiskSetting setting = c2cRiskSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null) {
			setting = C2cRiskSetting.build();
		}
		BeanUtils.copyProperties(param, setting);
		setting.setLatelyUpdateTime(LocalDateTime.now());
		c2cRiskSettingRepo.save(setting);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public LatestAppInfoVO getLatestAppInfo() {
		SystemSetting setting = systemSettingRepo.findTopByOrderByLatelyUpdateTime();
		return LatestAppInfoVO.convertFor(setting);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public SystemSettingVO getSystemSetting() {
		SystemSetting setting = systemSettingRepo.findTopByOrderByLatelyUpdateTime();
		return SystemSettingVO.convertFor(setting);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateSystemSetting(@Valid SystemSettingParam param) {
		SystemSetting setting = systemSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null) {
			setting = SystemSetting.build();
		}
		BeanUtils.copyProperties(param, setting);
		setting.setLatelyUpdateTime(LocalDateTime.now());
		systemSettingRepo.save(setting);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public OssSettingVO getOssSetting() {
		OssSetting setting = ossSettingRepo.findTopByOrderByLatelyUpdateTime();
		return OssSettingVO.convertFor(setting);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateOssSetting(@Valid OssSettingParam param) {
		if (!Constant.存储类型_本地.equals(param.getType())) {
			throw new BizException("暂只支持本地存储");
		}
		normalizeOssSettingParam(param);
		OssSetting setting = ossSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null) {
			setting = OssSetting.build();
		}
		BeanUtils.copyProperties(param, setting);
		setting.setLatelyUpdateTime(LocalDateTime.now());
		ossSettingRepo.save(setting);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public OssSetting requireOssSetting() {
		OssSetting setting = ossSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null) {
			throw new BizException("请先在运营后台配置文件存储");
		}
		if (!Constant.存储类型_本地.equals(setting.getType())) {
			throw new BizException("暂不支持该存储类型");
		}
		if (setting.getLocalStoragePath() == null || setting.getLocalStoragePath().trim().isEmpty()) {
			throw new BizException("本地存储目录未配置");
		}
		if (setting.getPublicFetchUrl() == null || setting.getPublicFetchUrl().trim().isEmpty()) {
			throw new BizException("文件读取网关未配置");
		}
		return setting;
	}

	private void normalizeOssSettingParam(OssSettingParam param) {
		param.setLocalStoragePath(param.getLocalStoragePath().trim());
		param.setPublicFetchUrl(trimTrailingSlash(param.getPublicFetchUrl().trim()));
	}

	private String trimTrailingSlash(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		while (value.endsWith("/")) {
			value = value.substring(0, value.length() - 1);
		}
		return value;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public FiatRateSyncSettingVO getFiatRateSyncSetting() {
		FiatRateSyncSetting setting = fiatRateSyncSettingRepo.findTopByOrderByLatelyUpdateTime();
		return FiatRateSyncSettingVO.convertFor(setting);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateFiatRateSyncSetting(@Valid FiatRateSyncSettingParam param) {
		FiatRateSyncSetting setting = fiatRateSyncSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null) {
			setting = FiatRateSyncSetting.build();
		}
		BeanUtils.copyProperties(param, setting);
		setting.setLatelyUpdateTime(LocalDateTime.now());
		fiatRateSyncSettingRepo.save(setting);
	}

	public void refreshCache() {
		List<String> cacheItems = Arrays.asList(Constant.当前项目 + "dict*", Constant.当前项目 + "chainNetwork*");
		List<String> deleteSuccessKeys = new ArrayList<>();
		List<String> deleteFailKeys = new ArrayList<>();
		for (String cacheItem : cacheItems) {
			Set<String> keys = redisTemplate.keys(cacheItem);
			for (String key : keys) {
				Boolean flag = redisTemplate.delete(key);
				if (flag) {
					deleteSuccessKeys.add(key);
				} else {
					deleteFailKeys.add(key);
				}
			}
		}
		if (!deleteFailKeys.isEmpty()) {
			log.warn("以下的缓存删除失败:", deleteFailKeys);
		}
	}

	// ===== 闪兑设置 =====

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public ConvertSettingVO getConvertSetting() {
		ConvertSetting setting = convertSettingRepo.findTopByOrderByLatelyUpdateTime();
		return ConvertSettingVO.convertFor(setting);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateConvertSetting(@Valid ConvertSettingParam param) {
		ConvertSetting setting = convertSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null) {
			setting = ConvertSetting.build();
		}
		BeanUtils.copyProperties(param, setting);
		setting.setLatelyUpdateTime(LocalDateTime.now());
		convertSettingRepo.save(setting);
	}

	// ===== 直推返佣比例配置 =====

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public RebateSettingVO getRebateSetting() {
		RebateSetting setting = rebateSettingRepo.findTopByOrderByLatelyUpdateTime();
		return RebateSettingVO.convertFor(setting);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateRebateSetting(@Valid RebateSettingSaveParam param) {
		RebateSetting setting = rebateSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null) {
			setting = RebateSetting.build();
		}
		BeanUtils.copyProperties(param, setting);
		setting.setLatelyUpdateTime(LocalDateTime.now());
		rebateSettingRepo.save(setting);
	}

	// ===== 安全设置 =====

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public SecuritySettingVO getSecuritySetting() {
		return SecuritySettingVO.convertFor(securitySettingRepo.findTopByOrderByLatelyUpdateTime());
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateSecuritySetting(@Valid SecuritySettingParam param) {
		SecuritySetting setting = securitySettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null) {
			setting = SecuritySetting.build();
		}
		BeanUtils.copyProperties(param, setting);
		setting.setLatelyUpdateTime(LocalDateTime.now());
		securitySettingRepo.save(setting);
	}

}

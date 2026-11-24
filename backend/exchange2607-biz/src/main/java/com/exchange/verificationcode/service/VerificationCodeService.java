package com.exchange.verificationcode.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.exception.BizException;
import com.exchange.common.i18n.I18nUtils;
import com.exchange.common.i18n.LocaleUtils;
import com.exchange.common.mq.MqPublisher;
import com.exchange.common.vo.PageResult;
import com.exchange.constants.Constant;
import com.exchange.member.domain.MemberAccount;
import com.exchange.member.repo.MemberAccountRepo;
import com.exchange.setting.domain.MailSetting;
import com.exchange.setting.repo.MailSettingRepo;
import com.exchange.verificationcode.domain.VerificationCodeSendRecord;
import com.exchange.verificationcode.param.SendForgetVerificationCodeParam;
import com.exchange.verificationcode.param.SendLoginVerificationCodeParam;
import com.exchange.verificationcode.param.SendRegisterVerificationCodeParam;
import com.exchange.verificationcode.param.VerifyCodeParam;
import com.exchange.verificationcode.param.VerificationCodeSendRecordQueryCondParam;
import com.exchange.verificationcode.repo.VerificationCodeSendRecordRepo;
import com.exchange.verificationcode.vo.VerificationCodeSendRecordVO;
import com.zengtengpeng.annotation.Lock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@Service
public class VerificationCodeService {

	@Autowired
	private MqPublisher mqPublisher;

	@Autowired
	private StringRedisTemplate redisTemplate;

	public static String 发送成功 = "sendSuccess";

	@Autowired
	private VerificationCodeSendRecordRepo sendRecordRepo;

	@Autowired
	private MailSettingRepo mailSettingRepo;

	@Autowired
	private MemberAccountRepo memberAccountRepo;

	@Value("${useProxy:false}")
	private Boolean useProxy;

	@Value("${proxyHost:}")
	private String proxyHost;

	@Value("${proxyPort:0}")
	private Integer proxyPort;

	@Lock(keys = "'sendRegisterVerificationCode' + #param.contact")
	@Transactional
	public void sendRegisterVerificationCode(@Valid SendRegisterVerificationCodeParam param) {
		MemberAccount existAccount = null;
		if (Constant.联系方式_手机号码.equals(param.getContactType())) {
			existAccount = memberAccountRepo.findByPhoneAreaCodeAndPhone(param.getPhoneAreaCode(), param.getContact());
		} else {
			if (!Validator.isEmail(param.getContact())) {
				throw new BizException("邮箱地址无效");
			}
			existAccount = memberAccountRepo.findByEmail(param.getContact());
		}
		if (existAccount != null) {
			throw new BizException("该账号已存在请联系客服");
		}
		// 注册时账号尚不存在，用当前请求语言
		createVerificationCodeRecord(param.getContactType(), param.getPhoneAreaCode(), param.getContact(),
				Constant.验证码类型_注册, param.getIp(), LocaleUtils.currentLocale().toLanguageTag());
	}

	/**
	 * 创建验证码发送记录。
	 * <p>
	 * 语言由调用方在请求线程内解析后传入（见 {@link #resolveMailLangCode(String)}）：实际发信由 MQ
	 * 消费线程执行，届时已无请求上下文，无法再读取 Lang 请求头。
	 *
	 * @param langCode 收件语言码，如 zh-CN / zh-TW / en-US
	 */
	@Transactional
	public void createVerificationCodeRecord(String channel, String phoneAreaCode, String contact, String bizType,
			String ip, String langCode) {
		if (Constant.联系方式_手机号码.equals(channel)) {
			if (contact.length() < 6 || contact.length() > 12) {
				throw new BizException("无效的手机号");
			}
		}
		String smsCodeGet = redisTemplate.opsForValue().get(Constant.验证码发送限制 + contact);
		if (StrUtil.isNotBlank(smsCodeGet)) {
			throw new BizException("请不要频繁获取验证码");
		}

		redisTemplate.opsForValue().set(Constant.验证码发送限制 + contact, "1", 60, TimeUnit.SECONDS);
		String verificationCode = RandomUtil.randomNumbers(6);
		// Redis key 增加业务类型(bizType)隔离，避免注册/找回等场景验证码互相覆盖
		redisTemplate.opsForValue().set(Constant.验证码 + bizType + contact, verificationCode, Constant.验证码有效期分钟 * 60L,
				TimeUnit.SECONDS);

		VerificationCodeSendRecord verificationCodeSendRecord = VerificationCodeSendRecord.build(channel, phoneAreaCode,
				contact, bizType, verificationCode, ip, langCode);
		sendRecordRepo.save(verificationCodeSendRecord);

		mqPublisher.publishAfterCommit(Constant.发送验证码, verificationCodeSendRecord.getId());
	}

	/**
	 * 解析邮件语言：收件账号的通知语言优先，无账号（注册）时用当前请求语言。
	 * <p>
	 * 供调用方在请求线程内预先解析后传给 {@link #createVerificationCodeRecord}； MQ 消费线程拿不到
	 * LocaleContext，不能延后到发信时再解析。
	 */
	public String resolveMailLangCode(String memberAccountId) {
		if (StrUtil.isNotBlank(memberAccountId)) {
			MemberAccount member = memberAccountRepo.findById(memberAccountId).orElse(null);
			if (member != null && StrUtil.isNotBlank(member.getNotifyLangCode())) {
				return member.getNotifyLangCode();
			}
		}
		return LocaleUtils.currentLocale().toLanguageTag();
	}

	@Lock(keys = "'sendLoginVerificationCode' + #param.contact")
	@Transactional
	public void sendLoginVerificationCode(@Valid SendLoginVerificationCodeParam param) {
		String contact = param.getContact();
		if (Constant.联系方式_手机号码.equals(param.getContactType())) {
			if (contact.length() < 6 || contact.length() > 12) {
				throw new BizException("无效的手机号");
			}
		} else if (Constant.联系方式_邮箱.equals(param.getContactType())) {
			if (!Validator.isEmail(contact)) {
				throw new BizException("邮箱地址无效");
			}
		} else {
			throw new BizException("不支持的登录方式");
		}
		// 账号不存在则静默返回成功（不暴露账号是否已注册，也不真正发码）
		MemberAccount account = resolveMemberForLogin(param.getContactType(), param.getPhoneAreaCode(), contact);
		if (account != null) {
			createVerificationCodeRecord(param.getContactType(), param.getPhoneAreaCode(), contact, Constant.验证码类型_登录,
					param.getIp(), account.getNotifyLangCode());
		}
	}

	private MemberAccount resolveMemberForLogin(String contactType, String phoneAreaCode, String contact) {
		if (Constant.联系方式_邮箱.equals(contactType)) {
			return memberAccountRepo.findByEmailAndStatusIn(contact, Arrays.asList(Constant.账号状态_正常, Constant.账号状态_冻结));
		}
		if (Constant.联系方式_手机号码.equals(contactType)) {
			return memberAccountRepo.findByPhoneAreaCodeAndPhoneAndStatusIn(phoneAreaCode, contact,
					Arrays.asList(Constant.账号状态_正常, Constant.账号状态_冻结));
		}
		return null;
	}

	@Lock(keys = "'sendForgetVerificationCode' + #param.contact")
	@Transactional
	public void sendForgetVerificationCode(@Valid SendForgetVerificationCodeParam param) {
		String contact = param.getContact();
		if (Constant.联系方式_手机号码.equals(param.getContactType())) {
			contact = param.getPhoneAreaCode() + contact;
		} else {
			if (!Validator.isEmail(param.getContact())) {
				throw new BizException("邮箱地址无效");
			}
		}
		// 找回密码：只有已存在的账号才发码（避免对未注册账号轰炸 / 泄露注册状态）
		MemberAccount existAccount;
		if (Constant.联系方式_邮箱.equals(param.getContactType())) {
			existAccount = memberAccountRepo.findByEmailAndStatusIn(param.getContact(),
					Arrays.asList(Constant.账号状态_正常, Constant.账号状态_冻结));
		} else {
			existAccount = memberAccountRepo.findByPhoneAreaCodeAndPhoneAndStatusIn(param.getPhoneAreaCode(),
					param.getContact(), Arrays.asList(Constant.账号状态_正常, Constant.账号状态_冻结));
		}
		if (existAccount == null) {
			throw new BizException("账号不存在");
		}
		createVerificationCodeRecord(param.getContactType(), param.getPhoneAreaCode(), param.getContact(),
				Constant.验证码类型_忘记密码, param.getIp(), existAccount.getNotifyLangCode());
	}

	/**
	 * 校验验证码（注册/找回密码等场景通用）。校验通过后立即删除 key，防止重放。 注意：contact
	 * 必须与发送验证码时传入的保持一致（邮箱或手机号原文）。
	 */
	public void verifyCode(@Valid VerifyCodeParam param) {
		String redisKey = Constant.验证码 + param.getBizType() + param.getContact();
		String cachedCode = redisTemplate.opsForValue().get(redisKey);
		if (StrUtil.isBlank(cachedCode)) {
			throw new BizException("验证码已过期或不存在");
		}
		if (!cachedCode.equals(param.getCode())) {
			throw new BizException("验证码错误");
		}
		redisTemplate.delete(redisKey);
	}

	@Transactional(readOnly = true)
	public PageResult<VerificationCodeSendRecordVO> findSendRecordByPage(
			@Valid VerificationCodeSendRecordQueryCondParam param) {
		Specification<VerificationCodeSendRecord> spec = param.buildSpecification();
		Page<VerificationCodeSendRecord> result = sendRecordRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("createTime"))));
		PageResult<VerificationCodeSendRecordVO> pageResult = new PageResult<>(
				VerificationCodeSendRecordVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
		return pageResult;
	}

	@Lock(keys = "'sendVerificationCode' + #id")
	@Transactional
	public void sendVerificationCode(String id) {
		VerificationCodeSendRecord sendRecord = sendRecordRepo.getOne(id);
		if (!Constant.验证码发送状态_未发送.equals(sendRecord.getSendStatus())) {
			return;
		}

		String result = "";
		if (Constant.联系方式_邮箱.equals(sendRecord.getContactType())) {
			result = sendEmail(sendRecord);
		} else {
			result = sendSms(sendRecord);
		}
		if (发送成功.equals(result)) {
			sendRecord.success();
		} else {
			sendRecord.fail(result);
		}
		sendRecordRepo.save(sendRecord);
	}

	public String sendEmail(VerificationCodeSendRecord sendRecord) {
		MailSetting mailSetting = mailSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (mailSetting == null) {
			return "邮件配置不存在";
		}
		MailAccount account = new MailAccount();
		account.setHost(mailSetting.getHost());
		account.setPort(mailSetting.getPort());
		account.setAuth(mailSetting.getNeedAuthFlag());
		account.setFrom(mailSetting.getMailAccount());
		account.setUser(mailSetting.getMailUser());
		account.setPass(mailSetting.getMailPwd());

		String enc = mailSetting.getEncryptionType();
		if (Constant.邮件加密方式_SSL.equals(enc)) {
			account.setSslEnable(true); // 465
		} else if (Constant.邮件加密方式_STARTTLS.equals(enc)) {
			account.setStarttlsEnable(true); // 587
		}

		// 走全局代理（application.yml 的 useProxy），与行情/WebSocket 等保持一致
		// Hutool MailAccount 无 setProxy，改用自定义属性映射 JavaMail 的 SMTP 代理（HTTP CONNECT 隧道）
		if (Boolean.TRUE.equals(useProxy) && StrUtil.isNotBlank(proxyHost) && proxyPort != null && proxyPort > 0) {
			account.setCustomProperty("mail.smtp.proxy.host", proxyHost);
			account.setCustomProperty("mail.smtp.proxy.port", String.valueOf(proxyPort));
		}

		// 标题与正文按发送记录上的语言多语言渲染（8 类验证码场景共用同一套文案）
		Locale locale = Locale.forLanguageTag(sendRecord.getLangCode());
		String subject = I18nUtils.getMessage("mail.verification.subject", locale);
		String content = I18nUtils.getMessage("mail.verification.content", locale, sendRecord.getVerificationCode(),
				Constant.验证码有效期分钟);

		try {
			MailUtil.send(account, CollUtil.newArrayList(sendRecord.getContact()), subject, content, false);
		} catch (Exception e) {
			log.error("邮件验证码发送失败", e);
			return e.getMessage();
		}
		return 发送成功;
	}

	public String sendSms(VerificationCodeSendRecord smsSendRecord) {
		List<Map<String, String>> phoneMap = new ArrayList<>();
		phoneMap.add(MapUtil.builder("phone", smsSendRecord.getPhoneAreaCode() + smsSendRecord.getContact()).build());
		String key = "Saup5bcbexfaZVA1yPP1AYmLooZfGrxj";
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("username", "Vvjxjt");
		paramMap.put("nonceStr", RandomUtil.randomString(32));
		paramMap.put("timestamp", System.currentTimeMillis());
		paramMap.put("signType", "MD5");
		paramMap.put("content", "Your verification code is " + smsSendRecord.getVerificationCode());
		paramMap.put("spNumber", RandomUtil.randomNumbers(6));
		paramMap.put("phones", phoneMap);

		StringBuilder signPlain = new StringBuilder();
		TreeMap<String, Object> map = new TreeMap<>(paramMap);
		for (Iterator<Entry<String, Object>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Object> entry = iterator.next();
			signPlain.append(entry.getKey()).append("=").append(entry.getValue());
			if (iterator.hasNext()) {
				signPlain.append("&");
			}
		}
		signPlain.append("&key=" + key);
		System.out.println(signPlain.toString());
		paramMap.put("sign", new Digester(DigestAlgorithm.MD5).digestHex(signPlain.toString()));

		String responseStr = "";
		boolean exceptionFlag = false;
		try {
			responseStr = HttpRequest.post("http://183.178.45.166/ta-sms/openapi/submittal")
					.body(JSONUtil.toJsonStr(paramMap)).execute().body();
		} catch (Exception e) {
			exceptionFlag = true;
			responseStr = e.getMessage();
			log.error("短信接口接口请求失败");
		}
		if (exceptionFlag) {
			return responseStr;
		}
		if (!JSONUtil.isTypeJSONObject(responseStr)) {
			return responseStr;
		}
		JSONObject jsonObject = JSONUtil.parseObj(responseStr);
		if (!jsonObject.containsKey("code")) {
			return responseStr;
		}
		if (!jsonObject.getStr("code").equals("200")) {
			return jsonObject.getStr("message");
		}
		return 发送成功;
	}

	public String sign(Map<String, Object> paramMap, String key) {
		StringBuilder sign = new StringBuilder();
		TreeMap<String, Object> map = new TreeMap<>(paramMap);
		for (Entry<String, Object> entry : map.entrySet()) {
			sign.append(entry.getKey()).append(entry.getValue());
		}
		sign.append(key);
		return new Digester(DigestAlgorithm.MD5).digestHex(sign.toString());
	}

}

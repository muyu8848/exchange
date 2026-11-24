package com.exchange.multilang.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.exception.BizException;
import com.exchange.common.i18n.LocaleUtils;
import com.exchange.common.vo.PageResult;
import com.exchange.constants.Constant;
import com.exchange.multilang.domain.MultiLang;
import com.exchange.multilang.param.AddOrUpdateMultiLangParam;
import com.exchange.multilang.param.MultiLangQueryCondParam;
import com.exchange.multilang.repo.MultiLangRepo;
import com.exchange.multilang.vo.MultiLangListVO;
import com.exchange.multilang.vo.MultiLangOptionVO;
import com.exchange.multilang.vo.MultiLangVO;

import cn.hutool.core.util.StrUtil;

/**
 * 多语言清单服务。
 * <p>
 * 只维护「语言清单」；具体文案在 exchange2607-api 的 i18n/messages_*.properties。 语言解析不走本服务（见
 * MultiLangHolder），本服务服务客户端语言列表与后台维护。
 */
@Validated
@Service
public class MultiLangService {

	@Autowired
	private MultiLangRepo multiLangRepo;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public MultiLangVO findById(@NotBlank String id) {
		return MultiLangVO.convertFor(multiLangRepo.getOne(id));
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<MultiLangVO> findAll(@Valid MultiLangQueryCondParam param) {
		List<MultiLang> result = multiLangRepo.findAll(param.buildSpecification(), Sort.by(Sort.Order.asc("sortNum")));
		return MultiLangVO.convertFor(result);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MultiLangVO> findByPage(@Valid MultiLangQueryCondParam param) {
		Page<MultiLang> result = multiLangRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.asc("sortNum"))));
		return new PageResult<>(MultiLangVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
	}

	/** 客户端语言清单（含默认语言）：仅启用中的语言 */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public MultiLangListVO findEnabledList() {
		MultiLangListVO vo = new MultiLangListVO();
		vo.setDefaultLang(LocaleUtils.defaultLang());
		vo.setLanguages(MultiLangOptionVO.convertFor(multiLangRepo.findByStatusOrderBySortNumAsc(Constant.启用状态_启用)));
		return vo;
	}

	/** 删除语言：默认语言不可删除，且至少保留一种启用中的语言 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void del(@NotBlank String id) {
		MultiLang po = multiLangRepo.getOne(id);
		if (Constant.默认语言_简体中文.equalsIgnoreCase(po.getLangCode())) {
			throw new BizException("默认语言不可删除");
		}
		if (Constant.启用状态_启用.equals(po.getStatus())) {
			validNotLastEnabled();
		}
		multiLangRepo.delete(po);
	}

	/**
	 * 新增 / 编辑语言：语言编码不可修改（改编码会破坏客户端本地缓存与文案文件名）。
	 *
	 * @return 文案文件缺失提示（无需提示时返回 null）
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public String addOrUpdate(@Valid AddOrUpdateMultiLangParam param) {
		String langCode = StrUtil.trim(param.getLangCode());
		Locale locale = parseLangCode(langCode);
		if (StrUtil.isBlank(param.getId())) {
			if (multiLangRepo.findByLangCode(langCode) != null) {
				throw new BizException("语言编码已存在");
			}
			multiLangRepo.save(param.convertToPo());
			return buildMissingMessageTip(langCode, locale);
		}
		MultiLang po = multiLangRepo.getOne(param.getId());
		if (Constant.启用状态_停用.equals(param.getStatus())) {
			validDisable(po);
		}
		// 语言编码不可修改：忽略 id 与 langCode
		BeanUtils.copyProperties(param, po, "id", "langCode");
		po.setUpdateTime(LocalDateTime.now());
		multiLangRepo.save(po);
		return buildMissingMessageTip(po.getLangCode(), locale);
	}

	/** 停用前置校验：默认语言不可停用，且至少保留一种启用中的语言 */
	private void validDisable(MultiLang po) {
		if (Constant.默认语言_简体中文.equalsIgnoreCase(po.getLangCode())) {
			throw new BizException("默认语言不可停用");
		}
		if (Constant.启用状态_启用.equals(po.getStatus())) {
			validNotLastEnabled();
		}
	}

	private void validNotLastEnabled() {
		if (multiLangRepo.countByStatus(Constant.启用状态_启用) <= 1) {
			throw new BizException("至少需要保留一种启用中的语言");
		}
	}

	/** 语言编码格式校验：须能被 Locale.forLanguageTag 解析 */
	private Locale parseLangCode(String langCode) {
		try {
			Locale locale = Locale.forLanguageTag(langCode);
			if (StrUtil.isEmpty(locale.getLanguage())) {
				throw new BizException("语言编码格式不正确，如 zh-TW、en-US");
			}
			return locale;
		} catch (BizException e) {
			throw e;
		} catch (Exception e) {
			throw new BizException("语言编码格式不正确，如 zh-TW、en-US");
		}
	}

	/**
	 * 检测文案文件是否存在：不存在时返回提示（不阻断保存，运营可能先加语言再等发版）。
	 */
	private String buildMissingMessageTip(String langCode, Locale locale) {
		// 默认语言使用 messages.properties，无独立后缀文件
		if (Constant.默认语言_简体中文.equalsIgnoreCase(langCode)) {
			return null;
		}
		String fileName = "i18n/messages_" + locale.toString() + ".properties";
		if (Thread.currentThread().getContextClassLoader().getResource(fileName) != null) {
			return null;
		}
		return "未检测到文案文件 " + fileName + "，该语言的界面文案将回落默认语言";
	}

}

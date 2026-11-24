package com.exchange.content.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.exchange.common.i18n.LocaleUtils;
import com.exchange.common.utils.IdUtils;
import com.exchange.common.vo.PageResult;
import com.exchange.constants.Constant;
import com.exchange.content.domain.UsingHelp;
import com.exchange.content.domain.UsingHelpI18n;
import com.exchange.content.param.AddOrUpdateUsingHelpParam;
import com.exchange.content.param.UsingHelpQueryCondParam;
import com.exchange.content.repo.UsingHelpI18nRepo;
import com.exchange.content.repo.UsingHelpRepo;
import com.exchange.content.vo.UsingHelpAbstractVO;
import com.exchange.content.vo.UsingHelpDetailVO;
import com.exchange.content.vo.UsingHelpI18nVO;
import com.exchange.content.vo.UsingHelpVO;

import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class UsingHelpService {

	@Autowired
	private UsingHelpRepo usingHelpRepo;

	@Autowired
	private UsingHelpI18nRepo usingHelpI18nRepo;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void addOrUpdate(@Valid AddOrUpdateUsingHelpParam param) {
		UsingHelp usingHelp;
		// 新增
		if (StrUtil.isBlank(param.getId())) {
			usingHelp = param.convertToPo();
			usingHelpRepo.save(usingHelp);
		}
		// 修改
		else {
			usingHelp = usingHelpRepo.getOne(param.getId());
			BeanUtils.copyProperties(param, usingHelp);
			usingHelp.setLatelyUpdateTime(LocalDateTime.now());
			usingHelpRepo.save(usingHelp);
		}
		// 默认语言也写入译文表：保持 i18n 为「所有语言」的完整集合，后续切换默认语言只需改常量
		AddOrUpdateUsingHelpParam.UsingHelpI18nParam defaultContent = new AddOrUpdateUsingHelpParam.UsingHelpI18nParam();
		defaultContent.setLangCode(Constant.默认语言_简体中文);
		defaultContent.setTitle(param.getTitle());
		defaultContent.setContent(param.getContent());
		saveTranslations(usingHelp.getId(), defaultContent, param.getTranslations());
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void delById(@NotBlank String id) {
		usingHelpI18nRepo.deleteByUsingHelpId(id);
		usingHelpRepo.deleteById(id);
	}

	/** 后台详情：主表 + 各语言译文（编辑回填） */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public UsingHelpVO findById(@NotBlank String id) {
		UsingHelpVO vo = UsingHelpVO.convertFor(usingHelpRepo.getOne(id));
		vo.setTranslations(UsingHelpI18nVO.convertFor(usingHelpI18nRepo.findByUsingHelpId(id)));
		return vo;
	}

	/** 客户端详情：i18n 优先，未命中回落主表 */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public UsingHelpDetailVO getUsingHelpDetail(@NotBlank String id) {
		UsingHelpDetailVO vo = UsingHelpDetailVO.convertFor(usingHelpRepo.getOne(id));
		if (vo == null) {
			return null;
		}
		UsingHelpI18n i18n = usingHelpI18nRepo.findByUsingHelpIdAndLangCode(id,
				LocaleUtils.currentLocale().toLanguageTag());
		if (i18n != null) {
			vo.setTitle(i18n.getTitle());
			vo.setContent(i18n.getContent());
		}
		return vo;
	}

	/** 后台分页列表：含正文（查看/编辑用） */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<UsingHelpVO> findUsingHelpByPage(@Valid UsingHelpQueryCondParam param) {
		Page<UsingHelp> result = usingHelpRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(),
						Sort.by(Sort.Order.desc("latelyUpdateTime"))));
		return new PageResult<>(UsingHelpVO.convertFor(result.getContent()), param.getPageNum(),
				param.getPageSize(), result.getTotalElements());
	}

	/** 客户端分页列表：不含正文，按 Lang 批量替换标题（一次查询，条数不变，未命中回落主表） */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<UsingHelpAbstractVO> findUsingHelpAbstractByPage(@Valid UsingHelpQueryCondParam param) {
		Page<UsingHelp> result = usingHelpRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(),
						Sort.by(Sort.Order.desc("latelyUpdateTime"))));
		List<UsingHelpAbstractVO> vos = UsingHelpAbstractVO.convertFor(result.getContent());
		applyTranslation(vos);
		return new PageResult<>(vos, param.getPageNum(), param.getPageSize(), result.getTotalElements());
	}

	/**
	 * 保存译文：默认语言 + 其他语言统一进 i18n 表。
	 * <p>
	 * 先清理本次未提交的语言（默认语言始终保留），再 upsert。
	 */
	private void saveTranslations(String usingHelpId, AddOrUpdateUsingHelpParam.UsingHelpI18nParam defaultContent,
			List<AddOrUpdateUsingHelpParam.UsingHelpI18nParam> translations) {
		List<AddOrUpdateUsingHelpParam.UsingHelpI18nParam> valid = new ArrayList<>();
		if (defaultContent != null && StrUtil.isNotBlank(defaultContent.getTitle())) {
			valid.add(defaultContent);
		}
		if (translations != null) {
			for (AddOrUpdateUsingHelpParam.UsingHelpI18nParam t : translations) {
				// 标题为空视为未翻译；默认语言已单独处理
				if (StrUtil.isBlank(t.getTitle())) {
					continue;
				}
				if (Constant.默认语言_简体中文.equalsIgnoreCase(t.getLangCode())) {
					continue;
				}
				valid.add(t);
			}
		}
		List<String> langCodes = valid.stream().map(AddOrUpdateUsingHelpParam.UsingHelpI18nParam::getLangCode)
				.collect(Collectors.toList());
		if (langCodes.isEmpty()) {
			usingHelpI18nRepo.deleteByUsingHelpId(usingHelpId);
			return;
		}
		// 默认语言在 langCodes 中，不会被清理
		usingHelpI18nRepo.deleteByUsingHelpIdAndLangCodeNotIn(usingHelpId, langCodes);
		for (AddOrUpdateUsingHelpParam.UsingHelpI18nParam t : valid) {
			UsingHelpI18n i18n = usingHelpI18nRepo.findByUsingHelpIdAndLangCode(usingHelpId, t.getLangCode());
			if (i18n == null) {
				i18n = new UsingHelpI18n();
				i18n.setId(IdUtils.getId());
				i18n.setUsingHelpId(usingHelpId);
				i18n.setLangCode(t.getLangCode());
				i18n.setCreateTime(LocalDateTime.now());
			}
			i18n.setTitle(t.getTitle());
			i18n.setContent(t.getContent());
			i18n.setUpdateTime(LocalDateTime.now());
			usingHelpI18nRepo.save(i18n);
		}
	}

	/** 批量套用译文标题：一次查询，条数不变，未命中回落主表 */
	private void applyTranslation(List<UsingHelpAbstractVO> vos) {
		if (vos.isEmpty()) {
			return;
		}
		List<String> ids = vos.stream().map(UsingHelpAbstractVO::getId).collect(Collectors.toList());
		Map<String, String> titleMap = usingHelpI18nRepo
				.findByUsingHelpIdInAndLangCode(ids, LocaleUtils.currentLocale().toLanguageTag()).stream()
				.collect(Collectors.toMap(UsingHelpI18n::getUsingHelpId, UsingHelpI18n::getTitle));
		vos.forEach(vo -> Optional.ofNullable(titleMap.get(vo.getId())).ifPresent(vo::setTitle));
	}

}

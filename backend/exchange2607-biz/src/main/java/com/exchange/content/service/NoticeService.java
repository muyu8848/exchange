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
import com.exchange.content.domain.Notice;
import com.exchange.content.domain.NoticeI18n;
import com.exchange.content.param.AddOrUpdateNoticeParam;
import com.exchange.content.param.NoticeQueryCondParam;
import com.exchange.content.repo.NoticeI18nRepo;
import com.exchange.content.repo.NoticeRepo;
import com.exchange.content.vo.NoticeAbstractVO;
import com.exchange.content.vo.NoticeDetailVO;
import com.exchange.content.vo.NoticeI18nVO;
import com.exchange.content.vo.NoticeVO;

import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class NoticeService {

	@Autowired
	private NoticeRepo noticeRepo;

	@Autowired
	private NoticeI18nRepo noticeI18nRepo;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void addOrUpdateNotice(@Valid AddOrUpdateNoticeParam param) {
		Notice notice;
		// 新增
		if (StrUtil.isBlank(param.getId())) {
			notice = param.convertToPo();
			noticeRepo.save(notice);
		}
		// 修改
		else {
			notice = noticeRepo.getOne(param.getId());
			BeanUtils.copyProperties(param, notice);
			noticeRepo.save(notice);
		}
		// 默认语言也写入译文表：保持 i18n 为「所有语言」的完整集合，后续切换默认语言只需改常量
		AddOrUpdateNoticeParam.NoticeI18nParam defaultContent = new AddOrUpdateNoticeParam.NoticeI18nParam();
		defaultContent.setLangCode(Constant.默认语言_简体中文);
		defaultContent.setTitle(param.getTitle());
		defaultContent.setContent(param.getContent());
		saveTranslations(notice.getId(), defaultContent, param.getTranslations());
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void delById(@NotBlank String id) {
		noticeI18nRepo.deleteByNoticeId(id);
		noticeRepo.deleteById(id);
	}

	/** 后台详情：主表 + 各语言译文（编辑回填） */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public NoticeVO findById(@NotBlank String id) {
		NoticeVO vo = NoticeVO.convertFor(noticeRepo.getOne(id));
		vo.setTranslations(NoticeI18nVO.convertFor(noticeI18nRepo.findByNoticeId(id)));
		return vo;
	}

	/** 客户端详情：i18n 优先，未命中回落主表 */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public NoticeDetailVO getNoticeDetail(@NotBlank String id) {
		NoticeDetailVO vo = NoticeDetailVO.convertFor(noticeRepo.getOne(id));
		if (vo == null) {
			return null;
		}
		NoticeI18n i18n = noticeI18nRepo.findByNoticeIdAndLangCode(id,
				LocaleUtils.currentLocale().toLanguageTag());
		if (i18n != null) {
			vo.setTitle(i18n.getTitle());
			vo.setContent(i18n.getContent());
		}
		return vo;
	}

	/** 客户端分页列表：只返回摘要，按 Lang 批量替换标题（一次查询，条数不变，未命中回落主表） */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<NoticeAbstractVO> findNoticeAbstractByPage(@Valid NoticeQueryCondParam param) {
		Page<Notice> result = noticeRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("publishTime"))));
		List<NoticeAbstractVO> vos = NoticeAbstractVO.convertFor(result.getContent());
		applyTranslation(vos);
		return new PageResult<>(vos, param.getPageNum(), param.getPageSize(), result.getTotalElements());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<NoticeVO> findByPage(@Valid NoticeQueryCondParam param) {
		Page<Notice> result = noticeRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("publishTime"))));
		PageResult<NoticeVO> pageResult = new PageResult<>(NoticeVO.convertFor(result.getContent()), param.getPageNum(),
				param.getPageSize(), result.getTotalElements());
		return pageResult;
	}

	/**
	 * 保存译文：默认语言 + 其他语言统一进 i18n 表。
	 * <p>
	 * 先清理本次未提交的语言（默认语言始终保留），再 upsert。
	 */
	private void saveTranslations(String noticeId, AddOrUpdateNoticeParam.NoticeI18nParam defaultContent,
			List<AddOrUpdateNoticeParam.NoticeI18nParam> translations) {
		List<AddOrUpdateNoticeParam.NoticeI18nParam> valid = new ArrayList<>();
		if (defaultContent != null && StrUtil.isNotBlank(defaultContent.getTitle())) {
			valid.add(defaultContent);
		}
		if (translations != null) {
			for (AddOrUpdateNoticeParam.NoticeI18nParam t : translations) {
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
		List<String> langCodes = valid.stream().map(AddOrUpdateNoticeParam.NoticeI18nParam::getLangCode)
				.collect(Collectors.toList());
		if (langCodes.isEmpty()) {
			noticeI18nRepo.deleteByNoticeId(noticeId);
			return;
		}
		// 默认语言在 langCodes 中，不会被清理
		noticeI18nRepo.deleteByNoticeIdAndLangCodeNotIn(noticeId, langCodes);
		for (AddOrUpdateNoticeParam.NoticeI18nParam t : valid) {
			NoticeI18n i18n = noticeI18nRepo.findByNoticeIdAndLangCode(noticeId, t.getLangCode());
			if (i18n == null) {
				i18n = new NoticeI18n();
				i18n.setId(IdUtils.getId());
				i18n.setNoticeId(noticeId);
				i18n.setLangCode(t.getLangCode());
				i18n.setCreateTime(LocalDateTime.now());
			}
			i18n.setTitle(t.getTitle());
			i18n.setContent(t.getContent());
			i18n.setUpdateTime(LocalDateTime.now());
			noticeI18nRepo.save(i18n);
		}
	}

	/** 批量套用译文标题：一次查询，条数不变，未命中回落主表 */
	private void applyTranslation(List<NoticeAbstractVO> vos) {
		if (vos.isEmpty()) {
			return;
		}
		List<String> ids = vos.stream().map(NoticeAbstractVO::getId).collect(Collectors.toList());
		Map<String, String> titleMap = noticeI18nRepo
				.findByNoticeIdInAndLangCode(ids, LocaleUtils.currentLocale().toLanguageTag()).stream()
				.collect(Collectors.toMap(NoticeI18n::getNoticeId, NoticeI18n::getTitle));
		vos.forEach(vo -> Optional.ofNullable(titleMap.get(vo.getId())).ifPresent(vo::setTitle));
	}

}

package com.exchange.member.service;

import java.time.LocalDateTime;
import java.util.List;

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

import com.exchange.common.vo.PageResult;
import com.exchange.constants.Constant;
import com.exchange.member.domain.CountryAreaCode;
import com.exchange.member.param.AddOrUpdateCountryAreaCodeParam;
import com.exchange.member.param.CountryAreaCodeQueryCondParam;
import com.exchange.member.repo.CountryAreaCodeRepo;
import com.exchange.member.vo.CountryAreaCodeOptionVO;
import com.exchange.member.vo.CountryAreaCodeVO;

import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class CountryAreaCodeService {

	@Autowired
	private CountryAreaCodeRepo repo;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public CountryAreaCodeVO findById(@NotBlank String id) {
		return CountryAreaCodeVO.convertFor(repo.getOne(id));
	}

	/** 管理端分页列表（含停用），按 sortNum 升序 */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<CountryAreaCodeVO> findByPage(CountryAreaCodeQueryCondParam param) {
		Page<CountryAreaCode> result = repo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.asc("sortNum"))));
		return new PageResult<>(CountryAreaCodeVO.convertFor(result.getContent()), param.getPageNum(),
				param.getPageSize(), result.getTotalElements());
	}

	/** 后台全量列表（含停用，供筛选使用，按 sortNum 升序） */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<CountryAreaCodeVO> findAll() {
		return CountryAreaCodeVO.convertFor(repo.findAllByOrderBySortNumAsc());
	}

	/** 客户端列表（仅启用，按 sortNum 升序） */
	public List<CountryAreaCodeOptionVO> findEnabledList() {
		return CountryAreaCodeOptionVO
				.convertFor(repo.findByStatusOrderBySortNumAsc(Constant.启用状态_启用));
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void del(@NotBlank String id) {
		repo.deleteById(id);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void addOrUpdate(@Valid AddOrUpdateCountryAreaCodeParam param) {
		if (StrUtil.isBlank(param.getId())) {
			repo.save(param.convertToPo());
			return;
		}
		CountryAreaCode po = repo.getOne(param.getId());
		BeanUtils.copyProperties(param, po);
		po.setUpdateTime(LocalDateTime.now());
		repo.save(po);
	}

}

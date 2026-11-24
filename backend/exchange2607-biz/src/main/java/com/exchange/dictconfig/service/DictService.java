package com.exchange.dictconfig.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.exception.BizException;
import com.exchange.common.vo.PageResult;
import com.exchange.constants.Constant;
import com.exchange.dictconfig.domain.DictItem;
import com.exchange.dictconfig.domain.DictType;
import com.exchange.dictconfig.param.AddOrUpdateDictTypeParam;
import com.exchange.dictconfig.param.DictDataParam;
import com.exchange.dictconfig.param.DictTypeQueryCondParam;
import com.exchange.dictconfig.param.UpdateDictDataParam;
import com.exchange.dictconfig.repo.DictItemRepo;
import com.exchange.dictconfig.repo.DictTypeRepo;
import com.exchange.dictconfig.vo.DictItemVO;
import com.exchange.dictconfig.vo.DictTypeVO;

import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class DictService {

	@Autowired
	private DictItemRepo dictItemRepo;

	@Autowired
	private DictTypeRepo dictTypeRepo;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateDictData(@Valid UpdateDictDataParam param) {
		Set<String> dictItemCodeSet = new HashSet<String>();
		for (DictDataParam dictDataParam : param.getDictDatas()) {
			if (!dictItemCodeSet.add(dictDataParam.getDictItemCode())) {
				throw new BizException("字典项code不能重复");
			}
		}

		// 提前查出类型：重建字典项时要把 dictTypeCode 一并写入冗余字段
		DictType dictType = dictTypeRepo.getOne(param.getDictTypeId());

		List<DictItem> oldDictItems = dictItemRepo.findByDictTypeIdOrderBySortNum(param.getDictTypeId());
		dictItemRepo.deleteAll(oldDictItems);

		Integer sortNum = 1;
		for (DictDataParam dictDataParam : param.getDictDatas()) {
			DictItem dictItem = dictDataParam.convertToPo();
			dictItem.setDictTypeId(param.getDictTypeId());
			dictItem.setDictTypeCode(dictType.getDictTypeCode());
			dictItem.setSortNum(sortNum);
			dictItemRepo.save(dictItem);
			sortNum++;
		}
		dictType.setLastUpdateTime(LocalDateTime.now());
		dictTypeRepo.save(dictType);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void delDictType(@NotBlank String id) {
		DictType dictType = dictTypeRepo.getOne(id);
		List<DictItem> dictItems = dictItemRepo.findByDictTypeIdOrderBySortNum(id);
		dictItemRepo.deleteAll(dictItems);
		dictTypeRepo.delete(dictType);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public DictTypeVO findDictTypeById(@NotBlank String id) {
		return DictTypeVO.convertFor(dictTypeRepo.getOne(id));
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void addOrUpdateDictType(@Valid AddOrUpdateDictTypeParam param) {
		if (StrUtil.isBlank(param.getId())) {
			DictType dictType = param.convertToPo();
			dictTypeRepo.save(dictType);
			return;
		}
		DictType dictType = dictTypeRepo.getOne(param.getId());
		String oldDictTypeCode = dictType.getDictTypeCode();

		BeanUtils.copyProperties(param, dictType);
		dictType.setLastUpdateTime(LocalDateTime.now());
		dictTypeRepo.save(dictType);

		// 字典编码变更（BeanUtils 会覆盖该字段）→ 同步字典项的冗余字段，避免数据失真
		if (!StrUtil.equals(oldDictTypeCode, dictType.getDictTypeCode())) {
			List<DictItem> dictItems = dictItemRepo.findByDictTypeIdOrderBySortNum(dictType.getId());
			for (DictItem dictItem : dictItems) {
				dictItem.setDictTypeCode(dictType.getDictTypeCode());
			}
			dictItemRepo.saveAll(dictItems);
		}
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<DictTypeVO> findDictTypeByPage(@Valid DictTypeQueryCondParam param) {
		Specification<DictType> spec = new Specification<DictType>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<DictType> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotBlank(param.getDictTypeCode())) {
					predicates.add(builder.like(root.get("dictTypeCode"), "%" + param.getDictTypeCode() + "%"));
				}
				if (StrUtil.isNotBlank(param.getDictTypeName())) {
					predicates.add(builder.like(root.get("dictTypeName"), "%" + param.getDictTypeName() + "%"));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<DictType> result = dictTypeRepo.findAll(spec, PageRequest.of(param.getPageNum() - 1, param.getPageSize(),
				Sort.by(Sort.Order.desc("lastUpdateTime"))));
		PageResult<DictTypeVO> pageResult = new PageResult<>(DictTypeVO.convertFor(result.getContent()),
				param.getPageNum(), param.getPageSize(), result.getTotalElements());
		return pageResult;
	}

	@Cacheable(value = Constant.当前项目 + "dictItem", key = "#dictTypeCode + #dictItemCode")
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public DictItemVO findDictItemByDictTypeCodeAndDictItemCode(String dictTypeCode, String dictItemCode) {
		return DictItemVO
				.convertFor(dictItemRepo.findByDictTypeDictTypeCodeAndDictItemCode(dictTypeCode, dictItemCode));
	}

	@Cacheable(value = Constant.当前项目 + "dictItems", key = "#dictTypeCode")
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<DictItemVO> findDictItemByDictTypeCode(@NotBlank String dictTypeCode) {
		return DictItemVO.convertFor(dictItemRepo.findByDictTypeDictTypeCodeOrderBySortNum(dictTypeCode));
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<DictItemVO> findDictItemByDictTypeId(@NotBlank String dictTypeId) {
		return DictItemVO.convertFor(dictItemRepo.findByDictTypeIdOrderBySortNum(dictTypeId));
	}

}

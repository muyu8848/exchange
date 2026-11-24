package com.exchange.currency.service;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.exception.BizException;
import com.exchange.common.vo.PageResult;
import com.exchange.constants.Constant;
import com.exchange.currency.domain.ChainNetwork;
import com.exchange.currency.param.AddOrUpdateChainNetworkParam;
import com.exchange.currency.param.ChainNetworkQueryCondParam;
import com.exchange.currency.repo.ChainNetworkRepo;
import com.exchange.currency.vo.ChainNetworkVO;

import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class ChainNetworkService {

	@Autowired
	private ChainNetworkRepo chainNetworkRepo;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<ChainNetworkVO> findAll(ChainNetworkQueryCondParam param) {
		List<ChainNetwork> result = chainNetworkRepo.findAll(param.buildSpecification(),
				Sort.by(Sort.Order.asc("sortNum")));
		return ChainNetworkVO.convertFor(result);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<ChainNetworkVO> findChainNetworkByPage(ChainNetworkQueryCondParam param) {
		Page<ChainNetwork> result = chainNetworkRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.asc("sortNum"))));
		return new PageResult<>(ChainNetworkVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public ChainNetworkVO findById(@NotBlank String id) {
		ChainNetwork po = chainNetworkRepo.findById(id).orElseThrow(() -> new BizException("区块链网络不存在"));
		return ChainNetworkVO.convertFor(po);
	}

	@Cacheable(value = Constant.当前项目 + "chainNetwork", key = "#chainType")
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public ChainNetworkVO findChainNetworkByChainType(@NotBlank String chainType) {
		ChainNetwork po = chainNetworkRepo.findTopByChainTypeAndDeletedFlagIsFalse(chainType);
		return ChainNetworkVO.convertFor(po);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void addOrUpdate(@Valid AddOrUpdateChainNetworkParam param) {
		if (StrUtil.isBlank(param.getId())) {
			ChainNetwork exist = chainNetworkRepo.findTopByChainTypeAndDeletedFlagIsFalse(param.getChainType());
			if (exist != null) {
				throw new BizException("链类型已存在");
			}

			ChainNetwork po = param.convertToPo();
			chainNetworkRepo.save(po);
		} else {
			ChainNetwork exist = chainNetworkRepo.findTopByChainTypeAndDeletedFlagIsFalse(param.getChainType());
			if (exist != null && !exist.getId().equals(param.getId())) {
				throw new BizException("链类型已存在");
			}

			ChainNetwork po = chainNetworkRepo.getOne(param.getId());
			BeanUtils.copyProperties(param, po);
			po.setUpdateTime(LocalDateTime.now());
			chainNetworkRepo.save(po);
		}
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void del(@NotBlank String id) {
		ChainNetwork po = chainNetworkRepo.findById(id).orElseThrow(() -> new BizException("区块链网络不存在"));
		po.setDeletedFlag(true);
		po.setDeletedTime(LocalDateTime.now());
		chainNetworkRepo.save(po);
	}
}

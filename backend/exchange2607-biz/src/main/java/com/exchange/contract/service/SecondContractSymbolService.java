package com.exchange.contract.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.redisson.api.RedissonClient;
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
import com.exchange.common.vo.PageResult;
import com.exchange.constants.Constant;
import com.exchange.contract.domain.SecondContractPeriodConfig;
import com.exchange.contract.domain.SecondContractSymbol;
import com.exchange.contract.param.AddOrUpdateSecondContractSymbolParam;
import com.exchange.contract.param.SecondContractPeriodConfigParam;
import com.exchange.contract.param.SecondContractSymbolQueryCondParam;
import com.exchange.contract.param.UpdateSecondContractPeriodConfigParam;
import com.exchange.contract.repo.SecondContractPeriodConfigRepo;
import com.exchange.contract.repo.SecondContractSymbolRepo;
import com.exchange.contract.vo.EnableSecondContractSymbolVO;
import com.exchange.contract.vo.SecondContractPeriodConfigVO;
import com.exchange.contract.vo.SecondContractSymbolVO;
import com.zengtengpeng.annotation.Lock;

import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class SecondContractSymbolService {

	@Autowired
	private RedissonClient redissonClient;

	@Autowired
	private SecondContractSymbolRepo secondContractSymbolRepo;

	@Autowired
	private SecondContractPeriodConfigRepo secondContractPeriodConfigRepo;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void copyPeriodConfig(@NotBlank String source, @NotBlank String target) {
		if (source.equals(target)) {
			return;
		}
		List<SecondContractPeriodConfig> oldConfigs = secondContractPeriodConfigRepo
				.findBySymbolCodeOrderByPeriodAsc(target);
		secondContractPeriodConfigRepo.deleteAll(oldConfigs);

		List<SecondContractPeriodConfig> sourceConfigs = secondContractPeriodConfigRepo
				.findBySymbolCodeOrderByPeriodAsc(source);
		for (SecondContractPeriodConfig sourceConfig : sourceConfigs) {
			SecondContractPeriodConfig targetConfig = sourceConfig.copy(target);
			secondContractPeriodConfigRepo.save(targetConfig);
		}
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void updateSecondContractPeriodConfig(@Valid UpdateSecondContractPeriodConfigParam param) {
		Set<Integer> periodSet = new HashSet<Integer>();
		for (SecondContractPeriodConfigParam configParam : param.getConfigs()) {
			if (!periodSet.add(configParam.getPeriod())) {
				throw new BizException("周期不能重复");
			}
			// 赔率为含本金的返还倍数，须大于 1，否则用户方向正确也会亏损
			if (configParam.getOdds() == null
					|| configParam.getOdds().compareTo(BigDecimal.ONE) <= 0) {
				throw new BizException("周期{0}的赔率必须大于1", configParam.getPeriod());
			}
		}

		List<SecondContractPeriodConfig> oldConfigs = secondContractPeriodConfigRepo
				.findBySymbolCodeOrderByPeriodAsc(param.getSymbolCode());
		secondContractPeriodConfigRepo.deleteAll(oldConfigs);

		for (SecondContractPeriodConfigParam configParam : param.getConfigs()) {
			SecondContractPeriodConfig confiig = configParam.convertToPo(param.getSymbolCode());
			secondContractPeriodConfigRepo.save(confiig);
		}
		SecondContractSymbol symbol = secondContractSymbolRepo
				.findTopBySymbolCodeAndDeletedFlagFalse(param.getSymbolCode());
		symbol.setUpdateTime(LocalDateTime.now());
		secondContractSymbolRepo.save(symbol);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<SecondContractPeriodConfigVO> findSecondContractPeriodConfig(@NotBlank String symbolCode) {
		return SecondContractPeriodConfigVO
				.convertFor(secondContractPeriodConfigRepo.findBySymbolCodeOrderByPeriodAsc(symbolCode));
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public EnableSecondContractSymbolVO findSymbolInfo(String symbolCode) {
		SecondContractSymbol symbol = secondContractSymbolRepo.findTopBySymbolCodeAndDeletedFlagFalse(symbolCode);
		return EnableSecondContractSymbolVO.convertFor(symbol);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<EnableSecondContractSymbolVO> findEnableSymbol() {
		SecondContractSymbolQueryCondParam param = new SecondContractSymbolQueryCondParam();
		param.setStatus(Constant.启用状态_启用);
		List<SecondContractSymbol> result = secondContractSymbolRepo.findAll(param.buildSpecification(),
				Sort.by(Sort.Order.asc("sortNum")));
		return EnableSecondContractSymbolVO.convertFor(result);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<String> findEnableSymbolCode() {
		SecondContractSymbolQueryCondParam param = new SecondContractSymbolQueryCondParam();
		param.setStatus(Constant.启用状态_启用);
		List<SecondContractSymbol> result = secondContractSymbolRepo.findAll(param.buildSpecification(),
				Sort.by(Sort.Order.asc("sortNum")));
		return result.stream().map(SecondContractSymbol::getSymbolCode).collect(Collectors.toList());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<SecondContractSymbolVO> findAll() {
		SecondContractSymbolQueryCondParam param = new SecondContractSymbolQueryCondParam();
		List<SecondContractSymbol> result = secondContractSymbolRepo.findAll(param.buildSpecification(),
				Sort.by(Sort.Order.asc("sortNum")));
		return SecondContractSymbolVO.convertFor(result);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<SecondContractSymbolVO> findByPage(SecondContractSymbolQueryCondParam param) {
		Page<SecondContractSymbol> result = secondContractSymbolRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.asc("sortNum"))));
		PageResult<SecondContractSymbolVO> pageResult = new PageResult<>(
				SecondContractSymbolVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
		return pageResult;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public SecondContractSymbolVO findById(@NotBlank String id) {
		return SecondContractSymbolVO.convertFor(secondContractSymbolRepo.findById(id).orElse(null));
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void del(@NotBlank String id) {
		SecondContractSymbol po = secondContractSymbolRepo.findById(id).orElse(null);
		po.deleted();
		secondContractSymbolRepo.save(po);
	}

	@Lock(keys = "'addOrUpdateSecondContractSymbol' + #param.symbolCode")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void addOrUpdate(@Valid AddOrUpdateSecondContractSymbolParam param) {
		if (StrUtil.isBlank(param.getId())) {
			SecondContractSymbol exist = secondContractSymbolRepo
					.findTopBySymbolCodeAndDeletedFlagFalse(param.getSymbolCode());
			if (exist != null) {
				throw new BizException("交易对已存在");
			}
			SecondContractSymbol po = param.convertToPo();
			secondContractSymbolRepo.save(po);
			redissonClient.getTopic(Constant.监控新交易对).publish(po.getSymbolCode());
		} else {
			SecondContractSymbol exist = secondContractSymbolRepo
					.findTopBySymbolCodeAndDeletedFlagFalse(param.getSymbolCode());
			if (exist != null && !exist.getId().equals(param.getId())) {
				throw new BizException("交易对已存在");
			}

			SecondContractSymbol po = secondContractSymbolRepo.findById(param.getId()).orElse(null);
			BeanUtils.copyProperties(param, po);
			po.setUpdateTime(LocalDateTime.now());
			secondContractSymbolRepo.save(po);
		}
	}

}

package com.exchange.currency.service;

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

import com.exchange.common.exception.BizException;
import com.exchange.common.vo.PageResult;
import com.exchange.currency.domain.Currency;
import com.exchange.currency.param.AddOrUpdateCurrencyParam;
import com.exchange.currency.param.CurrencyQueryCondParam;
import com.exchange.currency.repo.CurrencyRepo;
import com.exchange.currency.vo.CurrencyOptionVO;
import com.exchange.currency.vo.CurrencyVO;
import com.zengtengpeng.annotation.Lock;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class CurrencyService {

	@Autowired
	private CurrencyRepo currencyRepo;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<CurrencyVO> findByPage(CurrencyQueryCondParam param) {
		Page<Currency> result = currencyRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.asc("sortNum"))));
		PageResult<CurrencyVO> pageResult = new PageResult<>(CurrencyVO.convertFor(result.getContent()),
				param.getPageNum(), param.getPageSize(), result.getTotalElements());
		return pageResult;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<CurrencyVO> findAll(CurrencyQueryCondParam param) {
		List<Currency> result = currencyRepo.findAll(param.buildSpecification(), Sort.by(Sort.Order.asc("sortNum")));
		return CurrencyVO.convertFor(result);
	}

	/** 客户端币种选项（全部未删除）：全局低频配置，客户端启动预取并缓存 */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<CurrencyOptionVO> findAllCurrencyOption() {
		CurrencyQueryCondParam param = new CurrencyQueryCondParam();
		List<Currency> result = currencyRepo.findAll(param.buildSpecification(), Sort.by(Sort.Order.asc("sortNum")));
		return CurrencyOptionVO.convertFor(result);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public CurrencyVO findById(@NotBlank String id) {
		return CurrencyVO.convertFor(currencyRepo.findById(id).orElse(null));
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void del(@NotBlank String id) {
		Currency po = currencyRepo.getOne(id);
		po.deleted();
		currencyRepo.save(po);
	}

	@Lock(keys = "'addOrUpdateCurrency' + #param.currencyCode")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void addOrUpdate(@Valid AddOrUpdateCurrencyParam param) {
		if (!Validator.isGeneral(param.getCurrencyCode())) {
			throw new BizException("币种代码只能是字母加数字");
		}
		if (StrUtil.isBlank(param.getId())) {
			Currency exist = currencyRepo.findTopByCurrencyCodeAndDeletedFlagFalse(param.getCurrencyCode());
			if (exist != null) {
				throw new BizException("币种代码已存在");
			}

			Currency po = param.convertToPo();
			currencyRepo.save(po);
		} else {
			Currency exist = currencyRepo.findTopByCurrencyCodeAndDeletedFlagFalse(param.getCurrencyCode());
			if (exist != null && !exist.getId().equals(param.getId())) {
				throw new BizException("币种代码已存在");
			}

			Currency po = currencyRepo.getOne(param.getId());
			BeanUtils.copyProperties(param, po);
			po.setUpdateTime(LocalDateTime.now());
			currencyRepo.save(po);
		}
	}

}

package com.exchange.withdraw.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.exchange.constants.Constant;
import com.exchange.currency.repo.ChainNetworkRepo;
import com.exchange.withdraw.domain.WithdrawChannel;
import com.exchange.withdraw.param.AddOrUpdateWithdrawChannelParam;
import com.exchange.withdraw.param.WithdrawChannelQueryCondParam;
import com.exchange.withdraw.repo.WithdrawChannelRepo;
import com.exchange.withdraw.repo.WithdrawOrderRepo;
import com.exchange.withdraw.vo.EnabledWithdrawChannelVO;
import com.exchange.withdraw.vo.WithdrawChannelVO;

import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class WithdrawChannelService {

	@Autowired
	private WithdrawChannelRepo withdrawChannelRepo;

	@Autowired
	private ChainNetworkRepo chainNetworkRepo;

	@Autowired
	private WithdrawOrderRepo withdrawOrderRepo;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public WithdrawChannelVO findById(@NotBlank String id) {
		return WithdrawChannelVO.convertFor(withdrawChannelRepo.getOne(id));
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<WithdrawChannelVO> findAll(WithdrawChannelQueryCondParam param) {
		List<WithdrawChannel> result = withdrawChannelRepo.findAll(param.buildSpecification(),
				Sort.by(Sort.Order.asc("sortNum"), Sort.Order.desc("updateTime")));
		return WithdrawChannelVO.convertFor(result);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<WithdrawChannelVO> findByPage(WithdrawChannelQueryCondParam param) {
		Page<WithdrawChannel> result = withdrawChannelRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.asc("sortNum"))));
		return new PageResult<>(WithdrawChannelVO.convertFor(result.getContent()), param.getPageNum(),
				param.getPageSize(), result.getTotalElements());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<EnabledWithdrawChannelVO> findEnabledChannels(WithdrawChannelQueryCondParam param) {
		param.setStatus(Constant.启用状态_启用);
		List<WithdrawChannel> result = withdrawChannelRepo.findAll(param.buildSpecification(),
				Sort.by(Sort.Order.asc("sortNum")));
		List<EnabledWithdrawChannelVO> vos = new ArrayList<>();
		for (WithdrawChannel channel : result) {
			vos.add(EnabledWithdrawChannelVO.convertFor(channel));
		}
		return vos;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void del(@NotBlank String id) {
		WithdrawChannel channel = withdrawChannelRepo.getOne(id);
		long pendingCount = withdrawOrderRepo.countByChainTypeAndAssetCodeAndStatusIn(channel.getChainType(),
				channel.getAssetCode(), Arrays.asList(Constant.提币订单状态_待审核, Constant.提币订单状态_审核通过));
		if (pendingCount > 0) {
			throw new BizException("该通道存在未完结的提币订单，无法删除");
		}
		channel.deleted();
		withdrawChannelRepo.save(channel);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void addOrUpdate(@Valid AddOrUpdateWithdrawChannelParam param) {
		String chainType = param.getChainType();
		if (chainNetworkRepo.findTopByChainTypeAndDeletedFlagIsFalse(chainType) == null) {
			throw new BizException("链类型不存在，请先在区块链网络配置");
		}
		if (StrUtil.isBlank(param.getId())) {
			WithdrawChannel exists = withdrawChannelRepo.findTopByChainTypeAndAssetCodeAndDeletedFlagIsFalse(chainType,
					param.getAssetCode());
			if (exists != null) {
				throw new BizException("该链类型与币种代码组合已配置提币通道");
			}
			WithdrawChannel po = param.convertToPo();
			withdrawChannelRepo.save(po);
			return;
		}
		WithdrawChannel channel = withdrawChannelRepo.getOne(param.getId());
		WithdrawChannel exists = withdrawChannelRepo.findTopByChainTypeAndAssetCodeAndDeletedFlagIsFalse(chainType,
				param.getAssetCode());
		if (exists != null && !exists.getId().equals(channel.getId())) {
			throw new BizException("该链类型与币种代码组合已配置提币通道");
		}
		BeanUtils.copyProperties(param, channel);
		channel.setUpdateTime(LocalDateTime.now());
		withdrawChannelRepo.save(channel);
	}

}

package com.exchange.deposit.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.exception.BizException;
import com.exchange.common.utils.IdUtils;
import com.exchange.common.vo.PageResult;
import com.exchange.constants.Constant;
import com.exchange.currency.domain.ChainNetwork;
import com.exchange.currency.repo.ChainNetworkRepo;
import com.exchange.deposit.domain.DepositAddress;
import com.exchange.deposit.param.AddDepositAddressParam;
import com.exchange.deposit.param.BatchImportDepositAddressParam;
import com.exchange.deposit.param.DepositAddressQueryCondParam;
import com.exchange.deposit.repo.DepositAddressRepo;
import com.exchange.deposit.vo.BatchImportDepositAddressResultVO;
import com.exchange.deposit.vo.DepositAddressVO;

import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class DepositAddressService {

	@Autowired
	private DepositAddressRepo depositAddressRepo;

	@Autowired
	private ChainNetworkRepo chainNetworkRepo;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<DepositAddressVO> findDepositAddressByPage(@Valid DepositAddressQueryCondParam param) {
		Page<DepositAddress> result = depositAddressRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("createTime"))));
		List<DepositAddressVO> vos = DepositAddressVO.convertFor(result.getContent());
		return new PageResult<>(vos, param.getPageNum(), param.getPageSize(), result.getTotalElements());
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void del(@NotBlank String id) {
		DepositAddress address = depositAddressRepo.getOne(id);
		if (Constant.充币地址状态_占用中.equals(address.getStatus())) {
			throw new BizException("地址占用中，无法删除");
		}
		address.deleted();
		depositAddressRepo.save(address);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void add(@Valid AddDepositAddressParam param) {
		ChainNetwork network = chainNetworkRepo
				.findTopByChainTypeAndDeletedFlagIsFalse(param.getChainType().trim());
		if (network == null) {
			throw new BizException("区块链网络不存在");
		}
		if (Constant.充币地址状态_占用中.equals(param.getStatus())) {
			throw new BizException("不能手动设为占用中");
		}
		DepositAddress exists = depositAddressRepo.findTopByChainTypeAndAddressAndDeletedFlagIsFalse(
				param.getChainType().trim(), param.getAddress().trim());
		if (exists != null) {
			throw new BizException("地址已存在");
		}
		depositAddressRepo.save(param.convertToPo());
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public BatchImportDepositAddressResultVO batchImport(@Valid BatchImportDepositAddressParam param) {
		ChainNetwork network = chainNetworkRepo
				.findTopByChainTypeAndDeletedFlagIsFalse(param.getChainType().trim());
		if (network == null) {
			throw new BizException("区块链网络不存在");
		}
		String[] lines = param.getAddresses().split("\\r?\\n");
		Set<String> seen = new HashSet<>();
		List<DepositAddress> toSave = new ArrayList<>();
		int skipExistsCount = 0;
		int skipDuplicateCount = 0;
		LocalDateTime now = LocalDateTime.now();
		for (String line : lines) {
			String address = StrUtil.trim(line);
			if (StrUtil.isBlank(address)) {
				continue;
			}
			if (!seen.add(address)) {
				skipDuplicateCount++;
				continue;
			}
			DepositAddress exists = depositAddressRepo
					.findTopByChainTypeAndAddressAndDeletedFlagIsFalse(network.getChainType(), address);
			if (exists != null) {
				skipExistsCount++;
				continue;
			}
			DepositAddress po = new DepositAddress();
			po.setId(IdUtils.getId());
			po.setChainType(network.getChainType());
			po.setAddress(address);
			po.setStatus(Constant.充币地址状态_空闲);
			po.setCreateTime(now);
			po.setDeletedFlag(false);
			po.setTotalReceived(BigDecimal.ZERO);
			toSave.add(po);
		}
		if (toSave.isEmpty()) {
			throw new BizException("没有可导入的地址");
		}
		depositAddressRepo.saveAll(toSave);
		BatchImportDepositAddressResultVO result = new BatchImportDepositAddressResultVO();
		result.setSuccessCount(toSave.size());
		result.setSkipExistsCount(skipExistsCount);
		result.setSkipDuplicateCount(skipDuplicateCount);
		return result;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void release(@NotBlank String id) {
		DepositAddress address = depositAddressRepo.getOne(id);
		if (!Constant.充币地址状态_占用中.equals(address.getStatus())) {
			throw new BizException("仅占用中的地址可释放");
		}
		address.setStatus(Constant.充币地址状态_空闲);
		address.setCurrentOrderId(null);
		depositAddressRepo.save(address);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void disable(@NotBlank String id) {
		DepositAddress address = depositAddressRepo.getOne(id);
		if (!Constant.充币地址状态_空闲.equals(address.getStatus())) {
			throw new BizException("仅空闲地址可停用");
		}
		address.setStatus(Constant.充币地址状态_停用);
		depositAddressRepo.save(address);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void enable(@NotBlank String id) {
		DepositAddress address = depositAddressRepo.getOne(id);
		if (!Constant.充币地址状态_停用.equals(address.getStatus())) {
			throw new BizException("仅停用地址可启用");
		}
		address.setStatus(Constant.充币地址状态_空闲);
		depositAddressRepo.save(address);
	}

}

package com.exchange.member.service;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;

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
import com.exchange.log.domain.MemberAssetChangeLog;
import com.exchange.log.repo.MemberAssetChangeLogRepo;
import com.exchange.member.domain.AssetTransferOrder;
import com.exchange.member.domain.MemberAccount;
import com.exchange.member.param.AssetTransferQueryCondParam;
import com.exchange.member.param.AssetTransferSubmitParam;
import com.exchange.member.repo.AssetTransferOrderRepo;
import com.exchange.member.repo.MemberAccountRepo;
import com.exchange.member.vo.AssetTransferOrderVO;
import com.exchange.member.vo.ExportAssetTransferOrderVO;

@Validated
@Service
public class AssetTransferService {

	@Autowired
	private AssetTransferOrderRepo assetTransferOrderRepo;

	@Autowired
	private MemberAccountRepo memberAccountRepo;

	@Autowired
	private MemberAssetBiz memberAssetBiz;

	@Autowired
	private MemberAssetChangeLogRepo memberAssetChangeLogRepo;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public String transfer(@Valid AssetTransferSubmitParam param) {
		MemberAccount member = memberAccountRepo.getOne(param.getMemberAccountId());
		member.validBasicRisk();

		String fromType = param.getFromAccountType();
		String toType = param.getToAccountType();
		String assetCode = param.getAssetCode();
		BigDecimal amount = param.getAmount();

		if (fromType.equals(toType)) {
			throw new BizException("划出和划入账户不能相同");
		}
		if (!Constant.账户类型_现货.equals(fromType) && !Constant.账户类型_合约.equals(fromType)
				&& !Constant.账户类型_法币.equals(fromType)) {
			throw new BizException("不支持的划出账户类型");
		}
		if (!Constant.账户类型_现货.equals(toType) && !Constant.账户类型_合约.equals(toType) && !Constant.账户类型_法币.equals(toType)) {
			throw new BizException("不支持的划入账户类型");
		}
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BizException("划转金额必须大于0");
		}

		AssetTransferOrder order = AssetTransferOrder.build(param.getMemberAccountId(), fromType, toType, assetCode,
				amount);

		BigDecimal balanceBeforeOut = memberAssetBiz.reduceAvailable(param.getMemberAccountId(), fromType, assetCode,
				amount);
		BigDecimal balanceBeforeIn = memberAssetBiz.addAvailable(param.getMemberAccountId(), toType, assetCode, amount);

		assetTransferOrderRepo.save(order);

		MemberAssetChangeLog outLog = MemberAssetChangeLog.buildWithAssetTransferOut(param.getMemberAccountId(),
				fromType, assetCode, balanceBeforeOut, amount, order.getOrderNo(), toType);
		MemberAssetChangeLog inLog = MemberAssetChangeLog.buildWithAssetTransferIn(param.getMemberAccountId(), toType,
				assetCode, balanceBeforeIn, amount, order.getOrderNo(), fromType);
		memberAssetChangeLogRepo.save(outLog);
		memberAssetChangeLogRepo.save(inLog);

		return order.getOrderNo();
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<AssetTransferOrderVO> findByPage(@Valid AssetTransferQueryCondParam param) {
		Page<AssetTransferOrder> result = assetTransferOrderRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("createTime"))));
		return new PageResult<>(AssetTransferOrderVO.convertFor(result.getContent()), param.getPageNum(),
				param.getPageSize(), result.getTotalElements());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<ExportAssetTransferOrderVO> findExportData(AssetTransferQueryCondParam param) {
		List<AssetTransferOrder> result = assetTransferOrderRepo.findAll(param.buildSpecification(),
				Sort.by(Sort.Order.desc("createTime")));
		return ExportAssetTransferOrderVO.convertFor(result);
	}

}

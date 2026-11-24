package com.exchange.withdraw.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.currency.ChainNetworkHolder;
import com.exchange.dictconfig.DictHolder;
import com.exchange.withdraw.domain.WithdrawOrder;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class WithdrawOrderVO {

	private String id;

	private String memberAccountId;

	private String orderNo;

	private String chainType;

	private String withdrawalAddress;

	private String assetCode;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal amount;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal fee;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal actualAmount;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal paidUsdtAmount;

	/** 手续费折合USDT */
	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal feeUsdtAmount;

	private String usdtConvertStatus;

	private String status;

	private String statusName;

	private String auditRemark;

	private String txHash;

	private String addressExplorerUrl;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime completeTime;

	private Long memberUid;

	public static List<WithdrawOrderVO> convertFor(List<WithdrawOrder> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<WithdrawOrderVO> vos = new ArrayList<>();
		for (WithdrawOrder po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static WithdrawOrderVO convertFor(WithdrawOrder po) {
		if (po == null) {
			return null;
		}
		WithdrawOrderVO vo = new WithdrawOrderVO();
		BeanUtils.copyProperties(po, vo);
		vo.setStatusName(DictHolder.getDictItemName("withdrawOrderStatus", vo.getStatus()));
		if (po.getMemberAccount() != null) {
			vo.setMemberUid(po.getMemberAccount().getUid());
		}
		vo.setAddressExplorerUrl(
				ChainNetworkHolder.formatAddressExplorerUrl(po.getChainType(), po.getWithdrawalAddress()));
		return vo;
	}

}

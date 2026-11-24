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
public class MyWithdrawOrderVO {

	private String id;

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

	private String status;

	private String statusName;

	private String auditRemark;

	private String txHash;

	private String addressExplorerUrl;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "MM/dd HH:mm")
	private LocalDateTime createTimeShort;

	public static List<MyWithdrawOrderVO> convertFor(List<WithdrawOrder> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MyWithdrawOrderVO> vos = new ArrayList<>();
		for (WithdrawOrder po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static MyWithdrawOrderVO convertFor(WithdrawOrder po) {
		if (po == null) {
			return null;
		}
		MyWithdrawOrderVO vo = new MyWithdrawOrderVO();
		BeanUtils.copyProperties(po, vo);
		if (po.getCreateTime() != null) {
			vo.setCreateTimeShort(po.getCreateTime());
		}
		vo.setStatusName(DictHolder.getDictItemName("withdrawOrderStatus", po.getStatus()));
		vo.setAddressExplorerUrl(
				ChainNetworkHolder.formatAddressExplorerUrl(po.getChainType(), po.getWithdrawalAddress()));
		return vo;
	}

}

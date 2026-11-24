package com.exchange.deposit.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.currency.ChainNetworkHolder;
import com.exchange.deposit.domain.DepositOrder;
import com.exchange.dictconfig.DictHolder;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 客户端（App/小程序）充币记录、充币详情使用的精简视图对象。
 */
@Data
public class MyDepositOrderVO {

	private String id;

	private String orderNo;

	private String chainType;

	private String depositAddress;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal amount;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal actualAmount;

	private String assetCode;

	private String txHash;

	private String fromAddress;

	private String addressExplorerUrl;

	private String status;

	private String statusName;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "MM/dd HH:mm")
	private LocalDateTime createTimeShort;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime expireTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime creditTime;

	private String closeReason;

	private Long expireRemainingSeconds;

	public static List<MyDepositOrderVO> convertFor(List<DepositOrder> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MyDepositOrderVO> vos = new ArrayList<>();
		for (DepositOrder po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static MyDepositOrderVO convertFor(DepositOrder po) {
		if (po == null) {
			return null;
		}
		MyDepositOrderVO vo = new MyDepositOrderVO();
		BeanUtils.copyProperties(po, vo);
		vo.setCreateTimeShort(po.getCreateTime());
		vo.setStatusName(DictHolder.getDictItemName("depositOrderStatus", po.getStatus()));
		if (po.getExpireTime() != null) {
			long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), po.getExpireTime());
			vo.setExpireRemainingSeconds(Math.max(0, seconds));
		}
		vo.setAddressExplorerUrl(
				ChainNetworkHolder.formatAddressExplorerUrl(po.getChainType(), po.getDepositAddress()));
		return vo;
	}

}

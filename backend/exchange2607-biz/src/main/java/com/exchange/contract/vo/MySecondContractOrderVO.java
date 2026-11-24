package com.exchange.contract.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.contract.domain.SecondContractOrder;
import com.exchange.dictconfig.DictHolder;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class MySecondContractOrderVO {

	private String id;

	private String orderNo;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal amount;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal openPrice;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal closePrice;

	private Integer period;

	private String direction;
	
	private String directionName;

	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private LocalDateTime openTime;

	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private LocalDateTime expectCloseTime;

	@JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private LocalDateTime closeTime;

	private String status;

	private String statusName;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal pnl;

	private String symbolCode;

	public static List<MySecondContractOrderVO> convertFor(List<SecondContractOrder> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MySecondContractOrderVO> vos = new ArrayList<>();
		for (SecondContractOrder po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static MySecondContractOrderVO convertFor(SecondContractOrder po) {
		if (po == null) {
			return null;
		}
		MySecondContractOrderVO vo = new MySecondContractOrderVO();
		BeanUtils.copyProperties(po, vo);
		vo.setDirectionName(DictHolder.getDictItemName("secondContractDirection", po.getDirection()));
		vo.setStatusName(DictHolder.getDictItemName("secondContractOrderStatus", po.getStatus()));
		return vo;
	}

}

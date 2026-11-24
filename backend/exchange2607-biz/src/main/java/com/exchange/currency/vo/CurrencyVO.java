package com.exchange.currency.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.currency.domain.Currency;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class CurrencyVO {

	private String id;

	private String currencyCode;

	private String currencyName;

	private String currencyLogo;

	private Integer scale;

	private Integer sortNum;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateTime;

	public static List<CurrencyVO> convertFor(List<Currency> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<CurrencyVO> vos = new ArrayList<>();
		for (Currency po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static CurrencyVO convertFor(Currency po) {
		if (po == null) {
			return null;
		}
		CurrencyVO vo = new CurrencyVO();
		BeanUtils.copyProperties(po, vo);
		return vo;
	}

}

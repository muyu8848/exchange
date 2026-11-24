package com.exchange.currency.vo;

import java.util.ArrayList;
import java.util.List;

import com.exchange.currency.domain.Currency;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 客户端币种选项（/common/findAllCurrency）：全部未删除的币种。
 * <p>
 * 币种为全局低频配置，客户端启动时预取并缓存；字段仅保留展示与精度所需，不含管理字段与 id。
 */
@Data
public class CurrencyOptionVO {

	private String currencyCode;

	private String currencyName;

	private String currencyLogo;

	/** 币种数量精度（小数位数） */
	private Integer scale;

	private Integer sortNum;

	public static List<CurrencyOptionVO> convertFor(List<Currency> records) {
		if (CollectionUtil.isEmpty(records)) {
			return new ArrayList<>();
		}
		List<CurrencyOptionVO> vos = new ArrayList<>();
		for (Currency record : records) {
			vos.add(convertFor(record));
		}
		return vos;
	}

	public static CurrencyOptionVO convertFor(Currency record) {
		if (record == null) {
			return null;
		}
		CurrencyOptionVO vo = new CurrencyOptionVO();
		vo.setCurrencyCode(record.getCurrencyCode());
		vo.setCurrencyName(record.getCurrencyName());
		vo.setCurrencyLogo(record.getCurrencyLogo());
		vo.setScale(record.getScale());
		vo.setSortNum(record.getSortNum());
		return vo;
	}

}
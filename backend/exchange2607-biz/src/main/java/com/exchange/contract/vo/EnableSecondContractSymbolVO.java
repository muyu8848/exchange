package com.exchange.contract.vo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.SymbolPriceUtils;
import com.exchange.contract.domain.SecondContractSymbol;
import com.exchange.constants.Constant;
import com.exchange.currency.domain.Currency;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class EnableSecondContractSymbolVO {

	private String id;

	private String symbolCode;

	private String baseCurrencyCode;

	private String quoteCurrencyCode;

	private String currencyName;

	private String currencyLogo;

	private String closePrice;

	private String openPrice;

	private String highestPrice;

	private String lowestPrice;

	private String chg;

	private Integer priceScale;

	public static List<EnableSecondContractSymbolVO> convertFor(List<SecondContractSymbol> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<EnableSecondContractSymbolVO> vos = new ArrayList<>();
		for (SecondContractSymbol po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static EnableSecondContractSymbolVO convertFor(SecondContractSymbol po) {
		if (po == null) {
			return null;
		}
		EnableSecondContractSymbolVO vo = new EnableSecondContractSymbolVO();
		BeanUtils.copyProperties(po, vo);
		vo.setOpenPrice(SymbolPriceUtils.getOpenPrice(Constant.行情业务类型_合约, po.getSymbolCode()));
		vo.setClosePrice(SymbolPriceUtils.getClosePrice(Constant.行情业务类型_合约, po.getSymbolCode()));
		vo.setHighestPrice(SymbolPriceUtils.getHighestPrice(Constant.行情业务类型_合约, po.getSymbolCode()));
		vo.setLowestPrice(SymbolPriceUtils.getLowestPrice(Constant.行情业务类型_合约, po.getSymbolCode()));
		vo.setChg(SymbolPriceUtils.getChg(Constant.行情业务类型_合约, po.getSymbolCode()));
		Currency baseCurrency = po.getBaseCurrency();
		if (baseCurrency != null) {
			vo.setCurrencyName(baseCurrency.getCurrencyName());
			vo.setCurrencyLogo(baseCurrency.getCurrencyLogo());
		}
		return vo;
	}

}

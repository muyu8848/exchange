package com.exchange.contract.vo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.SymbolPriceUtils;
import com.exchange.contract.domain.SecondContractSymbol;
import com.exchange.constants.Constant;
import com.exchange.dictconfig.DictHolder;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class SecondContractSymbolVO {

	private String id;

	private String symbolCode;

	private String baseCurrencyCode;

	private String quoteCurrencyCode;

	private String status;

	private String statusName;

	private Integer sortNum;

	private String closePrice;

	private String openPrice;

	private String chg;

	private Integer priceScale;

	public static List<SecondContractSymbolVO> convertFor(List<SecondContractSymbol> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<SecondContractSymbolVO> vos = new ArrayList<>();
		for (SecondContractSymbol po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static SecondContractSymbolVO convertFor(SecondContractSymbol po) {
		if (po == null) {
			return null;
		}
		SecondContractSymbolVO vo = new SecondContractSymbolVO();
		BeanUtils.copyProperties(po, vo);
		vo.setStatusName(DictHolder.getDictItemName("enableStatus", po.getStatus()));
		vo.setOpenPrice(SymbolPriceUtils.getOpenPrice(Constant.行情业务类型_合约, po.getSymbolCode()));
		vo.setClosePrice(SymbolPriceUtils.getClosePrice(Constant.行情业务类型_合约, po.getSymbolCode()));
		vo.setChg(SymbolPriceUtils.getChg(Constant.行情业务类型_合约, po.getSymbolCode()));
		return vo;
	}

}

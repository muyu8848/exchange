package com.exchange.contract.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.SymbolPriceUtils;
import com.exchange.constants.Constant;
import com.exchange.contract.domain.PerpetualContractSymbol;
import com.exchange.dictconfig.DictHolder;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class PerpetualContractSymbolVO {

	private String id;

	private String symbolCode;

	private String baseCurrencyCode;

	private String quoteCurrencyCode;

	private Integer maxLeverage;

	private Integer defaultLeverage;

	private BigDecimal maintenanceMarginRate;

	private BigDecimal takerFeeRate;

	private Integer fundingInterval;

	private BigDecimal defaultFundingRate;

	private Integer priceScale;

	private Integer amountScale;

	private BigDecimal minOrderValue;

	private String status;

	private String statusName;

	private Integer sortNum;

	private String closePrice;

	private String openPrice;

	private String highestPrice;

	private String lowestPrice;

	private String chg;

	public static List<PerpetualContractSymbolVO> convertFor(List<PerpetualContractSymbol> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<PerpetualContractSymbolVO> vos = new ArrayList<>();
		for (PerpetualContractSymbol po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static PerpetualContractSymbolVO convertFor(PerpetualContractSymbol po) {
		if (po == null) {
			return null;
		}
		PerpetualContractSymbolVO vo = new PerpetualContractSymbolVO();
		BeanUtils.copyProperties(po, vo);
		vo.setStatusName(DictHolder.getDictItemName("enableStatus", po.getStatus()));
		vo.setOpenPrice(SymbolPriceUtils.getOpenPrice(Constant.行情业务类型_合约, po.getSymbolCode()));
		vo.setClosePrice(SymbolPriceUtils.getClosePrice(Constant.行情业务类型_合约, po.getSymbolCode()));
		vo.setHighestPrice(SymbolPriceUtils.getHighestPrice(Constant.行情业务类型_合约, po.getSymbolCode()));
		vo.setLowestPrice(SymbolPriceUtils.getLowestPrice(Constant.行情业务类型_合约, po.getSymbolCode()));
		vo.setChg(SymbolPriceUtils.getChg(Constant.行情业务类型_合约, po.getSymbolCode()));
		return vo;
	}

}

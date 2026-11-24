package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.exchange.common.utils.AmountUtils;
import com.exchange.constants.Constant;
import com.exchange.investment.domain.InvestmentRecord;

import lombok.Data;

@Data
public class InvestmentRecordSubtotalVO {

	/** 按币种分组的汇总 */
	private List<AssetSubtotal> items = new ArrayList<>();

	@Data
	public static class AssetSubtotal {
		private String assetCode;
		private long recordCount;
		private BigDecimal totalPrincipal = BigDecimal.ZERO;
	}

	/**
	 * 按币种分组汇总
	 */
	public static InvestmentRecordSubtotalVO buildGroupedByAsset(List<InvestmentRecord> records) {
		InvestmentRecordSubtotalVO vo = new InvestmentRecordSubtotalVO();
		Map<String, AssetSubtotal> map = new LinkedHashMap<>();
		for (InvestmentRecord record : records) {
			String key = record.getAssetCode() != null ? record.getAssetCode() : "UNKNOWN";
			AssetSubtotal sub = map.computeIfAbsent(key, k -> {
				AssetSubtotal s = new AssetSubtotal();
				s.setAssetCode(k);
				return s;
			});
			sub.setRecordCount(sub.getRecordCount() + 1);
			sub.setTotalPrincipal(
					AmountUtils.add(sub.getTotalPrincipal(), record.getPrincipal(), Constant.小数部分位数));
		}
		vo.setItems(new ArrayList<>(map.values()));
		return vo;
	}

}

package com.exchange.log.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class MemberAssetChangeSubtotalVO {

	/** 按币种分组的汇总，key = assetCode */
	private List<AssetSubtotal> items = new ArrayList<>();

	@Data
	public static class AssetSubtotal {
		private String assetCode;
		private BigDecimal income = BigDecimal.ZERO;
		private BigDecimal expenditure = BigDecimal.ZERO;
		private long recordCount;
	}

	/**
	 * 按币种分组汇总
	 */
	public static MemberAssetChangeSubtotalVO buildGroupedByAsset(
			List<com.exchange.log.domain.MemberAssetChangeLog> records) {
		MemberAssetChangeSubtotalVO vo = new MemberAssetChangeSubtotalVO();
		Map<String, AssetSubtotal> map = new LinkedHashMap<>();
		for (com.exchange.log.domain.MemberAssetChangeLog record : records) {
			String key = record.getAssetCode() != null ? record.getAssetCode() : "UNKNOWN";
			AssetSubtotal sub = map.computeIfAbsent(key, k -> {
				AssetSubtotal s = new AssetSubtotal();
				s.setAssetCode(k);
				return s;
			});
			if (record.getBalanceChange().compareTo(BigDecimal.ZERO) >= 0) {
				sub.setIncome(sub.getIncome().add(record.getBalanceChange()));
			} else {
				sub.setExpenditure(sub.getExpenditure().add(record.getBalanceChange().abs()));
			}
			sub.setRecordCount(sub.getRecordCount() + 1);
		}
		vo.setItems(new ArrayList<>(map.values()));
		return vo;
	}

}

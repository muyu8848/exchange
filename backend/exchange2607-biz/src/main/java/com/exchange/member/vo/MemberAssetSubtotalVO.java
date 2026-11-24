package com.exchange.member.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.exchange.common.utils.AmountUtils;
import com.exchange.constants.Constant;
import com.exchange.member.domain.MemberAsset;

import lombok.Data;

@Data
public class MemberAssetSubtotalVO {

	/** 按币种分组的汇总 */
	private List<AssetSubtotal> items = new ArrayList<>();

	@Data
	public static class AssetSubtotal {
		private String assetCode;
		private long accountCount;
		private BigDecimal totalAvailable = BigDecimal.ZERO;
		private BigDecimal totalFrozen = BigDecimal.ZERO;
	}

	/**
	 * 按币种分组汇总
	 */
	public static MemberAssetSubtotalVO buildGroupedByAsset(List<MemberAsset> assets) {
		MemberAssetSubtotalVO vo = new MemberAssetSubtotalVO();
		Map<String, AssetSubtotal> map = new LinkedHashMap<>();
		for (MemberAsset asset : assets) {
			String key = asset.getAssetCode() != null ? asset.getAssetCode() : "UNKNOWN";
			AssetSubtotal sub = map.computeIfAbsent(key, k -> {
				AssetSubtotal s = new AssetSubtotal();
				s.setAssetCode(k);
				return s;
			});
			sub.setAccountCount(sub.getAccountCount() + 1);
			sub.setTotalAvailable(
					AmountUtils.add(sub.getTotalAvailable(), asset.getAvailable(), Constant.小数部分位数));
			sub.setTotalFrozen(
					AmountUtils.add(sub.getTotalFrozen(), asset.getFrozen(), Constant.小数部分位数));
		}
		vo.setItems(new ArrayList<>(map.values()));
		return vo;
	}
}

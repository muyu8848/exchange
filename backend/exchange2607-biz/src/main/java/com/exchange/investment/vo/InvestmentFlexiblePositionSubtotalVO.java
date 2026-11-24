package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.exchange.common.utils.AmountUtils;
import com.exchange.constants.Constant;
import com.exchange.investment.domain.InvestmentFlexiblePosition;

import lombok.Data;

@Data
public class InvestmentFlexiblePositionSubtotalVO {

	/** 按币种分组的汇总 */
	private List<AssetSubtotal> items = new ArrayList<>();

	@Data
	public static class AssetSubtotal {
		private String assetCode;
		private long orderCount;
		private BigDecimal totalPrincipal = BigDecimal.ZERO;
		private BigDecimal totalAccruedInterest = BigDecimal.ZERO;
	}

	/**
	 * 按币种分组汇总
	 */
	public static InvestmentFlexiblePositionSubtotalVO buildGroupedByAsset(List<InvestmentFlexiblePosition> positions) {
		InvestmentFlexiblePositionSubtotalVO vo = new InvestmentFlexiblePositionSubtotalVO();
		Map<String, AssetSubtotal> map = new LinkedHashMap<>();
		for (InvestmentFlexiblePosition pos : positions) {
			String key = pos.getAssetCode() != null ? pos.getAssetCode() : "UNKNOWN";
			AssetSubtotal sub = map.computeIfAbsent(key, k -> {
				AssetSubtotal s = new AssetSubtotal();
				s.setAssetCode(k);
				return s;
			});
			sub.setOrderCount(sub.getOrderCount() + 1);
			sub.setTotalPrincipal(
					AmountUtils.add(sub.getTotalPrincipal(), pos.getPrincipal(), Constant.小数部分位数));
			sub.setTotalAccruedInterest(
					AmountUtils.add(sub.getTotalAccruedInterest(), pos.getAccruedInterest(), Constant.小数部分位数));
		}
		vo.setItems(new ArrayList<>(map.values()));
		return vo;
	}

}

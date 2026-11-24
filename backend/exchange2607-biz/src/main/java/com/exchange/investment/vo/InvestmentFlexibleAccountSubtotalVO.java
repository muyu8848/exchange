package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.exchange.common.utils.AmountUtils;
import com.exchange.constants.Constant;
import com.exchange.investment.domain.InvestmentFlexibleAccount;

import lombok.Data;

@Data
public class InvestmentFlexibleAccountSubtotalVO {

	/** 按币种分组的汇总 */
	private List<AssetSubtotal> items = new ArrayList<>();

	@Data
	public static class AssetSubtotal {
		private String assetCode;
		private long accountCount;
		private BigDecimal totalPrincipal = BigDecimal.ZERO;
		private BigDecimal totalAccruedInterest = BigDecimal.ZERO;
	}

	/**
	 * 按币种分组汇总
	 */
	public static InvestmentFlexibleAccountSubtotalVO buildGroupedByAsset(
			List<InvestmentFlexibleAccount> accounts) {
		InvestmentFlexibleAccountSubtotalVO vo = new InvestmentFlexibleAccountSubtotalVO();
		Map<String, AssetSubtotal> map = new LinkedHashMap<>();
		for (InvestmentFlexibleAccount account : accounts) {
			String key = account.getAssetCode() != null ? account.getAssetCode() : "UNKNOWN";
			AssetSubtotal sub = map.computeIfAbsent(key, k -> {
				AssetSubtotal s = new AssetSubtotal();
				s.setAssetCode(k);
				return s;
			});
			sub.setAccountCount(sub.getAccountCount() + 1);
			BigDecimal principal = account.getPosition() != null ? account.getPosition()
					: BigDecimal.ZERO;
			BigDecimal interest = account.getAccruedInterest() != null ? account.getAccruedInterest()
					: BigDecimal.ZERO;
			sub.setTotalPrincipal(AmountUtils.add(sub.getTotalPrincipal(), principal, Constant.小数部分位数));
			sub.setTotalAccruedInterest(
					AmountUtils.add(sub.getTotalAccruedInterest(), interest, Constant.小数部分位数));
		}
		vo.setItems(new ArrayList<>(map.values()));
		return vo;
	}
}

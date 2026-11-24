package com.exchange.investment.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.exchange.common.utils.AmountUtils;
import com.exchange.constants.Constant;
import com.exchange.investment.domain.InvestmentHourlyInterestLog;

import lombok.Data;

@Data
public class InvestmentHourlyInterestLogSubtotalVO {

	/** 按币种分组的汇总 */
	private List<AssetSubtotal> items = new ArrayList<>();

	@Data
	public static class AssetSubtotal {
		private String assetCode;
		private long recordCount;
		private BigDecimal totalInterestAmount = BigDecimal.ZERO;
	}

	/**
	 * 按币种分组汇总
	 */
	public static InvestmentHourlyInterestLogSubtotalVO buildGroupedByAsset(
			List<InvestmentHourlyInterestLog> logs) {
		InvestmentHourlyInterestLogSubtotalVO vo = new InvestmentHourlyInterestLogSubtotalVO();
		Map<String, AssetSubtotal> map = new LinkedHashMap<>();
		for (InvestmentHourlyInterestLog log : logs) {
			String key = log.getAssetCode() != null ? log.getAssetCode() : "UNKNOWN";
			AssetSubtotal sub = map.computeIfAbsent(key, k -> {
				AssetSubtotal s = new AssetSubtotal();
				s.setAssetCode(k);
				return s;
			});
			sub.setRecordCount(sub.getRecordCount() + 1);
			sub.setTotalInterestAmount(
					AmountUtils.add(sub.getTotalInterestAmount(), log.getInterestAmount(), Constant.小数部分位数));
		}
		vo.setItems(new ArrayList<>(map.values()));
		return vo;
	}

}

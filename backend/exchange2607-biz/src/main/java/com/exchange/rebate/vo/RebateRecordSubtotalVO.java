package com.exchange.rebate.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.rebate.domain.RebateRecord;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 返佣小计（按币种分组汇总 + 全量合计）
 */
@Data
public class RebateRecordSubtotalVO {

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	private Integer count = 0;

	private List<RebateRecordSubtotalItem> items = new ArrayList<>();

	public static RebateRecordSubtotalVO convertFor(List<RebateRecord> list) {
		RebateRecordSubtotalVO vo = new RebateRecordSubtotalVO();
		if (CollectionUtil.isEmpty(list)) {
			return vo;
		}
		Map<String, List<RebateRecord>> byAsset = list.stream()
				.filter(r -> r.getAssetCode() != null)
				.collect(Collectors.groupingBy(RebateRecord::getAssetCode));
		List<RebateRecordSubtotalItem> items = new ArrayList<>();
		BigDecimal total = BigDecimal.ZERO;
		int totalCount = 0;
		for (Map.Entry<String, List<RebateRecord>> entry : byAsset.entrySet()) {
			List<RebateRecord> group = entry.getValue();
			BigDecimal sum = group.stream().map(RebateRecord::getAmount)
					.filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
			RebateRecordSubtotalItem item = new RebateRecordSubtotalItem();
			item.setAssetCode(entry.getKey());
			item.setTotalAmount(sum);
			item.setCount(group.size());
			items.add(item);
			total = total.add(sum);
			totalCount += group.size();
		}
		items.sort(Comparator.comparing(RebateRecordSubtotalItem::getAssetCode));
		vo.setItems(items);
		vo.setTotalAmount(total);
		vo.setCount(totalCount);
		return vo;
	}
}

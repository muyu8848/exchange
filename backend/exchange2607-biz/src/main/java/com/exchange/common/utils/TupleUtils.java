package com.exchange.common.utils;

import java.math.BigDecimal;

import javax.persistence.Tuple;

/**
 * JPA Criteria 聚合结果取值工具：空值统一按 0 处理。
 */
public class TupleUtils {

	private TupleUtils() {
	}

	public static Long getLong(Tuple tuple, String alias) {
		if (tuple == null) {
			return 0L;
		}
		Long value = tuple.get(alias, Long.class);
		return value != null ? value : 0L;
	}

	public static BigDecimal getBigDecimal(Tuple tuple, String alias) {
		if (tuple == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal value = tuple.get(alias, BigDecimal.class);
		return value != null ? value : BigDecimal.ZERO;
	}
}

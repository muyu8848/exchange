package com.exchange.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class AmountUtils {

	private AmountUtils() {
	}

	public static BigDecimal toBigDecimal(BigDecimal value) {
		return value;
	}

	public static BigDecimal toBigDecimal(Double value) {
		return value == null ? null : BigDecimal.valueOf(value);
	}

	public static BigDecimal toBigDecimal(double value) {
		return BigDecimal.valueOf(value);
	}

	public static Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}

	public static BigDecimal round2(BigDecimal value) {
		return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
	}

	/** 按指定精度四舍五入 */
	public static BigDecimal round(BigDecimal value, int scale) {
		return value == null ? null : value.setScale(scale, RoundingMode.HALF_UP);
	}

	public static BigDecimal add(BigDecimal a, BigDecimal b) {
		if (a == null) {
			return round2(b);
		}
		if (b == null) {
			return round2(a);
		}
		return round2(a.add(b));
	}

	/** 按指定精度加法 */
	public static BigDecimal add(BigDecimal a, BigDecimal b, int scale) {
		if (a == null) {
			return round(b, scale);
		}
		if (b == null) {
			return round(a, scale);
		}
		return a.add(b).setScale(scale, RoundingMode.HALF_UP);
	}

	public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
		if (a == null) {
			return b == null ? BigDecimal.ZERO : round2(b.negate());
		}
		if (b == null) {
			return round2(a);
		}
		return round2(a.subtract(b));
	}

	/** 按指定精度减法 */
	public static BigDecimal subtract(BigDecimal a, BigDecimal b, int scale) {
		if (a == null) {
			return b == null ? BigDecimal.ZERO : b.negate().setScale(scale, RoundingMode.HALF_UP);
		}
		if (b == null) {
			return a.setScale(scale, RoundingMode.HALF_UP);
		}
		return a.subtract(b).setScale(scale, RoundingMode.HALF_UP);
	}

	public static BigDecimal percentOf(BigDecimal amount, BigDecimal percent) {
		if (amount == null || percent == null) {
			return null;
		}
		return round2(amount.multiply(percent).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
	}

	public static BigDecimal multiply(BigDecimal amount, BigDecimal rate) {
		if (amount == null || rate == null) {
			return null;
		}
		return round2(amount.multiply(rate));
	}

	public static BigDecimal multiply(BigDecimal amount, Double rate) {
		return rate == null ? null : multiply(amount, BigDecimal.valueOf(rate));
	}

	public static BigDecimal multiply(BigDecimal amount, double rate) {
		return multiply(amount, BigDecimal.valueOf(rate));
	}

	/**
	 * 格式化为纯数字字符串：去除末尾多余零 + toPlainString杜绝科学计数法。
	 * 适用于异常消息拼接等非JSON输出场景。
	 */
	public static String toPlainString(BigDecimal value) {
		if (value == null) {
			return "0";
		}
		BigDecimal stripped = value.stripTrailingZeros();
		if (stripped.compareTo(BigDecimal.ZERO) == 0) {
			return "0";
		}
		return stripped.toPlainString();
	}

}

package com.exchange.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.exchange.constants.Constant;

import cn.hutool.core.util.StrUtil;

/**
 * 法币汇率工具类。
 * <p>
 * 法币兑 USDT 的汇率由 {@code SyncExchangeRate} 定时同步到 Redis（key:
 * c2c:fiatRate:{fiatCode}）。 本类通过 {@link RedisUtils} 静态读取 Redis，参考
 * {@link SymbolPriceUtils} 的模式，无需 Spring 注入。
 */
public class FiatRateUtils {

	private FiatRateUtils() {
	}

	/**
	 * 获取法币兑 USDT 的汇率（如 CNY→7.12）。
	 *
	 * @param fiatCode 法币代码（如 CNY、USD）
	 * @return 汇率字符串，Redis 中无值返回 {@code null}
	 */
	public static String getFiatRate(String fiatCode) {
		if (StrUtil.isBlank(fiatCode)) {
			return null;
		}
		return RedisUtils.opsForValueGet(Constant.C2C法币汇率 + fiatCode);
	}

	/**
	 * 获取 C2C 交易对的市场参考价。
	 * <p>
	 * 以 USDT 为桥梁：法币/币种 = 法币兑USDT汇率 × 币种兑USDT现货价。
	 *
	 * @param fiatCode  法币代码（如 CNY、USD）
	 * @param assetCode 币种代码（如 USDT、BTC）
	 * @return 法币/币种 的参考价；获取失败返回 {@code null}
	 */
	public static BigDecimal getMarketPrice(String fiatCode, String assetCode) {
		if (StrUtil.isBlank(fiatCode) || StrUtil.isBlank(assetCode)) {
			return null;
		}
		// 1. 取法币兑 USDT 汇率
		String fiatRateStr = getFiatRate(fiatCode);
		if (StrUtil.isBlank(fiatRateStr)) {
			return null;
		}
		BigDecimal fiatRate = new BigDecimal(fiatRateStr);

		// 2. USDT 本身：直接返回法币/USDT 汇率
		if (Constant.数字货币_USDT.equals(assetCode)) {
			return fiatRate;
		}

		// 3. 取币种兑 USDT 的现货收盘价
		String closePriceStr = SymbolPriceUtils.getClosePrice(Constant.行情业务类型_现货, assetCode + "-USDT");
		if (StrUtil.isBlank(closePriceStr)) {
			return null;
		}
		BigDecimal cryptoUsdt = new BigDecimal(closePriceStr);

		// 4. 法币/币种 = 法币/USDT × USDT/币种
		return fiatRate.multiply(cryptoUsdt).setScale(Constant.小数部分位数, RoundingMode.HALF_UP);
	}

}

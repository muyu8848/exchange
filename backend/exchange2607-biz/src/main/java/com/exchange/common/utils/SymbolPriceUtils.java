package com.exchange.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.exchange.constants.Constant;

import cn.hutool.core.util.StrUtil;

public class SymbolPriceUtils {

	/**
	 * 业务类型 + 交易对 → OKX instId。 合约（秒合约）跟踪永续合约行情，instId 追加 -SWAP；现货直接使用交易对。
	 */
	public static String getInstId(String bizType, String symbolCode) {
		if (Constant.行情业务类型_合约.equals(bizType)) {
			return symbolCode + "-SWAP";
		}
		return symbolCode;
	}

	public static String getOpenPrice(String bizType, String symbolCode) {
		return RedisUtils.opsForValueGet(Constant.开盘价 + getInstId(bizType, symbolCode));
	}

	public static String getClosePrice(String bizType, String symbolCode) {
		return RedisUtils.opsForValueGet(Constant.收盘价 + getInstId(bizType, symbolCode));
	}

	/**
	 * 币种对 USDT 的实时单价：USDT 返回 1，无行情返回 0。
	 * <p>
	 * 仅用于估值/展示等允许兜底的场景；需要判断行情是否存在时请用 {@link #getUsdtPrice}。
	 */
	public static BigDecimal getUsdtPriceOrZero(String assetCode) {
		BigDecimal usdtPrice = getUsdtPrice(assetCode);
		return usdtPrice == null ? BigDecimal.ZERO : usdtPrice;
	}

	/** 币种对 USDT 的实时单价：USDT 返回 1，无行情返回 null */
	public static BigDecimal getUsdtPrice(String assetCode) {
		if (Constant.数字货币_USDT.equals(assetCode)) {
			return BigDecimal.ONE;
		}
		String priceStr = getClosePrice(Constant.行情业务类型_现货, assetCode + "-USDT");
		return StrUtil.isBlank(priceStr) ? null : new BigDecimal(priceStr);
	}

	/** 数量折 USDT：无行情返回 null，数量为 0 返回 0 */
	public static BigDecimal toUsdtAmount(String assetCode, BigDecimal amount) {
		if (amount == null) {
			return null;
		}
		if (amount.signum() == 0) {
			return BigDecimal.ZERO;
		}
		BigDecimal price = getUsdtPrice(assetCode);
		if (price == null) {
			return null;
		}
		return amount.multiply(price).setScale(Constant.折合USDT精度, RoundingMode.HALF_UP);
	}

	public static String getHighestPrice(String bizType, String symbolCode) {
		return RedisUtils.opsForValueGet(Constant.最高价 + getInstId(bizType, symbolCode));
	}

	public static String getLowestPrice(String bizType, String symbolCode) {
		return RedisUtils.opsForValueGet(Constant.最低价 + getInstId(bizType, symbolCode));
	}

	public static String getChg(String bizType, String symbolCode) {
		String instId = getInstId(bizType, symbolCode);
		String openPrice = RedisUtils.opsForValueGet(Constant.开盘价 + instId);
		String closePrice = RedisUtils.opsForValueGet(Constant.收盘价 + instId);
		if (StrUtil.isBlank(openPrice) || StrUtil.isBlank(closePrice)) {
			return null;
		}
		try {
			BigDecimal open = new BigDecimal(openPrice);
			BigDecimal close = new BigDecimal(closePrice);
			if (open.compareTo(BigDecimal.ZERO) == 0) {
				return null;
			}
			BigDecimal chg = close.subtract(open).divide(open, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
					.setScale(2, RoundingMode.HALF_UP);
			return Double.parseDouble(chg.toString()) + "%";
		} catch (NumberFormatException e) {
			return null;
		}
	}

}

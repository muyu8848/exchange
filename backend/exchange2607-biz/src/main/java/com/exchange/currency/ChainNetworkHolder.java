package com.exchange.currency;

import com.exchange.common.utils.SpringUtils;
import com.exchange.currency.service.ChainNetworkService;
import com.exchange.currency.vo.ChainNetworkVO;

import cn.hutool.core.util.StrUtil;

/**
 * 区块链网络相关的静态门面，对标 DictHolder。 内部通过 ChainNetworkService（带缓存）获取网络信息，避免在多个 VO
 * 中重复拼接浏览器地址。
 */
public class ChainNetworkHolder {

	/** 按 chainType 取缓存的区块链网络信息（含浏览器地址模板） */
	public static ChainNetworkVO getNetwork(String chainType) {
		if (StrUtil.isBlank(chainType)) {
			return null;
		}
		return SpringUtils.getBean(ChainNetworkService.class).findChainNetworkByChainType(chainType);
	}

	/**
	 * 一步到位：传入 chainType 与地址，返回完整的区块浏览器 URL。 内部走缓存查询模板并完成占位符替换，无需调用方关心模板。
	 *
	 * @param chainType 链类型
	 * @param address   地址（可为 depositAddress / withdrawalAddress / address）
	 * @return 完整 URL，缺失模板或地址时返回 null
	 */
	public static String formatAddressExplorerUrl(String chainType, String address) {
		if (StrUtil.isBlank(chainType) || StrUtil.isBlank(address)) {
			return null;
		}
		ChainNetworkVO network = getNetwork(chainType);
		if (network == null || StrUtil.isBlank(network.getAddressExplorerUrlTemplate())) {
			return null;
		}
		String addr = address.trim();
		return network.getAddressExplorerUrlTemplate().trim().replace("{address}", addr).replace("{}", addr);
	}
}

package com.exchange.member.vo;

import java.util.List;

import lombok.Data;

@Data
public class MemberAssetSummaryVO {

	/** 总估值（USDT + 折合法币） */
	private AssetTotalValueVO totalValue;

	/** 按账户类型（现货/合约/赚币）汇总 */
	private List<AssetByAccountVO> byAccountType;

	/** 按币种汇总 */
	private List<AssetByCurrencyVO> byCurrency;

}

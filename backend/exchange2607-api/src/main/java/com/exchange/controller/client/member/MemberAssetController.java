package com.exchange.controller.client.member;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.member.service.MemberAssetService;
import com.exchange.member.vo.AssetByAccountVO;
import com.exchange.member.vo.AssetByCurrencyVO;
import com.exchange.member.vo.AssetTotalValueVO;
import com.exchange.member.vo.CurrencyAssetDetailVO;
import com.exchange.member.vo.MemberAssetVO;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/memberAsset")
public class MemberAssetController {

	@Autowired
	private MemberAssetService memberAssetService;

	@PostMapping("/getAvailableBalance")
	public Result<BigDecimal> getAvailableBalance(String accountType, String assetCode) {
		return Result
				.success(memberAssetService.getAvailableBalance(StpUtil.getLoginIdAsString(), accountType, assetCode));
	}

	@PostMapping("/findByCurrencySummary")
	public Result<List<AssetByCurrencyVO>> findByCurrencySummary() {
		return Result.success(memberAssetService.findAssetsByCurrencySummary(StpUtil.getLoginIdAsString()));
	}

	@PostMapping("/findByAccountTypeSummary")
	public Result<List<AssetByAccountVO>> findByAccountTypeSummary() {
		return Result.success(memberAssetService.findAssetsByAccountTypeSummary(StpUtil.getLoginIdAsString()));
	}

	/** 资产总估值：fiatCode 为折算展示货币（如 CNY/USD），为空时默认 CNY */
	@PostMapping("/getTotalValue")
	public Result<AssetTotalValueVO> getTotalValue(String fiatCode) {
		return Result.success(memberAssetService.getTotalValue(StpUtil.getLoginIdAsString(), fiatCode));
	}

	@PostMapping("/findDetailByCurrency")
	public Result<CurrencyAssetDetailVO> findDetailByCurrency(String assetCode) {
		return Result.success(memberAssetService.findDetailByCurrency(StpUtil.getLoginIdAsString(), assetCode));
	}

	@PostMapping("/findSpotAssetsByCodes")
	public Result<List<MemberAssetVO>> findSpotAssetsByCodes(String assetCodes) {
		List<String> codes = Arrays.asList(assetCodes.split(","));
		return Result.success(
				memberAssetService.findAssetsByAccountTypeAndCodes(StpUtil.getLoginIdAsString(), "SPOT", codes));
	}

}

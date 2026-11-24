package com.exchange.controller.client.contract;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.contract.param.SaveContractLeverageParam;
import com.exchange.contract.service.MemberContractLeverageService;
import com.exchange.contract.service.PerpetualContractSymbolService;
import com.exchange.contract.vo.MemberContractLeverageVO;
import com.exchange.contract.vo.PerpetualContractSymbolVO;
import com.exchange.contract.vo.PerpetualFundingCountdownVO;

import cn.dev33.satoken.stp.StpUtil;

@Validated
@RestController
@RequestMapping("/perpetualContractSymbol")
public class PerpetualContractSymbolController {

	@Autowired
	private PerpetualContractSymbolService perpetualContractSymbolService;
	@Autowired
	private MemberContractLeverageService memberContractLeverageService;

	@PostMapping("/findEnable")
	public Result<List<PerpetualContractSymbolVO>> findEnable() {
		return Result.success(perpetualContractSymbolService.findEnabled());
	}

	@PostMapping("/findSymbolInfo")
	public Result<PerpetualContractSymbolVO> findSymbolInfo(String symbolCode) {
		return Result.success(perpetualContractSymbolService.findBySymbolCode(symbolCode));
	}

	@PostMapping("/findFundingCountdown")
	public Result<PerpetualFundingCountdownVO> findFundingCountdown(String symbolCode) {
		return Result.success(perpetualContractSymbolService.findFundingCountdown(symbolCode));
	}

	@PostMapping("/findMyLeverage")
	public Result<MemberContractLeverageVO> findMyLeverage(String symbolCode) {
		return Result
				.success(memberContractLeverageService.findByMemberAccountIdAndSymbolCode(StpUtil.getLoginIdAsString(), symbolCode));
	}

	@PostMapping("/saveLeverage")
	public Result<String> saveLeverage(SaveContractLeverageParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		memberContractLeverageService.saveLeverage(param);
		return Result.success();
	}

}

package com.exchange.controller.client.contract;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.contract.service.SecondContractSymbolService;
import com.exchange.contract.vo.EnableSecondContractSymbolVO;
import com.exchange.contract.vo.SecondContractPeriodConfigVO;

@Validated
@RestController
@RequestMapping("/secondContractSymbol")
public class SecondContractSymbolController {

	@Autowired
	private SecondContractSymbolService secondContractSymbolService;

	@PostMapping("/findEnableSymbol")
	public Result<List<EnableSecondContractSymbolVO>> findEnableSymbol() {
		return Result.success(secondContractSymbolService.findEnableSymbol());
	}

	@PostMapping("/findSymbolInfo")
	public Result<EnableSecondContractSymbolVO> findSymbolInfo(String symbolCode) {
		return Result.success(secondContractSymbolService.findSymbolInfo(symbolCode));
	}

	@PostMapping("/findPeriodConfig")
	public Result<List<SecondContractPeriodConfigVO>> findPeriodConfig(String symbolCode) {
		return Result.success(secondContractSymbolService.findSecondContractPeriodConfig(symbolCode));
	}

}

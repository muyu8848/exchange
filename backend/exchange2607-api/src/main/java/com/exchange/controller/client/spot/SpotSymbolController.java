package com.exchange.controller.client.spot;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.spot.service.SpotSymbolService;
import com.exchange.spot.vo.EnableSpotSymbolVO;

@Validated
@RestController
@RequestMapping("/spotSymbol")
public class SpotSymbolController {

	@Autowired
	private SpotSymbolService spotSymbolService;

	@PostMapping("/findEnableSymbol")
	public Result<List<EnableSpotSymbolVO>> findEnableSymbol() {
		return Result.success(spotSymbolService.findEnableSymbol());
	}

	@PostMapping("/findSymbolInfo")
	public Result<EnableSpotSymbolVO> findSymbolInfo(String symbolCode) {
		return Result.success(spotSymbolService.findSymbolInfo(symbolCode));
	}

}

package com.exchange.controller.admin.contract;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.contract.param.AddOrUpdatePerpetualContractSymbolParam;
import com.exchange.contract.service.PerpetualContractSymbolService;
import com.exchange.contract.vo.PerpetualContractSymbolVO;

import com.exchange.common.operlog.OperLog;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

@Validated
@RestController
@RequestMapping("/perpetualContractSymbol")
public class PerpetualContractSymbolController {

	@Autowired
	private PerpetualContractSymbolService symbolService;

	@SaCheckPermission("perpetualContractSymbol.save")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_永续合约交易对, operate = "新增或修改")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(AddOrUpdatePerpetualContractSymbolParam param) {
		symbolService.addOrUpdate(param);
		return Result.success();
	}

	@SaCheckPermission("perpetualContractSymbol.del")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_永续合约交易对, operate = "删除")
	@PostMapping("/del")
	public Result<String> del(String id) {
		symbolService.del(id);
		return Result.success();
	}

	@PostMapping("/findById")
	public Result<PerpetualContractSymbolVO> findById(String id) {
		return Result.success(symbolService.findById(id));
	}

	@PostMapping("/findAll")
	public Result<List<PerpetualContractSymbolVO>> findAll() {
		return Result.success(symbolService.findAll());
	}

}

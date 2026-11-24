package com.exchange.controller.admin.contract;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.contract.param.AddOrUpdateSecondContractSymbolParam;
import com.exchange.contract.param.SecondContractSymbolQueryCondParam;
import com.exchange.contract.param.UpdateSecondContractPeriodConfigParam;
import com.exchange.contract.service.SecondContractSymbolService;
import com.exchange.contract.vo.SecondContractPeriodConfigVO;
import com.exchange.contract.vo.SecondContractSymbolVO;

import com.exchange.common.operlog.OperLog;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/secondContractSymbol")
public class SecondContractSymbolController {

	@Autowired
	private SecondContractSymbolService secondContractSymbolService;

	@SaCheckPermission("secondContractSymbol.period")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_秒合约交易对, operate = "复制周期配置", permissionName = "周期配置")
	@PostMapping("/copyPeriodConfig")
	public Result<String> copyPeriodConfig(String source, String target) {
		secondContractSymbolService.copyPeriodConfig(source, target);
		return Result.success();
	}

	@SaCheckPermission("secondContractSymbol.period")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_秒合约交易对, operate = "更新周期配置", permissionName = "周期配置")
	@PostMapping("/updateSecondContractPeriodConfig")
	public Result<String> updateSecondContractPeriodConfig(@RequestBody UpdateSecondContractPeriodConfigParam param) {
		secondContractSymbolService.updateSecondContractPeriodConfig(param);
		return Result.success();
	}

	@PostMapping("/findSecondContractPeriodConfig")
	public Result<List<SecondContractPeriodConfigVO>> findSecondContractPeriodConfig(String symbolCode) {
		return Result.success(secondContractSymbolService.findSecondContractPeriodConfig(symbolCode));
	}

	@PostMapping("/findAll")
	public Result<List<SecondContractSymbolVO>> findAll() {
		return Result.success(secondContractSymbolService.findAll());
	}

	@PostMapping("/findByPage")
	public Result<PageResult<SecondContractSymbolVO>> findByPage(SecondContractSymbolQueryCondParam param) {
		return Result.success(secondContractSymbolService.findByPage(param));
	}

	@PostMapping("/findById")
	public Result<SecondContractSymbolVO> findById(String id) {
		return Result.success(secondContractSymbolService.findById(id));
	}

	@SaCheckPermission("secondContractSymbol.del")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_秒合约交易对, operate = "删除")
	@PostMapping("/del")
	public Result<String> del(String id) {
		secondContractSymbolService.del(id);
		return Result.success();
	}

	@SaCheckPermission("secondContractSymbol.save")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_秒合约交易对, operate = "新增或修改")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(AddOrUpdateSecondContractSymbolParam param) {
		secondContractSymbolService.addOrUpdate(param);
		return Result.success();
	}

}

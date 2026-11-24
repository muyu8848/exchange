package com.exchange.controller.admin.c2c;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.c2c.param.C2cSymbolParam;
import com.exchange.c2c.param.C2cSymbolQueryCondParam;
import com.exchange.c2c.service.C2cSymbolService;
import com.exchange.c2c.vo.C2cSymbolVO;
import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/c2cSymbol")
public class C2cSymbolController {

	@Autowired
	private C2cSymbolService c2cSymbolService;

	@PostMapping("/findAll")
	public Result<List<C2cSymbolVO>> findAll() {
		return Result.success(c2cSymbolService.findAllSymbol());
	}

	@PostMapping("/findByPage")
	public Result<PageResult<C2cSymbolVO>> findByPage(C2cSymbolQueryCondParam param) {
		return Result.success(c2cSymbolService.findSymbolByPage(param));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_C2C交易对配置, operate = "新增或修改")
	@SaCheckPermission("c2cSymbol.save")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(C2cSymbolParam param) {
		c2cSymbolService.addOrUpdateSymbol(param);
		return Result.success();
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_C2C交易对配置, operate = "删除")
	@SaCheckPermission("c2cSymbol.del")
	@PostMapping("/del")
	public Result<String> del(String id) {
		c2cSymbolService.delSymbol(id);
		return Result.success();
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_C2C交易对配置, operate = "批量生成交易对")
	@SaCheckPermission("c2cSymbol.generate")
	@PostMapping("/generateAll")
	public Result<String> generateAll() {
		c2cSymbolService.generateAll();
		return Result.success();
	}

}

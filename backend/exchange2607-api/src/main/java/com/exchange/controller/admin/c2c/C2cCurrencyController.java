package com.exchange.controller.admin.c2c;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.c2c.param.AddOrUpdateC2cCurrencyParam;
import com.exchange.c2c.param.C2cCurrencyQueryCondParam;
import com.exchange.c2c.service.C2cCurrencyService;
import com.exchange.c2c.vo.C2cCurrencyVO;
import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/c2cCurrency")
public class C2cCurrencyController {

	@Autowired
	private C2cCurrencyService c2cCurrencyService;

	@PostMapping("/findAll")
	public Result<List<C2cCurrencyVO>> findAll() {
		return Result.success(c2cCurrencyService.findAllCurrency());
	}

	@PostMapping("/findByPage")
	public Result<PageResult<C2cCurrencyVO>> findByPage(C2cCurrencyQueryCondParam param) {
		return Result.success(c2cCurrencyService.findCurrencyByPage(param));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_C2C币种配置, operate = "新增或修改")
	@SaCheckPermission("c2cCurrency.save")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(AddOrUpdateC2cCurrencyParam param) {
		c2cCurrencyService.addOrUpdateCurrency(param);
		return Result.success();
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_C2C币种配置, operate = "删除")
	@SaCheckPermission("c2cCurrency.del")
	@PostMapping("/del")
	public Result<String> del(String id) {
		c2cCurrencyService.delCurrency(id);
		return Result.success();
	}

}

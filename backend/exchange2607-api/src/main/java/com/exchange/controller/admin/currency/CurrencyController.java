package com.exchange.controller.admin.currency;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.currency.param.AddOrUpdateCurrencyParam;
import com.exchange.currency.param.CurrencyQueryCondParam;
import com.exchange.currency.service.CurrencyService;
import com.exchange.currency.vo.CurrencyVO;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/currency")
public class CurrencyController {

	@Autowired
	private CurrencyService currencyService;

	@PostMapping("/findAll")
	public Result<List<CurrencyVO>> findAll(CurrencyQueryCondParam param) {
		return Result.success(currencyService.findAll(param));
	}

	@PostMapping("/findByPage")
	public Result<PageResult<CurrencyVO>> findByPage(CurrencyQueryCondParam param) {
		return Result.success(currencyService.findByPage(param));
	}

	@PostMapping("/findById")
	public Result<CurrencyVO> findById(String id) {
		return Result.success(currencyService.findById(id));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_币种, operate = "新增或修改")
	@SaCheckPermission("currency.save")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(AddOrUpdateCurrencyParam param) {
		currencyService.addOrUpdate(param);
		return Result.success();
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_币种, operate = "删除")
	@SaCheckPermission("currency.del")
	@PostMapping("/del")
	public Result<String> del(String id) {
		currencyService.del(id);
		return Result.success();
	}
}

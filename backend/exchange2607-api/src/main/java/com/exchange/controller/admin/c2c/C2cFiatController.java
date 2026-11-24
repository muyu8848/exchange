package com.exchange.controller.admin.c2c;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.c2c.param.AddOrUpdateC2cFiatParam;
import com.exchange.c2c.param.C2cFiatQueryCondParam;
import com.exchange.c2c.service.C2cFiatService;
import com.exchange.c2c.vo.C2cFiatVO;
import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/c2cFiat")
public class C2cFiatController {

	@Autowired
	private C2cFiatService c2cFiatService;

	@PostMapping("/findAll")
	public Result<List<C2cFiatVO>> findAll() {
		return Result.success(c2cFiatService.findAllFiat());
	}

	@PostMapping("/findByPage")
	public Result<PageResult<C2cFiatVO>> findByPage(C2cFiatQueryCondParam param) {
		return Result.success(c2cFiatService.findFiatByPage(param));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_C2C法币配置, operate = "新增或修改")
	@SaCheckPermission("c2cFiat.save")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(AddOrUpdateC2cFiatParam param) {
		c2cFiatService.addOrUpdateFiat(param);
		return Result.success();
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_C2C法币配置, operate = "删除")
	@SaCheckPermission("c2cFiat.del")
	@PostMapping("/del")
	public Result<String> del(String id) {
		c2cFiatService.delFiat(id);
		return Result.success();
	}

}

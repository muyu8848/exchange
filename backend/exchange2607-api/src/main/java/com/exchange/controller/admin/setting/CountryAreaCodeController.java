package com.exchange.controller.admin.setting;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.member.param.AddOrUpdateCountryAreaCodeParam;
import com.exchange.member.param.CountryAreaCodeQueryCondParam;
import com.exchange.member.service.CountryAreaCodeService;
import com.exchange.member.vo.CountryAreaCodeVO;

import com.exchange.common.operlog.OperLog;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/countryAreaCode")
public class CountryAreaCodeController {

	@Autowired
	private CountryAreaCodeService service;

	@PostMapping("/findById")
	public Result<CountryAreaCodeVO> findById(String id) {
		return Result.success(service.findById(id));
	}

	@PostMapping("/findByPage")
	public Result<PageResult<CountryAreaCodeVO>> findByPage(CountryAreaCodeQueryCondParam param) {
		return Result.success(service.findByPage(param));
	}

	/** 后台全量列表（含停用，供筛选使用） */
	@PostMapping("/findAll")
	public Result<List<CountryAreaCodeVO>> findAll() {
		return Result.success(service.findAll());
	}

	@SaCheckPermission("countryAreaCode.save")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_国家区号, operate = "新增或修改")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(AddOrUpdateCountryAreaCodeParam param) {
		service.addOrUpdate(param);
		return Result.success();
	}

	@SaCheckPermission("countryAreaCode.del")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_国家区号, operate = "删除")
	@PostMapping("/del")
	public Result<String> del(String id) {
		service.del(id);
		return Result.success();
	}

}

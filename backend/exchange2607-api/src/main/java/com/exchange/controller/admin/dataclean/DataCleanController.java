package com.exchange.controller.admin.dataclean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.dataclean.param.DataCleanParam;
import com.exchange.dataclean.service.DataCleanService;

import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 数据清理
 */
@RestController
@RequestMapping("/dataClean")
public class DataCleanController {

	@Autowired
	private DataCleanService dataCleanService;

	/**
	 * 数据清理
	 * @param param
	 * @return
	 */
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_数据清理, operate = "数据清理")
	@SaCheckPermission("dataClean.clean")
	@PostMapping("/clean")
	public Result<String> clean(@RequestBody DataCleanParam param) {
		dataCleanService.clean(param);
		return Result.success();
	}

}

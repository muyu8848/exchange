package com.exchange.controller.admin.c2c;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.c2c.param.AppealProcessParam;
import com.exchange.c2c.param.C2cAppealRecordQueryCondParam;
import com.exchange.c2c.service.C2cAppealService;
import com.exchange.c2c.vo.C2cAppealRecordVO;
import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 申诉记录
 */
@RestController
@RequestMapping("/c2cAppeal")
public class C2cAppealController {

	@Autowired
	private C2cAppealService appealService;

	/**
	 * 申诉处理
	 * 
	 * @param param
	 * @return
	 */
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_C2C订单, operate = "申诉处理")
	@SaCheckPermission("c2cAppeal.handle")
	@PostMapping("/appealProcess")
	public Result<String> appealProcess(AppealProcessParam param) {
		appealService.appealProcess(param);
		return Result.success();
	}

	/**
	 * 分页获取申诉记录
	 */
	@PostMapping("/findByPage")
	public Result<PageResult<C2cAppealRecordVO>> findByPage(C2cAppealRecordQueryCondParam param) {
		return Result.success(appealService.findByPage(param));
	}

}

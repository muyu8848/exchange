package com.exchange.controller.admin.withdraw;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.withdraw.param.AddOrUpdateWithdrawChannelParam;
import com.exchange.withdraw.param.WithdrawChannelQueryCondParam;
import com.exchange.withdraw.service.WithdrawChannelService;
import com.exchange.withdraw.vo.WithdrawChannelVO;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/withdrawChannel")
public class WithdrawChannelController {

	@Autowired
	private WithdrawChannelService withdrawChannelService;

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_提币通道, operate = "删除")
	@SaCheckPermission("withdrawChannel.del")
	@PostMapping("/del")
	public Result<String> del(String id) {
		withdrawChannelService.del(id);
		return Result.success();
	}

	@PostMapping("/findById")
	public Result<WithdrawChannelVO> findById(String id) {
		return Result.success(withdrawChannelService.findById(id));
	}

	@PostMapping("/findAll")
	public Result<List<WithdrawChannelVO>> findAll(WithdrawChannelQueryCondParam param) {
		return Result.success(withdrawChannelService.findAll(param));
	}

	@PostMapping("/findByPage")
	public Result<PageResult<WithdrawChannelVO>> findByPage(WithdrawChannelQueryCondParam param) {
		return Result.success(withdrawChannelService.findByPage(param));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_提币通道, operate = "新增或修改")
	@SaCheckPermission("withdrawChannel.save")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(AddOrUpdateWithdrawChannelParam param) {
		withdrawChannelService.addOrUpdate(param);
		return Result.success();
	}

}

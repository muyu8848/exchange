package com.exchange.controller.admin.deposit;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.deposit.param.AddOrUpdateDepositChannelParam;
import com.exchange.deposit.param.DepositChannelQueryCondParam;
import com.exchange.deposit.service.DepositChannelService;
import com.exchange.deposit.vo.DepositChannelVO;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/depositChannel")
public class DepositChannelController {

	@Autowired
	private DepositChannelService depositChannelService;

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_充币通道, operate = "删除")
	@SaCheckPermission("depositChannel.del")
	@PostMapping("/del")
	public Result<String> del(String id) {
		depositChannelService.del(id);
		return Result.success();
	}

	@PostMapping("/findById")
	public Result<DepositChannelVO> findById(String id) {
		return Result.success(depositChannelService.findById(id));
	}

	@PostMapping("/findAll")
	public Result<List<DepositChannelVO>> findAll(DepositChannelQueryCondParam param) {
		return Result.success(depositChannelService.findAll(param));
	}

	@PostMapping("/findByPage")
	public Result<PageResult<DepositChannelVO>> findByPage(DepositChannelQueryCondParam param) {
		return Result.success(depositChannelService.findDepositChannelByPage(param));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_充币通道, operate = "新增或修改")
	@SaCheckPermission("depositChannel.save")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(AddOrUpdateDepositChannelParam param) {
		depositChannelService.addOrUpdate(param);
		return Result.success();
	}

}

package com.exchange.controller.admin.currency;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.currency.param.AddOrUpdateChainNetworkParam;
import com.exchange.currency.param.ChainNetworkQueryCondParam;
import com.exchange.currency.service.ChainNetworkService;
import com.exchange.currency.vo.ChainNetworkVO;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chainNetwork")
@RequiredArgsConstructor
public class ChainNetworkController {

	private final ChainNetworkService chainNetworkService;

	@PostMapping("/findAll")
	public Result<List<ChainNetworkVO>> findAll(ChainNetworkQueryCondParam param) {
		return Result.success(chainNetworkService.findAll(param));
	}

	@PostMapping("/findByPage")
	public Result<PageResult<ChainNetworkVO>> findByPage(ChainNetworkQueryCondParam param) {
		return Result.success(chainNetworkService.findChainNetworkByPage(param));
	}

	@PostMapping("/findById")
	public Result<ChainNetworkVO> findById(String id) {
		return Result.success(chainNetworkService.findById(id));
	}

	@SaCheckPermission("chainNetwork.save")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_区块链网络, operate = "新增或修改")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(AddOrUpdateChainNetworkParam param) {
		chainNetworkService.addOrUpdate(param);
		return Result.success();
	}

	@SaCheckPermission("chainNetwork.del")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_区块链网络, operate = "删除")
	@PostMapping("/del")
	public Result<String> del(String id) {
		chainNetworkService.del(id);
		return Result.success();
	}
}

package com.exchange.controller.client.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.member.param.AssetTransferQueryCondParam;
import com.exchange.member.param.AssetTransferSubmitParam;
import com.exchange.member.service.AssetTransferService;
import com.exchange.member.vo.AssetTransferOrderVO;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/assetTransfer")
public class AssetTransferController {

	@Autowired
	private AssetTransferService assetTransferService;

	@PostMapping("/transfer")
	public Result<String> transfer(AssetTransferSubmitParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		String orderNo = assetTransferService.transfer(param);
		return Result.success(orderNo);
	}

	@PostMapping("/findByPage")
	public Result<PageResult<AssetTransferOrderVO>> findByPage(AssetTransferQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(assetTransferService.findByPage(param));
	}

}

package com.exchange.controller.client.withdraw;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.utils.IpUtils;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.member.service.MemberAssetService;
import com.exchange.member.vo.MemberAssetVO;
import com.exchange.withdraw.param.CreateWithdrawOrderParam;
import com.exchange.withdraw.param.WithdrawChannelQueryCondParam;
import com.exchange.withdraw.param.WithdrawOrderQueryCondParam;
import com.exchange.withdraw.service.WithdrawChannelService;
import com.exchange.withdraw.service.WithdrawOrderService;
import com.exchange.withdraw.vo.EnabledWithdrawChannelVO;
import com.exchange.withdraw.vo.MyWithdrawOrderVO;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/withdraw")
public class WithdrawController {

	@Autowired
	private WithdrawChannelService withdrawChannelService;

	@Autowired
	private WithdrawOrderService withdrawOrderService;

	@Autowired
	private MemberAssetService memberAssetService;

	@PostMapping("/findWithdrawableAssets")
	public Result<List<MemberAssetVO>> findWithdrawableAssets() {
		return Result.success(memberAssetService.findWithdrawableAssets(StpUtil.getLoginIdAsString()));
	}

	@PostMapping("/findEnabledChannels")
	public Result<List<EnabledWithdrawChannelVO>> findEnabledChannels(WithdrawChannelQueryCondParam param) {
		return Result.success(withdrawChannelService.findEnabledChannels(param));
	}

	@PostMapping("/sendVerificationCode")
	public Result<String> sendVerificationCode(String contactType, HttpServletRequest request) {
		withdrawOrderService.sendVerificationCode(StpUtil.getLoginIdAsString(), contactType,
				IpUtils.getClientIp(request));
		return Result.success();
	}

	@PostMapping("/createOrder")
	public Result<String> createOrder(CreateWithdrawOrderParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(withdrawOrderService.createOrder(param));
	}

	@PostMapping("/findOrderById")
	public Result<MyWithdrawOrderVO> findOrderById(String id) {
		return Result.success(withdrawOrderService.findMyWithdrawOrder(StpUtil.getLoginIdAsString(), id));
	}

	@PostMapping("/findByPage")
	public Result<PageResult<MyWithdrawOrderVO>> findByPage(WithdrawOrderQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(withdrawOrderService.findMyWithdrawOrderByPage(param));
	}

	@PostMapping("/cancelOrder")
	public Result<String> cancelOrder(String orderId) {
		withdrawOrderService.cancel(orderId, StpUtil.getLoginIdAsString());
		return Result.success();
	}

}

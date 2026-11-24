package com.exchange.controller.admin.c2c;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.c2c.param.PaymentAccountQueryCondParam;
import com.exchange.c2c.service.PaymentAccountService;
import com.exchange.c2c.vo.PaymentAccountVO;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;

import com.exchange.common.operlog.OperLog;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 支付方式
 */
@RestController
@RequestMapping("/paymentAccount")
public class PaymentAccountController {

	@Autowired
	private PaymentAccountService paymentAccountService;

	@PostMapping("/findByPage")
	@ResponseBody
	public Result<PageResult<PaymentAccountVO>> findByPage(PaymentAccountQueryCondParam param) {
		return Result.success(paymentAccountService.findByPage(param));
	}

	@SaCheckPermission("paymentAccount.del")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_支付方式, operate = "删除")
	@PostMapping("/del")
	public Result<String> del(String id) {
		paymentAccountService.del(id);
		return Result.success();
	}

}

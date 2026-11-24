package com.exchange.controller.client.c2c;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.c2c.param.AddPaymentAccountParam;
import com.exchange.c2c.param.PaymentAccountQueryCondParam;
import com.exchange.c2c.param.UpdatePaymentAccountParam;
import com.exchange.c2c.service.PaymentAccountService;
import com.exchange.c2c.vo.MyPaymentAccountVO;
import com.exchange.common.vo.Result;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/paymentAccount")
public class PaymentAccountController {

	@Autowired
	private PaymentAccountService paymentAccountService;

	@PostMapping("/findById")
	public Result<MyPaymentAccountVO> findById(String id) {
		return Result.success(paymentAccountService.findByIdForClient(id, StpUtil.getLoginIdAsString()));
	}

	@PostMapping("/findAll")
	public Result<List<MyPaymentAccountVO>> findAll(PaymentAccountQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(paymentAccountService.findAllForClient(param));
	}

	@PostMapping("/del")
	public Result<String> del(String id) {
		paymentAccountService.del(id, StpUtil.getLoginIdAsString());
		return Result.success();
	}

	@PostMapping("/add")
	public Result<String> add(@RequestBody AddPaymentAccountParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		paymentAccountService.add(param);
		return Result.success();
	}

	@PostMapping("/disable")
	public Result<String> disable(String id) {
		paymentAccountService.updateEnabled(id, StpUtil.getLoginIdAsString(), false);
		return Result.success();
	}

	@PostMapping("/enable")
	public Result<String> enable(String id) {
		paymentAccountService.updateEnabled(id, StpUtil.getLoginIdAsString(), true);
		return Result.success();
	}

	@PostMapping("/update")
	public Result<String> update(@RequestBody UpdatePaymentAccountParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		paymentAccountService.update(param);
		return Result.success();
	}

}

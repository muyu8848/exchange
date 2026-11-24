package com.exchange.controller.client.c2c;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.c2c.service.PaymentAccountTypeService;
import com.exchange.c2c.vo.EnabledPaymentAccountTypeVO;
import com.exchange.common.vo.Result;

@RestController
@RequestMapping("/paymentAccountType")
public class PaymentAccountTypeController {

	@Autowired
	private PaymentAccountTypeService paymentAccountTypeService;

	@PostMapping("/findAllEnabled")
	public Result<List<EnabledPaymentAccountTypeVO>> findAllEnabled() {
		return Result.success(paymentAccountTypeService.findEnabledForClient());
	}

	@PostMapping("/findByCode")
	public Result<EnabledPaymentAccountTypeVO> findByCode(String code) {
		return Result.success(paymentAccountTypeService.findByCodeForClient(code));
	}

}

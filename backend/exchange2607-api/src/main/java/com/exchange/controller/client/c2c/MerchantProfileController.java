package com.exchange.controller.client.c2c;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.c2c.service.MerchantProfileService;
import com.exchange.c2c.vo.MerchantProfileVO;
import com.exchange.common.vo.Result;

@RestController
@RequestMapping("/merchant")
public class MerchantProfileController {

	@Autowired
	private MerchantProfileService merchantProfileService;

	@PostMapping("/profile")
	public Result<MerchantProfileVO> getProfile(String userId) {
		return Result.success(merchantProfileService.getProfile(userId));
	}

}

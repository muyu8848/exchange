package com.exchange.controller.admin.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.verificationcode.param.VerificationCodeSendRecordQueryCondParam;
import com.exchange.verificationcode.service.VerificationCodeService;
import com.exchange.verificationcode.vo.VerificationCodeSendRecordVO;

@RestController
@RequestMapping("/verificationCodeSendRecord")
public class VerificationCodeSendRecordController {

	@Autowired
	private VerificationCodeService verificationCodeService;

	@PostMapping("/findSendRecordByPage")
	public Result<PageResult<VerificationCodeSendRecordVO>> findSendRecordByPage(
			VerificationCodeSendRecordQueryCondParam param) {
		return Result.success(verificationCodeService.findSendRecordByPage(param));
	}
}

package com.exchange.controller.admin.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.log.param.MemberContactBindLogQueryCondParam;
import com.exchange.log.service.MemberContactBindLogService;
import com.exchange.log.vo.MemberContactBindLogVO;

/**
 * 会员联系方式绑定流水
 */
@Validated
@RestController
@RequestMapping("/memberContactBindLog")
public class MemberContactBindLogController {

	@Autowired
	private MemberContactBindLogService memberContactBindLogService;

	@PostMapping("/findByPage")
	public Result<PageResult<MemberContactBindLogVO>> findByPage(MemberContactBindLogQueryCondParam param) {
		return Result.success(memberContactBindLogService.findMemberContactBindLogByPage(param));
	}
}

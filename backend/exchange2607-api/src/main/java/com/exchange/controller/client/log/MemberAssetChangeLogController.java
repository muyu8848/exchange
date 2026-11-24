package com.exchange.controller.client.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.log.param.MemberAssetChangeLogQueryCondParam;
import com.exchange.log.service.MemberAssetChangeLogService;
import com.exchange.log.vo.MyAssetChangeLogDetailVO;
import com.exchange.log.vo.MyAssetChangeLogVO;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/memberAssetChangeLog")
public class MemberAssetChangeLogController {

	@Autowired
	private MemberAssetChangeLogService memberAssetChangeLogService;

	@PostMapping("/getDetail")
	public Result<MyAssetChangeLogDetailVO> getDetail(String id) {
		return Result.success(memberAssetChangeLogService.getDetail(id, StpUtil.getLoginIdAsString()));
	}

	@PostMapping("/findByPage")
	public Result<PageResult<MyAssetChangeLogVO>> findByPage(MemberAssetChangeLogQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(memberAssetChangeLogService.findMyAssetChangeLogByPage(param));
	}

}

package com.exchange.controller.client.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.member.param.MyTeamMemberQueryParam;
import com.exchange.member.service.MemberTeamService;
import com.exchange.member.vo.MyDirectTeamOverviewVO;
import com.exchange.member.vo.MyTeamMemberVO;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 客户端我的好友（直推）相关接口
 */
@RestController
@RequestMapping("/team")
public class MemberTeamController {

	@Autowired
	private MemberTeamService memberTeamService;

	/** 我的好友概览：直推人数 + 今日新增直推 */
	@PostMapping("/findMyDirectOverview")
	public Result<MyDirectTeamOverviewVO> findMyDirectOverview() {
		return Result.success(memberTeamService.findDirectTeamOverview(StpUtil.getLoginIdAsString()));
	}

	/** 我的直推成员分页（服务端强制当前会员 + 直推，忽略客户端传入的 ancestorId / level） */
	@PostMapping("/findMyTeamDirectMemberByPage")
	public Result<PageResult<MyTeamMemberVO>> findMyTeamDirectMemberByPage(MyTeamMemberQueryParam param) {
		return Result.success(memberTeamService.findDirectMemberByPage(param, StpUtil.getLoginIdAsString()));
	}

}

package com.exchange.controller.client.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.notification.param.MemberNotificationQueryCondParam;
import com.exchange.notification.service.MemberNotificationService;
import com.exchange.notification.vo.MyNotificationVO;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/memberNotification")
public class MemberNotificationController {

	@Autowired
	private MemberNotificationService memberNotificationService;

	@PostMapping("/findByPage")
	public Result<PageResult<MyNotificationVO>> findByPage(MemberNotificationQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(memberNotificationService.findMemberPage(param));
	}

	@PostMapping("/findPendingPopup")
	public Result<MyNotificationVO> findPendingPopup() {
		return Result.success(memberNotificationService.findPendingPopup(StpUtil.getLoginIdAsString()));
	}

	@PostMapping("/countUnread")
	public Result<Long> countUnread() {
		return Result.success(memberNotificationService.countUnread(StpUtil.getLoginIdAsString()));
	}

	@PostMapping("/markRead")
	public Result<String> markRead(String id) {
		memberNotificationService.markRead(StpUtil.getLoginIdAsString(), id);
		return Result.success();
	}

	@PostMapping("/markAllRead")
	public Result<String> markAllRead() {
		memberNotificationService.markAllRead(StpUtil.getLoginIdAsString());
		return Result.success();
	}

}

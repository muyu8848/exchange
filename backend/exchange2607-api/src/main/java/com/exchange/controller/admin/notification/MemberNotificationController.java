package com.exchange.controller.admin.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.notification.param.MemberNotificationQueryCondParam;
import com.exchange.notification.param.SendMemberNotificationParam;
import com.exchange.notification.service.MemberNotificationService;
import com.exchange.notification.vo.MemberNotificationVO;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/memberNotification")
public class MemberNotificationController {

	@Autowired
	private MemberNotificationService memberNotificationService;

	@PostMapping("/findByPage")
	public Result<PageResult<MemberNotificationVO>> findByPage(MemberNotificationQueryCondParam param) {
		return Result.success(memberNotificationService.findAdminPage(param));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_会员消息, operate = "发送系统消息")
	@SaCheckPermission("memberNotification.send")
	@PostMapping("/sendSystemMessage")
	public Result<String> sendSystemMessage(SendMemberNotificationParam param) {
		memberNotificationService.sendSystemMessage(param);
		return Result.success();
	}

}

package com.exchange.controller.admin.c2c;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.c2c.param.C2cChatRecordQueryCondParam;
import com.exchange.c2c.param.CustomerServiceSendMsgParam;
import com.exchange.c2c.service.C2cChatService;
import com.exchange.c2c.vo.C2cChatRecordVO;
import com.exchange.common.vo.Result;

import com.exchange.common.operlog.OperLog;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * C2C 聊天管理（运营后台）
 */
@RestController
@RequestMapping("/c2cChat")
public class C2cChatController {

	@Autowired
	private C2cChatService c2cChatService;

	/**
	 * 客服发送消息
	 */
	@SaCheckPermission("c2cChat.send")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_C2C订单, operate = "客服发送消息")
	@PostMapping("/customerServiceSendMsg")
	public Result<String> customerServiceSendMsg(CustomerServiceSendMsgParam param) {
		c2cChatService.customerServiceSendMsg(param);
		return Result.success();
	}

	/**
	 * 获取聊天记录
	 */
	@PostMapping("/findChatRecord")
	public Result<List<C2cChatRecordVO>> findChatRecord(C2cChatRecordQueryCondParam param) {
		return Result.success(c2cChatService.findChatRecord(param));
	}

}

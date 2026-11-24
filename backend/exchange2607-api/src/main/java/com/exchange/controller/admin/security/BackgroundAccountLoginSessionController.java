package com.exchange.controller.admin.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.security.param.AccountLoginSessionQueryCondParam;
import com.exchange.security.service.AccountLoginSessionService;
import com.exchange.security.vo.AccountLoginSessionVO;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/backgroundAccountLoginSession")
public class BackgroundAccountLoginSessionController {

	@Autowired
	private AccountLoginSessionService accountLoginSessionService;

	@PostMapping("/findByPage")
	public Result<PageResult<AccountLoginSessionVO>> findByPage(AccountLoginSessionQueryCondParam param) {
		param.setAccountType(Constant.登录会话账号类型_后台);
		return Result.success(accountLoginSessionService.findByPage(param));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_后台账号会话, operate = "强制下线")
	@SaCheckPermission("backgroundAccountLoginSession.kickout")
	@PostMapping("/kickout")
	public Result<String> kickout(String id) {
		accountLoginSessionService.kickout(id, Constant.登录会话账号类型_后台);
		return Result.success();
	}
}

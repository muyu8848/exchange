package com.exchange.controller.admin.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.log.param.LoginLogQueryCondParam;
import com.exchange.log.service.LoginLogService;
import com.exchange.log.vo.LoginLogVO;

/**
 * 登录日志
 */
@RestController
@RequestMapping("/loginLog")
public class LoginLogController {

	@Autowired
	private LoginLogService loginLogService;

	/**
	 * 分页获取登录日志
	 * @param param
	 * @return
	 */
	@PostMapping("/findLoginLogByPage")
	public Result<PageResult<LoginLogVO>> findLoginLogByPage(LoginLogQueryCondParam param) {
		return Result.success(loginLogService.findLoginLogByPage(param));
	}

}

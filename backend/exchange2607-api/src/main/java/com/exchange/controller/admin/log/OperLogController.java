package com.exchange.controller.admin.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.log.param.OperLogQueryCondParam;
import com.exchange.log.service.OperLogService;
import com.exchange.log.vo.OperLogVO;

/**
 * 操作日志
 */
@RestController
@RequestMapping("/operLog")
public class OperLogController {

	@Autowired
	private OperLogService operLogService;

	/**
	 * 分页获取操作日志
	 * @param param
	 * @return
	 */
	@PostMapping("/findOperLogByPage")
	public Result<PageResult<OperLogVO>> findOperLogByPage(OperLogQueryCondParam param) {
		return Result.success(operLogService.findOperLogByPage(param));
	}

}

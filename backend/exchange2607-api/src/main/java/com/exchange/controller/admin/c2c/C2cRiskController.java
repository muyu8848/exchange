package com.exchange.controller.admin.c2c;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.c2c.param.C2cRiskRecordQueryCondParam;
import com.exchange.c2c.service.C2cRiskService;
import com.exchange.c2c.vo.C2cRiskRecordVO;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;

/**
 * 风控记录
 */
@RestController
@RequestMapping("/c2cRisk")
public class C2cRiskController {

	@Autowired
	private C2cRiskService riskService;

	/**
	 * 获取风控记录
	 * @param param
	 * @return
	 */
	@PostMapping("/findByPage")
	public Result<PageResult<C2cRiskRecordVO>> findRiskRecordByPage(C2cRiskRecordQueryCondParam param) {
		return Result.success(riskService.findRiskRecordByPage(param));
	}

}

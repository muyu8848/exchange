package com.exchange.controller.client.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.content.param.UsingHelpQueryCondParam;
import com.exchange.content.service.UsingHelpService;
import com.exchange.content.vo.UsingHelpAbstractVO;
import com.exchange.content.vo.UsingHelpDetailVO;

@RestController
@RequestMapping("/usingHelp")
public class UsingHelpController {

	@Autowired
	private UsingHelpService usingHelpService;

	/** 帮助详情（含正文） */
	@PostMapping("/getUsingHelpDetail")
	public Result<UsingHelpDetailVO> getUsingHelpDetail(String id) {
		return Result.success(usingHelpService.getUsingHelpDetail(id));
	}

	/** 帮助列表（不含正文，分页） */
	@PostMapping("/findUsingHelpByPage")
	public Result<PageResult<UsingHelpAbstractVO>> findUsingHelpByPage(UsingHelpQueryCondParam param) {
		return Result.success(usingHelpService.findUsingHelpAbstractByPage(param));
	}

}

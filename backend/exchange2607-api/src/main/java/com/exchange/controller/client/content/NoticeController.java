package com.exchange.controller.client.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.content.param.NoticeQueryCondParam;
import com.exchange.content.service.NoticeService;
import com.exchange.content.vo.NoticeAbstractVO;
import com.exchange.content.vo.NoticeDetailVO;

@RestController
@RequestMapping("/notice")
public class NoticeController {

	@Autowired
	private NoticeService noticeService;

	@PostMapping("/getNoticeDetail")
	public Result<NoticeDetailVO> getNoticeDetail(String id) {
		return Result.success(noticeService.getNoticeDetail(id));
	}

	/** 公告列表（摘要，分页） */
	@PostMapping("/findNoticeByPage")
	public Result<PageResult<NoticeAbstractVO>> findNoticeByPage(NoticeQueryCondParam param) {
		return Result.success(noticeService.findNoticeAbstractByPage(param));
	}

}

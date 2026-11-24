package com.exchange.controller.admin.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.content.param.AddOrUpdateNoticeParam;
import com.exchange.content.param.NoticeQueryCondParam;
import com.exchange.content.service.NoticeService;
import com.exchange.content.vo.NoticeVO;

import com.exchange.common.operlog.OperLog;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 站内公告
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

	@Autowired
	private NoticeService noticeService;

	/**
	 * 根据id获取公告
	 * 
	 * @param id
	 * @return
	 */
	@PostMapping("/findById")
	public Result<NoticeVO> findById(String id) {
		return Result.success(noticeService.findById(id));
	}

	/**
	 * 删除
	 * 
	 * @param id
	 * @return
	 */
	@SaCheckPermission("notice.del")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_公告, operate = "删除")
	@PostMapping("/delById")
	public Result<String> delById(String id) {
		noticeService.delById(id);
		return Result.success();
	}

	/**
	 * 新增或编辑
	 * 
	 * @param param
	 * @return
	 */
	@SaCheckPermission("notice.save")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_公告, operate = "新增或修改")
	@PostMapping("/addOrUpdateNotice")
	public Result<String> addOrUpdateNotice(@RequestBody AddOrUpdateNoticeParam param) {
		noticeService.addOrUpdateNotice(param);
		return Result.success();
	}

	/**
	 * 分页获取公告
	 * 
	 * @param param
	 * @return
	 */
	@PostMapping("/findByPage")
	public Result<PageResult<NoticeVO>> findByPage(NoticeQueryCondParam param) {
		return Result.success(noticeService.findByPage(param));
	}

}

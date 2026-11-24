package com.exchange.controller.admin.setting;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.multilang.param.AddOrUpdateMultiLangParam;
import com.exchange.multilang.param.MultiLangQueryCondParam;
import com.exchange.multilang.service.MultiLangService;
import com.exchange.multilang.vo.MultiLangVO;

import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 多语言维护。
 * <p>
 * 只维护「语言清单」；文案本身在 i18n/messages_*.properties，新增语言需同步提供文案文件。
 */
@RestController
@RequestMapping("/multiLang")
public class MultiLangController {

	@Autowired
	private MultiLangService multiLangService;

	@PostMapping("/findById")
	public Result<MultiLangVO> findById(String id) {
		return Result.success(multiLangService.findById(id));
	}

	@PostMapping("/findAll")
	public Result<List<MultiLangVO>> findAll(MultiLangQueryCondParam param) {
		return Result.success(multiLangService.findAll(param));
	}

	@PostMapping("/findByPage")
	public Result<PageResult<MultiLangVO>> findByPage(MultiLangQueryCondParam param) {
		return Result.success(multiLangService.findByPage(param));
	}

	/** data 为文案文件缺失提示，无需提示时为空 */
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_多语言, operate = "新增或修改")
	@SaCheckPermission("multiLang.save")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(AddOrUpdateMultiLangParam param) {
		return Result.success(multiLangService.addOrUpdate(param));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_多语言, operate = "删除")
	@SaCheckPermission("multiLang.del")
	@PostMapping("/del")
	public Result<String> del(String id) {
		multiLangService.del(id);
		return Result.success();
	}

}

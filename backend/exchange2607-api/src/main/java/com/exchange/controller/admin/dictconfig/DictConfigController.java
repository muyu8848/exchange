package com.exchange.controller.admin.dictconfig;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.dictconfig.DictHolder;
import com.exchange.dictconfig.param.AddOrUpdateDictTypeParam;
import com.exchange.dictconfig.param.DictTypeQueryCondParam;
import com.exchange.dictconfig.param.UpdateDictDataParam;
import com.exchange.dictconfig.service.DictService;
import com.exchange.dictconfig.vo.DictItemVO;
import com.exchange.dictconfig.vo.DictTypeVO;

import cn.dev33.satoken.annotation.SaCheckPermission;

/**
 * 数据字典
 */
@RestController
@RequestMapping("/dictconfig")
public class DictConfigController {

	@Autowired
	private DictService dictService;

	/**
	 * 更新字典数据
	 * 
	 * @param param
	 * @return
	 */
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_字典管理, operate = "更新字典数据", permissionName = "字典维护")
	@SaCheckPermission("dictConfig.save")
	@PostMapping("/updateDictData")
	public Result<String> updateDictData(@RequestBody UpdateDictDataParam param) {
		dictService.updateDictData(param);
		return Result.success();
	}

	/**
	 * 根据字典类型id获取字典数据
	 * 
	 * @param dictTypeId
	 * @return
	 */
	@PostMapping("/findDictItemByDictTypeId")
	public Result<List<DictItemVO>> findDictItemByDictTypeId(String dictTypeId) {
		return Result.success(dictService.findDictItemByDictTypeId(dictTypeId));
	}

	/**
	 * 删除字典
	 * 
	 * @param id
	 * @return
	 */
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_字典管理, operate = "删除字典")
	@SaCheckPermission("dictConfig.del")
	@PostMapping("/delDictType")
	public Result<String> delDictType(String id) {
		dictService.delDictType(id);
		return Result.success();
	}

	/**
	 * 添加或修改字典
	 * 
	 * @param param
	 * @return
	 */
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_字典管理, operate = "添加或修改字典", permissionName = "字典维护")
	@SaCheckPermission("dictConfig.save")
	@PostMapping("/addOrUpdateDictType")
	public Result<String> addOrUpdateDictType(AddOrUpdateDictTypeParam param) {
		dictService.addOrUpdateDictType(param);
		return Result.success();
	}

	/**
	 * 根据id获取字典类型
	 * 
	 * @param id
	 * @return
	 */
	@PostMapping("/findDictTypeById")
	public Result<DictTypeVO> findDictTypeById(String id) {
		return Result.success(dictService.findDictTypeById(id));
	}

	/**
	 * 分页获取字典类型
	 * 
	 * @param param
	 * @return
	 */
	@PostMapping("/findDictTypeByPage")
	public Result<PageResult<DictTypeVO>> findDictTypeByPage(DictTypeQueryCondParam param) {
		return Result.success(dictService.findDictTypeByPage(param));
	}

	/**
	 * 根据字典类型code获取字典数据
	 * 
	 * @param dictTypeCode
	 * @return
	 */
	@PostMapping("/findDictItemInCache")
	public Result<List<DictItemVO>> findDictItemInCache(String dictTypeCode) {
		return Result.success(DictHolder.findDictItem(dictTypeCode));
	}

}

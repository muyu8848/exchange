package com.exchange.controller.admin.background;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.background.param.MenuParam;
import com.exchange.background.param.SyncPermissionParam;
import com.exchange.background.service.MenuService;
import com.exchange.background.vo.MenuVO;
import com.exchange.background.vo.PermissionCodeVO;
import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/menu")
public class MenuController {

	@Autowired
	private MenuService menuService;

	@Autowired
	private PermissionSyncService permissionSyncService;

	@PostMapping("/findById")
	public Result<MenuVO> findById(String id) {
		return Result.success(menuService.findById(id));
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_菜单管理, operate = "删除")
	@SaCheckPermission("menu.del")
	@PostMapping("/del")
	public Result<String> del(String id) {
		menuService.del(id);
		return Result.success();
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_菜单管理, operate = "新增或修改", permissionName = "菜单维护（含排序）")
	@SaCheckPermission("menu.save")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(MenuParam param) {
		menuService.addOrUpdate(param);
		return Result.success();
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_菜单管理, operate = "上移", permissionName = "菜单维护（含排序）")
	@SaCheckPermission("menu.save")
	@PostMapping("/moveUp")
	public Result<String> moveUp(String id) {
		menuService.moveUp(id);
		return Result.success();
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_菜单管理, operate = "下移", permissionName = "菜单维护（含排序）")
	@SaCheckPermission("menu.save")
	@PostMapping("/moveDown")
	public Result<String> moveDown(String id) {
		menuService.moveDown(id);
		return Result.success();
	}

	@PostMapping("/findMenuTree")
	public Result<List<MenuVO>> findMenuTree() {
		return Result.success(menuService.findMenuTree());
	}

	@PostMapping("/findUnsyncedPermissions")
	public Result<List<PermissionCodeVO>> findUnsyncedPermissions() {
		return Result.success(permissionSyncService.findUnsynced());
	}

	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_菜单管理, operate = "同步权限码", permissionName = "菜单维护（含排序）")
	@SaCheckPermission("menu.save")
	@PostMapping("/syncPermissions")
	public Result<String> syncPermissions(@RequestBody SyncPermissionParam param) {
		permissionSyncService.syncPermissions(param.getCodes());
		return Result.success();
	}

}

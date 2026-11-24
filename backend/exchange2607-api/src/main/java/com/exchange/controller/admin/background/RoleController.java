package com.exchange.controller.admin.background;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.background.param.AssignMenuParam;
import com.exchange.background.param.RoleParam;
import com.exchange.background.service.RoleService;
import com.exchange.background.vo.MenuVO;
import com.exchange.background.vo.RoleVO;
import com.exchange.common.operlog.OperLog;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.security.service.BackgroundAccountLoginSessionService;

import cn.dev33.satoken.annotation.SaCheckPermission;

@RestController
@RequestMapping("/role")
public class RoleController {

	@Autowired
	private RoleService roleService;

	@Autowired
	private BackgroundAccountLoginSessionService backgroundAccountLoginSessionService;

	@SaCheckPermission("role.assignMenu")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_角色管理, operate = "分配菜单")
	@PostMapping("/assignMenu")
	public Result<String> assignMenu(@RequestBody AssignMenuParam param) {
		boolean changed = roleService.assignMenu(param);
		if (changed) {
			backgroundAccountLoginSessionService.kickoutByRoleId(param.getRoleId(), Constant.强制下线原因_权限变更);
		}
		return Result.success();
	}

	@PostMapping("/findMenuByRoleId")
	public Result<List<MenuVO>> findMenuByRoleId(String roleId) {
		return Result.success(roleService.findMenuByRoleId(roleId));
	}

	@PostMapping("/findAll")
	public Result<List<RoleVO>> findAll() {
		return Result.success(roleService.findAll());
	}

	@SaCheckPermission("role.save")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_角色管理, operate = "新增或修改")
	@PostMapping("/addOrUpdate")
	public Result<String> addOrUpdate(RoleParam param) {
		roleService.addOrUpdate(param);
		return Result.success();
	}

	@SaCheckPermission("role.del")
	@OperLog(subSystem = Constant.子系统_后台管理, module = Constant.模块_角色管理, operate = "删除")
	@PostMapping("/del")
	public Result<String> del(String id) {
		roleService.del(id);
		backgroundAccountLoginSessionService.kickoutByRoleId(id, Constant.强制下线原因_权限变更);
		return Result.success();
	}

	@PostMapping("/findById")
	public Result<RoleVO> findById(String id) {
		return Result.success(roleService.findById(id));
	}

}

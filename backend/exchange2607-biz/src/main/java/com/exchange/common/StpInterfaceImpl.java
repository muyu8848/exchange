package com.exchange.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.exchange.constants.Constant;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;

@Component
public class StpInterfaceImpl implements StpInterface {

	@Override
	@SuppressWarnings("unchecked")
	public List<String> getPermissionList(Object loginId, String loginType) {
		// 仅后台管理账号参与按钮级权限；客户端会员不走菜单权限体系
		String subSystem = StpUtil.getSession().getString("subSystem");
		if (!Constant.子系统_后台管理.equals(subSystem)) {
			return new ArrayList<String>();
		}
		// 登录时已写入 session（见 BackgroundAccountService.buildLoginTokenInfo），此处直接复用，避免每请求查库
		Object cached = StpUtil.getSession().get("permissions");
		if (cached instanceof List) {
			return (List<String>) cached;
		}
		return new ArrayList<String>();
	}

	@Override
	public List<String> getRoleList(Object loginId, String loginType) {
		List<String> list = new ArrayList<String>();
		String subSystem = StpUtil.getSession().getString("subSystem");
		list.add(subSystem);
		return list;
	}

}

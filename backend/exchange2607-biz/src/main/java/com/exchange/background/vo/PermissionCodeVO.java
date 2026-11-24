package com.exchange.background.vo;

import lombok.Data;

/**
 * 反射扫描出的按钮权限码（用于「同步权限码」弹窗与批量导入）
 */
@Data
public class PermissionCodeVO {

	/** 权限码，如 spotSymbol.save */
	private String code;

	/** 权限名，如「删除」 */
	private String name;

	/** 目标二级菜单 id */
	private String targetMenuId;

	/** 目标二级菜单名 */
	private String targetMenuName;

	/** 目标二级菜单 url */
	private String targetMenuUrl;

	/** 是否匹配到目标菜单 */
	private boolean matched;

}

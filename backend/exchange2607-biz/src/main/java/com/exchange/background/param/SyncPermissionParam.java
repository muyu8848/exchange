package com.exchange.background.param;

import java.util.List;

import lombok.Data;

/**
 * 批量导入权限码参数
 */
@Data
public class SyncPermissionParam {

	/** 要导入的权限码列表 */
	private List<String> codes;

}

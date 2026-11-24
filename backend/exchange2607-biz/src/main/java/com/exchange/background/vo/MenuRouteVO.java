package com.exchange.background.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.exchange.background.domain.Menu;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

@Data
public class MenuRouteVO {

	private String path;

	private String name;

	private String component;

	private String redirect;

	private Integer sortNum;

	private Map<String, Object> meta = new HashMap<>();

	private List<MenuRouteVO> children = new ArrayList<MenuRouteVO>();

	public static MenuRouteVO convertFor(Menu menu) {
		MenuRouteVO vo = new MenuRouteVO();
		vo.setPath(menu.getUrl());
		vo.setName(StrUtil.upperFirst(StrUtil.toCamelCase(menu.getUrl(), '-')));
		vo.setComponent("/" + menu.getUrl() + "/index");
		vo.setSortNum(menu.getSortNum());
		vo.getMeta().put("title", menu.getName());
		return vo;
	}

}

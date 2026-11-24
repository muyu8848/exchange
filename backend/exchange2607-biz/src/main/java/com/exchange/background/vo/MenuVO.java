package com.exchange.background.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.background.domain.Menu;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class MenuVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private String name;

	private String url;

	private String type;

	private Integer sortNum;

	private String parentId;

	private String parentName;

	private List<MenuVO> subMenus = new ArrayList<MenuVO>();

	public static List<MenuVO> convertFor(List<Menu> menus) {
		if (CollectionUtil.isEmpty(menus)) {
			return new ArrayList<>();
		}
		List<MenuVO> vos = new ArrayList<>();
		for (Menu menu : menus) {
			vos.add(convertFor(menu));
		}
		return vos;
	}

	public static MenuVO convertFor(Menu menu) {
		if (menu == null) {
			return null;
		}
		MenuVO vo = new MenuVO();
		BeanUtils.copyProperties(menu, vo);
		Menu parent = menu.getParent();
		if (parent != null) {
			vo.setParentName(parent.getName());
		}
		return vo;
	}

}

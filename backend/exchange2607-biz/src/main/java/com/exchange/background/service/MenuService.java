package com.exchange.background.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.background.domain.Menu;
import com.exchange.background.param.MenuParam;
import com.exchange.background.repo.MenuRepo;
import com.exchange.background.vo.MenuVO;
import com.exchange.common.exception.BizException;
import com.exchange.constants.Constant;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class MenuService {

	@Autowired
	private MenuRepo menuRepo;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public MenuVO findById(@NotBlank String id) {
		return MenuVO.convertFor(menuRepo.getOne(id));
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void del(@NotBlank String id) {
		List<Menu> subMenus = menuRepo.findByParentIdAndDeletedFlagFalse(id);
		for (Menu subMenu : subMenus) {
			List<Menu> btns = menuRepo.findByParentIdAndDeletedFlagFalse(subMenu.getId());
			for (Menu btn : btns) {
				btn.deleted();
				menuRepo.save(btn);
			}
		}
		if (CollectionUtil.isNotEmpty(subMenus)) {
			for (Menu subMenu : subMenus) {
				subMenu.deleted();
				menuRepo.save(subMenu);
			}
		}
		Menu menu = menuRepo.getOne(id);
		menu.deleted();
		menuRepo.save(menu);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void addOrUpdate(@Valid MenuParam param) {
		if (Constant.菜单类型_一级菜单.equals(param.getType()) || Constant.菜单类型_二级菜单.equals(param.getType())) {
			if (StrUtil.isBlank(param.getUrl())) {
				throw new BizException("请输入url");
			}
		}
		if (Constant.菜单类型_权限.equals(param.getType())) {
			if (StrUtil.isBlank(param.getUrl())) {
				throw new BizException("请输入权限标识");
			}
			if (StrUtil.isBlank(param.getParentId())
					|| !menuRepo.existsByIdAndTypeAndDeletedFlagFalse(param.getParentId(), Constant.菜单类型_二级菜单)) {
				throw new BizException("权限只能挂在二级菜单下");
			}
		}
		LocalDateTime now = LocalDateTime.now();
		if (StrUtil.isBlank(param.getId())) {
			Menu menu = param.convertToPo();
			if (menu.getSortNum() == null) {
				menu.setSortNum(nextSortNum(menu.getParentId()));
			}
			menu.setCreateTime(now);
			menu.setUpdateTime(now);
			menuRepo.save(menu);
		} else {
			Menu menu = menuRepo.getOne(param.getId());
			BeanUtils.copyProperties(param, menu);
			menu.setUpdateTime(now);
			menuRepo.save(menu);
		}
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void moveUp(@NotBlank String id) {
		moveOrder(id, -1);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void moveDown(@NotBlank String id) {
		moveOrder(id, 1);
	}

	private void moveOrder(String id, int direction) {
		Menu menu = menuRepo.getOne(id);
		String parentId = normalizeParentId(menu.getParentId());
		String type = menu.getType();
		List<Menu> typedSiblings = findSiblings(parentId).stream().filter(m -> type.equals(m.getType()))
				.sorted(Comparator.comparingInt(m -> m.getSortNum() != null ? m.getSortNum() : 0))
				.collect(Collectors.toList());
		int idx = -1;
		for (int i = 0; i < typedSiblings.size(); i++) {
			if (typedSiblings.get(i).getId().equals(id)) {
				idx = i;
				break;
			}
		}
		if (idx < 0) {
			throw new BizException("菜单不存在");
		}
		int targetIdx = idx + direction;
		if (targetIdx < 0) {
			throw new BizException("已在同级最上方");
		}
		if (targetIdx >= typedSiblings.size()) {
			throw new BizException("已在同级最下方");
		}
		Menu current = typedSiblings.get(idx);
		Menu adjacent = typedSiblings.get(targetIdx);
		Integer currentSortNum = current.getSortNum();
		Integer adjacentSortNum = adjacent.getSortNum();
		LocalDateTime now = LocalDateTime.now();
		current.setSortNum(adjacentSortNum);
		current.setUpdateTime(now);
		adjacent.setSortNum(currentSortNum);
		adjacent.setUpdateTime(now);
		menuRepo.save(current);
		menuRepo.save(adjacent);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<MenuVO> findMenuTree() {
		List<Menu> menus = menuRepo.findByDeletedFlagFalseOrderBySortNum();
		List<MenuVO> menuVOs = MenuVO.convertFor(menus);
		return buildMenuTree(menuVOs);
	}

	public List<MenuVO> buildMenuTree(List<MenuVO> menuVOs) {
		List<MenuVO> menu1s = new ArrayList<>();
		List<MenuVO> menu2s = new ArrayList<>();
		Map<String, List<MenuVO>> menu3Map = new HashMap<>();
		for (MenuVO m : menuVOs) {
			if (Constant.菜单类型_一级菜单.equals(m.getType())) {
				menu1s.add(m);
			}
			if (Constant.菜单类型_二级菜单.equals(m.getType())) {
				menu2s.add(m);
			}
			if (Constant.菜单类型_权限.equals(m.getType())) {
				List<MenuVO> menu3s = menu3Map.get(m.getParentId());
				if (CollUtil.isEmpty(menu3s)) {
					menu3Map.put(m.getParentId(), new ArrayList<>());
				}
				menu3Map.get(m.getParentId()).add(m);
			}
		}
		for (MenuVO menu1 : menu1s) {
			for (MenuVO menu2 : menu2s) {
				if (menu1.getId().equals(menu2.getParentId())) {
					menu2.setParentId(menu1.getId());
					menu1.getSubMenus().add(menu2);
				}
			}
		}
		for (MenuVO menu2 : menu2s) {
			List<MenuVO> permissions = menu3Map.get(menu2.getId());
			if (CollUtil.isNotEmpty(permissions)) {
				for (MenuVO permission : permissions) {
					permission.setParentId(menu2.getId());
				}
				menu2.getSubMenus().addAll(permissions);
			}
		}
		sortMenuTree(menu1s);
		return menu1s;
	}

	private void sortMenuTree(List<MenuVO> menus) {
		if (CollUtil.isEmpty(menus)) {
			return;
		}
		menus.sort(Comparator.comparingInt(m -> m.getSortNum() != null ? m.getSortNum() : 0));
		for (MenuVO menu : menus) {
			sortMenuTree(menu.getSubMenus());
		}
	}

	private List<Menu> findSiblings(String parentId) {
		String normalizedParentId = normalizeParentId(parentId);
		if (StrUtil.isBlank(normalizedParentId)) {
			return menuRepo.findByDeletedFlagFalseOrderBySortNum().stream()
					.filter(m -> StrUtil.isBlank(m.getParentId()))
					.collect(Collectors.toList());
		}
		return menuRepo.findByParentIdAndDeletedFlagFalse(normalizedParentId);
	}

	private int nextSortNum(String parentId) {
		return findSiblings(parentId).stream()
				.mapToInt(m -> m.getSortNum() != null ? m.getSortNum() : 0)
				.max()
				.orElse(0) + 1;
	}

	private String normalizeParentId(String parentId) {
		return StrUtil.blankToDefault(parentId, "");
	}

}

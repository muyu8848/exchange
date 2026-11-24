package com.exchange.background.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.background.domain.BackgroundAccount;
import com.exchange.dictconfig.DictHolder;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class BackgroundAccountVO {

	/**
	 * id
	 */
	private String id;

	/**
	 * 账号
	 */
	private String userName;

	/**
	 * 启用状态 code
	 */
	private String status;

	/**
	 * 启用状态 name
	 */
	private String statusName;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	/**
	 * 最近登录时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime latelyLoginTime;

	/**
	 * 是否为超级管理员
	 */
	private Boolean superAdminFlag;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime googleAuthBindTime;

	public static List<BackgroundAccountVO> convertFor(List<BackgroundAccount> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<BackgroundAccountVO> vos = new ArrayList<>();
		for (BackgroundAccount userAccount : pos) {
			vos.add(convertFor(userAccount));
		}
		return vos;
	}

	public static BackgroundAccountVO convertFor(BackgroundAccount po) {
		if (po == null) {
			return null;
		}
		BackgroundAccountVO vo = new BackgroundAccountVO();
		BeanUtils.copyProperties(po, vo);
		vo.setStatusName(DictHolder.getDictItemName("enableStatus", vo.getStatus()));
		return vo;
	}

}

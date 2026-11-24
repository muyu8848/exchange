package com.exchange.security.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.security.domain.AccountLoginSession;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class AccountLoginSessionVO {

	/** 会话主键，用于强制下线 */
	private String id;

	private String accountId;

	private String accountType;

	private Long accountUid;

	private String accountName;

	private String deviceType;

	private String deviceTypeName;

	private String ip;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime loginTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime lastActiveTime;

	private String status;

	private String statusName;

	/** 会话结束时间：登出/踢出/顶下线/过期 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime logoutTime;

	public static List<AccountLoginSessionVO> convertFor(List<AccountLoginSession> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<AccountLoginSessionVO> vos = new ArrayList<>();
		for (AccountLoginSession po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static AccountLoginSessionVO convertFor(AccountLoginSession po) {
		if (po == null) {
			return null;
		}
		AccountLoginSessionVO vo = new AccountLoginSessionVO();
		BeanUtils.copyProperties(po, vo);
		vo.setDeviceTypeName(DictHolder.getDictItemName("loginDeviceType", po.getDeviceType()));
		vo.setStatusName(DictHolder.getDictItemName("accountLoginSessionStatus", po.getStatus()));
		return vo;
	}
}

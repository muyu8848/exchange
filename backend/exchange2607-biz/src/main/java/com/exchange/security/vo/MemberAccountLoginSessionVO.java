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
public class MemberAccountLoginSessionVO {

	/** 会话主键，用于踢出 */
	private String id;

	/** 设备类型：APP / WEB */
	private String deviceType;

	private String deviceTypeName;

	/** 登录IP */
	private String ip;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime loginTime;

	/** 是否当前会话 */
	private Boolean current;

	public static List<MemberAccountLoginSessionVO> convertFor(List<AccountLoginSession> pos, String currentToken) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MemberAccountLoginSessionVO> vos = new ArrayList<>();
		for (AccountLoginSession po : pos) {
			vos.add(convertFor(po, currentToken));
		}
		return vos;
	}

	public static MemberAccountLoginSessionVO convertFor(AccountLoginSession po, String currentToken) {
		if (po == null) {
			return null;
		}
		MemberAccountLoginSessionVO vo = new MemberAccountLoginSessionVO();
		BeanUtils.copyProperties(po, vo);
		vo.setDeviceTypeName(DictHolder.getDictItemName("loginDeviceType", po.getDeviceType()));
		vo.setCurrent(currentToken != null && currentToken.equals(po.getToken()));
		return vo;
	}
}

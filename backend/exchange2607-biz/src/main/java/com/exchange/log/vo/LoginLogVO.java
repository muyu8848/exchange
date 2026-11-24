package com.exchange.log.vo;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.log.domain.LoginLog;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class LoginLogVO {

	private String id;

	private String subSystem;
	
	private String subSystemName;

	private String status;

	private String statusName;

	private String ip;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime loginTime;

	private String browser;

	private String os;

	private String msg;

	private String userName;

	private String accountId;

	public static List<LoginLogVO> convertFor(List<LoginLog> loginLogs) {
		if (CollectionUtil.isEmpty(loginLogs)) {
			return new ArrayList<>();
		}
		List<LoginLogVO> vos = new ArrayList<>();
		for (LoginLog loginLog : loginLogs) {
			vos.add(convertFor(loginLog));
		}
		return vos;
	}

	public static LoginLogVO convertFor(LoginLog loginLog) {
		if (loginLog == null) {
			return null;
		}
		LoginLogVO vo = new LoginLogVO();
		BeanUtils.copyProperties(loginLog, vo);
		vo.setStatusName(DictHolder.getDictItemName("loginStatus", vo.getStatus()));
		vo.setSubSystemName(DictHolder.getDictItemName("subSystem", vo.getSubSystem()));
		return vo;
	}

}

package com.exchange.log.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.log.domain.LoginLog;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class MyLoginLogVO {

	private String id;

	private String status;

	private String statusName;

	private String ip;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime loginTime;

	private String browser;

	private String os;

	private String msg;

	public static List<MyLoginLogVO> convertFor(List<LoginLog> loginLogs) {
		if (CollectionUtil.isEmpty(loginLogs)) {
			return new ArrayList<>();
		}
		List<MyLoginLogVO> vos = new ArrayList<>();
		for (LoginLog loginLog : loginLogs) {
			vos.add(convertFor(loginLog));
		}
		return vos;
	}

	public static MyLoginLogVO convertFor(LoginLog loginLog) {
		if (loginLog == null) {
			return null;
		}
		MyLoginLogVO vo = new MyLoginLogVO();
		BeanUtils.copyProperties(loginLog, vo);
		vo.setStatusName(DictHolder.getDictItemName("loginStatus", vo.getStatus()));
		return vo;
	}

}

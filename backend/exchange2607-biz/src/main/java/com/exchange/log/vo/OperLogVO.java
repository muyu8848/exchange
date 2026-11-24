package com.exchange.log.vo;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.log.domain.OperLog;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class OperLogVO {

	private String id;
	
	private String subSystem;

	private String subSystemName;

	private String module;

	private String moduleName;

	private String operate;

	private String requestMethod;

	private String requestUrl;

	private String requestParam;

	private String ip;

	private String operAccountId;

	private String operName;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime operTime;

	/** SUCCESS / FAIL */
	private String status;

	private String statusName;

	/** 耗时（毫秒） */
	private Long costTime;

	/** 异常信息 */
	private String errorMsg;

	public static List<OperLogVO> convertFor(List<OperLog> operLogs) {
		if (CollectionUtil.isEmpty(operLogs)) {
			return new ArrayList<>();
		}
		List<OperLogVO> vos = new ArrayList<>();
		for (OperLog operLog : operLogs) {
			vos.add(convertFor(operLog));
		}
		return vos;
	}

	public static OperLogVO convertFor(OperLog operLog) {
		if (operLog == null) {
			return null;
		}
		OperLogVO vo = new OperLogVO();
		BeanUtils.copyProperties(operLog, vo);
		vo.setSubSystemName(DictHolder.getDictItemName("subSystem", vo.getSubSystem()));
		vo.setModuleName(DictHolder.getDictItemName("operModule", vo.getModule()));
		vo.setStatusName(DictHolder.getDictItemName("operLogStatus", vo.getStatus()));
		return vo;
	}

}

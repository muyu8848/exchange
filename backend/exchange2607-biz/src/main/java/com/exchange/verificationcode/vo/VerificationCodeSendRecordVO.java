package com.exchange.verificationcode.vo;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.verificationcode.domain.VerificationCodeSendRecord;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class VerificationCodeSendRecordVO {

	private String id;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime sendTime;

	// 联系方式类型（手机/邮箱），由实体拷贝
	private String contactType;

	private String phoneAreaCode;

	private String contact;

	// 业务类型（注册/登录/忘记密码），由实体拷贝
	private String bizType;

	private String bizTypeName;

	private String verificationCode;

	private String errorMsg;

	private String sendStatus;

	private String sendStatusName;

	public static List<VerificationCodeSendRecordVO> convertFor(List<VerificationCodeSendRecord> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<VerificationCodeSendRecordVO> vos = new ArrayList<>();
		for (VerificationCodeSendRecord po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static VerificationCodeSendRecordVO convertFor(VerificationCodeSendRecord po) {
		if (po == null) {
			return null;
		}
		VerificationCodeSendRecordVO vo = new VerificationCodeSendRecordVO();
		BeanUtils.copyProperties(po, vo);
		vo.setBizTypeName(DictHolder.getDictItemName("verificationCodeType", po.getBizType()));
		vo.setSendStatusName(DictHolder.getDictItemName("verificationCodeSendStatus", po.getSendStatus()));
		return vo;
	}

}

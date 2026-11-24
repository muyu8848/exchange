package com.exchange.member.vo;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.member.domain.MemberAccount;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

@Data
public class MemberInfoVO {

	private Long uid;

	private String inviteCode;

	private String nickName;

	/** 注册方式：phone / email */
	private String contactType;

	private String realName;

	private String authenticationStatus;

	private String authenticationStatusName;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime latelyLoginTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime googleAuthBindTime;

	/** 手机号 */
	private String phone;

	/** 手机区号，如 86/1 */
	private String phoneAreaCode;

	/** 邮箱 */
	private String email;

	/** 是否已设置资金密码 */
	private Boolean hasFundPwdFlag;

	/** 通知语言 */
	private String notifyLangCode;

	/** 通知语言名称 */
	private String notifyLangName;

	public static MemberInfoVO convertFor(MemberAccount po) {
		if (po == null) {
			return null;
		}
		MemberInfoVO vo = new MemberInfoVO();
		BeanUtils.copyProperties(po, vo);
		vo.setHasFundPwdFlag(StrUtil.isNotBlank(po.getFundPwd()));
		vo.setAuthenticationStatusName(DictHolder.getDictItemName("authenticationStatus", vo.getAuthenticationStatus()));
		if (po.getNotifyMultiLang() != null) {
			vo.setNotifyLangName(po.getNotifyMultiLang().getLangName());
		}
		return vo;
	}

}

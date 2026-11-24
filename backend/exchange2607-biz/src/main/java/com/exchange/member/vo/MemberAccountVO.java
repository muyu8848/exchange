package com.exchange.member.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.constants.Constant;
import com.exchange.dictconfig.DictHolder;
import com.exchange.member.domain.MemberAccount;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class MemberAccountVO {

	private String id;

	private Long uid;

	private String email;

	private String phone;

	private String phoneAreaCode;

	private String contactType;

	private String contactTypeName;

	private String nickName;

	private String realName;

	private String status;

	private String statusName;

	private String c2cBuyStatus;

	private String c2cBuyStatusName;

	private Boolean buyRiskLimit;

	private String c2cSellStatus;

	private String c2cSellStatusName;

	private Boolean sellRiskLimit;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime latelyLoginTime;

	private String authenticationStatus;

	private String authenticationStatusName;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime authenticationPassedTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime googleAuthBindTime;

	private String inviteCode;

	private Long inviterUid;

	/** 通知语言 */
	private String notifyLangCode;

	/** 通知语言名称，取自 MultiLang.langName */
	private String notifyLangName;

	public static List<MemberAccountVO> convertFor(List<MemberAccount> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MemberAccountVO> vos = new ArrayList<>();
		for (MemberAccount po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static MemberAccountVO convertFor(MemberAccount po) {
		if (po == null) {
			return null;
		}
		MemberAccountVO vo = new MemberAccountVO();
		BeanUtils.copyProperties(po, vo);
		vo.setBuyRiskLimit(po.checkRiskLimit(Constant.C2C风控处罚_限制买入));
		vo.setSellRiskLimit(po.checkRiskLimit(Constant.C2C风控处罚_限制卖出));
		vo.setStatusName(DictHolder.getDictItemName("memberAccountStatus", po.getStatus()));
		vo.setC2cBuyStatusName(DictHolder.getDictItemName("enableStatus", po.getC2cBuyStatus()));
		vo.setC2cSellStatusName(DictHolder.getDictItemName("enableStatus", po.getC2cSellStatus()));
		vo.setAuthenticationStatusName(
				DictHolder.getDictItemName("authenticationStatus", po.getAuthenticationStatus()));
		vo.setContactTypeName(DictHolder.getDictItemName("contactType", po.getContactType()));
		if (po.getInviter() != null) {
			vo.setInviterUid(po.getInviter().getUid());
		}
		if (po.getNotifyMultiLang() != null) {
			vo.setNotifyLangName(po.getNotifyMultiLang().getLangName());
		}
		return vo;
	}

}

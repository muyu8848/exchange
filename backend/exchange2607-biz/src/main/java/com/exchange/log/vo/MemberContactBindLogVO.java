package com.exchange.log.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.log.domain.MemberContactBindLog;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 会员联系方式绑定流水
 */
@Data
public class MemberContactBindLogVO {

	private String id;

	private String memberAccountId;

	/** 联系方式类型 email:邮箱 phone:手机 */
	private String contactType;

	/** 手机号或邮箱（明文） */
	private String contact;

	/** 手机区号，邮箱时为空 */
	private String phoneAreaCode;

	/** 动作：register / bind / unbind */
	private String action;

	/** 所属会员 UID（来自 member_account 关联） */
	private Long memberUid;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	/** 联系方式类型名称（字典转义） */
	private String contactTypeName;

	/** 动作名称（字典转义） */
	private String actionName;

	public static List<MemberContactBindLogVO> convertFor(List<MemberContactBindLog> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MemberContactBindLogVO> vos = new ArrayList<>();
		for (MemberContactBindLog po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static MemberContactBindLogVO convertFor(MemberContactBindLog po) {
		if (po == null) {
			return null;
		}
		MemberContactBindLogVO vo = new MemberContactBindLogVO();
		BeanUtils.copyProperties(po, vo);
		vo.setContactTypeName(DictHolder.getDictItemName("contactType", po.getContactType()));
		vo.setActionName(DictHolder.getDictItemName("contactBindAction", po.getAction()));
		if (po.getMemberAccount() != null) {
			vo.setMemberUid(po.getMemberAccount().getUid());
		}
		return vo;
	}
}

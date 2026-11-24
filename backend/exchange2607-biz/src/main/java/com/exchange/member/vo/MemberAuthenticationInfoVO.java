package com.exchange.member.vo;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.member.domain.MemberAuthenticationInfo;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class MemberAuthenticationInfoVO {

	private String id;

	private String status;

	private String statusName;

	/** 驳回原因（审核不通过时填写，用户端可见） */
	private String failReason;

	private String realName;

	private String idType;

	private String idTypeName;

	private String idNumber;

	/** 证件正面 / 护照资料页 */
	private String identityCardPic1;

	/** 证件反面（护照为空） */
	private String identityCardPic2;

	/** 证件有效期 */
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate expiryDate;

	/** 签发国家/地区二字码 */
	private String issuingCountry;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	/**
	 * 提交时间戳（epoch 毫秒），供前端计算「已等待时长」。
	 * <p>
	 * createTime 是无时区字符串，前端按本地时区解析会在跨时区场景算错，必须用绝对时间戳。
	 */
	private Long createTimestamp;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime dealTime;

	private Long memberUid;

	private String memberAccountId;

	public static List<MemberAuthenticationInfoVO> convertFor(List<MemberAuthenticationInfo> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MemberAuthenticationInfoVO> vos = new ArrayList<>();
		for (MemberAuthenticationInfo po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static MemberAuthenticationInfoVO convertFor(MemberAuthenticationInfo po) {
		if (po == null) {
			return null;
		}
		MemberAuthenticationInfoVO vo = new MemberAuthenticationInfoVO();
		BeanUtils.copyProperties(po, vo);
		if (po.getMemberAccount() != null) {
			vo.setMemberUid(po.getMemberAccount().getUid());
		}
		vo.setStatusName(DictHolder.getDictItemName("authenticationStatus", vo.getStatus()));
		vo.setIdTypeName(DictHolder.getDictItemName("idType", vo.getIdType()));
		if (po.getCreateTime() != null) {
			vo.setCreateTimestamp(po.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		}
		return vo;
	}

}

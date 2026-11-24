package com.exchange.log.vo;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.log.domain.MemberAssetChangeLog;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class MemberAssetChangeLogVO {

	private String id;

	private String memberAccountId;

	private String bizOrderNo;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime changeTime;

	private String changeType;

	private String changeTypeName;

	private BigDecimal balanceChange;

	private BigDecimal balanceBefore;

	private BigDecimal balanceAfter;

	private String note;

	private String accountType;

	private String accountTypeName;

	private String assetCode;

	private String symbolCode;

	private Long memberUid;

	private String realName;

	public static List<MemberAssetChangeLogVO> convertFor(List<MemberAssetChangeLog> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MemberAssetChangeLogVO> vos = new ArrayList<>();
		for (MemberAssetChangeLog po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static MemberAssetChangeLogVO convertFor(MemberAssetChangeLog po) {
		if (po == null) {
			return null;
		}
		MemberAssetChangeLogVO vo = new MemberAssetChangeLogVO();
		BeanUtils.copyProperties(po, vo);
		if (po.getMemberAccount() != null) {
			vo.setMemberUid(po.getMemberAccount().getUid());
			vo.setRealName(po.getMemberAccount().getRealName());
		}
		vo.setChangeTypeName(DictHolder.getDictItemName("memberAssetChangeType", po.getChangeType()));
		vo.setAccountTypeName(DictHolder.getDictItemName("assetAccountType", po.getAccountType()));
		return vo;
	}

}

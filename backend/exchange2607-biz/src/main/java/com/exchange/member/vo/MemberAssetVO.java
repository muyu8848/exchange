package com.exchange.member.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.member.domain.MemberAsset;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class MemberAssetVO {

	private String id;

	private String memberAccountId;

	private Long memberUid;

	private String accountType;

	private String accountTypeName;

	private String assetCode;

	private BigDecimal available;

	private BigDecimal frozen;

	/** 法币估值 */
	private BigDecimal fiatValue;

	/** USDT 估值 */
	private BigDecimal usdtValue;

	/** 总余额 = available + frozen */
	private BigDecimal total;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	public static List<MemberAssetVO> convertFor(List<MemberAsset> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MemberAssetVO> vos = new ArrayList<>();
		for (MemberAsset po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static MemberAssetVO convertFor(MemberAsset po) {
		if (po == null) {
			return null;
		}
		MemberAssetVO vo = new MemberAssetVO();
		BeanUtils.copyProperties(po, vo);
		vo.setAccountTypeName(DictHolder.getDictItemName("assetAccountType", po.getAccountType()));
		if (po.getMemberAccount() != null) {
			vo.setMemberUid(po.getMemberAccount().getUid());
		}
		return vo;
	}

}

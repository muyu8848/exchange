package com.exchange.member.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.member.domain.AssetTransferOrder;
import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

@Data
public class AssetTransferOrderVO {

	private String id;

	private String orderNo;

	private String memberAccountId;

	private Long memberUid;

	private String fromAccountType;

	private String fromAccountTypeName;

	private String toAccountType;

	private String toAccountTypeName;

	private String assetCode;

	private BigDecimal amount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	public static List<AssetTransferOrderVO> convertFor(List<AssetTransferOrder> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<AssetTransferOrderVO> vos = new ArrayList<>();
		for (AssetTransferOrder po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static AssetTransferOrderVO convertFor(AssetTransferOrder po) {
		if (po == null) {
			return null;
		}
		AssetTransferOrderVO vo = new AssetTransferOrderVO();
		BeanUtils.copyProperties(po, vo);
		vo.setFromAccountTypeName(DictHolder.getDictItemName("assetAccountType", po.getFromAccountType()));
		vo.setToAccountTypeName(DictHolder.getDictItemName("assetAccountType", po.getToAccountType()));
		if (po.getMemberAccount() != null) {
			vo.setMemberUid(po.getMemberAccount().getUid());
		}
		return vo;
	}

}

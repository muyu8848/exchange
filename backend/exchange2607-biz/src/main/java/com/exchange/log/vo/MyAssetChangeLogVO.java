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
public class MyAssetChangeLogVO {

	private String id;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime changeTime;

	private String changeType;

	private String changeTypeName;

	private String assetCode;

	private BigDecimal balanceChange;

	private BigDecimal balanceAfter;

	private String accountType;

	private String accountTypeName;

	private String symbolCode;

	public static List<MyAssetChangeLogVO> convertFor(List<MemberAssetChangeLog> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MyAssetChangeLogVO> vos = new ArrayList<>();
		for (MemberAssetChangeLog po : pos) {
			vos.add(convertFor(po));
		}
		return vos;
	}

	public static MyAssetChangeLogVO convertFor(MemberAssetChangeLog po) {
		if (po == null) {
			return null;
		}
		MyAssetChangeLogVO vo = new MyAssetChangeLogVO();
		BeanUtils.copyProperties(po, vo);
		vo.setChangeTypeName(DictHolder.getDictItemName("memberAssetChangeType", vo.getChangeType()));
		vo.setAccountTypeName(DictHolder.getDictItemName("assetAccountType", po.getAccountType()));
		return vo;
	}

}

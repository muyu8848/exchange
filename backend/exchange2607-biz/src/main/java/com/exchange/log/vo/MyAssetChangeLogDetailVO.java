package com.exchange.log.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.log.domain.MemberAssetChangeLog;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class MyAssetChangeLogDetailVO {

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

	private String note;

	public static MyAssetChangeLogDetailVO convertFor(MemberAssetChangeLog po) {
		if (po == null) {
			return null;
		}
		MyAssetChangeLogDetailVO vo = new MyAssetChangeLogDetailVO();
		BeanUtils.copyProperties(po, vo);
		vo.setChangeTypeName(DictHolder.getDictItemName("memberAssetChangeType", vo.getChangeType()));
		vo.setAccountTypeName(DictHolder.getDictItemName("assetAccountType", po.getAccountType()));
		return vo;
	}

}

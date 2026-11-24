package com.exchange.rebate.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.exchange.common.json.BigDecimalStripTrailingZerosToPlainString;
import com.exchange.dictconfig.DictHolder;
import com.exchange.rebate.domain.RebateRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;

/**
 * 客户端（我的返佣）展示 VO：受益人即当前登录会员，故不暴露 beneficiary 字段，仅展示来源会员（下级）与返佣信息
 */
@Data
public class MyRebateRecordVO {

	private String id;

	private String sourceMemberId;

	private Long sourceMemberUid;

	private String sourceOrderId;

	private Integer level;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal amount;

	private String bizType;

	private String bizTypeName;

	private String assetCode;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@JsonFormat(pattern = "MM/dd HH:mm")
	private LocalDateTime createTimeShort;

	public static List<MyRebateRecordVO> convertFor(List<RebateRecord> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<MyRebateRecordVO> vos = new ArrayList<>();
		for (RebateRecord po : pos) {
			MyRebateRecordVO vo = new MyRebateRecordVO();
			BeanUtils.copyProperties(po, vo);
			if (po.getCreateTime() != null) {
				vo.setCreateTimeShort(po.getCreateTime());
			}
			vo.setBizTypeName(DictHolder.getDictItemName("rebateBizType", po.getBizType()));
			if (po.getSourceMember() != null) {
				vo.setSourceMemberUid(po.getSourceMember().getUid());
			}
			vos.add(vo);
		}
		return vos;
	}
}

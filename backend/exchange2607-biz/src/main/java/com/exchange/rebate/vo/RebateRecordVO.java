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
 * 返佣记录展示 VO（运营后台列表/详情）
 */
@Data
public class RebateRecordVO {

	private String id;

	private String beneficiaryId;

	private Long beneficiaryUid;

	private String sourceMemberId;

	private Long sourceMemberUid;

	private String sourceOrderId;

	private Integer level;

	@JsonSerialize(using = BigDecimalStripTrailingZerosToPlainString.class)
	private BigDecimal amount;

	private String bizType;

	private String bizTypeName;

	private String symbolCode;

	private String assetCode;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	public static List<RebateRecordVO> convertFor(List<RebateRecord> pos) {
		if (CollectionUtil.isEmpty(pos)) {
			return new ArrayList<>();
		}
		List<RebateRecordVO> vos = new ArrayList<>();
		for (RebateRecord po : pos) {
			RebateRecordVO vo = new RebateRecordVO();
			BeanUtils.copyProperties(po, vo);
			vo.setBizTypeName(DictHolder.getDictItemName("rebateBizType", po.getBizType()));
			vo.setBeneficiaryUid(po.getBeneficiary().getUid());
			vo.setSourceMemberUid(po.getSourceMember().getUid());
			vos.add(vo);
		}
		return vos;
	}
}

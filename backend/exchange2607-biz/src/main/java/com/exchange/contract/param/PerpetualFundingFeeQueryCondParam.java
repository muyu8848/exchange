package com.exchange.contract.param;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import com.exchange.common.param.PageParam;
import com.exchange.contract.domain.PerpetualFundingFee;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PerpetualFundingFeeQueryCondParam extends PageParam {

	/** 会员UID */
	private Long memberUid;

	private String symbolCode;

	/** 方向：long / short */
	private String direction;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	public Specification<PerpetualFundingFee> buildSpecification() {
		PerpetualFundingFeeQueryCondParam param = this;
		return (root, query, builder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (param.getMemberUid() != null) {
				predicates.add(builder.equal(root.join("memberAccount").get("uid"), param.getMemberUid()));
			}
			if (StrUtil.isNotBlank(param.getSymbolCode())) {
				predicates.add(builder.equal(root.get("symbolCode"), param.getSymbolCode()));
			}
			if (StrUtil.isNotBlank(param.getDirection())) {
				predicates.add(builder.equal(root.get("direction"), param.getDirection()));
			}
			if (param.getTimeStart() != null) {
				predicates.add(builder.greaterThanOrEqualTo(root.get("fundingTime"), param.getTimeStart()));
			}
			if (param.getTimeEnd() != null) {
				predicates.add(builder.lessThanOrEqualTo(root.get("fundingTime"), param.getTimeEnd()));
			}
			return predicates.isEmpty() ? null : builder.and(predicates.toArray(new Predicate[0]));
		};
	}
}

package com.exchange.contract.param;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import com.exchange.common.param.PageParam;
import com.exchange.contract.domain.PerpetualFundingRateHistory;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FundingRateHistoryQueryCondParam extends PageParam {

	/** 交易对（客户端必传，后台可空=全部） */
	private String symbolCode;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	public Pageable toPageable() {
		return PageRequest.of(getPageNum() - 1, getPageSize(), Sort.by(Sort.Order.desc("fundingTime")));
	}

	public Specification<PerpetualFundingRateHistory> buildSpecification() {
		FundingRateHistoryQueryCondParam param = this;
		return new Specification<PerpetualFundingRateHistory>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<PerpetualFundingRateHistory> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getSymbolCode())) {
					predicates.add(builder.equal(root.get("symbolCode"), param.getSymbolCode().trim()));
				}
				if (param.getTimeStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("fundingTime").as(LocalDateTime.class),
							param.getTimeStart()));
				}
				if (param.getTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("fundingTime").as(LocalDateTime.class),
							param.getTimeEnd()));
				}
				return builder.and(predicates.toArray(new Predicate[0]));
			}
		};
	}

}

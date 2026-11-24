package com.exchange.investment.param;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import com.exchange.common.param.PageParam;
import com.exchange.investment.domain.InvestmentHourlyInterestLog;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InvestmentHourlyInterestLogQueryCondParam extends PageParam {

	private String memberAccountId;

	private Long memberUid;

	private String assetCode;

	/** 活期账户 ID：传入后只查该账户的流水（新的分账户口径） */
	private String flexibleAccountId;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate interestDateStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate interestDateEnd;

	public Specification<InvestmentHourlyInterestLog> buildSpecification() {
		InvestmentHourlyInterestLogQueryCondParam param = this;
		return new Specification<InvestmentHourlyInterestLog>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<InvestmentHourlyInterestLog> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getMemberAccountId())) {
					predicates.add(builder.equal(root.get("memberAccountId"), param.getMemberAccountId()));
				}
				if (StrUtil.isNotBlank(param.getAssetCode())) {
					predicates.add(builder.equal(root.get("assetCode"), param.getAssetCode()));
				}
				if (StrUtil.isNotBlank(param.getFlexibleAccountId())) {
					predicates.add(builder.equal(root.get("flexibleAccountId"), param.getFlexibleAccountId()));
				}
				if (param.getInterestDateStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("interestDate"), param.getInterestDateStart()));
				}
				if (param.getInterestDateEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("interestDate"), param.getInterestDateEnd()));
				}
				if (param.getMemberUid() != null) {
		
					predicates.add(builder.equal(root.join("memberAccount").get("uid"), param.getMemberUid()));
				}
				return predicates.isEmpty() ? null : builder.and(predicates.toArray(new Predicate[0]));
			}
		};
	}

}

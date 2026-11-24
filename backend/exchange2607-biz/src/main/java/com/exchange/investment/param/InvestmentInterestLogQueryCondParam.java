package com.exchange.investment.param;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import com.exchange.common.param.PageParam;
import com.exchange.investment.domain.InvestmentFixedPosition;
import com.exchange.investment.domain.InvestmentFlexiblePosition;
import com.exchange.investment.domain.InvestmentInterestLog;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InvestmentInterestLogQueryCondParam extends PageParam {

	private String memberAccountId;

	private String flexibleAccountId;

	private String positionId;

	private String orderNo;

	private Long memberUid;

	private String assetCode;

	private String productType;

	private String productId;

	private String interestType;

	/** 是否在首页追加「当天实时汇总」行（账户详情弹窗用，需配合 flexibleAccountId） */
	private Boolean includeTodayRealtime;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate interestDateStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate interestDateEnd;

	public Specification<InvestmentInterestLog> buildSpecification() {
		InvestmentInterestLogQueryCondParam param = this;
		return new Specification<InvestmentInterestLog>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<InvestmentInterestLog> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getMemberAccountId())) {
					predicates.add(builder.equal(root.get("memberAccountId"), param.getMemberAccountId()));
				}
				if (StrUtil.isNotBlank(param.getFlexibleAccountId())) {
					predicates.add(builder.equal(root.get("flexibleAccountId"), param.getFlexibleAccountId()));
				}
				if (StrUtil.isNotBlank(param.getPositionId())) {
					predicates.add(builder.equal(root.get("positionId"), param.getPositionId()));
				}
				if (StrUtil.isNotBlank(param.getProductType())) {
					predicates.add(builder.equal(root.get("productType"), param.getProductType()));
				}
				if (StrUtil.isNotBlank(param.getProductId())) {
					predicates.add(builder.equal(root.get("productId"), param.getProductId()));
				}
				if (StrUtil.isNotBlank(param.getInterestType())) {
					predicates.add(builder.equal(root.get("interestType"), param.getInterestType()));
				}
				if (StrUtil.isNotBlank(param.getAssetCode())) {
					predicates.add(builder.equal(root.get("assetCode"), param.getAssetCode()));
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
				if (StrUtil.isNotBlank(param.getOrderNo())) {
					Predicate positionPredicate = buildPositionIdPredicate(root, query, builder, param);
					if (positionPredicate != null) {
						predicates.add(positionPredicate);
					}
				}
				return predicates.isEmpty() ? null : builder.and(predicates.toArray(new Predicate[0]));
			}
		};
	}

	private static Predicate buildPositionIdPredicate(Root<InvestmentInterestLog> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, InvestmentInterestLogQueryCondParam param) {
		Subquery<String> fixedSubquery = query.subquery(String.class);
		Root<InvestmentFixedPosition> fixedRoot = fixedSubquery.from(InvestmentFixedPosition.class);
		fixedSubquery.select(fixedRoot.get("id"));
		fixedSubquery.where(builder.equal(fixedRoot.get("orderNo"), param.getOrderNo().trim()));

		Subquery<String> flexibleSubquery = query.subquery(String.class);
		Root<InvestmentFlexiblePosition> flexibleRoot = flexibleSubquery.from(InvestmentFlexiblePosition.class);
		flexibleSubquery.select(flexibleRoot.get("id"));
		flexibleSubquery.where(builder.equal(flexibleRoot.get("orderNo"), param.getOrderNo().trim()));

		return builder.or(
				builder.in(root.get("positionId")).value(fixedSubquery),
				builder.in(root.get("positionId")).value(flexibleSubquery));
	}

}

package com.exchange.investment.param;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import com.exchange.common.param.PageParam;
import com.exchange.investment.domain.InvestmentFlexiblePosition;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InvestmentFlexiblePositionQueryCondParam extends PageParam {

	private String orderNo;

	private String memberAccountId;

	/** 所属活期账户（在投周期） */
	private String flexibleAccountId;

	private Long memberUid;

	private String productId;

	private String assetCode;

	private String status;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime investTimeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime investTimeEnd;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime redeemTimeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime redeemTimeEnd;

	public Specification<InvestmentFlexiblePosition> buildSpecification() {
		InvestmentFlexiblePositionQueryCondParam param = this;
		return new Specification<InvestmentFlexiblePosition>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<InvestmentFlexiblePosition> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getOrderNo())) {
					predicates.add(builder.equal(root.get("orderNo"), param.getOrderNo().trim()));
				}
				if (StrUtil.isNotBlank(param.getMemberAccountId())) {
					predicates.add(builder.equal(root.get("memberAccountId"), param.getMemberAccountId()));
				}
				if (StrUtil.isNotBlank(param.getFlexibleAccountId())) {
					predicates.add(builder.equal(root.get("flexibleAccountId"), param.getFlexibleAccountId()));
				}
				if (param.getMemberUid() != null) {
					predicates.add(builder.equal(root.join("memberAccount").get("uid"),
							param.getMemberUid()));
				}
				if (StrUtil.isNotBlank(param.getProductId())) {
					predicates.add(builder.equal(root.get("productId"), param.getProductId()));
				}
				if (StrUtil.isNotBlank(param.getAssetCode())) {
					predicates.add(builder.equal(root.get("assetCode"), param.getAssetCode()));
				}
				if (StrUtil.isNotBlank(param.getStatus())) {
					predicates.add(builder.equal(root.get("status"), param.getStatus()));
				}
				if (param.getInvestTimeStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("investTime"), param.getInvestTimeStart()));
				}
				if (param.getInvestTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("investTime"), param.getInvestTimeEnd()));
				}
				if (param.getRedeemTimeStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("redeemTime"), param.getRedeemTimeStart()));
				}
				if (param.getRedeemTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("redeemTime"), param.getRedeemTimeEnd()));
				}
				return predicates.isEmpty() ? null : builder.and(predicates.toArray(new Predicate[0]));
			}
		};
	}

}

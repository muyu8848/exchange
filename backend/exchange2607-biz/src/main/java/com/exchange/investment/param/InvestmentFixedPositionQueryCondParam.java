package com.exchange.investment.param;

import java.time.LocalDate;
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
import com.exchange.investment.domain.InvestmentFixedPosition;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InvestmentFixedPositionQueryCondParam extends PageParam {

	private String orderNo;

	private String memberAccountId;

	private Long memberUid;

	private String productId;

	private String assetCode;

	private String status;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime investTimeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime investTimeEnd;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate valueDateStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate valueDateEnd;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate maturityDateStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate maturityDateEnd;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime nextAccrualTimeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime nextAccrualTimeEnd;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime redeemTimeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime redeemTimeEnd;

	public Specification<InvestmentFixedPosition> buildSpecification() {
		InvestmentFixedPositionQueryCondParam param = this;
		return new Specification<InvestmentFixedPosition>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<InvestmentFixedPosition> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getOrderNo())) {
					predicates.add(builder.equal(root.get("orderNo"), param.getOrderNo().trim()));
				}
				if (StrUtil.isNotBlank(param.getMemberAccountId())) {
					predicates.add(builder.equal(root.get("memberAccountId"), param.getMemberAccountId()));
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
				if (param.getValueDateStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("valueDate"), param.getValueDateStart()));
				}
				if (param.getValueDateEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("valueDate"), param.getValueDateEnd()));
				}
				if (param.getMaturityDateStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("maturityDate"), param.getMaturityDateStart()));
				}
				if (param.getMaturityDateEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("maturityDate"), param.getMaturityDateEnd()));
				}
				if (param.getNextAccrualTimeStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("nextAccrualTime"),
							param.getNextAccrualTimeStart()));
				}
				if (param.getNextAccrualTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("nextAccrualTime"), param.getNextAccrualTimeEnd()));
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

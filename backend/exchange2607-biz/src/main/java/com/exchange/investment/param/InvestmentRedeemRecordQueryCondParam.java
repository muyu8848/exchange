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
import com.exchange.investment.domain.InvestmentRedeemRecord;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InvestmentRedeemRecordQueryCondParam extends PageParam {

	private String memberAccountId;

	private Long memberUid;

	private String orderNo;

	private String assetCode;

	private String productType;

	private String productId;

	/** 所属活期账户（在投周期） */
	private String flexibleAccountId;

	private String redeemType;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTimeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTimeEnd;

	public Specification<InvestmentRedeemRecord> buildSpecification() {
		InvestmentRedeemRecordQueryCondParam param = this;
		return new Specification<InvestmentRedeemRecord>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<InvestmentRedeemRecord> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getMemberAccountId())) {
					predicates.add(builder.equal(root.get("memberAccountId"), param.getMemberAccountId()));
				}
				if (param.getMemberUid() != null) {
					predicates.add(builder.equal(root.join("memberAccount").get("uid"),
							param.getMemberUid()));
				}
				if (StrUtil.isNotBlank(param.getOrderNo())) {
					predicates.add(builder.equal(root.get("orderNo"), param.getOrderNo().trim()));
				}
				if (StrUtil.isNotBlank(param.getAssetCode())) {
					predicates.add(builder.equal(root.get("assetCode"), param.getAssetCode()));
				}
				if (StrUtil.isNotBlank(param.getProductType())) {
					predicates.add(builder.equal(root.get("productType"), param.getProductType()));
				}
				if (StrUtil.isNotBlank(param.getProductId())) {
					predicates.add(builder.equal(root.get("productId"), param.getProductId()));
				}
				if (StrUtil.isNotBlank(param.getFlexibleAccountId())) {
					predicates.add(builder.equal(root.get("flexibleAccountId"), param.getFlexibleAccountId()));
				}
				if (StrUtil.isNotBlank(param.getRedeemType())) {
					predicates.add(builder.equal(root.get("redeemType"), param.getRedeemType()));
				}
				if (param.getCreateTimeStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("createTime"), param.getCreateTimeStart()));
				}
				if (param.getCreateTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("createTime"), param.getCreateTimeEnd()));
				}
				return predicates.isEmpty() ? null : builder.and(predicates.toArray(new Predicate[0]));
			}
		};
	}

}

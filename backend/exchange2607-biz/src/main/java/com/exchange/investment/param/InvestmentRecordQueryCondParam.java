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
import com.exchange.investment.domain.InvestmentRecord;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InvestmentRecordQueryCondParam extends PageParam {

	private String orderNo;

	private String memberAccountId;

	private Long memberUid;

	private String productId;

	private String assetCode;

	private String productType;

	/** 所属活期账户（在投周期） */
	private String flexibleAccountId;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTimeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTimeEnd;

	public Specification<InvestmentRecord> buildSpecification() {
		InvestmentRecordQueryCondParam param = this;
		return new Specification<InvestmentRecord>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<InvestmentRecord> root, CriteriaQuery<?> query,
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
				if (StrUtil.isNotBlank(param.getFlexibleAccountId())) {
					predicates.add(builder.equal(root.get("flexibleAccountId"), param.getFlexibleAccountId()));
				}
				if (StrUtil.isNotBlank(param.getProductType())) {
					predicates.add(builder.equal(root.get("productType"), param.getProductType()));
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

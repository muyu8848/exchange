package com.exchange.investment.param;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.exchange.common.param.PageParam;
import com.exchange.investment.domain.InvestmentProduct;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InvestmentProductQueryCondParam extends PageParam {

	private String status;

	private String assetCode;

	private String productType;

	public Specification<InvestmentProduct> buildSpecification() {
		InvestmentProductQueryCondParam param = this;
		return new Specification<InvestmentProduct>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<InvestmentProduct> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				predicates.add(builder.equal(root.get("deletedFlag"), false));
			if (StrUtil.isNotBlank(param.getStatus())) {
				predicates.add(builder.equal(root.get("status"), param.getStatus()));
			}
			if (StrUtil.isNotBlank(param.getProductType())) {
				predicates.add(builder.equal(root.get("productType"), param.getProductType()));
			}
			if (StrUtil.isNotBlank(param.getAssetCode())) {
					predicates.add(builder.equal(root.get("assetCode"), param.getAssetCode()));
				}
				return builder.and(predicates.toArray(new Predicate[0]));
			}
		};
	}

}

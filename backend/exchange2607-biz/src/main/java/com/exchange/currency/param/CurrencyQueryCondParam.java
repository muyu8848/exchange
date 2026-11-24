package com.exchange.currency.param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.exchange.common.param.PageParam;
import com.exchange.currency.domain.Currency;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CurrencyQueryCondParam extends PageParam {

	private String currencyCode;

	private String currencyName;

	public Specification<Currency> buildSpecification() {
		CurrencyQueryCondParam param = this;
		Specification<Currency> spec = new Specification<Currency>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<Currency> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("deletedFlag"), false));
				if (StrUtil.isNotBlank(param.getCurrencyCode())) {
					predicates.add(root.get("currencyCode").in(Arrays.asList(param.getCurrencyCode().split(","))));
				}
				if (StrUtil.isNotBlank(param.getCurrencyName())) {
					predicates.add(builder.equal(root.get("currencyName"), param.getCurrencyName()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		return spec;
	}

}

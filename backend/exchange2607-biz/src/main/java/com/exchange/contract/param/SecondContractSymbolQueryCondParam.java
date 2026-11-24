package com.exchange.contract.param;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.exchange.common.param.PageParam;
import com.exchange.contract.domain.SecondContractSymbol;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SecondContractSymbolQueryCondParam extends PageParam {

	private String symbolCode;

	private String status;

	public Specification<SecondContractSymbol> buildSpecification() {
		SecondContractSymbolQueryCondParam param = this;
		Specification<SecondContractSymbol> spec = new Specification<SecondContractSymbol>() {
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<SecondContractSymbol> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("deletedFlag"), false));
				if (StrUtil.isNotEmpty(param.getSymbolCode())) {
					predicates.add(builder.equal(root.get("symbolCode"), param.getSymbolCode()));
				}
				if (StrUtil.isNotEmpty(param.getStatus())) {
					predicates.add(builder.equal(root.get("status"), param.getStatus()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		return spec;
	}

}

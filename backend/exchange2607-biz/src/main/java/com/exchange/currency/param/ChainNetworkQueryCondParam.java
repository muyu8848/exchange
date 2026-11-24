package com.exchange.currency.param;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.exchange.common.param.PageParam;
import com.exchange.currency.domain.ChainNetwork;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ChainNetworkQueryCondParam extends PageParam {

	private String chainType;

	private String status;

	public Specification<ChainNetwork> buildSpecification() {
		ChainNetworkQueryCondParam param = this;
		return new Specification<ChainNetwork>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<ChainNetwork> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				predicates.add(builder.equal(root.get("deletedFlag"), false));
				if (StrUtil.isNotBlank(param.getChainType())) {
					predicates.add(builder.equal(root.get("chainType"), param.getChainType().trim()));
				}
				if (StrUtil.isNotBlank(param.getStatus())) {
					predicates.add(builder.equal(root.get("status"), param.getStatus()));
				}
				return builder.and(predicates.toArray(new Predicate[0]));
			}
		};
	}

}

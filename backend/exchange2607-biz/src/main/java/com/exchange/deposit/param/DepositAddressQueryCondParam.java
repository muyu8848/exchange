package com.exchange.deposit.param;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.exchange.common.param.PageParam;
import com.exchange.deposit.domain.DepositAddress;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DepositAddressQueryCondParam extends PageParam {

	private String chainType;

	private String status;

	private String address;

	public Specification<DepositAddress> buildSpecification() {
		DepositAddressQueryCondParam param = this;
		return new Specification<DepositAddress>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<DepositAddress> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				predicates.add(builder.equal(root.get("deletedFlag"), false));
				if (StrUtil.isNotBlank(param.getChainType())) {
					predicates.add(builder.equal(root.get("chainType"), param.getChainType()));
				}
				if (StrUtil.isNotBlank(param.getStatus())) {
					predicates.add(builder.equal(root.get("status"), param.getStatus()));
				}
				if (StrUtil.isNotBlank(param.getAddress())) {
					predicates.add(builder.like(root.get("address"), "%" + param.getAddress() + "%"));
				}
				return builder.and(predicates.toArray(new Predicate[0]));
			}
		};
	}
}

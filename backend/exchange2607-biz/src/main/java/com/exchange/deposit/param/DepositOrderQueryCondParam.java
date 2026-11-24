package com.exchange.deposit.param;

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
import com.exchange.deposit.domain.DepositOrder;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DepositOrderQueryCondParam extends PageParam {

	private String orderNo;

	private String memberAccountId;

	private Long memberUid;

	private String status;

	private String chainType;

	private String assetCode;

	private String depositAddressKw;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	public Specification<DepositOrder> buildSpecification() {
		DepositOrderQueryCondParam param = this;
		return new Specification<DepositOrder>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<DepositOrder> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getOrderNo())) {
					predicates.add(builder.equal(root.get("orderNo"), param.getOrderNo().trim()));
				}
				if (StrUtil.isNotBlank(param.getMemberAccountId())) {
					predicates.add(builder.equal(root.get("memberAccountId"), param.getMemberAccountId()));
				}
				if (param.getMemberUid() != null) {
					predicates.add(builder.equal(root.join("memberAccount").get("uid"), param.getMemberUid()));
				}
				if (StrUtil.isNotBlank(param.getStatus())) {
					predicates.add(builder.equal(root.get("status"), param.getStatus()));
				}
				if (StrUtil.isNotBlank(param.getChainType())) {
					predicates.add(builder.equal(root.get("chainType"), param.getChainType()));
				}
				if (StrUtil.isNotBlank(param.getAssetCode())) {
					predicates.add(builder.equal(root.get("assetCode"), param.getAssetCode()));
				}
				if (StrUtil.isNotBlank(param.getDepositAddressKw())) {
					predicates.add(
							builder.like(root.get("depositAddress"), "%" + param.getDepositAddressKw().trim() + "%"));
				}
				if (param.getTimeStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("createTime"), param.getTimeStart()));
				}
				if (param.getTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("createTime"), param.getTimeEnd()));
				}
				return predicates.isEmpty() ? null : builder.and(predicates.toArray(new Predicate[0]));
			}
		};
	}

}

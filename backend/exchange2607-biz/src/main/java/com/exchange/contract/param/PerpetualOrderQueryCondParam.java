package com.exchange.contract.param;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import com.exchange.common.param.PageParam;
import com.exchange.contract.domain.PerpetualOrder;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PerpetualOrderQueryCondParam extends PageParam {

	private String orderNo;
	private String memberAccountId;
	private Long memberUid;
	private String symbolCode;
	private String direction;
	/** 订单类型：支持逗号分隔多值（如 open,close） */
	private String type;
	/** 订单状态：支持逗号分隔多值（如 1,2） */
	private String status;
	/** 委托类型：market=市价, limit=限价 */
	private String entrustType;
	private String positionId;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	public Pageable toPageable() {
		return PageRequest.of(getPageNum() - 1, getPageSize(), Sort.by(Sort.Order.desc("createTime")));
	}

	public Specification<PerpetualOrder> buildSpecification() {
		PerpetualOrderQueryCondParam param = this;
		return new Specification<PerpetualOrder>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<PerpetualOrder> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getOrderNo())) {
					predicates.add(builder.equal(root.get("orderNo"), param.getOrderNo().trim()));
				}
				if (StrUtil.isNotBlank(param.getMemberAccountId())) {
					predicates.add(builder.equal(root.get("memberAccountId"), param.getMemberAccountId()));
				}
				if (param.getMemberUid() != null) {
					predicates.add(builder.equal(root.join("memberAccount", JoinType.INNER).get("uid"),
							param.getMemberUid()));
				}
				if (StrUtil.isNotBlank(param.getSymbolCode())) {
					predicates.add(builder.equal(root.get("symbolCode"), param.getSymbolCode()));
				}
				if (StrUtil.isNotBlank(param.getDirection())) {
					predicates.add(builder.equal(root.get("direction"), param.getDirection()));
				}
				if (StrUtil.isNotBlank(param.getType())) {
					predicates.add(root.get("type").in(StrUtil.split(param.getType(), ',')));
				}
				if (StrUtil.isNotBlank(param.getStatus())) {
					predicates.add(root.get("status").in(StrUtil.split(param.getStatus(), ',')));
				}
				if (StrUtil.isNotBlank(param.getEntrustType())) {
					predicates.add(builder.equal(root.get("entrustType"), param.getEntrustType()));
				}
				if (StrUtil.isNotBlank(param.getPositionId())) {
					predicates.add(builder.equal(root.get("positionId"), param.getPositionId()));
				}
				if (param.getTimeStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("createTime").as(LocalDateTime.class),
							param.getTimeStart()));
				}
				if (param.getTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("createTime").as(LocalDateTime.class),
							param.getTimeEnd()));
				}
				return predicates.isEmpty() ? null : builder.and(predicates.toArray(new Predicate[0]));
			}
		};
	}

}

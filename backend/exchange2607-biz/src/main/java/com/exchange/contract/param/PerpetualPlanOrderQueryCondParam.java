package com.exchange.contract.param;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import com.exchange.common.param.PageParam;
import com.exchange.contract.domain.PerpetualPlanOrder;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PerpetualPlanOrderQueryCondParam extends PageParam {

	private String orderNo;

	private String memberAccountId;

	private Long memberUid;

	private String symbolCode;

	private String type;

	private String direction;

	private String status;

	private String source;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	public Specification<PerpetualPlanOrder> buildSpecification() {
		PerpetualPlanOrderQueryCondParam param = this;
		Specification<PerpetualPlanOrder> spec = new Specification<PerpetualPlanOrder>() {
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<PerpetualPlanOrder> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getOrderNo())) {
					predicates.add(builder.equal(root.get("orderNo"), param.getOrderNo()));
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
				if (StrUtil.isNotBlank(param.getType())) {
					predicates.add(builder.equal(root.get("type"), param.getType()));
				}
				if (StrUtil.isNotBlank(param.getDirection())) {
					predicates.add(builder.equal(root.get("direction"), param.getDirection()));
				}
				if (StrUtil.isNotBlank(param.getStatus())) {
					List<String> statusList = StrUtil.split(param.getStatus(), ',');
					predicates.add(root.get("status").in(statusList));
				}
				if (StrUtil.isNotBlank(param.getSource())) {
					List<String> sourceList = StrUtil.split(param.getSource(), ',');
					predicates.add(root.get("source").in(sourceList));
				}
				if (param.getTimeStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("createTime").as(LocalDateTime.class),
							param.getTimeStart()));
				}
				if (param.getTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("createTime").as(LocalDateTime.class),
							param.getTimeEnd()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		return spec;
	}

}

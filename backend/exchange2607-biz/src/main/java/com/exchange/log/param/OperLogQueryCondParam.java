package com.exchange.log.param;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import com.exchange.common.param.PageParam;
import com.exchange.log.domain.OperLog;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class OperLogQueryCondParam extends PageParam {

	private String ip;

	private String operName;

	private String operAccountId;

	private String subSystem;

	private String module;

	/** SUCCESS / FAIL */
	private String status;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	public Specification<OperLog> buildSpecification() {
		OperLogQueryCondParam param = this;
		Specification<OperLog> spec = new Specification<OperLog>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<OperLog> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotEmpty(param.getIp())) {
					predicates.add(builder.equal(root.get("ip"), param.getIp()));
				}
				if (StrUtil.isNotEmpty(param.getOperName())) {
					predicates.add(builder.equal(root.get("operName"), param.getOperName()));
				}
				if (StrUtil.isNotEmpty(param.getOperAccountId())) {
					predicates.add(builder.equal(root.get("operAccountId"), param.getOperAccountId()));
				}
				if (StrUtil.isNotEmpty(param.getSubSystem())) {
					predicates.add(builder.equal(root.get("subSystem"), param.getSubSystem()));
				}
				if (StrUtil.isNotEmpty(param.getModule())) {
					predicates.add(builder.equal(root.get("module"), param.getModule()));
				}
				if (StrUtil.isNotEmpty(param.getStatus())) {
					predicates.add(builder.equal(root.get("status"), param.getStatus()));
				}
				if (param.getTimeStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("operTime"), param.getTimeStart()));
				}
				if (param.getTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("operTime"), param.getTimeEnd()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		return spec;
	}

}

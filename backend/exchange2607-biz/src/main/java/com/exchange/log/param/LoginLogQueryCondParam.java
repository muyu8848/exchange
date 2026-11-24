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
import com.exchange.log.domain.LoginLog;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LoginLogQueryCondParam extends PageParam {

	private String ip;

	private String userName;

	private String subSystem;

	private String status;

	private String accountId;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	public Specification<LoginLog> buildSpecification() {
		LoginLogQueryCondParam param = this;
		Specification<LoginLog> spec = new Specification<LoginLog>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<LoginLog> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotEmpty(param.getIp())) {
					predicates.add(builder.equal(root.get("ip"), param.getIp()));
				}
				if (StrUtil.isNotEmpty(param.getUserName())) {
					predicates.add(builder.equal(root.get("userName"), param.getUserName()));
				}
				if (StrUtil.isNotEmpty(param.getSubSystem())) {
					predicates.add(builder.equal(root.get("subSystem"), param.getSubSystem()));
				}
				if (StrUtil.isNotEmpty(param.getStatus())) {
					predicates.add(builder.equal(root.get("status"), param.getStatus()));
				}
				if (StrUtil.isNotEmpty(param.getAccountId())) {
					predicates.add(builder.equal(root.get("accountId"), param.getAccountId()));
				}
				if (param.getTimeStart() != null) {
					predicates.add(
							builder.greaterThanOrEqualTo(root.get("loginTime"), param.getTimeStart()));
				}
				if (param.getTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("loginTime"), param.getTimeEnd()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		return spec;
	}

}

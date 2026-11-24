package com.exchange.verificationcode.param;

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
import com.exchange.verificationcode.domain.VerificationCodeSendRecord;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class VerificationCodeSendRecordQueryCondParam extends PageParam {

	private String contact;

	private String bizType;

	private String sendStatus;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	public Specification<VerificationCodeSendRecord> buildSpecification() {
		VerificationCodeSendRecordQueryCondParam param = this;
		Specification<VerificationCodeSendRecord> spec = new Specification<VerificationCodeSendRecord>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<VerificationCodeSendRecord> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotBlank(param.getContact())) {
					predicates.add(builder.equal(root.get("contact"), param.getContact()));
				}
				if (StrUtil.isNotBlank(param.getBizType())) {
					predicates.add(builder.equal(root.get("bizType"), param.getBizType()));
				}
				if (StrUtil.isNotBlank(param.getSendStatus())) {
					predicates.add(builder.equal(root.get("sendStatus"), param.getSendStatus()));
				}
				if (param.getTimeStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("createTime"), param.getTimeStart()));
				}
				if (param.getTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("createTime"), param.getTimeEnd()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		return spec;
	}

}

package com.exchange.log.param;

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
import com.exchange.log.domain.MemberContactBindLog;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MemberContactBindLogQueryCondParam extends PageParam {

	/** 联系方式类型 email:邮箱 phone:手机 */
	private String contactType;

	/** 手机号或邮箱（支持模糊） */
	private String contact;

	/** 手机区号，邮箱时为空 */
	private String phoneAreaCode;

	/** 动作：register / bind / unbind */
	private String action;

	/** 会员 UID（来自 member_account 关联，精确匹配） */
	private Long memberUid;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	public Specification<MemberContactBindLog> buildSpecification() {
		MemberContactBindLogQueryCondParam param = this;
		return new Specification<MemberContactBindLog>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<MemberContactBindLog> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getContactType())) {
					predicates.add(builder.equal(root.get("contactType"), param.getContactType()));
				}
				if (StrUtil.isNotBlank(param.getContact())) {
					predicates.add(builder.equal(root.get("contact"), param.getContact()));
				}
				if (StrUtil.isNotBlank(param.getPhoneAreaCode())) {
					predicates.add(builder.equal(root.get("phoneAreaCode"), param.getPhoneAreaCode()));
				}
				if (StrUtil.isNotBlank(param.getAction())) {
					predicates.add(builder.equal(root.get("action"), param.getAction()));
				}
				if (param.getMemberUid() != null) {
					predicates.add(builder.equal(root.join("memberAccount").get("uid"), param.getMemberUid()));
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

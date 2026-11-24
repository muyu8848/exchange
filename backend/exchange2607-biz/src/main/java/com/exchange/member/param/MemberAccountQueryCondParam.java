package com.exchange.member.param;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import com.exchange.common.param.PageParam;
import com.exchange.member.domain.MemberAccount;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MemberAccountQueryCondParam extends PageParam {

	private String currentAccountId;

	private String uid;

	private String inviterId;

	private String inviterUserName;

	private String inviterUid;

	private String status;

	private String inviteCode;

	private String authenticationStatus;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	public Specification<MemberAccount> buildSpecification() {
		MemberAccountQueryCondParam param = this;
		Specification<MemberAccount> spec = new Specification<MemberAccount>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<MemberAccount> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotEmpty(param.getStatus())) {
					predicates.add(root.get("status").in(Arrays.asList(param.getStatus().split(","))));
				}
				if (StrUtil.isNotEmpty(param.getUid())) {
					predicates.add(builder.equal(root.get("uid"), param.getUid()));
				}
				if (StrUtil.isNotEmpty(param.getInviterId())) {
					predicates.add(builder.equal(root.get("inviterId"), param.getInviterId()));
				}
				if (StrUtil.isNotEmpty(param.getInviterUid())) {
					predicates.add(builder.equal(root.join("inviter").get("uid"), param.getInviterUid()));
				}
				if (StrUtil.isNotEmpty(param.getInviteCode())) {
					predicates.add(builder.equal(root.get("inviteCode"), param.getInviteCode().trim()));
				}
				if (StrUtil.isNotEmpty(param.getAuthenticationStatus())) {
					predicates.add(builder.equal(root.get("authenticationStatus"), param.getAuthenticationStatus()));
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

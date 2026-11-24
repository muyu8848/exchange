package com.exchange.notification.param;

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
import com.exchange.notification.domain.MemberNotification;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MemberNotificationQueryCondParam extends PageParam {

	private String memberAccountId;

	private Long memberUid;

	private String type;

	private Boolean readFlag;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTimeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTimeEnd;

	public Specification<MemberNotification> buildSpecification() {
		MemberNotificationQueryCondParam param = this;
		return new Specification<MemberNotification>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<MemberNotification> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getMemberAccountId())) {
					predicates.add(builder.equal(root.get("memberAccountId"), param.getMemberAccountId()));
				}
				if (param.getMemberUid() != null) {
					predicates.add(builder.equal(root.join("memberAccount").get("uid"), param.getMemberUid()));
				}
				if (StrUtil.isNotBlank(param.getType())) {
					predicates.add(builder.equal(root.get("type"), param.getType()));
				}
				if (param.getReadFlag() != null) {
					predicates.add(builder.equal(root.get("readFlag"), param.getReadFlag()));
				}
				if (param.getCreateTimeStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("createTime"), param.getCreateTimeStart()));
				}
				if (param.getCreateTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("createTime"), param.getCreateTimeEnd()));
				}
				return predicates.isEmpty() ? null : builder.and(predicates.toArray(new Predicate[0]));
			}
		};
	}

}

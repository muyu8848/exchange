package com.exchange.member.param;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.exchange.common.param.PageParam;
import com.exchange.member.domain.MemberClosure;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MyTeamMemberQueryParam extends PageParam {

	/**
	 * 被查看会员的 accountId（团队所属人），由前端提交（运营选中的目标会员），作为闭包查询的祖先节点
	 */
	private String ancestorId;

	/**
	 * 按层级筛选（1=直推，2=二级...），为空表示查询全部下级
	 */
	private Integer level;

	public Specification<MemberClosure> buildSpecification() {
		MyTeamMemberQueryParam param = this;
		return new Specification<MemberClosure>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<MemberClosure> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				predicates.add(builder.equal(root.get("ancestorId"), param.getAncestorId()));
				predicates.add(builder.greaterThanOrEqualTo(root.get("depth"), 1));
				if (param.getLevel() != null) {
					predicates.add(builder.equal(root.get("depth"), param.getLevel()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
	}

}

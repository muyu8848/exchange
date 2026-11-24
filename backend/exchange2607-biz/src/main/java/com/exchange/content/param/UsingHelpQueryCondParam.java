package com.exchange.content.param;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.exchange.common.param.PageParam;
import com.exchange.content.domain.UsingHelp;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UsingHelpQueryCondParam extends PageParam {

	private String title;

	public Specification<UsingHelp> buildSpecification() {
		UsingHelpQueryCondParam param = this;
		return new Specification<UsingHelp>() {
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<UsingHelp> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getTitle())) {
					predicates.add(builder.like(root.get("title"), "%" + param.getTitle() + "%"));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[0])) : null;
			}
		};
	}

}

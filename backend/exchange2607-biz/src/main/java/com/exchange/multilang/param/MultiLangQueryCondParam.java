package com.exchange.multilang.param;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.exchange.common.param.PageParam;
import com.exchange.multilang.domain.MultiLang;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MultiLangQueryCondParam extends PageParam {

	private String langCode;

	private String langName;

	private String status;

	public Specification<MultiLang> buildSpecification() {
		MultiLangQueryCondParam param = this;
		return new Specification<MultiLang>() {
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<MultiLang> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getLangCode())) {
					predicates.add(builder.like(root.get("langCode"), "%" + param.getLangCode() + "%"));
				}
				if (StrUtil.isNotBlank(param.getLangName())) {
					predicates.add(builder.like(root.get("langName"), "%" + param.getLangName() + "%"));
				}
				if (StrUtil.isNotBlank(param.getStatus())) {
					predicates.add(builder.equal(root.get("status"), param.getStatus()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[0])) : null;
			}
		};
	}

}

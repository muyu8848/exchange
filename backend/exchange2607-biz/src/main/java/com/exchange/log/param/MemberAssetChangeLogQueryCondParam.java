package com.exchange.log.param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import com.exchange.common.param.PageParam;
import com.exchange.log.domain.MemberAssetChangeLog;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MemberAssetChangeLogQueryCondParam extends PageParam {

	private String memberAccountId;

	private String bizOrderNo;

	private String changeType;

	/** all / in / out */
	private String direction;

	private Long memberUid;

	private String realName;

	private String accountType;

	private String assetCode;

	private String symbolCode;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	public Specification<MemberAssetChangeLog> buildSpecification() {
		MemberAssetChangeLogQueryCondParam param = this;
		Specification<MemberAssetChangeLog> spec = new Specification<MemberAssetChangeLog>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<MemberAssetChangeLog> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotBlank(param.getMemberAccountId())) {
					predicates.add(builder.equal(root.get("memberAccountId"), param.getMemberAccountId()));
				}
				if (param.getMemberUid() != null) {
					predicates.add(builder.equal(root.join("memberAccount").get("uid"), param.getMemberUid()));
				}
			if (StrUtil.isNotBlank(param.getRealName())) {
				predicates.add(builder.like(root.join("memberAccount").get("realName"),
						"%" + param.getRealName().trim() + "%"));
			}
			if (StrUtil.isNotBlank(param.getAccountType())) {
				predicates.add(builder.equal(root.get("accountType"), param.getAccountType()));
			}
			if (StrUtil.isNotBlank(param.getAssetCode())) {
				predicates.add(builder.equal(root.get("assetCode"), param.getAssetCode()));
			}
			if (StrUtil.isNotBlank(param.getSymbolCode())) {
				predicates.add(builder.equal(root.get("symbolCode"), param.getSymbolCode()));
			}
				if (StrUtil.isNotBlank(param.getBizOrderNo())) {
					predicates.add(builder.like(root.get("bizOrderNo"), "%" + param.getBizOrderNo().trim() + "%"));
				}
				if (StrUtil.isNotEmpty(param.getChangeType())) {
					predicates.add(root.get("changeType").in(Arrays.asList(param.getChangeType().split(","))));
				}
				if ("in".equals(param.getDirection())) {
					predicates.add(builder.greaterThan(root.get("balanceChange"), BigDecimal.ZERO));
				} else if ("out".equals(param.getDirection())) {
					predicates.add(builder.lessThan(root.get("balanceChange"), BigDecimal.ZERO));
				}
				if (param.getTimeStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("changeTime"), param.getTimeStart()));
				}
				if (param.getTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("changeTime"), param.getTimeEnd()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		return spec;
	}

}

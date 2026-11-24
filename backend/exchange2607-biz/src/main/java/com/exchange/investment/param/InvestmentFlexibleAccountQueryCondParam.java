package com.exchange.investment.param;

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
import com.exchange.investment.domain.InvestmentFlexibleAccount;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InvestmentFlexibleAccountQueryCondParam extends PageParam {

	private String memberAccountId;

	private Long memberUid;

	private String assetCode;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTimeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTimeEnd;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime nextAccrualTimeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime nextAccrualTimeEnd;

	public Specification<InvestmentFlexibleAccount> buildSpecification() {
		InvestmentFlexibleAccountQueryCondParam param = this;
		return new Specification<InvestmentFlexibleAccount>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<InvestmentFlexibleAccount> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getMemberAccountId())) {
					predicates.add(builder.equal(root.get("memberAccountId"), param.getMemberAccountId()));
				}
				if (param.getMemberUid() != null) {
					predicates.add(builder.equal(root.join("memberAccount").get("uid"),
							param.getMemberUid()));
				}
				if (StrUtil.isNotBlank(param.getAssetCode())) {
					predicates.add(builder.equal(root.get("assetCode"), param.getAssetCode()));
				}
			if (param.getCreateTimeStart() != null) {
				predicates.add(builder.greaterThanOrEqualTo(root.get("createTime"), param.getCreateTimeStart()));
			}
				if (param.getCreateTimeEnd() != null) {
				predicates.add(builder.lessThanOrEqualTo(root.get("createTime"), param.getCreateTimeEnd()));
			}
				if (param.getNextAccrualTimeStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("nextAccrualTime"),
							param.getNextAccrualTimeStart()));
				}
				if (param.getNextAccrualTimeEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("nextAccrualTime"),
							param.getNextAccrualTimeEnd()));
				}
				return predicates.isEmpty() ? null : builder.and(predicates.toArray(new Predicate[0]));
			}
		};
	}

}

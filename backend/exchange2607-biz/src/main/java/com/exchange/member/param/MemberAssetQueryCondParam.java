package com.exchange.member.param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.exchange.common.param.PageParam;
import com.exchange.member.domain.MemberAsset;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MemberAssetQueryCondParam extends PageParam {

	private Long memberUid;

	/** 会员账号状态：逗号分隔，如 NORMAL,FROZEN */
	private String memberAccountStatus;

	private String assetCode;

	private String accountType;

	private BigDecimal availableStart;

	private BigDecimal availableEnd;

	public Specification<MemberAsset> buildSpecification() {
		MemberAssetQueryCondParam param = this;
		Specification<MemberAsset> spec = new Specification<MemberAsset>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<MemberAsset> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (param.getMemberUid() != null) {
					predicates.add(builder.equal(root.join("memberAccount").get("uid"), param.getMemberUid()));
				}
				if (StrUtil.isNotBlank(param.getMemberAccountStatus())) {
					predicates.add(root.join("memberAccount").get("status")
							.in(Arrays.asList(param.getMemberAccountStatus().split(","))));
				}
				if (StrUtil.isNotBlank(param.getAssetCode())) {
					predicates.add(builder.equal(root.get("assetCode"), param.getAssetCode().trim()));
				}
				if (StrUtil.isNotBlank(param.getAccountType())) {
					predicates.add(builder.equal(root.get("accountType"), param.getAccountType()));
				}
				if (param.getAvailableStart() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("available"), param.getAvailableStart()));
				}
				if (param.getAvailableEnd() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("available"), param.getAvailableEnd()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		return spec;
	}

}

package com.exchange.rebate.param;

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
import com.exchange.rebate.domain.RebateRecord;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 返佣记录查询条件（客户端/后台共用）
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RebateRecordQueryCondParam extends PageParam {

	/** 受益人会员UID（后台查询用，按 beneficiary 关联 uid 过滤） */
	private Long beneficiaryUid;

	/** 来源会员UID（后台查询用，按 sourceMember 关联 uid 过滤） */
	private Long sourceMemberUid;

	/** 受益人（收佣人）会员ID */
	private String beneficiaryId;

	/** 来源会员会员ID */
	private String sourceMemberId;

	/** 返佣层级 */
	private Integer level;

	/** 业务类型：1=现货返佣 2=合约返佣 */
	private String bizType;

	/** 币种（如 USDT） */
	private String assetCode;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	public Specification<RebateRecord> buildSpecification() {
		RebateRecordQueryCondParam param = this;
		return new Specification<RebateRecord>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<RebateRecord> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<>();
				if (StrUtil.isNotBlank(param.getBeneficiaryId())) {
					predicates.add(builder.equal(root.get("beneficiaryId"), param.getBeneficiaryId()));
				}
				if (param.getBeneficiaryUid() != null) {
					predicates.add(builder.equal(root.join("beneficiary").get("uid"), param.getBeneficiaryUid()));
				}
				if (StrUtil.isNotBlank(param.getSourceMemberId())) {
					predicates.add(builder.equal(root.get("sourceMemberId"), param.getSourceMemberId()));
				}
				if (param.getSourceMemberUid() != null) {
					predicates.add(builder.equal(root.join("sourceMember").get("uid"), param.getSourceMemberUid()));
				}
				if (param.getLevel() != null) {
					predicates.add(builder.equal(root.get("level"), param.getLevel()));
				}
				if (StrUtil.isNotBlank(param.getBizType())) {
					predicates.add(builder.equal(root.get("bizType"), param.getBizType()));
				}
				if (StrUtil.isNotBlank(param.getAssetCode())) {
					predicates.add(builder.equal(root.get("assetCode"), param.getAssetCode()));
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

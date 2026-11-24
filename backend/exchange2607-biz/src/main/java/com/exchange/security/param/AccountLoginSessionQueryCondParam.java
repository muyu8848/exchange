package com.exchange.security.param;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

import com.exchange.common.param.PageParam;
import com.exchange.security.domain.AccountLoginSession;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AccountLoginSessionQueryCondParam extends PageParam {

	/** MEMBER / ADMIN，由 Controller 固定 */
	private String accountType;

	private Long accountUid;

	/** 会员登录账号 / 后台账号名，模糊匹配 */
	private String accountName;

	private String deviceType;

	/** IP 地址，模糊匹配 */
	private String ip;

	/** ONLINE / LOGGED_OUT / KICKED / REPLACED / EXPIRED */
	private String status;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeStart;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timeEnd;

	public Specification<AccountLoginSession> buildSpecification() {
		AccountLoginSessionQueryCondParam param = this;
		return (root, query, builder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (StrUtil.isNotBlank(param.getAccountType())) {
				predicates.add(builder.equal(root.get("accountType"), param.getAccountType()));
			}
			if (param.getAccountUid() != null) {
				predicates.add(builder.equal(root.get("accountUid"), param.getAccountUid()));
			}
			if (StrUtil.isNotBlank(param.getAccountName())) {
				predicates.add(builder.like(root.get("accountName"), "%" + param.getAccountName().trim() + "%"));
			}
			if (StrUtil.isNotBlank(param.getDeviceType())) {
				predicates.add(builder.equal(root.get("deviceType"), param.getDeviceType()));
			}
			if (StrUtil.isNotBlank(param.getIp())) {
				predicates.add(builder.like(root.get("ip"), "%" + param.getIp().trim() + "%"));
			}
			if (StrUtil.isNotBlank(param.getStatus())) {
				predicates.add(root.get("status").in(Arrays.asList(param.getStatus().split(","))));
			}
			if (param.getTimeStart() != null) {
				predicates.add(builder.greaterThanOrEqualTo(root.<LocalDateTime>get("lastActiveTime"),
						param.getTimeStart()));
			}
			if (param.getTimeEnd() != null) {
				predicates.add(builder.lessThanOrEqualTo(root.<LocalDateTime>get("lastActiveTime"), param.getTimeEnd()));
			}
			return builder.and(predicates.toArray(new Predicate[0]));
		};
	}
}

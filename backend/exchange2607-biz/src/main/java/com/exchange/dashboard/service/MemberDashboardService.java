package com.exchange.dashboard.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.utils.TupleUtils;
import com.exchange.constants.Constant;
import com.exchange.dashboard.param.MemberDashboardQueryParam;
import com.exchange.dashboard.vo.MemberDashboardDailyItemVO;
import com.exchange.dashboard.vo.MemberDashboardDistributionItemVO;
import com.exchange.dashboard.vo.MemberDashboardOverviewVO;
import com.exchange.dashboard.vo.MemberDashboardSummaryVO;
import com.exchange.dashboard.vo.MemberDashboardTrendItemVO;
import com.exchange.log.domain.LoginLog;
import com.exchange.member.domain.MemberAccount;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 会员看板统计。
 * <p>
 * 新增/实名新增按发生时间统计；活跃会员取登录日志（客户端 + 成功登录 + 去重），
 * 不能使用 latelyLoginTime（只保留最后一次登录，无法回溯历史日活）。
 */
@Slf4j
@Validated
@Service
public class MemberDashboardService {

	@Autowired
	private EntityManager entityManager;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public MemberDashboardSummaryVO summary(MemberDashboardQueryParam param) {
		normalizeParam(param);

		List<Tuple> newByDate = newMemberByDate(param);
		List<Tuple> verifiedByDate = newVerifiedByDate(param);
		List<Tuple> activeByDate = activeByDate(param);

		long newMemberCount = sumCount(newByDate);
		long newVerifiedCount = sumCount(verifiedByDate);
		long activeMemberCount = sumCount(activeByDate);
		long totalRegisteredCount = countAllMembers();
		long totalMemberCount = countMembers(param, null);
		long verifiedMemberCount = countMembers(param, Constant.会员认证状态_已认证);
		long disabledMemberCount = countDisabled(param);

		MemberDashboardOverviewVO overview = new MemberDashboardOverviewVO();
		overview.setNewMemberCount(newMemberCount);
		overview.setTotalRegisteredCount(totalRegisteredCount);
		overview.setTotalMemberCount(totalMemberCount);
		overview.setActiveMemberCount(activeMemberCount);
		overview.setVerifiedMemberCount(verifiedMemberCount);
		overview.setNewVerifiedCount(newVerifiedCount);
		overview.setDisabledMemberCount(disabledMemberCount);

		MemberDashboardSummaryVO vo = new MemberDashboardSummaryVO();
		vo.setOverview(overview);
		vo.setTrend(buildTrend(newByDate, activeByDate));
		vo.setContactTypeDistribution(contactTypeDistribution(param));
		vo.setAuthenticationStatusDistribution(authenticationStatusDistribution(param));
		return vo;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<MemberDashboardDailyItemVO> daily(MemberDashboardQueryParam param) {
		normalizeParam(param);

		Map<String, MemberDashboardDailyItemVO> map = new TreeMap<>();
		for (Tuple tuple : newMemberByDate(param)) {
			MemberDashboardDailyItemVO item = item(map, tuple.get("date", String.class));
			item.setNewMemberCount(item.getNewMemberCount() + TupleUtils.getLong(tuple, "count"));
		}
		for (Tuple tuple : newVerifiedByDate(param)) {
			MemberDashboardDailyItemVO item = item(map, tuple.get("date", String.class));
			item.setNewVerifiedCount(item.getNewVerifiedCount() + TupleUtils.getLong(tuple, "count"));
		}
		for (Tuple tuple : activeByDate(param)) {
			MemberDashboardDailyItemVO item = item(map, tuple.get("date", String.class));
			item.setActiveMemberCount(item.getActiveMemberCount() + TupleUtils.getLong(tuple, "count"));
		}
		return new ArrayList<>(map.values());
	}

	// ============ 新增 / 实名新增（member_account） ============

	private List<Tuple> newMemberByDate(MemberDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<MemberAccount> root = query.from(MemberAccount.class);
		Expression<String> dateExpr = dateExpr(cb, root, "createTime", param.getGranularity());

		query.multiselect(dateExpr.alias("date"), cb.count(root).alias("count"));
		List<Predicate> predicates = buildMemberPredicate(cb, root, param);
		predicates.addAll(buildTimePredicate(cb, root, "createTime", param));
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		query.groupBy(dateExpr);
		query.orderBy(cb.asc(dateExpr));
		return entityManager.createQuery(query).getResultList();
	}

	private List<Tuple> newVerifiedByDate(MemberDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<MemberAccount> root = query.from(MemberAccount.class);
		Expression<String> dateExpr = dateExpr(cb, root, "authenticationPassedTime", param.getGranularity());

		query.multiselect(dateExpr.alias("date"), cb.count(root).alias("count"));
		List<Predicate> predicates = buildMemberPredicate(cb, root, param);
		predicates.addAll(buildTimePredicate(cb, root, "authenticationPassedTime", param));
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		query.groupBy(dateExpr);
		query.orderBy(cb.asc(dateExpr));
		return entityManager.createQuery(query).getResultList();
	}

	// ============ 活跃会员（login_log） ============

	private List<Tuple> activeByDate(MemberDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<LoginLog> root = query.from(LoginLog.class);
		Expression<String> dateExpr = dateExpr(cb, root, "loginTime", param.getGranularity());

		query.multiselect(dateExpr.alias("date"),
				cb.countDistinct(root.get("accountId")).alias("count"));
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.isNotNull(root.get("loginTime")));
		predicates.add(cb.equal(root.get("status"), Constant.登录状态_成功));
		predicates.add(cb.equal(root.get("subSystem"), Constant.子系统_客户端));
		if (param.getTimeStart() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("loginTime"), param.getTimeStart()));
		}
		if (param.getTimeEnd() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("loginTime"), param.getTimeEnd()));
		}
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		query.groupBy(dateExpr);
		query.orderBy(cb.asc(dateExpr));
		return entityManager.createQuery(query).getResultList();
	}

	// ============ 快照 / 分布 ============

	/** 累计注册（全量快照，含已注销，不受筛选影响） */
	private long countAllMembers() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<MemberAccount> root = query.from(MemberAccount.class);
		query.select(cb.count(root));
		Long count = entityManager.createQuery(query).getSingleResult();
		return count != null ? count : 0L;
	}

	private long countMembers(MemberDashboardQueryParam param, String fixedAuthenticationStatus) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<MemberAccount> root = query.from(MemberAccount.class);
		query.select(cb.count(root));
		List<Predicate> predicates = buildMemberPredicate(cb, root, param);
		if (fixedAuthenticationStatus != null) {
			predicates.add(cb.equal(root.get("authenticationStatus"), fixedAuthenticationStatus));
		}
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		Long count = entityManager.createQuery(query).getSingleResult();
		return count != null ? count : 0L;
	}

	private long countDisabled(MemberDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<MemberAccount> root = query.from(MemberAccount.class);
		query.select(cb.count(root));
		List<Predicate> predicates = buildMemberPredicate(cb, root, param);
		predicates.add(cb.equal(root.get("status"), Constant.账号状态_冻结));
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		Long count = entityManager.createQuery(query).getSingleResult();
		return count != null ? count : 0L;
	}

	private List<MemberDashboardDistributionItemVO> contactTypeDistribution(MemberDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<MemberAccount> root = query.from(MemberAccount.class);
		query.multiselect(root.get("contactType").alias("code"), cb.count(root).alias("count"));
		List<Predicate> predicates = buildMemberPredicate(cb, root, param);
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		query.groupBy(root.get("contactType"));

		List<MemberDashboardDistributionItemVO> list = new ArrayList<>();
		for (Tuple tuple : entityManager.createQuery(query).getResultList()) {
			MemberDashboardDistributionItemVO item = new MemberDashboardDistributionItemVO();
			item.setName(contactTypeName(tuple.get("code", String.class)));
			item.setValue(TupleUtils.getLong(tuple, "count"));
			list.add(item);
		}
		return list;
	}

	private List<MemberDashboardDistributionItemVO> authenticationStatusDistribution(MemberDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<MemberAccount> root = query.from(MemberAccount.class);
		query.multiselect(root.get("authenticationStatus").alias("code"), cb.count(root).alias("count"));
		List<Predicate> predicates = buildMemberPredicate(cb, root, param);
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		query.groupBy(root.get("authenticationStatus"));

		List<MemberDashboardDistributionItemVO> list = new ArrayList<>();
		for (Tuple tuple : entityManager.createQuery(query).getResultList()) {
			MemberDashboardDistributionItemVO item = new MemberDashboardDistributionItemVO();
			item.setName(authenticationStatusName(tuple.get("code", String.class)));
			item.setValue(TupleUtils.getLong(tuple, "count"));
			list.add(item);
		}
		return list;
	}

	// ============ 工具 ============

	private List<Predicate> buildMemberPredicate(CriteriaBuilder cb, Root<MemberAccount> root,
			MemberDashboardQueryParam param) {
		List<Predicate> predicates = new ArrayList<>();
		if (StrUtil.isNotBlank(param.getStatus())) {
			predicates.add(root.get("status").in(Arrays.asList(param.getStatus().split(","))));
		} else {
			predicates.add(root.get("status").in(
					Arrays.asList(Constant.账号状态_正常, Constant.账号状态_冻结)));
		}
		if (StrUtil.isNotBlank(param.getAuthenticationStatus())) {
			predicates.add(cb.equal(root.get("authenticationStatus"), param.getAuthenticationStatus()));
		}
		if (StrUtil.isNotBlank(param.getContactType())) {
			predicates.add(cb.equal(root.get("contactType"), param.getContactType()));
		}
		return predicates;
	}

	private List<Predicate> buildTimePredicate(CriteriaBuilder cb, Root<MemberAccount> root, String timeField,
			MemberDashboardQueryParam param) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.isNotNull(root.get(timeField)));
		if (param.getTimeStart() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get(timeField), param.getTimeStart()));
		}
		if (param.getTimeEnd() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get(timeField), param.getTimeEnd()));
		}
		return predicates;
	}

	private List<MemberDashboardTrendItemVO> buildTrend(List<Tuple> newByDate, List<Tuple> activeByDate) {
		Map<String, MemberDashboardTrendItemVO> map = new TreeMap<>();
		for (Tuple tuple : newByDate) {
			MemberDashboardTrendItemVO item = trendItem(map, tuple.get("date", String.class));
			item.setNewMemberCount(item.getNewMemberCount() + TupleUtils.getLong(tuple, "count"));
		}
		for (Tuple tuple : activeByDate) {
			MemberDashboardTrendItemVO item = trendItem(map, tuple.get("date", String.class));
			item.setActiveMemberCount(item.getActiveMemberCount() + TupleUtils.getLong(tuple, "count"));
		}
		return new ArrayList<>(map.values());
	}

	private MemberDashboardTrendItemVO trendItem(Map<String, MemberDashboardTrendItemVO> map, String date) {
		return map.computeIfAbsent(date, key -> {
			MemberDashboardTrendItemVO item = new MemberDashboardTrendItemVO();
			item.setDate(key);
			return item;
		});
	}

	private MemberDashboardDailyItemVO item(Map<String, MemberDashboardDailyItemVO> map, String date) {
		return map.computeIfAbsent(date, key -> {
			MemberDashboardDailyItemVO item = new MemberDashboardDailyItemVO();
			item.setDate(key);
			return item;
		});
	}

	private Expression<String> dateExpr(CriteriaBuilder cb, Root<?> root, String timeField, String granularity) {
		String pattern = "hour".equals(granularity) ? "%Y-%m-%d %H:00" : "%Y-%m-%d";
		return cb.function("DATE_FORMAT", String.class, root.get(timeField), cb.literal(pattern));
	}

	private void normalizeParam(MemberDashboardQueryParam param) {
		// timeStart 为空表示查全部历史，不补默认起点
		if (param.getTimeEnd() == null) {
			param.setTimeEnd(LocalDateTime.now());
		}
		if (StrUtil.isBlank(param.getGranularity())) {
			boolean shortRange = param.getTimeStart() != null
					&& Duration.between(param.getTimeStart(), param.getTimeEnd()).toHours() <= 48;
			param.setGranularity(shortRange ? "hour" : "day");
		}
	}

	private long sumCount(List<Tuple> list) {
		long total = 0;
		for (Tuple tuple : list) {
			total += TupleUtils.getLong(tuple, "count");
		}
		return total;
	}

	private String contactTypeName(String code) {
		if (Constant.联系方式_邮箱.equals(code)) {
			return "邮箱";
		}
		if (Constant.联系方式_手机号码.equals(code)) {
			return "手机号";
		}
		return "未知";
	}

	private String authenticationStatusName(String code) {
		if (Constant.会员认证状态_未认证.equals(code)) {
			return "未认证";
		}
		if (Constant.会员认证状态_认证待审核.equals(code)) {
			return "待审核";
		}
		if (Constant.会员认证状态_已认证.equals(code)) {
			return "已认证";
		}
		if (Constant.会员认证状态_审核不通过.equals(code)) {
			return "审核不通过";
		}
		return "未知";
	}

}

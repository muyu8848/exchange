package com.exchange.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.c2c.domain.C2cAppealRecord;
import com.exchange.c2c.domain.C2cOrder;
import com.exchange.c2c.domain.C2cPreOrder;
import com.exchange.common.utils.TupleUtils;
import com.exchange.constants.Constant;
import com.exchange.dashboard.param.C2cDashboardQueryParam;
import com.exchange.dashboard.vo.C2cDashboardDailyItemVO;
import com.exchange.dashboard.vo.C2cDashboardDistributionItemVO;
import com.exchange.dashboard.vo.C2cDashboardOverviewVO;
import com.exchange.dashboard.vo.C2cDashboardSummaryVO;
import com.exchange.dashboard.vo.C2cDashboardTrendItemVO;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * C2C 交易看板统计。
 * <p>
 * 成交类指标按完成时间 finishTime 统计，创建/取消/状态分布按创建时间 createTime 统计；
 * 成交额使用订单落库的 usdt_amount（未折算订单不计入）。
 */
@Slf4j
@Validated
@Service
public class C2cDashboardService {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	@Autowired
	private EntityManager entityManager;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public C2cDashboardSummaryVO summary(C2cDashboardQueryParam param) {
		normalizeParam(param);

		List<Tuple> dealList = dealByTime(param);
		List<Tuple> createdList = createdByTime(param);

		long completedCount = 0;
		BigDecimal dealAmount = BigDecimal.ZERO;
		for (Tuple tuple : dealList) {
			completedCount += TupleUtils.getLong(tuple, "completedCount");
			dealAmount = dealAmount.add(TupleUtils.getBigDecimal(tuple, "dealAmount"));
		}

		long createdCount = 0;
		long cancelledCount = 0;
		for (Tuple tuple : createdList) {
			createdCount += TupleUtils.getLong(tuple, "createdCount");
			cancelledCount += TupleUtils.getLong(tuple, "cancelledCount");
		}

		C2cDashboardOverviewVO overview = new C2cDashboardOverviewVO();
		overview.setCreatedCount(createdCount);
		overview.setCompletedCount(completedCount);
		overview.setDealAmount(scale(dealAmount));
		overview.setCompletionRate(rate(completedCount, createdCount));
		overview.setCancelRate(rate(cancelledCount, createdCount));
		overview.setAppealingCount(appealingCount(param));
		overview.setAvgReleaseSeconds(avgReleaseSeconds(param));

		C2cDashboardSummaryVO vo = new C2cDashboardSummaryVO();
		vo.setOverview(overview);
		vo.setTrend(buildTrend(dealList));
		vo.setFiatDistribution(fiatDistribution(param));
		vo.setPaymentDistribution(paymentDistribution(param));
		vo.setAssetDistribution(assetDistribution(param));
		vo.setDirectionDistribution(directionDistribution(param));
		return vo;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<C2cDashboardDailyItemVO> daily(C2cDashboardQueryParam param) {
		normalizeParam(param);

		Map<String, C2cDashboardDailyItemVO> map = new TreeMap<>();
		for (Tuple tuple : dealByTime(param)) {
			C2cDashboardDailyItemVO item = item(map, tuple.get("date", String.class));
			item.setCompletedCount(item.getCompletedCount() + TupleUtils.getLong(tuple, "completedCount"));
			item.setDealAmount(item.getDealAmount().add(TupleUtils.getBigDecimal(tuple, "dealAmount")));
		}
		for (Tuple tuple : createdByTime(param)) {
			C2cDashboardDailyItemVO item = item(map, tuple.get("date", String.class));
			item.setCreatedCount(item.getCreatedCount() + TupleUtils.getLong(tuple, "createdCount"));
			item.setCancelledCount(item.getCancelledCount() + TupleUtils.getLong(tuple, "cancelledCount"));
		}
		for (Tuple tuple : appealByTime(param)) {
			C2cDashboardDailyItemVO item = item(map, tuple.get("date", String.class));
			item.setAppealCount(item.getAppealCount() + TupleUtils.getLong(tuple, "appealCount"));
		}

		List<C2cDashboardDailyItemVO> list = new ArrayList<>(map.values());
		for (C2cDashboardDailyItemVO item : list) {
			item.setDealAmount(scale(item.getDealAmount()));
			item.setCompletionRate(rate(item.getCompletedCount(), item.getCreatedCount()));
			item.setCancelRate(rate(item.getCancelledCount(), item.getCreatedCount()));
		}
		return list;
	}

	// ============ 成交聚合（按 finishTime） ============

	private List<Tuple> dealByTime(C2cDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<C2cOrder> root = query.from(C2cOrder.class);
		Expression<String> dateExpr = dateExpr(cb, root, "finishTime", param.getGranularity());

		query.multiselect(dateExpr.alias("date"),
				cb.count(root).alias("completedCount"),
				cb.sum(root.<BigDecimal>get("usdtAmount")).alias("dealAmount"));

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("status"), Constant.C2C订单状态_已完成));
		predicates.add(cb.isNotNull(root.get("finishTime")));
		if (param.getTimeStart() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("finishTime"), param.getTimeStart()));
		}
		if (param.getTimeEnd() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("finishTime"), param.getTimeEnd()));
		}
		appendFilters(cb, root, param, predicates);
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		query.groupBy(dateExpr);
		query.orderBy(cb.asc(dateExpr));
		return entityManager.createQuery(query).getResultList();
	}

	// ============ 创建/取消聚合（按 createTime） ============

	private List<Tuple> createdByTime(C2cDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<C2cOrder> root = query.from(C2cOrder.class);
		Expression<String> dateExpr = dateExpr(cb, root, "createTime", param.getGranularity());
		Expression<Integer> cancelFlag = cb.selectCase()
				.when(cb.equal(root.get("status"), Constant.C2C订单状态_已取消), 1)
				.otherwise(0).as(Integer.class);

		query.multiselect(dateExpr.alias("date"),
				cb.count(root).alias("createdCount"),
				cb.sum(cancelFlag).alias("cancelledCount"));

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.isNotNull(root.get("createTime")));
		if (param.getTimeStart() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("createTime"), param.getTimeStart()));
		}
		if (param.getTimeEnd() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("createTime"), param.getTimeEnd()));
		}
		appendFilters(cb, root, param, predicates);
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		query.groupBy(dateExpr);
		query.orderBy(cb.asc(dateExpr));
		return entityManager.createQuery(query).getResultList();
	}

	// ============ 申诉聚合（按 createTime） ============

	private List<Tuple> appealByTime(C2cDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<C2cAppealRecord> root = query.from(C2cAppealRecord.class);
		Expression<String> dateExpr = dateExpr(cb, root, "createTime", param.getGranularity());

		query.multiselect(dateExpr.alias("date"), cb.count(root).alias("appealCount"));
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.isNotNull(root.get("createTime")));
		if (param.getTimeStart() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("createTime"), param.getTimeStart()));
		}
		if (param.getTimeEnd() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("createTime"), param.getTimeEnd()));
		}
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		query.groupBy(dateExpr);
		query.orderBy(cb.asc(dateExpr));
		return entityManager.createQuery(query).getResultList();
	}

	// ============ 快照指标 ============

	private long appealingCount(C2cDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<C2cOrder> root = query.from(C2cOrder.class);
		query.select(cb.count(root));
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("status"), Constant.C2C订单状态_申诉中));
		appendFilters(cb, root, param, predicates);
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		Long count = entityManager.createQuery(query).getSingleResult();
		return count != null ? count : 0L;
	}

	private long avgReleaseSeconds(C2cDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<C2cOrder> root = query.from(C2cOrder.class);

		query.multiselect(cb.avg(root.<Long>get("releaseSeconds")).alias("avgSeconds"));
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("status"), Constant.C2C订单状态_已完成));
		predicates.add(cb.isNotNull(root.get("releaseSeconds")));
		if (param.getTimeStart() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("finishTime"), param.getTimeStart()));
		}
		if (param.getTimeEnd() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("finishTime"), param.getTimeEnd()));
		}
		appendFilters(cb, root, param, predicates);
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		Tuple tuple = entityManager.createQuery(query).getSingleResult();
		Double avg = tuple.get("avgSeconds", Double.class);
		return avg != null ? Math.round(avg) : 0L;
	}

	// ============ 分布 ============

	private List<C2cDashboardDistributionItemVO> fiatDistribution(C2cDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<C2cOrder> root = query.from(C2cOrder.class);

		query.multiselect(root.get("fiatCode").alias("key"),
				cb.count(root).alias("orderCount"),
				cb.sum(root.<BigDecimal>get("usdtAmount")).alias("dealAmount"));
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("status"), Constant.C2C订单状态_已完成));
		predicates.add(cb.isNotNull(root.get("finishTime")));
		if (param.getTimeStart() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("finishTime"), param.getTimeStart()));
		}
		if (param.getTimeEnd() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("finishTime"), param.getTimeEnd()));
		}
		appendFilters(cb, root, param, predicates);
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		query.groupBy(root.get("fiatCode"));
		return buildDistribution(query, tuple -> tuple.get("key", String.class));
	}

	private List<C2cDashboardDistributionItemVO> paymentDistribution(C2cDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<C2cOrder> root = query.from(C2cOrder.class);

		query.multiselect(root.get("paymentAccountTypeCode").alias("key"),
				cb.count(root).alias("orderCount"),
				cb.sum(root.<BigDecimal>get("usdtAmount")).alias("dealAmount"));
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("status"), Constant.C2C订单状态_已完成));
		predicates.add(cb.isNotNull(root.get("finishTime")));
		if (param.getTimeStart() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("finishTime"), param.getTimeStart()));
		}
		if (param.getTimeEnd() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("finishTime"), param.getTimeEnd()));
		}
		appendFilters(cb, root, param, predicates);
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		query.groupBy(root.get("paymentAccountTypeCode"));
		return buildDistribution(query, tuple -> tuple.get("key", String.class));
	}

	private List<C2cDashboardDistributionItemVO> assetDistribution(C2cDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<C2cOrder> root = query.from(C2cOrder.class);

		query.multiselect(root.get("assetCode").alias("key"),
				cb.count(root).alias("orderCount"),
				cb.sum(root.<BigDecimal>get("usdtAmount")).alias("dealAmount"));
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("status"), Constant.C2C订单状态_已完成));
		predicates.add(cb.isNotNull(root.get("finishTime")));
		if (param.getTimeStart() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("finishTime"), param.getTimeStart()));
		}
		if (param.getTimeEnd() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("finishTime"), param.getTimeEnd()));
		}
		appendFilters(cb, root, param, predicates);
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		query.groupBy(root.get("assetCode"));
		return buildDistribution(query, tuple -> tuple.get("key", String.class));
	}

	private List<C2cDashboardDistributionItemVO> directionDistribution(C2cDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<C2cOrder> root = query.from(C2cOrder.class);
		Join<C2cOrder, C2cPreOrder> preOrder = root.join("preOrder", JoinType.LEFT);

		query.multiselect(preOrder.get("orderType").alias("key"),
				cb.count(root).alias("orderCount"),
				cb.sum(root.<BigDecimal>get("usdtAmount")).alias("dealAmount"));
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("status"), Constant.C2C订单状态_已完成));
		predicates.add(cb.isNotNull(root.get("finishTime")));
		if (param.getTimeStart() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("finishTime"), param.getTimeStart()));
		}
		if (param.getTimeEnd() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("finishTime"), param.getTimeEnd()));
		}
		appendFilters(cb, root, param, predicates);
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		query.groupBy(preOrder.get("orderType"));
		return buildDistribution(query, tuple -> directionName(tuple.get("key", String.class)));
	}

	private List<C2cDashboardDistributionItemVO> buildDistribution(CriteriaQuery<Tuple> query,
			java.util.function.Function<Tuple, String> nameResolver) {
		List<C2cDashboardDistributionItemVO> list = new ArrayList<>();
		for (Tuple tuple : entityManager.createQuery(query).getResultList()) {
			C2cDashboardDistributionItemVO item = new C2cDashboardDistributionItemVO();
			String key = tuple.get("key", String.class);
			item.setKey(key);
			item.setName(nameResolver.apply(tuple));
			item.setOrderCount(TupleUtils.getLong(tuple, "orderCount"));
			item.setDealAmount(scale(TupleUtils.getBigDecimal(tuple, "dealAmount")));
			list.add(item);
		}
		list.sort((a, b) -> Long.compare(
				b.getOrderCount() != null ? b.getOrderCount() : 0L,
				a.getOrderCount() != null ? a.getOrderCount() : 0L));
		return list;
	}

	// ============ 工具 ============

	private void appendFilters(CriteriaBuilder cb, Root<C2cOrder> root, C2cDashboardQueryParam param,
			List<Predicate> predicates) {
		if (StrUtil.isNotBlank(param.getFiatCode())) {
			predicates.add(cb.equal(root.get("fiatCode"), param.getFiatCode()));
		}
		if (StrUtil.isNotBlank(param.getAssetCode())) {
			predicates.add(cb.equal(root.get("assetCode"), param.getAssetCode()));
		}
		if (StrUtil.isNotBlank(param.getPaymentTypeCode())) {
			predicates.add(cb.equal(root.get("paymentAccountTypeCode"), param.getPaymentTypeCode()));
		}
	}

	private List<C2cDashboardTrendItemVO> buildTrend(List<Tuple> dealList) {
		List<C2cDashboardTrendItemVO> list = new ArrayList<>();
		for (Tuple tuple : dealList) {
			C2cDashboardTrendItemVO item = new C2cDashboardTrendItemVO();
			item.setDate(tuple.get("date", String.class));
			item.setCompletedCount(TupleUtils.getLong(tuple, "completedCount"));
			item.setDealAmount(scale(TupleUtils.getBigDecimal(tuple, "dealAmount")));
			list.add(item);
		}
		return list;
	}

	private C2cDashboardDailyItemVO item(Map<String, C2cDashboardDailyItemVO> map, String date) {
		return map.computeIfAbsent(date, key -> {
			C2cDashboardDailyItemVO vo = new C2cDashboardDailyItemVO();
			vo.setDate(key);
			return vo;
		});
	}

	private Expression<String> dateExpr(CriteriaBuilder cb, Root<?> root, String timeField, String granularity) {
		String pattern = "hour".equals(granularity) ? "%Y-%m-%d %H:00" : "%Y-%m-%d";
		return cb.function("DATE_FORMAT", String.class, root.get(timeField), cb.literal(pattern));
	}

	private void normalizeParam(C2cDashboardQueryParam param) {
		if (param.getTimeEnd() == null) {
			param.setTimeEnd(LocalDateTime.now());
		}
		if (StrUtil.isBlank(param.getGranularity())) {
			boolean shortRange = param.getTimeStart() != null
					&& Duration.between(param.getTimeStart(), param.getTimeEnd()).toHours() <= 48;
			param.setGranularity(shortRange ? "hour" : "day");
		}
	}

	private BigDecimal rate(long numerator, long denominator) {
		if (denominator <= 0) {
			return BigDecimal.ZERO;
		}
		return BigDecimal.valueOf(numerator).multiply(HUNDRED)
				.divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
	}

	private BigDecimal scale(BigDecimal value) {
		return value.setScale(Constant.折合USDT精度, RoundingMode.HALF_UP);
	}

	private String directionName(String orderType) {
		if (Constant.C2C广告单类型_出售.equals(orderType)) {
			return "广告方卖出";
		}
		if (Constant.C2C广告单类型_购买.equals(orderType)) {
			return "广告方买入";
		}
		return "未知";
	}
}

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
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.utils.TupleUtils;
import com.exchange.constants.Constant;
import com.exchange.dashboard.param.SpotDashboardQueryParam;
import com.exchange.dashboard.vo.SpotDashboardDailyItemVO;
import com.exchange.dashboard.vo.SpotDashboardOverviewVO;
import com.exchange.dashboard.vo.SpotDashboardSummaryVO;
import com.exchange.dashboard.vo.SpotDashboardTrendItemVO;
import com.exchange.spot.domain.SpotOrder;
import com.exchange.spot.domain.SpotPlanOrder;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 现货交易看板统计。
 * <p>
 * 聚合统一走 JPA Criteria API（sum / DATE_FORMAT + groupBy），不写原生 SQL。
 * 成交类指标按成交时间 dealTime 统计；计划委托为当前快照，不受时间筛选影响。
 */
@Slf4j
@Validated
@Service
public class SpotDashboardService {

	@Autowired
	private EntityManager entityManager;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public SpotDashboardSummaryVO summary(SpotDashboardQueryParam param) {
		normalizeParam(param);

		List<Tuple> dealList = dealByTime(param);
		PlanSnapshot plan = planSnapshot(param);

		long dealCount = 0;
		BigDecimal buyDealTotal = BigDecimal.ZERO;
		BigDecimal sellDealTotal = BigDecimal.ZERO;
		BigDecimal feeUsdtAmount = BigDecimal.ZERO;
		for (Tuple tuple : dealList) {
			dealCount += TupleUtils.getLong(tuple, "dealCount");
			buyDealTotal = buyDealTotal.add(TupleUtils.getBigDecimal(tuple, "buyDealTotal"));
			sellDealTotal = sellDealTotal.add(TupleUtils.getBigDecimal(tuple, "sellDealTotal"));
			feeUsdtAmount = feeUsdtAmount.add(TupleUtils.getBigDecimal(tuple, "feeUsdtAmount"));
		}

		SpotDashboardOverviewVO overview = new SpotDashboardOverviewVO();
		overview.setDealCount(dealCount);
		overview.setBuyDealTotal(scale(buyDealTotal));
		overview.setSellDealTotal(scale(sellDealTotal));
		overview.setTotalDealTotal(scale(buyDealTotal.add(sellDealTotal)));
		overview.setFeeUsdtAmount(scale(feeUsdtAmount));
		overview.setPendingPlanCount(plan.getCount());
		overview.setPendingPlanAmount(scale(plan.getAmount()));

		SpotDashboardSummaryVO vo = new SpotDashboardSummaryVO();
		vo.setOverview(overview);
		vo.setTrend(buildTrend(dealList));
		return vo;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<SpotDashboardDailyItemVO> daily(SpotDashboardQueryParam param) {
		normalizeParam(param);

		Map<String, SpotDashboardDailyItemVO> map = new TreeMap<>();
		for (Tuple tuple : dealByTime(param)) {
			SpotDashboardDailyItemVO item = item(map, tuple);
			item.setDealCount(item.getDealCount() + TupleUtils.getLong(tuple, "dealCount"));
			item.setBuyDealTotal(item.getBuyDealTotal().add(TupleUtils.getBigDecimal(tuple, "buyDealTotal")));
			item.setSellDealTotal(item.getSellDealTotal().add(TupleUtils.getBigDecimal(tuple, "sellDealTotal")));
			item.setFeeUsdtAmount(item.getFeeUsdtAmount().add(TupleUtils.getBigDecimal(tuple, "feeUsdtAmount")));
		}
		List<SpotDashboardDailyItemVO> list = new ArrayList<>(map.values());
		for (SpotDashboardDailyItemVO item : list) {
			item.setTotalDealTotal(item.getBuyDealTotal().add(item.getSellDealTotal()));
		}
		return list;
	}

	// ============ 成交聚合（按 dealTime） ============

	private List<Tuple> dealByTime(SpotDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<SpotOrder> root = query.from(SpotOrder.class);
		Path<String> sidePath = root.get("side");
		Expression<String> dateExpr = dateExpr(cb, root, "dealTime", param.getGranularity());

		Expression<BigDecimal> buyDealTotal = cb.selectCase()
				.when(cb.equal(sidePath, Constant.现货方向_买入), root.<BigDecimal>get("dealTotal"))
				.otherwise(BigDecimal.ZERO).as(BigDecimal.class);
		Expression<BigDecimal> sellDealTotal = cb.selectCase()
				.when(cb.equal(sidePath, Constant.现货方向_卖出), root.<BigDecimal>get("dealTotal"))
				.otherwise(BigDecimal.ZERO).as(BigDecimal.class);

		query.multiselect(dateExpr.alias("date"),
				cb.count(root).alias("dealCount"),
				cb.sum(buyDealTotal).alias("buyDealTotal"),
				cb.sum(sellDealTotal).alias("sellDealTotal"),
				cb.sum(root.<BigDecimal>get("feeUsdtAmount")).alias("feeUsdtAmount"));

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("status"), Constant.现货订单状态_已完成));
		predicates.add(cb.isNotNull(root.get("dealTime")));
		if (param.getTimeStart() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get("dealTime"), param.getTimeStart()));
		}
		if (param.getTimeEnd() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get("dealTime"), param.getTimeEnd()));
		}
		if (StrUtil.isNotBlank(param.getSymbolCode())) {
			predicates.add(cb.equal(root.get("symbolCode"), param.getSymbolCode()));
		}
		if (StrUtil.isNotBlank(param.getSide())) {
			predicates.add(cb.equal(sidePath, param.getSide()));
		}
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		query.groupBy(dateExpr);
		query.orderBy(cb.asc(dateExpr));
		return entityManager.createQuery(query).getResultList();
	}

	// ============ 计划委托快照（不受时间筛选） ============

	private PlanSnapshot planSnapshot(SpotDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<SpotPlanOrder> root = query.from(SpotPlanOrder.class);
		Path<String> statusPath = root.get("status");

		Expression<Integer> pendingCount = cb.selectCase()
				.when(cb.equal(statusPath, Constant.现货计划委托状态_委托中), 1)
				.otherwise(0).as(Integer.class);
		Expression<BigDecimal> pendingAmount = cb.selectCase()
				.when(cb.equal(statusPath, Constant.现货计划委托状态_委托中),
						cb.prod(root.<BigDecimal>get("triggerPrice"), root.<BigDecimal>get("amount"))
								.as(BigDecimal.class))
				.otherwise(BigDecimal.ZERO).as(BigDecimal.class);

		query.multiselect(cb.sum(pendingCount).alias("pendingCount"),
				cb.sum(pendingAmount).alias("pendingAmount"));

		List<Predicate> predicates = new ArrayList<>();
		if (StrUtil.isNotBlank(param.getSymbolCode())) {
			predicates.add(cb.equal(root.get("symbolCode"), param.getSymbolCode()));
		}
		if (StrUtil.isNotBlank(param.getSide())) {
			predicates.add(cb.equal(root.get("side"), param.getSide()));
		}
		if (!predicates.isEmpty()) {
			query.where(cb.and(predicates.toArray(new Predicate[0])));
		}
		Tuple tuple = entityManager.createQuery(query).getSingleResult();
		return new PlanSnapshot(TupleUtils.getLong(tuple, "pendingCount"), TupleUtils.getBigDecimal(tuple, "pendingAmount"));
	}

	// ============ 工具 ============

	private List<SpotDashboardTrendItemVO> buildTrend(List<Tuple> dealList) {
		Map<String, SpotDashboardTrendItemVO> map = new TreeMap<>();
		for (Tuple tuple : dealList) {
			String date = tuple.get("date", String.class);
			SpotDashboardTrendItemVO item = map.computeIfAbsent(date, key -> {
				SpotDashboardTrendItemVO vo = new SpotDashboardTrendItemVO();
				vo.setDate(key);
				return vo;
			});
			item.setBuyDealTotal(TupleUtils.getBigDecimal(tuple, "buyDealTotal"));
			item.setSellDealTotal(TupleUtils.getBigDecimal(tuple, "sellDealTotal"));
			item.setFeeUsdtAmount(TupleUtils.getBigDecimal(tuple, "feeUsdtAmount"));
		}
		return new ArrayList<>(map.values());
	}

	private SpotDashboardDailyItemVO item(Map<String, SpotDashboardDailyItemVO> map, Tuple tuple) {
		String date = tuple.get("date", String.class);
		return map.computeIfAbsent(date, key -> {
			SpotDashboardDailyItemVO vo = new SpotDashboardDailyItemVO();
			vo.setDate(key);
			return vo;
		});
	}

	private Expression<String> dateExpr(CriteriaBuilder cb, Root<?> root, String timeField, String granularity) {
		String pattern = "hour".equals(granularity) ? "%Y-%m-%d %H:00" : "%Y-%m-%d";
		return cb.function("DATE_FORMAT", String.class, root.get(timeField), cb.literal(pattern));
	}

	private void normalizeParam(SpotDashboardQueryParam param) {
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

	private BigDecimal scale(BigDecimal value) {
		return value.setScale(Constant.折合USDT精度, RoundingMode.HALF_UP);
	}



	private static class PlanSnapshot {

		private final long count;

		private final BigDecimal amount;

		private PlanSnapshot(long count, BigDecimal amount) {
			this.count = count;
			this.amount = amount;
		}

		private long getCount() {
			return count;
		}

		private BigDecimal getAmount() {
			return amount;
		}
	}
}

package com.exchange.dashboard.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
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
import com.exchange.dashboard.param.DepositWithdrawDashboardQueryParam;
import com.exchange.dashboard.vo.DepositWithdrawDailyItemVO;
import com.exchange.dashboard.vo.DepositWithdrawOverviewVO;
import com.exchange.dashboard.vo.DepositWithdrawSummaryVO;
import com.exchange.dashboard.vo.DepositWithdrawTrendItemVO;
import com.exchange.deposit.domain.DepositOrder;
import com.exchange.withdraw.domain.WithdrawOrder;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 充提看板统计。
 * <p>
 * 聚合统一走 JPA Criteria API（selectCase + sum + groupBy），不写原生 SQL。
 * 时间口径：金额按到账/完成时间，笔数与成功率按提交时间，待审核/在途为当前快照。
 */
@Slf4j
@Validated
@Service
public class DepositWithdrawDashboardService {

	@Autowired
	private EntityManager entityManager;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public DepositWithdrawSummaryVO summary(DepositWithdrawDashboardQueryParam param) {
		normalizeParam(param);

		List<Tuple> depositByCreateTime = depositCountByCreateTime(param);
		List<Tuple> depositByCreditTime = depositAmountByCreditTime(param);
		List<Tuple> withdrawByCreateTime = withdrawCountByCreateTime(param);
		List<Tuple> withdrawByCompleteTime = withdrawAmountByCompleteTime(param);

		DepositWithdrawOverviewVO overview = new DepositWithdrawOverviewVO();

		long depositOrderCount = 0;
		for (Tuple tuple : depositByCreateTime) {
			depositOrderCount += TupleUtils.getLong(tuple, "orderCount");
		}

		BigDecimal depositAmount = BigDecimal.ZERO;
		for (Tuple tuple : depositByCreditTime) {
			depositAmount = depositAmount.add(TupleUtils.getBigDecimal(tuple, "successAmount"));
		}

		long withdrawOrderCount = 0;
		for (Tuple tuple : withdrawByCreateTime) {
			withdrawOrderCount += TupleUtils.getLong(tuple, "orderCount");
		}

		BigDecimal withdrawAmount = BigDecimal.ZERO;
		BigDecimal withdrawFeeUsdtAmount = BigDecimal.ZERO;
		for (Tuple tuple : withdrawByCompleteTime) {
			withdrawAmount = withdrawAmount.add(TupleUtils.getBigDecimal(tuple, "successAmount"));
			withdrawFeeUsdtAmount = withdrawFeeUsdtAmount.add(TupleUtils.getBigDecimal(tuple, "feeUsdtAmount"));
		}

		overview.setDepositAmount(depositAmount);
		overview.setDepositOrderCount(depositOrderCount);
		overview.setPendingConfirmDepositCount(
				countDepositByStatusIn(param, Constant.充币订单状态_确认中));
		overview.setWithdrawAmount(withdrawAmount);
		overview.setWithdrawFeeUsdtAmount(withdrawFeeUsdtAmount);
		overview.setWithdrawOrderCount(withdrawOrderCount);
		overview.setNetInflow(depositAmount.subtract(withdrawAmount));
		overview.setPendingAuditCount(
				countWithdrawByStatusIn(param, Constant.提币订单状态_待审核));
		overview.setInTransitCount(countWithdrawByStatusIn(param, Constant.提币订单状态_待审核,
				Constant.提币订单状态_审核通过));

		DepositWithdrawSummaryVO vo = new DepositWithdrawSummaryVO();
		vo.setOverview(overview);
		vo.setTrend(buildTrend(depositByCreditTime, withdrawByCompleteTime));
		return vo;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<DepositWithdrawDailyItemVO> daily(DepositWithdrawDashboardQueryParam param) {
		normalizeParam(param);

		Map<String, DepositWithdrawDailyItemVO> map = new TreeMap<>();

		for (Tuple tuple : depositCountByCreateTime(param)) {
			item(map, tuple).setDepositOrderCount(TupleUtils.getLong(tuple, "orderCount"));
		}
		for (Tuple tuple : depositAmountByCreditTime(param)) {
			DepositWithdrawDailyItemVO item = item(map, tuple);
			item.setDepositSuccessCount(TupleUtils.getLong(tuple, "successCount"));
			item.setDepositAmount(TupleUtils.getBigDecimal(tuple, "successAmount"));
		}
		for (Tuple tuple : withdrawCountByCreateTime(param)) {
			item(map, tuple).setWithdrawOrderCount(TupleUtils.getLong(tuple, "orderCount"));
		}
		for (Tuple tuple : withdrawAmountByCompleteTime(param)) {
			DepositWithdrawDailyItemVO item = item(map, tuple);
			item.setWithdrawSuccessCount(TupleUtils.getLong(tuple, "successCount"));
			item.setWithdrawAmount(TupleUtils.getBigDecimal(tuple, "successAmount"));
			item.setWithdrawFeeUsdtAmount(TupleUtils.getBigDecimal(tuple, "feeUsdtAmount"));
		}

		List<DepositWithdrawDailyItemVO> list = new ArrayList<>(map.values());
		for (DepositWithdrawDailyItemVO item : list) {
			item.setNetInflow(item.getDepositAmount().subtract(item.getWithdrawAmount()));
		}
		return list;
	}

	// ============ 聚合查询 ============

	/** 充值提交量：按 createTime 分组，统计总笔数与成功笔数（提交口径） */
	private List<Tuple> depositCountByCreateTime(DepositWithdrawDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<DepositOrder> root = query.from(DepositOrder.class);
		Expression<String> dateExpr = dateExpr(cb, root, "createTime", param.getGranularity());
		query.multiselect(dateExpr.alias("date"),
				cb.count(root).alias("orderCount"));
		query.where(buildPredicate(cb, root, param, "createTime"));
		query.groupBy(dateExpr);
		query.orderBy(cb.asc(dateExpr));
		return entityManager.createQuery(query).getResultList();
	}

	/** 充值成功金额：按 creditTime 分组 */
	private List<Tuple> depositAmountByCreditTime(DepositWithdrawDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<DepositOrder> root = query.from(DepositOrder.class);
		Path<BigDecimal> amountPath = root.get("creditedUsdtAmount");
		Expression<String> dateExpr = dateExpr(cb, root, "creditTime", param.getGranularity());
		query.multiselect(dateExpr.alias("date"),
				cb.count(root).alias("successCount"),
				cb.sum(amountPath).alias("successAmount"));
		query.where(cb.and(buildPredicate(cb, root, param, "creditTime"),
				cb.equal(root.get("status"), Constant.充币订单状态_已完成)));
		query.groupBy(dateExpr);
		query.orderBy(cb.asc(dateExpr));
		return entityManager.createQuery(query).getResultList();
	}

	/** 提币提交量：按 createTime 分组，统计总笔数与成功笔数（提交口径） */
	private List<Tuple> withdrawCountByCreateTime(DepositWithdrawDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<WithdrawOrder> root = query.from(WithdrawOrder.class);
		Expression<String> dateExpr = dateExpr(cb, root, "createTime", param.getGranularity());
		query.multiselect(dateExpr.alias("date"),
				cb.count(root).alias("orderCount"));
		query.where(buildPredicate(cb, root, param, "createTime"));
		query.groupBy(dateExpr);
		query.orderBy(cb.asc(dateExpr));
		return entityManager.createQuery(query).getResultList();
	}

	/** 提币成功金额与手续费：按 completeTime 分组 */
	private List<Tuple> withdrawAmountByCompleteTime(DepositWithdrawDashboardQueryParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<WithdrawOrder> root = query.from(WithdrawOrder.class);
		Path<BigDecimal> amountPath = root.get("paidUsdtAmount");
		Path<BigDecimal> feeUsdtPath = root.get("feeUsdtAmount");
		Expression<String> dateExpr = dateExpr(cb, root, "completeTime", param.getGranularity());
		query.multiselect(dateExpr.alias("date"),
				cb.count(root).alias("successCount"),
				cb.sum(amountPath).alias("successAmount"),
				cb.sum(feeUsdtPath).alias("feeUsdtAmount"));
		query.where(cb.and(buildPredicate(cb, root, param, "completeTime"),
				cb.equal(root.get("status"), Constant.提币订单状态_已提币)));
		query.groupBy(dateExpr);
		query.orderBy(cb.asc(dateExpr));
		return entityManager.createQuery(query).getResultList();
	}

	/** 充币按状态快照计数（不受时间范围影响，但受币种/链筛选影响） */
	private long countDepositByStatusIn(DepositWithdrawDashboardQueryParam param, String... statuses) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<DepositOrder> root = query.from(DepositOrder.class);
		query.select(cb.count(root));
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(root.get("status").in(Arrays.asList(statuses)));
		if (StrUtil.isNotBlank(param.getAssetCode())) {
			predicates.add(cb.equal(root.get("assetCode"), param.getAssetCode()));
		}
		if (StrUtil.isNotBlank(param.getChainType())) {
			predicates.add(cb.equal(root.get("chainType"), param.getChainType()));
		}
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		Long count = entityManager.createQuery(query).getSingleResult();
		return count != null ? count : 0L;
	}

	/** 提币按状态快照计数（不受时间范围影响，但受币种/链筛选影响） */
	private long countWithdrawByStatusIn(DepositWithdrawDashboardQueryParam param, String... statuses) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<WithdrawOrder> root = query.from(WithdrawOrder.class);
		query.select(cb.count(root));
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(root.get("status").in(Arrays.asList(statuses)));
		if (StrUtil.isNotBlank(param.getAssetCode())) {
			predicates.add(cb.equal(root.get("assetCode"), param.getAssetCode()));
		}
		if (StrUtil.isNotBlank(param.getChainType())) {
			predicates.add(cb.equal(root.get("chainType"), param.getChainType()));
		}
		query.where(cb.and(predicates.toArray(new Predicate[0])));
		Long count = entityManager.createQuery(query).getSingleResult();
		return count != null ? count : 0L;
	}

	// ============ 条件与工具 ============

	private <T> Predicate buildPredicate(CriteriaBuilder cb, Root<T> root,
			DepositWithdrawDashboardQueryParam param, String timeField) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.isNotNull(root.get(timeField)));
		if (param.getTimeStart() != null) {
			predicates.add(cb.greaterThanOrEqualTo(root.<LocalDateTime>get(timeField), param.getTimeStart()));
		}
		if (param.getTimeEnd() != null) {
			predicates.add(cb.lessThanOrEqualTo(root.<LocalDateTime>get(timeField), param.getTimeEnd()));
		}
		if (StrUtil.isNotBlank(param.getAssetCode())) {
			predicates.add(cb.equal(root.get("assetCode"), param.getAssetCode()));
		}
		if (StrUtil.isNotBlank(param.getChainType())) {
			predicates.add(cb.equal(root.get("chainType"), param.getChainType()));
		}
		return cb.and(predicates.toArray(new Predicate[0]));
	}

	private Expression<String> dateExpr(CriteriaBuilder cb, Root<?> root, String timeField, String granularity) {
		String pattern = "hour".equals(granularity) ? "%Y-%m-%d %H:00" : "%Y-%m-%d";
		return cb.function("DATE_FORMAT", String.class, root.get(timeField), cb.literal(pattern));
	}

	private void normalizeParam(DepositWithdrawDashboardQueryParam param) {
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

	private DepositWithdrawDailyItemVO item(Map<String, DepositWithdrawDailyItemVO> map, Tuple tuple) {
		String date = tuple.get("date", String.class);
		return map.computeIfAbsent(date, key -> {
			DepositWithdrawDailyItemVO item = new DepositWithdrawDailyItemVO();
			item.setDate(key);
			return item;
		});
	}

	private List<DepositWithdrawTrendItemVO> buildTrend(List<Tuple> depositByCreditTime,
			List<Tuple> withdrawByCompleteTime) {
		Map<String, DepositWithdrawTrendItemVO> map = new LinkedHashMap<>();
		for (Tuple tuple : depositByCreditTime) {
			DepositWithdrawTrendItemVO item = map.computeIfAbsent(tuple.get("date", String.class), key -> {
				DepositWithdrawTrendItemVO vo = new DepositWithdrawTrendItemVO();
				vo.setDate(key);
				return vo;
			});
			item.setDepositAmount(TupleUtils.getBigDecimal(tuple, "successAmount"));
		}
		for (Tuple tuple : withdrawByCompleteTime) {
			DepositWithdrawTrendItemVO item = map.computeIfAbsent(tuple.get("date", String.class), key -> {
				DepositWithdrawTrendItemVO vo = new DepositWithdrawTrendItemVO();
				vo.setDate(key);
				return vo;
			});
			item.setWithdrawAmount(TupleUtils.getBigDecimal(tuple, "successAmount"));
		}
		List<DepositWithdrawTrendItemVO> list = new ArrayList<>(map.values());
		list.sort(Comparator.comparing(DepositWithdrawTrendItemVO::getDate));
		return list;
	}


}

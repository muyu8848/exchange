package com.exchange.investment.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.utils.TupleUtils;
import com.exchange.common.exception.BizException;
import com.exchange.common.vo.PageResult;
import com.exchange.constants.Constant;
import com.exchange.investment.domain.InvestmentFixedPosition;
import com.exchange.investment.domain.InvestmentFlexiblePosition;
import com.exchange.investment.domain.InvestmentHourlyInterestLog;
import com.exchange.investment.domain.InvestmentInterestLog;
import com.exchange.investment.domain.InvestmentRedeemRecord;
import com.exchange.investment.param.InvestmentHourlyInterestLogQueryCondParam;
import com.exchange.investment.param.InvestmentInterestLogQueryCondParam;
import com.exchange.investment.param.InvestmentRedeemRecordQueryCondParam;
import com.exchange.investment.repo.InvestmentFixedPositionRepo;
import com.exchange.investment.repo.InvestmentFlexiblePositionRepo;
import com.exchange.investment.repo.InvestmentHourlyInterestLogRepo;
import com.exchange.investment.repo.InvestmentInterestLogRepo;
import com.exchange.investment.repo.InvestmentRedeemRecordRepo;
import com.exchange.investment.vo.ExportInvestmentInterestLogDataVO;
import com.exchange.investment.vo.InvestmentHourlyInterestLogSubtotalVO;
import com.exchange.investment.vo.InvestmentHourlyInterestLogVO;
import com.exchange.investment.vo.InvestmentInterestLogSubtotalVO;
import com.exchange.investment.vo.InvestmentInterestLogVO;
import com.exchange.investment.vo.InvestmentRedeemRecordSubtotalVO;
import com.exchange.investment.vo.InvestmentRedeemRecordVO;
import com.exchange.investment.vo.MyDailyInterestDetailVO;
import com.exchange.investment.vo.MyDailyInterestRecordVO;
import com.exchange.investment.vo.MyHourlyInterestRecordVO;
import com.exchange.investment.vo.MyInterestRecordVO;
import com.exchange.investment.vo.MyRedeemRecordVO;

import cn.hutool.core.util.StrUtil;

/**
 * 理财流水查询服务：利息流水、活期小时利息流水、赎回记录
 * <p>
 * 同时提供用户端分页查询与管理端分页/小计/导出。
 */
@Validated
@Service
public class InvestmentInterestLogQueryService {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private InvestmentInterestLogRepo investmentInterestLogRepo;

	@Autowired
	private InvestmentHourlyInterestLogRepo investmentHourlyInterestLogRepo;

	@Autowired
	private InvestmentRedeemRecordRepo investmentRedeemRecordRepo;

	@Autowired
	private InvestmentFixedPositionRepo investmentFixedPositionRepo;

	@Autowired
	private InvestmentFlexiblePositionRepo investmentFlexiblePositionRepo;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MyInterestRecordVO> findMyInterestRecordByPage(@Valid InvestmentInterestLogQueryCondParam param) {
		Page<InvestmentInterestLog> result = investmentInterestLogRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(),
						Sort.by(Sort.Order.desc("interestDate"), Sort.Order.desc("createTime"))));
		return new PageResult<>(MyInterestRecordVO.convertFor(result.getContent()), param.getPageNum(),
				param.getPageSize(), result.getTotalElements());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MyRedeemRecordVO> findMyRedeemRecordByPage(@Valid InvestmentRedeemRecordQueryCondParam param) {
		Page<InvestmentRedeemRecord> result = investmentRedeemRecordRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("createTime"))));
		return new PageResult<>(MyRedeemRecordVO.convertFor(result.getContent()), param.getPageNum(),
				param.getPageSize(), result.getTotalElements());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MyHourlyInterestRecordVO> findMyHourlyInterestLogByPage(
			@Valid InvestmentHourlyInterestLogQueryCondParam param) {
		Page<InvestmentHourlyInterestLog> result = investmentHourlyInterestLogRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(),
						Sort.by(Sort.Order.desc("interestDate"), Sort.Order.desc("interestHour"))));
		return new PageResult<>(MyHourlyInterestRecordVO.convertFor(result.getContent()), param.getPageNum(),
				param.getPageSize(), result.getTotalElements());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MyDailyInterestRecordVO> findMyDailyInterestLogByPage(
			@Valid InvestmentInterestLogQueryCondParam param) {
		Page<InvestmentInterestLog> result = investmentInterestLogRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("interestDate"))));

		List<MyDailyInterestRecordVO> records = MyDailyInterestRecordVO.convertFor(result.getContent());
		long total = result.getTotalElements();

		// 第一页插入「今天」的实时行：仅按账户维度汇总；当日已有日汇总记录时不重复插入
		if (param.getPageNum() == 1 && StrUtil.isNotBlank(param.getFlexibleAccountId())) {
			LocalDate today = LocalDate.now();
			boolean alreadySettled = investmentInterestLogRepo
					.existsByFlexibleAccountIdAndInterestDate(param.getFlexibleAccountId(), today);
			if (!alreadySettled) {
				BigDecimal todayAmount = investmentHourlyInterestLogRepo
						.sumInterestAmountByFlexibleAccountIdAndInterestDate(param.getFlexibleAccountId(), today);
				MyDailyInterestRecordVO todayRecord = new MyDailyInterestRecordVO();
				todayRecord.setId("today-realtime");
				todayRecord.setInterestDate(today);
				todayRecord.setInterestAmount(todayAmount != null ? todayAmount : BigDecimal.ZERO);
				todayRecord.setRealtime(true);
				records.add(0, todayRecord);
				total += 1;
			}
		}

		return new PageResult<>(records, param.getPageNum(), param.getPageSize(), total);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public MyDailyInterestDetailVO findMyDailyInterestDetail(@NotBlank String memberAccountId, String assetCode,
			@NotNull LocalDate interestDate, String flexibleAccountId) {
		InvestmentHourlyInterestLogQueryCondParam param = new InvestmentHourlyInterestLogQueryCondParam();
		param.setMemberAccountId(memberAccountId);
		param.setAssetCode(assetCode);
		param.setFlexibleAccountId(flexibleAccountId);
		param.setInterestDateStart(interestDate);
		param.setInterestDateEnd(interestDate);

		List<InvestmentHourlyInterestLog> logs = investmentHourlyInterestLogRepo.findAll(param.buildSpecification(),
				Sort.by(Sort.Order.desc("interestHour")));

		MyDailyInterestDetailVO vo = new MyDailyInterestDetailVO();
		BigDecimal dailyTotal = BigDecimal.ZERO;
		for (InvestmentHourlyInterestLog log : logs) {
			if (log.getInterestAmount() != null) {
				dailyTotal = dailyTotal.add(log.getInterestAmount());
			}
		}
		vo.setDailyInterestAmount(dailyTotal);
		vo.setHourlyRecords(MyHourlyInterestRecordVO.convertFor(logs));
		return vo;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<InvestmentInterestLogVO> findPositionInterestLogs(@NotBlank String positionId) {
		InvestmentFixedPosition fixedPosition = investmentFixedPositionRepo.findById(positionId).orElse(null);
		if (fixedPosition != null) {
			List<InvestmentInterestLog> logs;
			InvestmentInterestLogQueryCondParam logParam = new InvestmentInterestLogQueryCondParam();
			logParam.setPositionId(positionId);
			logs = investmentInterestLogRepo.findAll(logParam.buildSpecification(),
					Sort.by(Sort.Order.desc("interestDate"), Sort.Order.desc("createTime")));
			return InvestmentInterestLogVO.convertFor(logs);
		}
		InvestmentFlexiblePosition flexiblePosition = investmentFlexiblePositionRepo.findById(positionId).orElse(null);
		if (flexiblePosition != null) {
			List<InvestmentInterestLog> logs;
			InvestmentInterestLogQueryCondParam logParam = new InvestmentInterestLogQueryCondParam();
			logParam.setMemberAccountId(flexiblePosition.getMemberAccountId());
			logParam.setProductType(Constant.理财产品类型_活期);
			logs = investmentInterestLogRepo.findAll(logParam.buildSpecification(),
					Sort.by(Sort.Order.desc("interestDate"), Sort.Order.desc("createTime")));
			return InvestmentInterestLogVO.convertFor(logs);
		}
		throw new BizException("持仓记录不存在");
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<InvestmentRedeemRecordVO> findInvestmentRedeemRecordPage(
			@Valid InvestmentRedeemRecordQueryCondParam param) {
		Page<InvestmentRedeemRecord> result = investmentRedeemRecordRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("createTime"))));
		return new PageResult<>(InvestmentRedeemRecordVO.convertFor(result.getContent()), param.getPageNum(),
				param.getPageSize(), result.getTotalElements());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public InvestmentRedeemRecordVO findRedeemLogDetail(@NotBlank String id) {
		InvestmentRedeemRecord log = investmentRedeemRecordRepo.findById(id).orElseThrow(() -> new BizException("赎回记录不存在"));
		return InvestmentRedeemRecordVO.convertFor(log);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public InvestmentRedeemRecordSubtotalVO redeemLogSubtotal(@Valid InvestmentRedeemRecordQueryCondParam param) {
		List<Tuple> tuples = groupByAsset(InvestmentRedeemRecord.class, param.buildSpecification(),
				(root, cb) -> Arrays.<Selection<?>>asList(
						cb.count(root).alias("recordCount"),
						cb.sum(root.<BigDecimal>get("redeemAmount")).alias("totalRedeemAmount")));

		InvestmentRedeemRecordSubtotalVO vo = new InvestmentRedeemRecordSubtotalVO();
		List<InvestmentRedeemRecordSubtotalVO.AssetSubtotal> items = new ArrayList<>();
		for (Tuple tuple : tuples) {
			InvestmentRedeemRecordSubtotalVO.AssetSubtotal item = new InvestmentRedeemRecordSubtotalVO.AssetSubtotal();
			item.setAssetCode(assetCode(tuple));
			item.setRecordCount(TupleUtils.getLong(tuple, "recordCount"));
			item.setTotalRedeemAmount(TupleUtils.getBigDecimal(tuple, "totalRedeemAmount"));
			items.add(item);
		}
		vo.setItems(items);
		return vo;
	}

	private <T> List<Tuple> groupByAsset(Class<T> clazz, Specification<T> specification,
			BiFunction<Root<T>, CriteriaBuilder, List<Selection<?>>> selectionsBuilder) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<T> root = query.from(clazz);
		List<Selection<?>> selections = new ArrayList<>();
		selections.add(root.get("assetCode").alias("assetCode"));
		selections.addAll(selectionsBuilder.apply(root, cb));
		query.multiselect(selections);
		Predicate predicate = specification.toPredicate(root, query, cb);
		if (predicate != null) {
			query.where(predicate);
		}
		query.groupBy(root.get("assetCode"));
		return entityManager.createQuery(query).getResultList();
	}

	private String assetCode(Tuple tuple) {
		String assetCode = tuple.get("assetCode", String.class);
		return assetCode != null ? assetCode : "UNKNOWN";
	}



	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<InvestmentInterestLogVO> findInvestmentInterestLogByPage(
			@Valid InvestmentInterestLogQueryCondParam param) {
		Page<InvestmentInterestLog> result = investmentInterestLogRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(),
						Sort.by(Sort.Order.desc("interestDate"), Sort.Order.desc("createTime"))));
		List<InvestmentInterestLogVO> records = InvestmentInterestLogVO.convertFor(result.getContent());
		long total = result.getTotalElements();

		// 账户维度首页补充当天实时汇总行；当天已有日汇总记录（如赎回补写）时不重复插入
		if (param.getPageNum() == 1 && Boolean.TRUE.equals(param.getIncludeTodayRealtime())
				&& StrUtil.isNotBlank(param.getFlexibleAccountId())) {
			LocalDate today = LocalDate.now();
			boolean alreadySettled = investmentInterestLogRepo
					.existsByFlexibleAccountIdAndInterestDate(param.getFlexibleAccountId(), today);
			if (!alreadySettled) {
				BigDecimal todayAmount = investmentHourlyInterestLogRepo
						.sumInterestAmountByFlexibleAccountIdAndInterestDate(param.getFlexibleAccountId(), today);
				InvestmentInterestLogVO todayRecord = new InvestmentInterestLogVO();
				todayRecord.setId("today-realtime");
				todayRecord.setInterestDate(today);
				todayRecord.setInterestAmount(todayAmount != null ? todayAmount : BigDecimal.ZERO);
				todayRecord.setInterestType(Constant.收益类型_收益复投);
				todayRecord.setInterestTypeName("收益复投(实时)");
				records.add(0, todayRecord);
				total += 1;
			}
		}

		return new PageResult<>(records, param.getPageNum(), param.getPageSize(), total);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public InvestmentInterestLogSubtotalVO interestLogSubtotal(@Valid InvestmentInterestLogQueryCondParam param) {
		List<Tuple> tuples = groupByAsset(InvestmentInterestLog.class, param.buildSpecification(),
				(root, cb) -> Arrays.<Selection<?>>asList(
						cb.count(root).alias("recordCount"),
						cb.sum(root.<BigDecimal>get("interestAmount")).alias("totalInterestAmount")));

		InvestmentInterestLogSubtotalVO vo = new InvestmentInterestLogSubtotalVO();
		List<InvestmentInterestLogSubtotalVO.AssetSubtotal> items = new ArrayList<>();
		for (Tuple tuple : tuples) {
			InvestmentInterestLogSubtotalVO.AssetSubtotal item = new InvestmentInterestLogSubtotalVO.AssetSubtotal();
			item.setAssetCode(assetCode(tuple));
			item.setRecordCount(TupleUtils.getLong(tuple, "recordCount"));
			item.setTotalInterestAmount(TupleUtils.getBigDecimal(tuple, "totalInterestAmount"));
			items.add(item);
		}
		vo.setItems(items);
		return vo;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<ExportInvestmentInterestLogDataVO> findInterestLogExportData(
			@Valid InvestmentInterestLogQueryCondParam param) {
		List<InvestmentInterestLog> logs = investmentInterestLogRepo.findAll(param.buildSpecification(),
				Sort.by(Sort.Order.desc("interestDate"), Sort.Order.desc("createTime")));
		return ExportInvestmentInterestLogDataVO.convertFor(logs);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<InvestmentHourlyInterestLogVO> findInvestmentHourlyInterestLogByPage(
			@Valid InvestmentHourlyInterestLogQueryCondParam param) {
		Page<InvestmentHourlyInterestLog> result = investmentHourlyInterestLogRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("interestDate"),
						Sort.Order.desc("interestHour"), Sort.Order.desc("createTime"))));
		return new PageResult<>(InvestmentHourlyInterestLogVO.convertFor(result.getContent()), param.getPageNum(),
				param.getPageSize(), result.getTotalElements());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public InvestmentHourlyInterestLogSubtotalVO hourlyInterestLogSubtotal(
			@Valid InvestmentHourlyInterestLogQueryCondParam param) {
		List<Tuple> tuples = groupByAsset(InvestmentHourlyInterestLog.class, param.buildSpecification(),
				(root, cb) -> Arrays.<Selection<?>>asList(
						cb.count(root).alias("recordCount"),
						cb.sum(root.<BigDecimal>get("interestAmount")).alias("totalInterestAmount")));

		InvestmentHourlyInterestLogSubtotalVO vo = new InvestmentHourlyInterestLogSubtotalVO();
		List<InvestmentHourlyInterestLogSubtotalVO.AssetSubtotal> items = new ArrayList<>();
		for (Tuple tuple : tuples) {
			InvestmentHourlyInterestLogSubtotalVO.AssetSubtotal item = new InvestmentHourlyInterestLogSubtotalVO.AssetSubtotal();
			item.setAssetCode(assetCode(tuple));
			item.setRecordCount(TupleUtils.getLong(tuple, "recordCount"));
			item.setTotalInterestAmount(TupleUtils.getBigDecimal(tuple, "totalInterestAmount"));
			items.add(item);
		}
		vo.setItems(items);
		return vo;
	}

}

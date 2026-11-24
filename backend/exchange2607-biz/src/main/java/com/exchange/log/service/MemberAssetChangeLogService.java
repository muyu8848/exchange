package com.exchange.log.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

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
import com.exchange.log.domain.MemberAssetChangeLog;
import com.exchange.log.param.MemberAssetChangeLogQueryCondParam;
import com.exchange.log.repo.MemberAssetChangeLogRepo;
import com.exchange.log.vo.ExportMemberAssetChangeLogDataVO;
import com.exchange.log.vo.MemberAssetChangeLogVO;
import com.exchange.log.vo.MemberAssetChangeSubtotalVO;
import com.exchange.log.vo.MyAssetChangeLogDetailVO;
import com.exchange.log.vo.MyAssetChangeLogVO;
import com.exchange.spot.domain.SpotOrder;

@Validated
@Service
public class MemberAssetChangeLogService {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private MemberAssetChangeLogRepo memberAssetChangeLogRepo;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<ExportMemberAssetChangeLogDataVO> findExportBalanceChangeData(
			MemberAssetChangeLogQueryCondParam param) {
		List<MemberAssetChangeLog> result = memberAssetChangeLogRepo.findAll(param.buildSpecification(),
				Sort.by(Sort.Order.desc("changeTime"), Sort.Order.desc("id")));
		return ExportMemberAssetChangeLogDataVO.convertFor(result);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public MemberAssetChangeSubtotalVO subtotal(MemberAssetChangeLogQueryCondParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<MemberAssetChangeLog> root = query.from(MemberAssetChangeLog.class);
		Path<BigDecimal> changePath = root.get("balanceChange");
		Expression<BigDecimal> income = cb.selectCase()
				.when(cb.greaterThanOrEqualTo(changePath, BigDecimal.ZERO), changePath)
				.otherwise(BigDecimal.ZERO).as(BigDecimal.class);
		Expression<BigDecimal> expenditure = cb.selectCase()
				.when(cb.lessThan(changePath, BigDecimal.ZERO), cb.neg(changePath))
				.otherwise(BigDecimal.ZERO).as(BigDecimal.class);
		query.multiselect(root.get("assetCode").alias("assetCode"),
				cb.sum(income).alias("income"),
				cb.sum(expenditure).alias("expenditure"),
				cb.count(root).alias("recordCount"));
		Predicate predicate = param.buildSpecification().toPredicate(root, query, cb);
		if (predicate != null) {
			query.where(predicate);
		}
		query.groupBy(root.get("assetCode"));

		MemberAssetChangeSubtotalVO vo = new MemberAssetChangeSubtotalVO();
		List<MemberAssetChangeSubtotalVO.AssetSubtotal> items = new ArrayList<>();
		for (Tuple tuple : entityManager.createQuery(query).getResultList()) {
			MemberAssetChangeSubtotalVO.AssetSubtotal item = new MemberAssetChangeSubtotalVO.AssetSubtotal();
			String assetCode = tuple.get("assetCode", String.class);
			item.setAssetCode(assetCode != null ? assetCode : "UNKNOWN");
			item.setIncome(TupleUtils.getBigDecimal(tuple, "income"));
			item.setExpenditure(TupleUtils.getBigDecimal(tuple, "expenditure"));
			item.setRecordCount(TupleUtils.getLong(tuple, "recordCount"));
			items.add(item);
		}
		vo.setItems(items);
		return vo;
	}



	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MemberAssetChangeLogVO> findByPage(@Valid MemberAssetChangeLogQueryCondParam param) {
		Specification<MemberAssetChangeLog> spec = param.buildSpecification();
		Page<MemberAssetChangeLog> result = memberAssetChangeLogRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(),
						Sort.by(Sort.Order.desc("changeTime"), Sort.Order.desc("id"))));
		PageResult<MemberAssetChangeLogVO> pageResult = new PageResult<>(
				MemberAssetChangeLogVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
		return pageResult;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public MyAssetChangeLogDetailVO getDetail(@NotBlank String id, @NotBlank String memberAccountId) {
		MemberAssetChangeLog log = memberAssetChangeLogRepo.findById(id).orElse(null);
		if (log == null) {
			throw new BizException("订单不存在");
		}
		if (!memberAccountId.equals(log.getMemberAccountId())) {
			throw new BizException("无权查看");
		}
		return MyAssetChangeLogDetailVO.convertFor(log);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MyAssetChangeLogVO> findMyAssetChangeLogByPage(@Valid MemberAssetChangeLogQueryCondParam param) {
		Specification<MemberAssetChangeLog> spec = param.buildSpecification();
		Page<MemberAssetChangeLog> result = memberAssetChangeLogRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(),
						Sort.by(Sort.Order.desc("changeTime"), Sort.Order.desc("id"))));
		PageResult<MyAssetChangeLogVO> pageResult = new PageResult<>(MyAssetChangeLogVO.convertFor(result.getContent()),
				param.getPageNum(), param.getPageSize(), result.getTotalElements());
		return pageResult;
	}

}

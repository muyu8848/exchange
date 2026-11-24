package com.exchange.contract.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.utils.TupleUtils;
import com.exchange.common.vo.PageResult;
import com.exchange.contract.domain.PerpetualFundingFee;
import com.exchange.contract.param.PerpetualFundingFeeQueryCondParam;
import com.exchange.contract.repo.PerpetualFundingFeeRepo;
import com.exchange.contract.vo.PerpetualFundingFeeSubtotalVO;
import com.exchange.contract.vo.PerpetualFundingFeeVO;

@Validated
@Service
public class PerpetualFundingFeeService {

	@Autowired
	private PerpetualFundingFeeRepo fundingFeeRepo;

	@Autowired
	private EntityManager entityManager;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<PerpetualFundingFeeVO> findByPage(PerpetualFundingFeeQueryCondParam param) {
		Page<PerpetualFundingFee> result = fundingFeeRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(),
						Sort.by(Sort.Order.desc("fundingTime"))));
		return new PageResult<>(PerpetualFundingFeeVO.convertFor(result.getContent()), param.getPageNum(),
				param.getPageSize(), result.getTotalElements());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PerpetualFundingFeeSubtotalVO subtotal(PerpetualFundingFeeQueryCondParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<PerpetualFundingFee> root = query.from(PerpetualFundingFee.class);

		Expression<BigDecimal> income = cb.selectCase()
				.when(cb.greaterThan(root.<BigDecimal>get("fundingAmount"), BigDecimal.ZERO),
						root.<BigDecimal>get("fundingAmount"))
				.otherwise(BigDecimal.ZERO).as(BigDecimal.class);
		Expression<BigDecimal> expense = cb.selectCase()
				.when(cb.lessThan(root.<BigDecimal>get("fundingAmount"), BigDecimal.ZERO),
						cb.neg(root.<BigDecimal>get("fundingAmount")))
				.otherwise(BigDecimal.ZERO).as(BigDecimal.class);

		query.multiselect(cb.count(root).alias("recordCount"),
				cb.sum(income).alias("income"),
				cb.sum(expense).alias("expense"));
		Predicate predicate = param.buildSpecification().toPredicate(root, query, cb);
		if (predicate != null) {
			query.where(predicate);
		}
		Tuple tuple = entityManager.createQuery(query).getSingleResult();

		PerpetualFundingFeeSubtotalVO vo = new PerpetualFundingFeeSubtotalVO();
		vo.setRecordCount(TupleUtils.getLong(tuple, "recordCount"));
		vo.setTotalIncome(TupleUtils.getBigDecimal(tuple, "income").setScale(2, RoundingMode.HALF_UP));
		vo.setTotalExpense(TupleUtils.getBigDecimal(tuple, "expense").setScale(2, RoundingMode.HALF_UP));
		vo.setNetAmount(vo.getTotalExpense().subtract(vo.getTotalIncome()));
		return vo;
	}


}

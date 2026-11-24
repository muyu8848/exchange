package com.exchange.rebate.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.exchange.common.utils.TupleUtils;
import com.exchange.common.exception.BizException;
import com.exchange.common.utils.IdUtils;
import com.exchange.common.vo.PageResult;
import com.exchange.constants.Constant;
import com.exchange.contract.domain.PerpetualOrder;
import com.exchange.contract.repo.PerpetualOrderRepo;
import com.exchange.log.domain.MemberAssetChangeLog;
import com.exchange.log.repo.MemberAssetChangeLogRepo;
import com.exchange.member.domain.MemberAccount;
import com.exchange.member.repo.MemberAccountRepo;
import com.exchange.member.service.MemberAssetBiz;
import com.exchange.rebate.domain.RebateRecord;
import com.exchange.rebate.param.RebateRecordQueryCondParam;
import com.exchange.rebate.repo.RebateRecordRepo;
import com.exchange.rebate.vo.ExportRebateRecordDataVO;
import com.exchange.rebate.vo.MyRebateRecordVO;
import com.exchange.rebate.vo.RebateRecordSubtotalItem;
import com.exchange.rebate.vo.RebateRecordSubtotalVO;
import com.exchange.rebate.vo.RebateRecordVO;
import com.exchange.setting.domain.RebateSetting;
import com.exchange.setting.repo.RebateSettingRepo;
import com.exchange.spot.domain.SpotOrder;
import com.exchange.spot.repo.SpotOrderRepo;
import com.zengtengpeng.annotation.Lock;

import cn.hutool.core.util.StrUtil;

@Service
public class RebateRecordService {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private RedissonClient redissonClient;

	@Autowired
	private MemberAssetBiz memberAssetBiz;

	@Autowired
	private RebateRecordRepo rebateRecordRepo;

	@Autowired
	private RebateSettingRepo rebateSettingRepo;

	@Autowired
	private MemberAccountRepo memberAccountRepo;

	@Autowired
	private SpotOrderRepo spotOrderRepo;

	@Autowired
	private PerpetualOrderRepo perpetualOrderRepo;

	@Autowired
	private MemberAssetChangeLogRepo memberAssetChangeLogRepo;

	// ============ 处理（核心，带锁 + 事务，保证单订单只返一次） ============

	@Lock(keys = "'rebateSpot' + #orderId")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void handleSpotRebate(String orderId) {
		SpotOrder order = spotOrderRepo.findById(orderId).orElse(null);
		if (order == null) {
			return;
		}
		// 无手续费或未真正成交（如已撤销）则不产生返佣，直接标记已返佣避免反复扫描
		if (order.getFee() == null || order.getFee().compareTo(BigDecimal.ZERO) <= 0
				|| !Constant.现货订单状态_已完成.equals(order.getStatus())) {
			order.setRebateStatus(Constant.返佣状态_已返佣);
			spotOrderRepo.save(order);
			return;
		}
		if (Constant.返佣状态_已返佣.equals(order.getRebateStatus())) {
			return;
		}
		if (rebateRecordRepo.existsBySourceOrderIdAndBizType(orderId, Constant.返佣业务类型_现货)) {
			order.setRebateStatus(Constant.返佣状态_已返佣);
			spotOrderRepo.save(order);
			return;
		}
		MemberAccount trader = memberAccountRepo.findById(order.getMemberAccountId()).orElse(null);
		if (trader == null || StrUtil.isBlank(trader.getInviterId())) {
			order.setRebateStatus(Constant.返佣状态_已返佣);
			spotOrderRepo.save(order);
			return;
		}
		BigDecimal rate = getRebateRate();
		BigDecimal rebate = order.getFee().multiply(rate).setScale(Constant.小数部分位数, RoundingMode.HALF_UP);
		if (rebate.compareTo(BigDecimal.ZERO) <= 0) {
			order.setRebateStatus(Constant.返佣状态_已返佣);
			spotOrderRepo.save(order);
			return;
		}
		String inviterId = trader.getInviterId();
		String assetCode = order.getFeeAssetCode();
		BigDecimal balanceBefore = memberAssetBiz.addAvailable(inviterId, Constant.账户类型_现货, assetCode, rebate);
		memberAssetChangeLogRepo.save(MemberAssetChangeLog.buildWithRebate(inviterId, Constant.账户类型_现货, assetCode,
				balanceBefore, rebate, order.getOrderNo(), order.getSymbolCode()));
		saveRebateRecord(inviterId, order.getMemberAccountId(), orderId, order.getSymbolCode(), rebate, assetCode,
				Constant.返佣业务类型_现货);
		order.setRebateStatus(Constant.返佣状态_已返佣);
		spotOrderRepo.save(order);
	}

	@Lock(keys = "'rebatePerp' + #orderId")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void handlePerpRebate(String orderId) {
		PerpetualOrder order = perpetualOrderRepo.findById(orderId).orElse(null);
		if (order == null) {
			return;
		}
		if (order.getFee() == null || order.getFee().compareTo(BigDecimal.ZERO) <= 0) {
			order.setRebateStatus(Constant.返佣状态_已返佣);
			perpetualOrderRepo.save(order);
			return;
		}
		if (Constant.返佣状态_已返佣.equals(order.getRebateStatus())) {
			return;
		}
		if (rebateRecordRepo.existsBySourceOrderIdAndBizType(orderId, Constant.返佣业务类型_合约)) {
			order.setRebateStatus(Constant.返佣状态_已返佣);
			perpetualOrderRepo.save(order);
			return;
		}
		MemberAccount trader = memberAccountRepo.findById(order.getMemberAccountId()).orElse(null);
		if (trader == null || StrUtil.isBlank(trader.getInviterId())) {
			order.setRebateStatus(Constant.返佣状态_已返佣);
			perpetualOrderRepo.save(order);
			return;
		}
		BigDecimal rate = getRebateRate();
		BigDecimal rebate = order.getFee().multiply(rate).setScale(Constant.小数部分位数, RoundingMode.HALF_UP);
		if (rebate.compareTo(BigDecimal.ZERO) <= 0) {
			order.setRebateStatus(Constant.返佣状态_已返佣);
			perpetualOrderRepo.save(order);
			return;
		}
		String inviterId = trader.getInviterId();
		BigDecimal balanceBefore = memberAssetBiz.addAvailable(inviterId, Constant.账户类型_合约, Constant.数字货币_USDT, rebate);
		memberAssetChangeLogRepo.save(MemberAssetChangeLog.buildWithRebate(inviterId, Constant.账户类型_合约,
				Constant.数字货币_USDT, balanceBefore, rebate, order.getOrderNo(), order.getSymbolCode()));
		saveRebateRecord(inviterId, order.getMemberAccountId(), orderId, order.getSymbolCode(), rebate,
				Constant.数字货币_USDT, Constant.返佣业务类型_合约);
		order.setRebateStatus(Constant.返佣状态_已返佣);
		perpetualOrderRepo.save(order);
	}

	private BigDecimal getRebateRate() {
		RebateSetting setting = rebateSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null || setting.getRate() == null) {
			throw new BizException("请先在运营后台配置返佣比例");
		}
		return setting.getRate();
	}

	/** 客户端查询当前返佣比例（直推，0~1）；未配置时返回 null，不抛异常 */
	public BigDecimal getRebateRateValue() {
		RebateSetting setting = rebateSettingRepo.findTopByOrderByLatelyUpdateTime();
		return setting == null ? null : setting.getRate();
	}

	private void saveRebateRecord(String beneficiaryId, String sourceMemberId, String sourceOrderId, String symbolCode,
			BigDecimal amount, String assetCode, String bizType) {
		RebateRecord rr = new RebateRecord();
		rr.setId(IdUtils.getId());
		rr.setBeneficiaryId(beneficiaryId);
		rr.setSourceMemberId(sourceMemberId);
		rr.setSourceOrderId(sourceOrderId);
		rr.setSymbolCode(symbolCode);
		rr.setLevel(1);
		rr.setAmount(amount);
		rr.setAssetCode(assetCode);
		rr.setBizType(bizType);
		rr.setStatus(Constant.返佣状态_已返佣);
		rr.setCreateTime(LocalDateTime.now());
		rebateRecordRepo.save(rr);
	}

	// ============ 补偿扫描（供定时任务调用，重新发布 Topic 信号由 RedisMqListener 消费处理） ============

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public void compensateSpotRebate() {
		List<SpotOrder> orders = spotOrderRepo.findByRebateStatus(Constant.返佣状态_待返佣);
		for (SpotOrder order : orders) {
			redissonClient.getTopic(Constant.现货成交返佣).publish(order.getId());
		}
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public void compensatePerpRebate() {
		List<PerpetualOrder> orders = perpetualOrderRepo.findByRebateStatus(Constant.返佣状态_待返佣);
		for (PerpetualOrder order : orders) {
			redissonClient.getTopic(Constant.永续合约成交返佣).publish(order.getId());
		}
	}

	// ============ 原有查询接口（保持不变） ============

	/** 客户端：当前会员的返佣记录（受益人固定为当前登录会员） */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MyRebateRecordVO> findMyByPage(RebateRecordQueryCondParam param) {
		return page(param, MyRebateRecordVO::convertFor);
	}

	/** 后台：分页查询（支持受益人/来源会员账号、层级、业务类型、时间） */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<RebateRecordVO> findByPage(RebateRecordQueryCondParam param) {
		return page(param, RebateRecordVO::convertFor);
	}

	/** 后台：小计 */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public RebateRecordSubtotalVO subtotal(RebateRecordQueryCondParam param) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
		Root<RebateRecord> root = query.from(RebateRecord.class);
		query.multiselect(root.get("assetCode").alias("assetCode"),
				cb.count(root).alias("count"),
				cb.sum(root.<BigDecimal>get("amount")).alias("totalAmount"));
		Predicate predicate = param.buildSpecification().toPredicate(root, query, cb);
		Predicate assetCodeNotNull = cb.isNotNull(root.get("assetCode"));
		query.where(predicate != null ? cb.and(predicate, assetCodeNotNull) : assetCodeNotNull);
		query.groupBy(root.get("assetCode"));
		query.orderBy(cb.asc(root.get("assetCode")));

		RebateRecordSubtotalVO vo = new RebateRecordSubtotalVO();
		List<RebateRecordSubtotalItem> items = new ArrayList<>();
		BigDecimal totalAmount = BigDecimal.ZERO;
		int totalCount = 0;
		for (Tuple tuple : entityManager.createQuery(query).getResultList()) {
			RebateRecordSubtotalItem item = new RebateRecordSubtotalItem();
			item.setAssetCode(tuple.get("assetCode", String.class));
			item.setCount(TupleUtils.getLong(tuple, "count").intValue());
			item.setTotalAmount(TupleUtils.getBigDecimal(tuple, "totalAmount"));
			items.add(item);
			totalAmount = totalAmount.add(item.getTotalAmount());
			totalCount += item.getCount();
		}
		vo.setItems(items);
		vo.setTotalAmount(totalAmount);
		vo.setCount(totalCount);
		return vo;
	}



	/** 后台：导出 */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<ExportRebateRecordDataVO> exportData(RebateRecordQueryCondParam param) {
		List<RebateRecord> list = rebateRecordRepo.findAll(param.buildSpecification(),
				Sort.by(Sort.Order.desc("createTime"), Sort.Order.desc("id")));
		return ExportRebateRecordDataVO.convertFor(list);
	}

	private <T> PageResult<T> page(RebateRecordQueryCondParam param, Function<List<RebateRecord>, List<T>> converter) {
		Specification<RebateRecord> spec = param.buildSpecification();
		Page<RebateRecord> pg = rebateRecordRepo.findAll(spec, PageRequest.of(param.getPageNum() - 1,
				param.getPageSize(), Sort.by(Sort.Order.desc("createTime"), Sort.Order.desc("id"))));
		return new PageResult<>(converter.apply(pg.getContent()),
				param.getPageNum(), param.getPageSize(), pg.getTotalElements());
	}

}

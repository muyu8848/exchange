package com.exchange.member.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.validation.Valid;

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
import com.exchange.c2c.domain.C2cFiat;
import com.exchange.c2c.repo.C2cFiatRepo;
import com.exchange.common.exception.BizException;
import com.exchange.common.utils.FiatRateUtils;
import com.exchange.common.utils.SymbolPriceUtils;
import com.exchange.common.vo.PageResult;
import com.exchange.constants.Constant;
import com.exchange.contract.domain.PerpetualPosition;
import com.exchange.contract.repo.PerpetualPositionRepo;
import com.exchange.dictconfig.DictHolder;
import com.exchange.investment.domain.InvestmentFixedPosition;
import com.exchange.investment.domain.InvestmentFlexiblePosition;
import com.exchange.investment.repo.InvestmentFixedPositionRepo;
import com.exchange.investment.repo.InvestmentFlexiblePositionRepo;
import com.exchange.log.domain.MemberAssetChangeLog;
import com.exchange.log.repo.MemberAssetChangeLogRepo;
import com.exchange.member.domain.MemberAccount;
import com.exchange.member.domain.MemberAsset;
import com.exchange.member.param.MemberAssetAdjustParam;
import com.exchange.member.param.MemberAssetOpenAccountParam;
import com.exchange.member.param.MemberAssetQueryCondParam;
import com.exchange.member.repo.MemberAccountRepo;
import com.exchange.member.repo.MemberAssetRepo;
import com.exchange.member.vo.AssetByAccountVO;
import com.exchange.member.vo.AssetByCurrencyVO;
import com.exchange.member.vo.AssetTotalValueVO;
import com.exchange.member.vo.CurrencyAssetDetailVO;
import com.exchange.member.vo.MemberAssetSubtotalVO;
import com.exchange.member.vo.MemberAssetVO;
import com.zengtengpeng.annotation.Lock;

import cn.hutool.core.util.StrUtil;

@Validated
@Service
public class MemberAssetService {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private MemberAssetRepo memberAssetRepo;

	@Autowired
	private MemberAccountRepo memberAccountRepo;

	@Autowired
	private MemberAssetBiz memberAssetBiz;

	@Autowired
	private InvestmentFixedPositionRepo investmentFixedPositionRepo;

	@Autowired
	private InvestmentFlexiblePositionRepo investmentFlexiblePositionRepo;

	@Autowired
	private MemberAssetChangeLogRepo memberAssetChangeLogRepo;

	@Autowired
	private PerpetualPositionRepo perpetualPositionRepo;

	@Autowired
	private C2cFiatRepo c2cFiatRepo;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public BigDecimal getAvailableBalance(String memberAccountId, String accountType, String assetCode) {
		BigDecimal balance = memberAssetRepo.findAvailable(memberAccountId, accountType, assetCode);
		return balance;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MemberAssetVO> findByPage(MemberAssetQueryCondParam param) {
		Page<MemberAsset> result = memberAssetRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("available"))));
		return new PageResult<>(MemberAssetVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public MemberAssetSubtotalVO subtotal(@Valid MemberAssetQueryCondParam param) {
		List<Tuple> tuples = groupByAsset(MemberAsset.class, param.buildSpecification(),
				(root, cb) -> Arrays.<Selection<?>>asList(
						cb.count(root).alias("accountCount"),
						cb.sum(root.<BigDecimal>get("available")).alias("totalAvailable"),
						cb.sum(root.<BigDecimal>get("frozen")).alias("totalFrozen")));

		MemberAssetSubtotalVO vo = new MemberAssetSubtotalVO();
		List<MemberAssetSubtotalVO.AssetSubtotal> items = new ArrayList<>();
		for (Tuple tuple : tuples) {
			MemberAssetSubtotalVO.AssetSubtotal item = new MemberAssetSubtotalVO.AssetSubtotal();
			item.setAssetCode(assetCode(tuple));
			item.setAccountCount(TupleUtils.getLong(tuple, "accountCount"));
			item.setTotalAvailable(TupleUtils.getBigDecimal(tuple, "totalAvailable"));
			item.setTotalFrozen(TupleUtils.getBigDecimal(tuple, "totalFrozen"));
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
	public List<MemberAssetVO> findWithdrawableAssets(String memberAccountId) {
		List<MemberAsset> assets = memberAssetRepo
				.findByMemberAccountIdAndAccountTypeAndAvailableGreaterThanOrderByAvailableDesc(memberAccountId,
						Constant.账户类型_现货, BigDecimal.ZERO);
		return MemberAssetVO.convertFor(assets);
	}

	/**
	 * 资产总估值（USDT 基准 + 按指定法币折算）。
	 * <p>
	 * fiatCode 为空或非法时回落 CNY；该法币汇率暂不可用时 totalFiat 返回 null（不抛异常），
	 * 由前端显示占位符，避免拖垮整个资产页的并发请求。
	 */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public AssetTotalValueVO getTotalValue(String memberAccountId, String fiatCode) {
		List<MemberAsset> assets = memberAssetRepo.findByMemberAccountId(memberAccountId);

		String targetFiatCode = StrUtil.isBlank(fiatCode) ? Constant.法币_CNY : fiatCode.trim();
		C2cFiat fiat = c2cFiatRepo.findByFiatCodeAndDeletedFlagFalse(targetFiatCode).orElse(null);
		if (fiat == null) {
			// 非法/已删除的法币代码：回落默认法币，保证接口始终可用
			targetFiatCode = Constant.法币_CNY;
			fiat = c2cFiatRepo.findByFiatCodeAndDeletedFlagFalse(targetFiatCode).orElse(null);
		}
		int fiatScale = fiat != null && fiat.getFiatScale() != null ? fiat.getFiatScale() : 2;

		String fiatRateStr = FiatRateUtils.getFiatRate(targetFiatCode);
		BigDecimal fiatRate = StrUtil.isBlank(fiatRateStr) ? null : new BigDecimal(fiatRateStr);

		BigDecimal totalUsdt = BigDecimal.ZERO;
		for (MemberAsset asset : assets) {
			BigDecimal total = asset.getAvailable().add(asset.getFrozen());
			BigDecimal usdtValue = SymbolPriceUtils.toUsdtAmount(asset.getAssetCode(), total);
			if (usdtValue != null) {
				totalUsdt = totalUsdt.add(usdtValue);
			}
		}
		// 加上赚币账户资产
		totalUsdt = totalUsdt.add(getInvestmentUsdtValue(memberAccountId));
		// 加上永续合约持仓未实现盈亏（保证金已计入 FUTURES 钱包冻结，此处仅补浮动盈亏增量）
		totalUsdt = totalUsdt.add(getPerpetualOpenUnrealizedPnl(memberAccountId));

		AssetTotalValueVO vo = new AssetTotalValueVO();
		vo.setTotalUsdt(totalUsdt.setScale(Constant.折合USDT精度, RoundingMode.HALF_UP));
		vo.setFiatCode(targetFiatCode);
		vo.setFiatRate(fiatRate);
		vo.setTotalFiat(fiatRate == null ? null
				: totalUsdt.multiply(fiatRate).setScale(fiatScale, RoundingMode.HALF_UP));
		return vo;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<AssetByCurrencyVO> findAssetsByCurrencySummary(String memberAccountId) {
		List<MemberAsset> assets = memberAssetRepo.findByMemberAccountId(memberAccountId);
		// 按币种分组汇总（原始精度累加）
		Map<String, AssetByCurrencyVO> grouped = new LinkedHashMap<>();
		for (MemberAsset asset : assets) {
			AssetByCurrencyVO vo = grouped.computeIfAbsent(asset.getAssetCode(), k -> newAssetByCurrencyVO(k));
			vo.setAvailable(vo.getAvailable().add(asset.getAvailable()));
			vo.setFrozen(vo.getFrozen().add(asset.getFrozen()));
		}
		// 现货，合约 total = available + frozen（原始精度）
		for (AssetByCurrencyVO vo : grouped.values()) {
			vo.setTotal(vo.getAvailable().add(vo.getFrozen()));
		}
		// 加上赚币资产（按币种合并到 total，原始精度）
		List<InvestmentFixedPosition> fixedPositions = investmentFixedPositionRepo
				.findByMemberAccountIdAndStatus(memberAccountId, Constant.理财持仓状态_持仓中);
		for (InvestmentFixedPosition pos : fixedPositions) {
			BigDecimal value = pos.getPrincipal() != null ? pos.getPrincipal() : BigDecimal.ZERO;
			value = value.add(pos.getAccruedInterest() != null ? pos.getAccruedInterest() : BigDecimal.ZERO);
			AssetByCurrencyVO vo = grouped.computeIfAbsent(pos.getAssetCode(), k -> newAssetByCurrencyVO(k));
			vo.setTotal(vo.getTotal().add(value));
		}
		List<InvestmentFlexiblePosition> flexiblePositions = investmentFlexiblePositionRepo
				.findByMemberAccountIdAndStatus(memberAccountId, Constant.理财持仓状态_持仓中);
		for (InvestmentFlexiblePosition pos : flexiblePositions) {
			BigDecimal value = pos.getPrincipal() != null ? pos.getPrincipal() : BigDecimal.ZERO;
			AssetByCurrencyVO vo = grouped.computeIfAbsent(pos.getAssetCode(), k -> newAssetByCurrencyVO(k));
			vo.setTotal(vo.getTotal().add(value));
		}
		// 加上永续合约未实现盈亏（已 USDT 计价），并入 USDT 币种（保证金已在 FUTURES 钱包冻结里计过）
		BigDecimal perpPnl = getPerpetualOpenUnrealizedPnl(memberAccountId);
		if (perpPnl.compareTo(BigDecimal.ZERO) != 0) {
			AssetByCurrencyVO usdtVo = grouped.computeIfAbsent(Constant.数字货币_USDT, k -> newAssetByCurrencyVO(k));
			usdtVo.setTotal(usdtVo.getTotal().add(perpPnl));
		}
		// 统一精度处理 + USDT 估值（只做一次）
		Map<String, Integer> scaleMap = memberAssetBiz.getScaleMap(grouped.keySet());
		for (AssetByCurrencyVO vo : grouped.values()) {
			int assetScale = scaleMap.getOrDefault(vo.getCurrencyCode(), 8);
			vo.setAvailable(vo.getAvailable().setScale(assetScale, RoundingMode.HALF_UP));
			vo.setFrozen(vo.getFrozen().setScale(assetScale, RoundingMode.HALF_UP));
			vo.setTotal(vo.getTotal().setScale(assetScale, RoundingMode.HALF_UP));
			BigDecimal usdtValue = SymbolPriceUtils.toUsdtAmount(vo.getCurrencyCode(), vo.getTotal());
			vo.setUsdtValue(usdtValue.setScale(Constant.折合USDT精度, RoundingMode.HALF_UP));
		}
		// 按 USDT 估值降序排序
		return grouped.values().stream().sorted((a, b) -> b.getUsdtValue().compareTo(a.getUsdtValue()))
				.collect(Collectors.toList());
	}

	private AssetByCurrencyVO newAssetByCurrencyVO(String code) {
		AssetByCurrencyVO v = new AssetByCurrencyVO();
		v.setCurrencyCode(code);
		v.setAvailable(BigDecimal.ZERO);
		v.setFrozen(BigDecimal.ZERO);
		v.setTotal(BigDecimal.ZERO);
		return v;
	}

	/**
	 * 计算赚币资产总值（USDT） 活期：本金（含复投）；定期：本金 + 累计收益
	 */
	private BigDecimal getInvestmentUsdtValue(String memberAccountId) {
		BigDecimal totalUsdt = BigDecimal.ZERO;
		List<InvestmentFixedPosition> fixedPositions = investmentFixedPositionRepo
				.findByMemberAccountIdAndStatus(memberAccountId, Constant.理财持仓状态_持仓中);
		for (InvestmentFixedPosition pos : fixedPositions) {
			BigDecimal value = pos.getPrincipal() != null ? pos.getPrincipal() : BigDecimal.ZERO;
			value = value.add(pos.getAccruedInterest() != null ? pos.getAccruedInterest() : BigDecimal.ZERO);
			BigDecimal usdtValue = SymbolPriceUtils.toUsdtAmount(pos.getAssetCode(), value);
			if (usdtValue != null) {
				totalUsdt = totalUsdt.add(usdtValue);
			}
		}
		List<InvestmentFlexiblePosition> flexiblePositions = investmentFlexiblePositionRepo
				.findByMemberAccountIdAndStatus(memberAccountId, Constant.理财持仓状态_持仓中);
		for (InvestmentFlexiblePosition pos : flexiblePositions) {
			BigDecimal value = pos.getPrincipal() != null ? pos.getPrincipal() : BigDecimal.ZERO;
			BigDecimal usdtValue = SymbolPriceUtils.toUsdtAmount(pos.getAssetCode(), value);
			if (usdtValue != null) {
				totalUsdt = totalUsdt.add(usdtValue);
			}
		}
		return totalUsdt.setScale(Constant.折合USDT精度, RoundingMode.HALF_UP);
	}

	/**
	 * 永续合约持仓中仓位的未实现盈亏合计（已是 USDT，无需折算）。
	 * 说明：开仓保证金已作为 FUTURES 钱包「冻结」计入总资产（getTotalValue 汇总 available+frozen），
	 * 资金费也已从钱包余额扣减，因此这里只补「未实现盈亏」增量，避免重复计算保证金。
	 * 盈亏可为负（浮亏），会正确拉低总资产。无行情的仓位按 0 处理。
	 */
	private BigDecimal getPerpetualOpenUnrealizedPnl(String memberAccountId) {
		List<PerpetualPosition> positions = perpetualPositionRepo
				.findByMemberAccountIdAndStatus(memberAccountId, Constant.永续持仓状态_持仓中);
		if (positions.isEmpty()) {
			return BigDecimal.ZERO;
		}
		int pnlScale = memberAssetBiz.getScale(Constant.数字货币_USDT);
		BigDecimal sum = BigDecimal.ZERO;
		for (PerpetualPosition pos : positions) {
			String priceStr = SymbolPriceUtils.getClosePrice(Constant.行情业务类型_合约, pos.getSymbolCode());
			if (StrUtil.isBlank(priceStr)) {
				continue; // 无行情按 0，不报错
			}
			BigDecimal markPrice = new BigDecimal(priceStr);
			BigDecimal pnl = pos.calcUnrealizedPnl(markPrice, pnlScale);
			if (pnl != null) {
				sum = sum.add(pnl);
			}
		}
		return sum;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<AssetByAccountVO> findAssetsByAccountTypeSummary(String memberAccountId) {
		List<MemberAsset> assets = memberAssetRepo.findByMemberAccountId(memberAccountId);
		// 按账户类型分组汇总 USDT（原始精度累加）
		Map<String, BigDecimal> grouped = new LinkedHashMap<>();
		for (MemberAsset asset : assets) {
			BigDecimal avail = asset.getAvailable() != null ? asset.getAvailable() : BigDecimal.ZERO;
			BigDecimal frozen = asset.getFrozen() != null ? asset.getFrozen() : BigDecimal.ZERO;
			BigDecimal usdtValue = SymbolPriceUtils.toUsdtAmount(asset.getAssetCode(), avail.add(frozen));
			grouped.merge(asset.getAccountType(), usdtValue, BigDecimal::add);
		}
		// 加上永续合约未实现盈亏（已 USDT 计价），并入合约账户（保证金已在 FUTURES 钱包冻结里计过）
		BigDecimal perpPnl = getPerpetualOpenUnrealizedPnl(memberAccountId);
		if (perpPnl.compareTo(BigDecimal.ZERO) != 0) {
			grouped.merge(Constant.账户类型_合约, perpPnl, BigDecimal::add);
		}
		List<AssetByAccountVO> result = new ArrayList<>();
		for (Map.Entry<String, BigDecimal> entry : grouped.entrySet()) {
			AssetByAccountVO vo = new AssetByAccountVO();
			vo.setAccountType(entry.getKey());
			vo.setAccountTypeName(DictHolder.getDictItemName("assetAccountType", entry.getKey()));
			vo.setUsdtValue(entry.getValue().setScale(Constant.折合USDT精度, RoundingMode.HALF_UP));
			result.add(vo);
		}
		// 加上赚币账户
		BigDecimal investUsdt = getInvestmentUsdtValue(memberAccountId);
		if (investUsdt.compareTo(BigDecimal.ZERO) > 0) {
			AssetByAccountVO vo = new AssetByAccountVO();
			vo.setAccountType("INVEST");
			vo.setAccountTypeName("赚币账户");
			vo.setUsdtValue(investUsdt);
			result.add(vo);
		}
		// 按 USDT 估值降序排序
		result.sort((a, b) -> b.getUsdtValue().compareTo(a.getUsdtValue()));
		return result;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public CurrencyAssetDetailVO findDetailByCurrency(String memberAccountId, String assetCode) {
		List<MemberAsset> assets = memberAssetRepo.findByMemberAccountIdAndAssetCode(memberAccountId, assetCode);
		BigDecimal spotFuturesTotal = BigDecimal.ZERO;
		for (MemberAsset asset : assets) {
			BigDecimal avail = asset.getAvailable();
			BigDecimal frozen = asset.getFrozen();
			spotFuturesTotal = spotFuturesTotal.add(avail).add(frozen);
		}

		// 赚币资产（同币种）
		List<InvestmentFixedPosition> fixedPositions = investmentFixedPositionRepo
				.findByMemberAccountIdAndAssetCodeAndStatus(memberAccountId, assetCode, Constant.理财持仓状态_持仓中);
		BigDecimal investTotal = BigDecimal.ZERO;
		for (InvestmentFixedPosition pos : fixedPositions) {
			BigDecimal value = pos.getPrincipal();
			value = value.add(pos.getAccruedInterest());
			investTotal = investTotal.add(value);
		}
		List<InvestmentFlexiblePosition> flexiblePositions = investmentFlexiblePositionRepo
				.findByMemberAccountIdAndAssetCodeAndStatus(memberAccountId, assetCode, Constant.理财持仓状态_持仓中);
		for (InvestmentFlexiblePosition pos : flexiblePositions) {
			investTotal = investTotal.add(pos.getPrincipal());
		}

		int assetScale = memberAssetBiz.getScale(assetCode);
		BigDecimal totalBalance = spotFuturesTotal.add(investTotal).setScale(assetScale, RoundingMode.HALF_UP);

		// 预查 USDT 单价，避免多次行情查询（无行情按 0 计）
		BigDecimal usdtPrice = SymbolPriceUtils.getUsdtPriceOrZero(assetCode);
		BigDecimal usdtValue = totalBalance.multiply(usdtPrice).setScale(Constant.折合USDT精度, RoundingMode.HALF_UP);

		// 按账户类型分布
		List<CurrencyAssetDetailVO.AccountDistribution> distributions = new ArrayList<>();
		for (MemberAsset asset : assets) {
			BigDecimal avail = asset.getAvailable();
			BigDecimal frozen = asset.getFrozen();
			BigDecimal total = avail.add(frozen).setScale(assetScale, RoundingMode.HALF_UP);
			String name = DictHolder.getDictItemName("assetAccountType", asset.getAccountType());
			distributions.add(buildDistribution(asset.getAccountType(), name, total, totalBalance, usdtPrice));
		}

		// 赚币账户分布
		if (investTotal.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal total = investTotal.setScale(assetScale, RoundingMode.HALF_UP);
			distributions.add(buildDistribution("INVEST", "赚币账户", total, totalBalance, usdtPrice));
		}
		// 按 USDT 估值降序排序
		distributions.sort((a, b) -> b.getUsdtValue().compareTo(a.getUsdtValue()));

		CurrencyAssetDetailVO vo = new CurrencyAssetDetailVO();
		vo.setAssetCode(assetCode);
		vo.setTotalBalance(totalBalance);
		vo.setUsdtValue(usdtValue);
		vo.setAccounts(distributions);
		return vo;
	}

	private CurrencyAssetDetailVO.AccountDistribution buildDistribution(String accountType, String accountTypeName,
			BigDecimal total, BigDecimal totalBalance, BigDecimal usdtPrice) {
		CurrencyAssetDetailVO.AccountDistribution dist = new CurrencyAssetDetailVO.AccountDistribution();
		dist.setAccountType(accountType);
		dist.setAccountTypeName(accountTypeName);
		dist.setTotal(total);
		dist.setUsdtValue(total.multiply(usdtPrice).setScale(Constant.折合USDT精度, RoundingMode.HALF_UP));
		dist.setPercentage(calcPercentage(total, totalBalance));
		return dist;
	}

	private BigDecimal calcPercentage(BigDecimal part, BigDecimal total) {
		if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return part.divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2,
				RoundingMode.HALF_UP);
	}

	/**
	 * 查询指定账户类型下多个币种的资产（用于交易页获取交易对资产）
	 */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public List<MemberAssetVO> findAssetsByAccountTypeAndCodes(String memberAccountId, String accountType,
			List<String> assetCodes) {
		List<MemberAsset> assets = memberAssetRepo.findByMemberAccountIdAndAccountTypeAndAssetCodeIn(memberAccountId,
				accountType, assetCodes);
		String fiatRateStr = FiatRateUtils.getFiatRate(Constant.法币_CNY);
		BigDecimal fiatRate = StrUtil.isBlank(fiatRateStr) ? null : new BigDecimal(fiatRateStr);
		return MemberAssetVO.convertFor(assets).stream().peek(vo -> fillFiatValue(vo, fiatRate))
				.collect(Collectors.toList());
	}

	private void fillFiatValue(MemberAssetVO vo, BigDecimal fiatRate) {
		int assetScale = memberAssetBiz.getScale(vo.getAssetCode());
		BigDecimal avail = vo.getAvailable() != null ? vo.getAvailable().setScale(assetScale, RoundingMode.HALF_UP)
				: BigDecimal.ZERO;
		BigDecimal frozen = vo.getFrozen() != null ? vo.getFrozen().setScale(assetScale, RoundingMode.HALF_UP)
				: BigDecimal.ZERO;
		vo.setAvailable(avail);
		vo.setFrozen(frozen);
		BigDecimal total = avail.add(frozen);
		vo.setTotal(total);
		BigDecimal usdtValue = SymbolPriceUtils.toUsdtAmount(vo.getAssetCode(), total);
		if (usdtValue != null) {
			vo.setUsdtValue(usdtValue.setScale(2, RoundingMode.HALF_UP));
			if (fiatRate != null && fiatRate.compareTo(BigDecimal.ZERO) > 0) {
				vo.setFiatValue(usdtValue.multiply(fiatRate).setScale(2, RoundingMode.HALF_UP));
			}
		}
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void addAvailableBalance(@Valid MemberAssetAdjustParam param) {
		MemberAsset asset = getAsset(param.getId());
		BigDecimal amount = validateAmount(param.getAmount());
		String memberAccountId = asset.getMemberAccountId();
		String accountType = asset.getAccountType();
		String assetCode = asset.getAssetCode();
		BigDecimal balanceBefore = memberAssetBiz.addAvailable(memberAccountId, accountType, assetCode, amount);
		memberAssetChangeLogRepo.save(MemberAssetChangeLog.buildWithAdminAdjust(memberAccountId, accountType, assetCode,
				balanceBefore, amount, Constant.会员余额变动类型_系统, param.getNote()));
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void reduceAvailableBalance(@Valid MemberAssetAdjustParam param) {
		MemberAsset asset = getAsset(param.getId());
		BigDecimal amount = validateAmount(param.getAmount());
		String memberAccountId = asset.getMemberAccountId();
		String accountType = asset.getAccountType();
		String assetCode = asset.getAssetCode();
		BigDecimal balanceBefore = memberAssetBiz.reduceAvailable(memberAccountId, accountType, assetCode, amount);
		memberAssetChangeLogRepo.save(MemberAssetChangeLog.buildWithAdminAdjust(memberAccountId, accountType, assetCode,
				balanceBefore, amount.negate(), Constant.会员余额变动类型_系统, param.getNote()));
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void freezeBalance(@Valid MemberAssetAdjustParam param) {
		MemberAsset asset = getAsset(param.getId());
		BigDecimal amount = validateAmount(param.getAmount());
		String memberAccountId = asset.getMemberAccountId();
		String accountType = asset.getAccountType();
		String assetCode = asset.getAssetCode();
		String note = param.getNote();
		BigDecimal availBefore = memberAssetBiz.reduceAvailable(memberAccountId, accountType, assetCode, amount);
		BigDecimal frozenBefore = memberAssetBiz.addFrozen(memberAccountId, accountType, assetCode, amount);
		memberAssetChangeLogRepo
				.save(MemberAssetChangeLog.buildWithAdminAdjust(memberAccountId, accountType, assetCode, availBefore,
						amount.negate(), Constant.会员余额变动类型_资金冻结, StrUtil.isNotBlank(note) ? note : "冻结资金-可用余额减少"));
		memberAssetChangeLogRepo.save(MemberAssetChangeLog.buildWithAdminAdjust(memberAccountId, accountType, assetCode,
				frozenBefore, amount, Constant.会员余额变动类型_资金冻结, StrUtil.isNotBlank(note) ? note : "冻结资金-冻结余额增加"));
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void unfreezeBalance(@Valid MemberAssetAdjustParam param) {
		MemberAsset asset = getAsset(param.getId());
		BigDecimal amount = validateAmount(param.getAmount());
		String memberAccountId = asset.getMemberAccountId();
		String accountType = asset.getAccountType();
		String assetCode = asset.getAssetCode();
		String note = param.getNote();
		BigDecimal frozenBefore = memberAssetBiz.reduceFrozen(memberAccountId, accountType, assetCode, amount);
		BigDecimal availBefore = memberAssetBiz.addAvailable(memberAccountId, accountType, assetCode, amount);
		memberAssetChangeLogRepo
				.save(MemberAssetChangeLog.buildWithAdminAdjust(memberAccountId, accountType, assetCode, frozenBefore,
						amount.negate(), Constant.会员余额变动类型_资金解冻, StrUtil.isNotBlank(note) ? note : "解冻资金-冻结余额减少"));
		memberAssetChangeLogRepo.save(MemberAssetChangeLog.buildWithAdminAdjust(memberAccountId, accountType, assetCode,
				availBefore, amount, Constant.会员余额变动类型_资金解冻, StrUtil.isNotBlank(note) ? note : "解冻资金-可用余额增加"));
	}

	private MemberAsset getAsset(String id) {
		return memberAssetRepo.findById(id).orElseThrow(() -> new BizException("资产账户不存在"));
	}

	private BigDecimal validateAmount(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BizException("调整金额必须大于0");
		}
		return amount;
	}

	@Lock(keys = "'openAssetAccount' + #param.memberUid")
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void openAssetAccount(@Valid MemberAssetOpenAccountParam param) {
		String accountType = param.getAccountType();
		String assetCode = param.getAssetCode();
		if (!(Constant.账户类型_现货.equals(accountType) || Constant.账户类型_合约.equals(accountType)
				|| Constant.账户类型_法币.equals(accountType))) {
			throw new BizException("不支持的账户类型");
		}
		MemberAccount member = memberAccountRepo.findByUid(param.getMemberUid());
		if (member == null) {
			throw new BizException("会员不存在");
		}
		String memberAccountId = member.getId();
		if (memberAssetRepo.findAvailable(memberAccountId, accountType, assetCode) != null) {
			throw new BizException("该资产账户已存在");
		}
		memberAssetBiz.initAsset(memberAccountId, accountType, assetCode);
	}

}

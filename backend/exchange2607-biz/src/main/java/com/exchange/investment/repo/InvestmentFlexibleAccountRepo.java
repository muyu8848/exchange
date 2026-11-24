package com.exchange.investment.repo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.investment.domain.InvestmentFlexibleAccount;

public interface InvestmentFlexibleAccountRepo
		extends JpaRepository<InvestmentFlexibleAccount, String>, JpaSpecificationExecutor<InvestmentFlexibleAccount> {

	/**
	 * 在投账户：同一会员同一币种同一时刻最多一条。
	 * <p>
	 * 活期账户以「在投周期」为单位，position 归零后即视为结束，再次申购会新建账户。
	 */
	Optional<InvestmentFlexibleAccount> findFirstByMemberAccountIdAndAssetCodeAndPositionGreaterThanOrderByCreateTimeDesc(
			String memberAccountId, String assetCode, BigDecimal position);

	/** 最新账户（含已清零），用于产品详情展示 */
	Optional<InvestmentFlexibleAccount> findFirstByMemberAccountIdAndAssetCodeOrderByCreateTimeDesc(
			String memberAccountId, String assetCode);

	/**
	 * 待计息账户：必须按 nextAccrualTime 升序，否则积压超过 100 条时可能反复取到同一批，部分账户长期得不到计息。
	 */
	List<InvestmentFlexibleAccount> findTop100ByPositionGreaterThanAndNextAccrualTimeLessThanEqualOrderByNextAccrualTimeAsc(
			BigDecimal position, LocalDateTime nextAccrualTime);

	List<InvestmentFlexibleAccount> findByMemberAccountId(String memberAccountId);

	/**
	 * 按创建时间区间清理活期账户（仅用于后台数据清理）。
	 * <p>
	 * 注意：账户是「会员 + 币种」聚合体，本方法按 create_time 切分，区间外创建的申购持仓不受影响，
	 * 可能出现账户被清理但区间外持仓仍在的跨区间残留，属数据清理工具的已知边界。
	 */
	@Modifying
	@Query(nativeQuery = true, value = "delete from investment_flexible_account where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

}

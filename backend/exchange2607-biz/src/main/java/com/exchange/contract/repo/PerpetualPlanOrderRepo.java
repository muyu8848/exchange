package com.exchange.contract.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.contract.domain.PerpetualPlanOrder;

public interface PerpetualPlanOrderRepo
		extends JpaRepository<PerpetualPlanOrder, String>, JpaSpecificationExecutor<PerpetualPlanOrder> {

	/** 计划委托撮合：查询全部委托中 */
	List<PerpetualPlanOrder> findByStatus(String status);

	/** 按持仓 + 会员 + 状态 + 来源 查询计划委托（单边重设/撤销联动用） */
	List<PerpetualPlanOrder> findByPositionIdAndMemberAccountIdAndStatusAndSource(String positionId,
			String memberAccountId, String status, String source);

	@Modifying
	@Query(nativeQuery = true, value = "delete from perpetual_plan_order where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

}

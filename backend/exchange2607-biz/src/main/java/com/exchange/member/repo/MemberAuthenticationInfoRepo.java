package com.exchange.member.repo;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.member.domain.MemberAuthenticationInfo;

public interface MemberAuthenticationInfoRepo
		extends JpaRepository<MemberAuthenticationInfo, String>, JpaSpecificationExecutor<MemberAuthenticationInfo> {
	
	@Modifying
	@Query(nativeQuery = true, value = "delete from member_authentication_info where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

	MemberAuthenticationInfo findTopByIdNumberAndStatus(String idNumber, String status);

	/** 按「证件类型 + 证件号码」查重：不同国家的护照号可能撞号，不能只看号码 */
	MemberAuthenticationInfo findTopByIdTypeAndIdNumberAndStatus(String idType, String idNumber, String status);

	/** 最近一次驳回记录，用于向会员展示驳回原因 */
	MemberAuthenticationInfo findTopByMemberAccountIdAndStatusOrderByDealTimeDesc(String memberAccountId, String status);

	/** 会员最近一次提交的实名记录（不限状态），客户端实名页使用 */
	MemberAuthenticationInfo findTopByMemberAccountIdOrderByCreateTimeDesc(String memberAccountId);

	long countByStatus(String status);
	
}

package com.exchange.notification.repo;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.exchange.notification.domain.MemberNotification;

public interface MemberNotificationRepo
		extends JpaRepository<MemberNotification, String>, JpaSpecificationExecutor<MemberNotification> {

	long countBymemberAccountIdAndReadFlagIsFalse(String memberAccountId);

	MemberNotification findTopByMemberAccountIdAndPopupFlagTrueAndReadTimeIsNullOrderByCreateTimeDesc(
			String memberAccountId);

	@Modifying
	@Query("UPDATE MemberNotification n SET n.readFlag = true, n.readTime = :readTime "
			+ "WHERE n.memberAccountId = :memberAccountId AND n.readFlag = false")
	int markAllReadBymemberAccountId(@Param("memberAccountId") String memberAccountId,
			@Param("readTime") LocalDateTime readTime);

	@Modifying
	@Query(nativeQuery = true, value = "delete from member_notification where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

}

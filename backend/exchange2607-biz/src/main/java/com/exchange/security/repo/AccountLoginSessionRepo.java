package com.exchange.security.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.security.domain.AccountLoginSession;

public interface AccountLoginSessionRepo
		extends JpaRepository<AccountLoginSession, String>, JpaSpecificationExecutor<AccountLoginSession> {

	AccountLoginSession findByToken(String token);

	List<AccountLoginSession> findByStatusAndLastActiveTimeBefore(String status, LocalDateTime time);

	List<AccountLoginSession> findByAccountIdAndAccountTypeAndStatusOrderByLastActiveTimeDesc(String accountId,
			String accountType, String status);

	@Modifying
	@Query(nativeQuery = true, value = "delete from account_login_session where create_time >= ?1 and create_time <= ?2")
	Integer dataClean(LocalDateTime startTime, LocalDateTime endTime);

}

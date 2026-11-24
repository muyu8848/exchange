package com.exchange.member.repo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.exchange.member.domain.MemberAsset;

public interface MemberAssetRepo extends JpaRepository<MemberAsset, String>, JpaSpecificationExecutor<MemberAsset> {

	@Query(nativeQuery = true, value = "select available from member_asset where member_account_id = ?1 and account_type = ?2 and asset_code = ?3 limit 1")
	BigDecimal findAvailable(String memberAccountId, String accountType, String assetCode);

	@Query(nativeQuery = true, value = "select frozen from member_asset where member_account_id = ?1 and account_type = ?2 and asset_code = ?3 limit 1")
	BigDecimal findFrozen(String memberAccountId, String accountType, String assetCode);

	@Modifying
	@Query(nativeQuery = true, value = "update member_asset set available = ?5, version = version + 1 where member_account_id = ?1 and account_type = ?2 and asset_code = ?3 and available = ?4 limit 1")
	Integer updateAvailable(String memberAccountId, String accountType, String assetCode, BigDecimal oldAvailable,
			BigDecimal newAvailable);

	@Modifying
	@Query(nativeQuery = true, value = "update member_asset set frozen = ?5, version = version + 1 where member_account_id = ?1 and account_type = ?2 and asset_code = ?3 and frozen = ?4 limit 1")
	Integer updateFrozen(String memberAccountId, String accountType, String assetCode, BigDecimal oldFrozen,
			BigDecimal newFrozen);

	@Modifying
	@Query(nativeQuery = true, value = "insert into member_asset (id, member_account_id, account_type, asset_code, available, frozen, version, create_time) values (?1, ?2, ?3, ?4, 0, 0, 0, ?5)")
	Integer initAsset(String id, String memberAccountId, String accountType, String assetCode,
			LocalDateTime createTime);

	List<MemberAsset> findByMemberAccountIdAndAccountTypeAndAvailableGreaterThanOrderByAvailableDesc(
			String memberAccountId, String accountType, BigDecimal available);

	List<MemberAsset> findByMemberAccountId(String memberAccountId);

	List<MemberAsset> findByMemberAccountIdAndAccountType(String memberAccountId, String accountType);
	
	List<MemberAsset> findByMemberAccountIdAndAssetCode(String memberAccountId, String assetCode);

	List<MemberAsset> findByMemberAccountIdAndAccountTypeAndAssetCodeIn(String memberAccountId, String accountType, List<String> assetCodes);

}

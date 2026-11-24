package com.exchange.member.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.exchange.member.domain.MemberAccount;

public interface MemberAccountRepo
		extends JpaRepository<MemberAccount, String>, JpaSpecificationExecutor<MemberAccount> {

	MemberAccount findByUid(Long uid);

	/** 按邮箱查找指定状态的账号（登录/找回密码仅允许有效账号） */
	MemberAccount findByEmailAndStatusIn(String email, List<String> statuses);

	/** 按手机号查找指定状态的账号（登录/找回密码仅允许有效账号） */
	MemberAccount findByPhoneAreaCodeAndPhoneAndStatusIn(String phoneAreaCode, String phone, List<String> statuses);

	/** 注册/绑定查重：包含已注销账号，避免手机号被重复注册 */
	MemberAccount findByEmail(String email);

	/** 注册/绑定查重：包含已注销账号，避免手机号被重复注册 */
	MemberAccount findByPhoneAreaCodeAndPhone(String phoneAreaCode, String phone);

	/** 按邀请码查找指定状态的邀请人（仅正常账号可作为邀请人） */
	MemberAccount findByInviteCodeAndStatusIn(String inviteCode, List<String> statuses);

	/** 生成邀请码查重：包含已注销账号，避免历史邀请码被重复分配 */
	MemberAccount findByInviteCode(String inviteCode);

	@Query("SELECT MAX(m.uid) FROM MemberAccount m")
	Long findMaxUid();

}

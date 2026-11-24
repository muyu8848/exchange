package com.exchange.member.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.member.domain.MemberClosure;

public interface MemberClosureRepo
		extends JpaRepository<MemberClosure, String>, JpaSpecificationExecutor<MemberClosure> {

	List<MemberClosure> findByDescendantId(String descendantId);

	List<MemberClosure> findByAncestorId(String ancestorId);

}

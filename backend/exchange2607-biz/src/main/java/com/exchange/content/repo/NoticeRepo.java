package com.exchange.content.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.content.domain.Notice;

public interface NoticeRepo extends JpaRepository<Notice, String>, JpaSpecificationExecutor<Notice> {

}

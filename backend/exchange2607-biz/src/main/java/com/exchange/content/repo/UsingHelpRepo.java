package com.exchange.content.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.content.domain.UsingHelp;

public interface UsingHelpRepo extends JpaRepository<UsingHelp, String>, JpaSpecificationExecutor<UsingHelp> {

}

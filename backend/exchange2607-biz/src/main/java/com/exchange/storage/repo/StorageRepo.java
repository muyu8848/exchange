package com.exchange.storage.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.exchange.storage.domain.Storage;

public interface StorageRepo extends JpaRepository<Storage, String>, JpaSpecificationExecutor<Storage> {

}

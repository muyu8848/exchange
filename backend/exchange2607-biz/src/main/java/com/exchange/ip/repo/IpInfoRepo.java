package com.exchange.ip.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exchange.ip.domain.IpInfo;

public interface IpInfoRepo extends JpaRepository<IpInfo, String> {

	IpInfo findByIp(String ip);

}

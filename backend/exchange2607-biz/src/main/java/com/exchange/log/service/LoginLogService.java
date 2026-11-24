package com.exchange.log.service;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.mq.MqPublisher;
import com.exchange.common.vo.PageResult;
import com.exchange.constants.Constant;
import com.exchange.log.domain.LoginLog;
import com.exchange.log.param.LoginLogQueryCondParam;
import com.exchange.log.repo.LoginLogRepo;
import com.exchange.log.vo.LoginLogVO;
import com.exchange.log.vo.MyLoginLogVO;

@Validated
@Service
public class LoginLogService {

	@Autowired
	private LoginLogRepo loginLogRepo;

	@Autowired
	private MqPublisher mqPublisher;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MyLoginLogVO> findMyLoginLogByPage(@Valid LoginLogQueryCondParam param) {
		Page<LoginLog> result = loginLogRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("loginTime"))));
		PageResult<MyLoginLogVO> pageResult = new PageResult<>(MyLoginLogVO.convertFor(result.getContent()),
				param.getPageNum(), param.getPageSize(), result.getTotalElements());
		return pageResult;
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<LoginLogVO> findLoginLogByPage(@Valid LoginLogQueryCondParam param) {
		Page<LoginLog> result = loginLogRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("loginTime"))));
		PageResult<LoginLogVO> pageResult = new PageResult<>(LoginLogVO.convertFor(result.getContent()),
				param.getPageNum(), param.getPageSize(), result.getTotalElements());
		return pageResult;
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void recordLoginLog(LoginLog loginLog) {
		loginLogRepo.save(loginLog);
		// 事务提交后再通知记录 IP 归属地，避免回滚时产生无对应日志的缓存
		mqPublisher.publishAfterCommit(Constant.记录IP信息, loginLog.getIp());
	}

}

package com.exchange.log.service;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.utils.IdUtils;
import com.exchange.common.vo.PageResult;
import com.exchange.log.domain.MemberContactBindLog;
import com.exchange.log.param.MemberContactBindLogQueryCondParam;
import com.exchange.log.repo.MemberContactBindLogRepo;
import com.exchange.log.vo.MemberContactBindLogVO;

/**
 * 会员联系方式绑定流水
 */
@Validated
@Service
public class MemberContactBindLogService {

	@Autowired
	private MemberContactBindLogRepo memberContactBindLogRepo;

	/**
	 * 记录一条联系方式绑定流水（append-only）
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void saveLog(String memberAccountId, String contactType, String contact, String phoneAreaCode,
			String action) {
		MemberContactBindLog po = new MemberContactBindLog();
		po.setId(IdUtils.getId());
		po.setMemberAccountId(memberAccountId);
		po.setContactType(contactType);
		po.setContact(contact);
		po.setPhoneAreaCode(phoneAreaCode);
		po.setAction(action);
		po.setCreateTime(LocalDateTime.now());
		memberContactBindLogRepo.save(po);
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MemberContactBindLogVO> findMemberContactBindLogByPage(
			@Valid MemberContactBindLogQueryCondParam param) {
		Page<MemberContactBindLog> result = memberContactBindLogRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("createTime"))));
		List<MemberContactBindLog> records = result.getContent();
		return new PageResult<>(MemberContactBindLogVO.convertFor(records), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
	}
}

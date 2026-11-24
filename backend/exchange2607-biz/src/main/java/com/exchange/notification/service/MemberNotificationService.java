package com.exchange.notification.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.common.exception.BizException;
import com.exchange.common.vo.PageResult;
import com.exchange.member.domain.MemberAccount;
import com.exchange.member.repo.MemberAccountRepo;
import com.exchange.notification.domain.MemberNotification;
import com.exchange.notification.param.MemberNotificationQueryCondParam;
import com.exchange.notification.param.SendMemberNotificationParam;
import com.exchange.notification.repo.MemberNotificationRepo;
import com.exchange.notification.vo.MemberNotificationVO;
import com.exchange.notification.vo.MyNotificationVO;

@Validated
@Service
public class MemberNotificationService {

	@Autowired
	private MemberNotificationRepo memberNotificationRepo;

	@Autowired
	private MemberAccountRepo memberAccountRepo;

	@Autowired
	private MemberNotificationBiz memberNotificationBiz;

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MyNotificationVO> findMemberPage(@Valid MemberNotificationQueryCondParam param) {
		Page<MemberNotification> result = memberNotificationRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("createTime"))));
		return new PageResult<>(MyNotificationVO.convertFor(result.getContent()), param.getPageNum(),
				param.getPageSize(), result.getTotalElements());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MemberNotificationVO> findAdminPage(@Valid MemberNotificationQueryCondParam param) {
		Page<MemberNotification> result = memberNotificationRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("createTime"))));
		List<MemberNotificationVO> vos = new ArrayList<>();
		for (MemberNotification notification : result.getContent()) {
			vos.add(MemberNotificationVO.convertFor(notification));
		}
		return new PageResult<>(vos, param.getPageNum(), param.getPageSize(), result.getTotalElements());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public long countUnread(@NotBlank String memberAccountId) {
		return memberNotificationRepo.countBymemberAccountIdAndReadFlagIsFalse(memberAccountId);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void markRead(@NotBlank String memberAccountId, @NotBlank String id) {
		MemberNotification notification = memberNotificationRepo.getOne(id);
		if (!memberAccountId.equals(notification.getMemberAccountId())) {
			throw new BizException("无权操作");
		}
		notification.setReadFlag(true);
		notification.setReadTime(LocalDateTime.now());
		memberNotificationRepo.save(notification);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void markAllRead(@NotBlank String memberAccountId) {
		memberNotificationRepo.markAllReadBymemberAccountId(memberAccountId, LocalDateTime.now());
	}

	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public MyNotificationVO findPendingPopup(@NotBlank String memberAccountId) {
		MemberNotification notification = memberNotificationRepo
				.findTopByMemberAccountIdAndPopupFlagTrueAndReadTimeIsNullOrderByCreateTimeDesc(memberAccountId);
		return MyNotificationVO.convertFor(notification);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void sendSystemMessage(@Valid SendMemberNotificationParam param) {
		MemberAccount member = memberAccountRepo.findByUid(param.getMemberUid());
		if (member == null) {
			throw new BizException("会员不存在");
		}
		memberNotificationBiz.sendSystemMessage(member.getId(), param.getTitle(), param.getContent());
	}

}

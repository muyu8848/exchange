package com.exchange.member.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.exchange.common.vo.PageResult;
import com.exchange.member.domain.MemberAccount;
import com.exchange.member.domain.MemberClosure;
import com.exchange.member.param.MyTeamMemberQueryParam;
import com.exchange.member.repo.MemberAccountRepo;
import com.exchange.member.repo.MemberClosureRepo;
import com.exchange.member.vo.MyDirectTeamOverviewVO;
import com.exchange.member.vo.MyTeamLevelCountVO;
import com.exchange.member.vo.MyTeamMemberVO;
import com.exchange.member.vo.MyTeamOverviewVO;

@Service
@Transactional
public class MemberTeamService {

	@Autowired
	private MemberClosureRepo memberClosureRepo;

	@Autowired
	private MemberAccountRepo memberAccountRepo;

	/**
	 * 获取会员团队概览（查看全量后代），同时返回返佣最大层级
	 */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public MyTeamOverviewVO findTeamOverview(String memberAccountId) {
		List<MemberClosure> descendants = memberClosureRepo.findByAncestorId(memberAccountId).stream()
				.filter(e -> e.getDepth() != null && e.getDepth() >= 1).collect(Collectors.toList());
		MyTeamOverviewVO vo = buildTeamOverview(descendants);
		vo.setRebateMaxLevel(1);
		return vo;
	}

	private MyTeamOverviewVO buildTeamOverview(List<MemberClosure> descendants) {
		long directCount = descendants.stream().filter(e -> e.getDepth() == 1).count();
		long teamTotalCount = descendants.size();
		Map<Integer, Long> grouped = descendants.stream()
				.collect(Collectors.groupingBy(MemberClosure::getDepth, Collectors.counting()));
		List<MyTeamLevelCountVO> levelCounts = grouped.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> {
			MyTeamLevelCountVO vo = new MyTeamLevelCountVO();
			vo.setLevel(e.getKey());
			vo.setCount(e.getValue());
			return vo;
		}).collect(Collectors.toList());

		// 统计今日新增人数
		LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
		long todayNewCount = 0;
		if (!descendants.isEmpty()) {
			List<String> descendantIds = descendants.stream().map(MemberClosure::getDescendantId)
					.collect(Collectors.toList());
			Map<String, MemberAccount> accountMap = memberAccountRepo.findAllById(descendantIds).stream()
					.collect(Collectors.toMap(MemberAccount::getId, e -> e));
			todayNewCount = descendants.stream().filter(e -> {
				MemberAccount m = accountMap.get(e.getDescendantId());
				return m != null && m.getCreateTime() != null && !m.getCreateTime().isBefore(todayStart);
			}).count();
		}

		MyTeamOverviewVO result = new MyTeamOverviewVO();
		result.setDirectCount(directCount);
		result.setTeamTotalCount(teamTotalCount);
		result.setLevelCounts(levelCounts);
		result.setTodayNewCount(todayNewCount);
		return result;
	}

	/**
	 * 按层级分页查询团队成员
	 */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MyTeamMemberVO> findTeamMemberByPage(@Valid MyTeamMemberQueryParam param) {
		Page<MemberClosure> paged = memberClosureRepo.findAll(param.buildSpecification(),
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(),
						Sort.by(Sort.Order.asc("depth"), Sort.Order.desc("descendantAccount.createTime"))));

		List<MemberClosure> content = paged.getContent();
		List<String> pageIds = content.stream().map(MemberClosure::getDescendantId).collect(Collectors.toList());
		Map<String, Integer> depthMap = content.stream()
				.collect(Collectors.toMap(MemberClosure::getDescendantId, MemberClosure::getDepth, (a, b) -> a));

		List<MyTeamMemberVO> list = new ArrayList<>();
		if (!pageIds.isEmpty()) {
			Map<String, MemberAccount> accountMap = memberAccountRepo.findAllById(pageIds).stream()
					.collect(Collectors.toMap(MemberAccount::getId, e -> e));
			for (String id : pageIds) {
				MemberAccount m = accountMap.get(id);
				if (m == null) {
					continue;
				}
				MyTeamMemberVO vo = MyTeamMemberVO.convertFor(m);
				vo.setLevel(depthMap.getOrDefault(id, 0));
				list.add(vo);
			}
		}

		return new PageResult<>(list, param.getPageNum(), param.getPageSize(), paged.getTotalElements());
	}

	/**
	 * 我的好友概览：仅统计直推（level=1）人数与今日新增直推
	 */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public MyDirectTeamOverviewVO findDirectTeamOverview(String memberAccountId) {
		List<MemberClosure> direct = memberClosureRepo.findByAncestorId(memberAccountId).stream()
				.filter(e -> e.getDepth() != null && e.getDepth() == 1).collect(Collectors.toList());
		long directCount = direct.size();
		long todayNewDirect = 0;
		if (!direct.isEmpty()) {
			LocalDateTime todayStart = LocalDate.now().atStartOfDay();
			List<String> ids = direct.stream().map(MemberClosure::getDescendantId).collect(Collectors.toList());
			Map<String, MemberAccount> map = memberAccountRepo.findAllById(ids).stream()
					.collect(Collectors.toMap(MemberAccount::getId, e -> e));
			todayNewDirect = direct.stream().filter(e -> {
				MemberAccount m = map.get(e.getDescendantId());
				return m != null && m.getCreateTime() != null && !m.getCreateTime().isBefore(todayStart);
			}).count();
		}
		MyDirectTeamOverviewVO vo = new MyDirectTeamOverviewVO();
		vo.setDirectCount(directCount);
		vo.setTodayNewDirect(todayNewDirect);
		return vo;
	}

	/**
	 * 我的直推成员分页（忽略客户端传入的 ancestorId / level，强制当前会员 + 直推）
	 */
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
	public PageResult<MyTeamMemberVO> findDirectMemberByPage(MyTeamMemberQueryParam param, String memberAccountId) {
		param.setAncestorId(memberAccountId);
		param.setLevel(1);
		return findTeamMemberByPage(param);
	}

}

package com.exchange.member.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;

import com.exchange.dictconfig.DictHolder;
import com.exchange.member.domain.MemberAccount;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class MyTeamMemberVO {

	private String id;

	/** 用户可见 UID（账号标识，运营核对用） */
	private Long uid;

	private String nickName;

	/** 会员状态：1=启用 0=停用 */
	private String status;

	/** 会员状态中文名（enableStatus 字典） */
	private String statusName;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	/** 加入时间（仅日期，yyyy-MM-dd） */
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate createDate;

	/**
	 * 在当前会员团队中的层级（1=直推）
	 */
	private int level;

	/**
	 * 由会员实体构造团队成员 VO（参考 MemberAccountVO.convertFor） 注：层级 level 不在 MemberAccount
	 * 上，由调用方另行 setLevel
	 */
	public static MyTeamMemberVO convertFor(MemberAccount po) {
		if (po == null) {
			return null;
		}
		MyTeamMemberVO vo = new MyTeamMemberVO();
		BeanUtils.copyProperties(po, vo);
		vo.setStatusName(DictHolder.getDictItemName("memberAccountStatus", po.getStatus()));
		vo.setCreateDate(po.getCreateTime() != null ? po.getCreateTime().toLocalDate() : null);
		return vo;
	}

}

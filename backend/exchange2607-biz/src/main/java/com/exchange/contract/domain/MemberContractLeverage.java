package com.exchange.contract.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.exchange.common.utils.IdUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "member_contract_leverage")
@DynamicInsert(true)
@DynamicUpdate(true)
public class MemberContractLeverage {

	@Id
	private String id;

	private String memberAccountId;

	private String symbolCode;

	/** 开多杠杆倍数 */
	private Integer longLeverage;

	/** 开空杠杆倍数 */
	private Integer shortLeverage;

	/** 最近一次选择的方向: long / short */
	private String direction;

	private LocalDateTime updateTime;

	public static MemberContractLeverage build(String memberAccountId, String symbolCode) {
		MemberContractLeverage po = new MemberContractLeverage();
		po.setId(IdUtils.getId());
		po.setMemberAccountId(memberAccountId);
		po.setSymbolCode(symbolCode);
		return po;
	}
}

package com.exchange.member.param;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import com.exchange.common.utils.IdUtils;
import com.exchange.constants.Constant;
import com.exchange.member.domain.MemberAuthenticationInfo;

import lombok.Data;

@Data
public class SubmitAuthenticationInfoParam {

	@NotBlank
	private String memberAccountId;

	/** 证件类型：idCard / passport / driverLicense，合法性在 Service 层校验 */
	@NotBlank
	private String idType;

	@NotBlank
	private String realName;

	@NotBlank
	private String idNumber;

	/** 证件正面 / 护照资料页 */
	@NotBlank
	private String identityCardPic1;

	/** 证件反面：护照场景为空，条件校验在 Service 层 */
	private String identityCardPic2;

	/** 证件有效期：护照、驾驶证必填，条件校验在 Service 层 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate expiryDate;

	public MemberAuthenticationInfo convertToPo() {
		MemberAuthenticationInfo po = new MemberAuthenticationInfo();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setCreateTime(LocalDateTime.now());
		po.setStatus(Constant.会员认证状态_认证待审核);
		return po;
	}

}

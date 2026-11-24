package com.exchange.background.param;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;

import com.exchange.background.domain.Role;
import com.exchange.common.utils.IdUtils;

import lombok.Data;

@Data
public class RoleParam {

	private String id;

	@NotBlank
	private String name;

	public Role convertToPo() {
		Role po = new Role();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setCreateTime(LocalDateTime.now());
		po.setDeletedFlag(false);
		return po;
	}

}

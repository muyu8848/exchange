package com.exchange.background.param;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;

import com.exchange.background.domain.Menu;
import com.exchange.common.utils.IdUtils;

import lombok.Data;

@Data
public class MenuParam {

	private String id;

	@NotBlank
	private String name;

	private String url;
	
	@NotBlank
	private String type;

	private Integer sortNum;

	private String parentId;

	public Menu convertToPo() {
		Menu po = new Menu();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setDeletedFlag(false);
		return po;
	}

}

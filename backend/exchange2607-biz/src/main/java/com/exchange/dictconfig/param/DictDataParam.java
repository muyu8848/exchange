package com.exchange.dictconfig.param;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.dictconfig.domain.DictItem;

import lombok.Data;

@Data
public class DictDataParam {

	@NotBlank
	private String dictItemCode;

	@NotBlank
	private String dictItemName;

	public DictItem convertToPo() {
		DictItem po = new DictItem();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		return po;
	}

}

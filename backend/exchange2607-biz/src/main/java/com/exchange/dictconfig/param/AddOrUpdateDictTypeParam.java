package com.exchange.dictconfig.param;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;

import com.exchange.common.utils.IdUtils;
import com.exchange.dictconfig.domain.DictType;

import lombok.Data;

@Data
public class AddOrUpdateDictTypeParam {

	private String id;

	@NotBlank
	private String dictTypeCode;

	@NotBlank
	private String dictTypeName;

	private String note;
	
	public DictType convertToPo() {
		DictType po = new DictType();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setLastUpdateTime(LocalDateTime.now());
		return po;
	}

}

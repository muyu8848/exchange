package com.exchange.dictconfig.param;

import com.exchange.common.param.PageParam;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DictTypeQueryCondParam extends PageParam {
	
	private String dictTypeCode;

	private String dictTypeName;

}

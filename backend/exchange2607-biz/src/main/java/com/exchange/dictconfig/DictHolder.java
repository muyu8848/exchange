package com.exchange.dictconfig;

import java.util.List;

import com.exchange.common.utils.SpringUtils;
import com.exchange.dictconfig.service.DictService;
import com.exchange.dictconfig.vo.DictItemVO;

import cn.hutool.core.util.StrUtil;

public class DictHolder {

	public static String getDictItemName(String dictTypeCode, String dictItemCode) {
		if (StrUtil.isBlank(dictItemCode)) {
			return null;
		}
		DictService dictService = SpringUtils.getBean(DictService.class);
		DictItemVO dictItemVO = dictService.findDictItemByDictTypeCodeAndDictItemCode(dictTypeCode, dictItemCode);
		if (dictItemVO == null) {
			return null;
		}
		return dictItemVO.getDictItemName();
	}

	public static List<DictItemVO> findDictItem(String dictTypeCode) {
		DictService dictService = SpringUtils.getBean(DictService.class);
		return dictService.findDictItemByDictTypeCode(dictTypeCode);
	}

}

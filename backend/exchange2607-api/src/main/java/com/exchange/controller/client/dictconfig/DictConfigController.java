package com.exchange.controller.client.dictconfig;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.Result;
import com.exchange.dictconfig.DictHolder;
import com.exchange.dictconfig.vo.DictItemVO;

@RestController
@RequestMapping("/dictconfig")
public class DictConfigController {

	@PostMapping("/findDictItemInCache")
	public Result<List<DictItemVO>> findDictItemInCache(String dictTypeCode) {
		return Result.success(DictHolder.findDictItem(dictTypeCode));
	}

}

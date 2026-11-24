package com.exchange.controller.admin.kline;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.utils.SymbolPriceUtils;
import com.exchange.common.vo.Result;
import com.exchange.constants.Constant;
import com.exchange.kline.service.KlineService;
import com.exchange.kline.vo.KlineVO;

@RestController
@RequestMapping("/kline")
public class KlineController {

	@Autowired
	private KlineService klineService;

	@PostMapping("/findKline")
	public Result<List<KlineVO>> findKline(String symbol, String period, Integer limit, String enddate, String endtime,
			String bizType) {
		String resolvedBizType = Constant.行情业务类型_现货.equals(bizType) ? Constant.行情业务类型_现货 : Constant.行情业务类型_合约;
		String instId = SymbolPriceUtils.getInstId(resolvedBizType, symbol);
		List<KlineVO> list = klineService.findKlineWithFallback(instId, period, limit, enddate, endtime);
		return Result.success(list);
	}

}

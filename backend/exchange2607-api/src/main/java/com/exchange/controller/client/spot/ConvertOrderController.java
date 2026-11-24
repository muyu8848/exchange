package com.exchange.controller.client.spot;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.currency.vo.CurrencyOptionVO;
import com.exchange.setting.service.SettingService;
import com.exchange.setting.vo.ConvertSettingVO;
import com.exchange.spot.param.ConvertOrderQueryCondParam;
import com.exchange.spot.param.ConvertQuoteParam;
import com.exchange.spot.param.ConvertTradeParam;
import com.exchange.spot.service.ConvertService;
import com.exchange.spot.vo.ConvertQuoteVO;
import com.exchange.spot.vo.MyConvertOrderVO;

import cn.dev33.satoken.stp.StpUtil;

@Validated
@RestController
@RequestMapping("/convertOrder")
public class ConvertOrderController {

	@Autowired
	private ConvertService convertService;

	@Autowired
	private SettingService settingService;

	@PostMapping("/getQuote")
	public Result<ConvertQuoteVO> getQuote(ConvertQuoteParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(convertService.getQuote(param));
	}

	@PostMapping("/convert")
	public Result<String> convert(ConvertTradeParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(convertService.convert(param));
	}

	@PostMapping("/findByPage")
	public Result<PageResult<MyConvertOrderVO>> findByPage(ConvertOrderQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(convertService.findMyByPage(param));
	}

	@PostMapping("/getSetting")
	public Result<ConvertSettingVO> getSetting() {
		return Result.success(settingService.getConvertSetting());
	}

	@PostMapping("/findCurrencies")
	public Result<List<CurrencyOptionVO>> findCurrencies() {
		return Result.success(convertService.findCurrencies());
	}
}

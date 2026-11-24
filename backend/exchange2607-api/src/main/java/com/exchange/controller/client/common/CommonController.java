package com.exchange.controller.client.common;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.c2c.service.C2cFiatService;
import com.exchange.c2c.vo.C2cFiatOptionVO;
import com.exchange.common.vo.Result;
import com.exchange.currency.service.CurrencyService;
import com.exchange.currency.vo.CurrencyOptionVO;
import com.exchange.member.service.CountryAreaCodeService;
import com.exchange.member.vo.CountryAreaCodeOptionVO;
import com.exchange.multilang.service.MultiLangService;
import com.exchange.multilang.vo.MultiLangListVO;

/**
 * 客户端公共元数据接口（免登录）：仅存放"全局、启动预取、纯只读"的配置数据。
 * <p>
 * 免登录白名单：/client/common/**（见 SaTokenConfig），禁止放入任何包含敏感信息的接口。
 */
@RestController
@RequestMapping("/common")
public class CommonController {

	@Autowired
	private C2cFiatService c2cFiatService;

	@Autowired
	private CurrencyService currencyService;

	@Autowired
	private CountryAreaCodeService countryAreaCodeService;

	@Autowired
	private MultiLangService multiLangService;

	/** 全部未删除的法币列表（含禁用态）：客户端启动预取并全局缓存 */
	@PostMapping("/findAllC2cFiat")
	public Result<List<C2cFiatOptionVO>> findAllC2cFiat() {
		return Result.success(c2cFiatService.findAllC2cFiatOption());
	}

	/** 全部未删除的币种列表：客户端启动预取并全局缓存 */
	@PostMapping("/findAllCurrency")
	public Result<List<CurrencyOptionVO>> findAllCurrency() {
		return Result.success(currencyService.findAllCurrencyOption());
	}

	/** 全部启用的国家区号列表：客户端启动预取并全局缓存 */
	@PostMapping("/findAllCountryAreaCode")
	public Result<List<CountryAreaCodeOptionVO>> findAllCountryAreaCode() {
		return Result.success(countryAreaCodeService.findEnabledList());
	}

	/** 启用中的语言清单（含默认语言）：客户端启动预取，切换语言只认后端下发的语言码 */
	@PostMapping("/findAllLanguage")
	public Result<MultiLangListVO> findAllLanguage() {
		return Result.success(multiLangService.findEnabledList());
	}

}
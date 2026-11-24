package com.exchange.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.exchange.common.exception.LoginExceptionHelper;
import com.exchange.constants.Constant;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

	@Autowired
	private DemoModeInterceptor demoModeInterceptor;

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.addPathPrefix(Constant.后台接口前缀, HandlerTypePredicate.forBasePackage("com.exchange.controller.admin"));
		configurer.addPathPrefix(Constant.客户端接口前缀,
				HandlerTypePredicate.forBasePackage("com.exchange.controller.client"));
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 演示模式同时覆盖后台与客户端：后台按 @SaCheckPermission 拦截，客户端按补充清单拦截
		registry.addInterceptor(demoModeInterceptor).addPathPatterns(Constant.后台接口前缀 + "/**",
				Constant.客户端接口前缀 + "/**");
	}

	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.addAllowedOrigin("*"); // 允许所有来源
		corsConfig.addAllowedMethod("*"); // 允许所有HTTP方法
		corsConfig.addAllowedHeader("*"); // 允许所有HTTP标头
		// 项目使用了 sa-token,并且是使用 token 前后端分离的方式，并不是使用 cookies传递用户token，所以要设置为false
		corsConfig.setAllowCredentials(false); // 不允许携带身份信息（如Cookies）

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);

		return new CorsFilter(source);
	}

	@Bean
	public SaServletFilter getSaServletFilter() {
		return new SaServletFilter().addInclude("/**").addExclude("/css/**", "/images/**", "/js/**")
				.addExclude(Constant.后台接口前缀 + "/login", Constant.后台接口前缀 + "/logout")
				.addExclude(Constant.客户端接口前缀 + "/login", Constant.客户端接口前缀 + "/register",
						Constant.客户端接口前缀 + "/sendRegisterVerificationCode",
						Constant.客户端接口前缀 + "/sendForgetVerificationCode", Constant.客户端接口前缀 + "/resetPwd",
						Constant.客户端接口前缀 + "/sendLoginVerificationCode", Constant.客户端接口前缀 + "/setting/**",
						Constant.客户端接口前缀 + "/storage/fetch/**", Constant.客户端接口前缀 + "/common/**",
						Constant.客户端接口前缀 + "/spotSymbol/findEnableSymbol",
						Constant.客户端接口前缀 + "/secondContractSymbol/findEnableSymbol",
						Constant.客户端接口前缀 + "/perpetualContractSymbol/findEnable",
						// ===== 游客可看的公开行情数据（交易页未登录也能看 K 线/盘口/交易对参数）=====
						// 仅放只读行情接口；findMy*/save*/trade 等含用户数据或写入的一律不放
						Constant.客户端接口前缀 + "/kline/**",
						Constant.客户端接口前缀 + "/spotSymbol/findSymbolInfo",
						Constant.客户端接口前缀 + "/perpetualContractSymbol/findSymbolInfo",
						Constant.客户端接口前缀 + "/perpetualContractSymbol/findFundingCountdown",
						// 合约资金费率历史（公开行情，合约页可进入的独立页面）
						Constant.客户端接口前缀 + "/perpetualContract/findFundingRateHistory",
						Constant.客户端接口前缀 + "/secondContractSymbol/findSymbolInfo",
						Constant.客户端接口前缀 + "/secondContractSymbol/findPeriodConfig")
				// 全局认证函数
				.setAuth(obj -> {
					SaRouter.match(Constant.后台接口前缀 + "/**", () -> {
						StpUtil.checkLogin();
						StpUtil.checkRole(Constant.子系统_后台管理);
					});
					SaRouter.match(Constant.客户端接口前缀 + "/**", () -> {
						StpUtil.checkLogin();
						StpUtil.checkRole(Constant.子系统_客户端);
					});
				})

				// 异常处理函数
				.setError(e -> {
					SaHolder.getResponse().setHeader("Content-Type", "application/json;charset=UTF-8");
					return LoginExceptionHelper.handle(e);
				})

				// 前置函数：在每次认证函数之前执行
				.setBeforeAuth(obj -> {
					// ---------- 设置跨域响应头 ----------
					SaHolder.getResponse()
							// 允许指定域访问跨域资源
							.setHeader("Access-Control-Allow-Origin", "*")
							// 允许所有请求方式
							.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
							// 有效时间
							.setHeader("Access-Control-Max-Age", "3600")
							// 允许的header参数
							.setHeader("Access-Control-Allow-Headers", "*");
					// 如果是预检请求，则立即返回到前端
					SaRouter.match(SaHttpMethod.OPTIONS).free(r -> {
					}).back();
				});
	}

}
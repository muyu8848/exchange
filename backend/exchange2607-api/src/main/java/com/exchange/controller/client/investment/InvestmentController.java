package com.exchange.controller.client.investment;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;
import com.exchange.investment.param.InvestmentEstimateParam;
import com.exchange.investment.param.InvestmentHourlyInterestLogQueryCondParam;
import com.exchange.investment.param.InvestmentInterestLogQueryCondParam;
import com.exchange.investment.param.InvestmentRecordQueryCondParam;
import com.exchange.investment.param.InvestmentRedeemRecordQueryCondParam;
import com.exchange.investment.param.RedeemFlexibleParam;
import com.exchange.investment.param.SubscribeInvestmentParam;
import com.exchange.investment.service.InvestmentInterestLogQueryService;
import com.exchange.investment.service.InvestmentPositionService;
import com.exchange.investment.service.InvestmentProductService;
import com.exchange.investment.vo.FixedPositionDetailVO;
import com.exchange.investment.vo.FlexibleProductDetailVO;
import com.exchange.investment.vo.InvestmentEstimateVO;
import com.exchange.investment.vo.InvestmentOverviewVO;
import com.exchange.investment.vo.InvestmentProductVO;
import com.exchange.investment.vo.MyDailyInterestDetailVO;
import com.exchange.investment.vo.MyDailyInterestRecordVO;
import com.exchange.investment.vo.MyHourlyInterestRecordVO;
import com.exchange.investment.vo.MyInterestRecordVO;
import com.exchange.investment.vo.MyInvestmentRecordVO;
import com.exchange.investment.vo.MyRedeemRecordVO;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/investment")
public class InvestmentController {

	@Autowired
	private InvestmentProductService investmentProductService;

	@Autowired
	private InvestmentPositionService investmentPositionService;

	@Autowired
	private InvestmentInterestLogQueryService investmentInterestLogQueryService;

	@PostMapping("/findEnabledProducts")
	public Result<List<InvestmentProductVO>> findEnabledProducts(String assetCode) {
		return Result.success(investmentProductService.findEnabledForMember(StpUtil.getLoginIdAsString(), assetCode));
	}

	@PostMapping("/estimate")
	public Result<InvestmentEstimateVO> estimate(InvestmentEstimateParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(investmentPositionService.estimate(param));
	}

	@PostMapping("/subscribe")
	public Result<String> subscribe(SubscribeInvestmentParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(investmentPositionService.subscribe(param));
	}

	@PostMapping("/findFixedPositionDetail")
	public Result<FixedPositionDetailVO> findFixedPositionDetail(String positionId) {
		return Result
				.success(investmentPositionService.findFixedPositionDetail(StpUtil.getLoginIdAsString(), positionId));
	}

	@PostMapping("/findMyInvestmentRecordByPage")
	public Result<PageResult<MyInvestmentRecordVO>> findMyInvestmentRecordByPage(InvestmentRecordQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(investmentPositionService.findMyInvestmentRecordByPage(param));
	}

	@PostMapping("/findMyInterestRecordByPage")
	public Result<PageResult<MyInterestRecordVO>> findMyInterestRecordByPage(
			InvestmentInterestLogQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(investmentInterestLogQueryService.findMyInterestRecordByPage(param));
	}

	@PostMapping("/findOverview")
	public Result<InvestmentOverviewVO> findOverview() {
		return Result.success(investmentPositionService.findOverview(StpUtil.getLoginIdAsString()));
	}

	/** 活期产品详情：优先按 flexibleAccountId 定位，未传时按 assetCode 取最新账户 */
	@PostMapping("/findFlexibleProductDetail")
	public Result<FlexibleProductDetailVO> findFlexibleProductDetail(String assetCode, String flexibleAccountId) {
		return Result.success(investmentPositionService.findFlexibleProductDetail(StpUtil.getLoginIdAsString(),
				assetCode, flexibleAccountId));
	}

	@PostMapping("/redeemFlexible")
	public Result<String> redeemFlexible(RedeemFlexibleParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		investmentPositionService.redeemFlexible(param);
		return Result.success();
	}

	@PostMapping("/findMyRedeemRecordByPage")
	public Result<PageResult<MyRedeemRecordVO>> findMyRedeemRecordByPage(InvestmentRedeemRecordQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(investmentInterestLogQueryService.findMyRedeemRecordByPage(param));
	}

	@PostMapping("/findMyHourlyInterestLogByPage")
	public Result<PageResult<MyHourlyInterestRecordVO>> findMyHourlyInterestLogByPage(
			InvestmentHourlyInterestLogQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(investmentInterestLogQueryService.findMyHourlyInterestLogByPage(param));
	}

	@PostMapping("/findMyDailyInterestLogByPage")
	public Result<PageResult<MyDailyInterestRecordVO>> findMyDailyInterestLogByPage(
			InvestmentInterestLogQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(investmentInterestLogQueryService.findMyDailyInterestLogByPage(param));
	}

	@PostMapping("/findMyDailyInterestDetail")
	public Result<MyDailyInterestDetailVO> findMyDailyInterestDetail(String assetCode,
			@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate interestDate, String flexibleAccountId) {
		return Result.success(investmentInterestLogQueryService.findMyDailyInterestDetail(StpUtil.getLoginIdAsString(),
				assetCode, interestDate, flexibleAccountId));
	}

}

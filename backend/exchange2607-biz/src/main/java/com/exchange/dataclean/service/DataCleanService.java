package com.exchange.dataclean.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.exchange.c2c.repo.C2cAppealRecordRepo;
import com.exchange.c2c.repo.C2cChatRecordRepo;
import com.exchange.c2c.repo.C2cOrderRepo;
import com.exchange.c2c.repo.C2cOrderReviewRepo;
import com.exchange.c2c.repo.C2cPreOrderPaymentAccountRepo;
import com.exchange.c2c.repo.C2cPreOrderRepo;
import com.exchange.c2c.repo.C2cRiskRecordRepo;
import com.exchange.contract.repo.PerpetualFundingFeeRepo;
import com.exchange.contract.repo.PerpetualFundingRateHistoryRepo;
import com.exchange.contract.repo.PerpetualOrderRepo;
import com.exchange.contract.repo.PerpetualPlanOrderRepo;
import com.exchange.contract.repo.PerpetualPositionRepo;
import com.exchange.contract.repo.SecondContractOrderRepo;
import com.exchange.dataclean.param.DataCleanParam;
import com.exchange.deposit.repo.DepositOrderRepo;
import com.exchange.investment.repo.InvestmentFixedPositionRepo;
import com.exchange.investment.repo.InvestmentFlexibleAccountRepo;
import com.exchange.investment.repo.InvestmentFlexiblePositionRepo;
import com.exchange.investment.repo.InvestmentHourlyInterestLogRepo;
import com.exchange.investment.repo.InvestmentInterestLogRepo;
import com.exchange.investment.repo.InvestmentRecordRepo;
import com.exchange.investment.repo.InvestmentRedeemRecordRepo;
import com.exchange.log.repo.LoginLogRepo;
import com.exchange.log.repo.MemberAssetChangeLogRepo;
import com.exchange.log.repo.MemberContactBindLogRepo;
import com.exchange.log.repo.OperLogRepo;
import com.exchange.member.repo.AssetTransferOrderRepo;
import com.exchange.member.repo.MemberAuthenticationInfoRepo;
import com.exchange.notification.repo.MemberNotificationRepo;
import com.exchange.rebate.repo.RebateRecordRepo;
import com.exchange.security.repo.AccountLoginSessionRepo;
import com.exchange.spot.repo.ConvertOrderRepo;
import com.exchange.spot.repo.SpotOrderRepo;
import com.exchange.spot.repo.SpotPlanOrderRepo;
import com.exchange.verificationcode.repo.VerificationCodeSendRecordRepo;
import com.exchange.withdraw.repo.WithdrawOrderRepo;

@Validated
@Service
public class DataCleanService {

	@Autowired
	private LoginLogRepo loginLogRepo;

	@Autowired
	private OperLogRepo operLogRepo;

	@Autowired
	private MemberAssetChangeLogRepo memberAssetChangeLogRepo;

	@Autowired
	private C2cPreOrderRepo preTradeOrderRepo;

	@Autowired
	private C2cOrderRepo c2cOrderRepo;

	@Autowired
	private C2cChatRecordRepo c2cChatRecordRepo;

	@Autowired
	private C2cAppealRecordRepo c2cAppealRecordRepo;

	@Autowired
	private C2cRiskRecordRepo tradeRiskRecordRepo;

	@Autowired
	private C2cOrderReviewRepo c2cOrderReviewRepo;

	@Autowired
	private C2cPreOrderPaymentAccountRepo c2cPreOrderPaymentAccountRepo;

	@Autowired
	private MemberAuthenticationInfoRepo memberAuthenticationInfoRepo;

	@Autowired
	private DepositOrderRepo depositOrderRepo;

	@Autowired
	private InvestmentFixedPositionRepo investmentFixedPositionRepo;

	@Autowired
	private InvestmentFlexiblePositionRepo investmentFlexiblePositionRepo;

	@Autowired
	private InvestmentRecordRepo investmentRecordRepo;

	@Autowired
	private InvestmentInterestLogRepo investmentInterestLogRepo;

	@Autowired
	private InvestmentRedeemRecordRepo investmentRedeemRecordRepo;

	@Autowired
	private InvestmentFlexibleAccountRepo investmentFlexibleAccountRepo;

	@Autowired
	private InvestmentHourlyInterestLogRepo investmentHourlyInterestLogRepo;

	@Autowired
	private MemberNotificationRepo memberNotificationRepo;

	@Autowired
	private VerificationCodeSendRecordRepo verificationCodeSendRecordRepo;

	@Autowired
	private WithdrawOrderRepo withdrawOrderRepo;

	// ===== 交易数据 =====
	@Autowired
	private SpotOrderRepo spotOrderRepo;

	@Autowired
	private SpotPlanOrderRepo spotPlanOrderRepo;

	@Autowired
	private ConvertOrderRepo convertOrderRepo;

	@Autowired
	private PerpetualOrderRepo perpetualOrderRepo;

	@Autowired
	private PerpetualPlanOrderRepo perpetualPlanOrderRepo;

	@Autowired
	private PerpetualPositionRepo perpetualPositionRepo;

	@Autowired
	private PerpetualFundingFeeRepo perpetualFundingFeeRepo;

	@Autowired
	private PerpetualFundingRateHistoryRepo perpetualFundingRateHistoryRepo;

	@Autowired
	private SecondContractOrderRepo secondContractOrderRepo;

	// ===== 资金 / 日志 =====
	@Autowired
	private AssetTransferOrderRepo assetTransferOrderRepo;

	@Autowired
	private RebateRecordRepo rebateRecordRepo;

	@Autowired
	private AccountLoginSessionRepo accountLoginSessionRepo;

	@Autowired
	private MemberContactBindLogRepo memberContactBindLogRepo;

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void clean(@Valid DataCleanParam param) {
		List<String> dataTypes = param.getDataTypes();
		LocalDateTime timeStart = param.getTimeStart().atStartOfDay();
		LocalDateTime timeEnd = param.getTimeEnd().atTime(LocalTime.MAX);
		if (dataTypes.contains("loginLog")) {
			loginLogRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("operLog")) {
			operLogRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("memberAssetChangeLog")) {
			memberAssetChangeLogRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("memberAuthenticationInfo")) {
			memberAuthenticationInfoRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("c2cPreOrder")) {
			c2cPreOrderPaymentAccountRepo.dataClean(timeStart, timeEnd);
			preTradeOrderRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("c2cRiskRecord")) {
			tradeRiskRecordRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("c2cOrder")) {
			c2cChatRecordRepo.dataClean(timeStart, timeEnd);
			c2cAppealRecordRepo.dataClean(timeStart, timeEnd);
			c2cOrderReviewRepo.dataClean(timeStart, timeEnd);
			c2cOrderRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("depositOrder")) {
			depositOrderRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("withdrawOrder")) {
			withdrawOrderRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("investmentPosition")) {
			// 理财持仓清理：各表均按自身时间列（invest_time / create_time）限定区间
			investmentInterestLogRepo.dataCleanByPosition(timeStart, timeEnd);
			investmentFixedPositionRepo.dataClean(timeStart, timeEnd);
			investmentFlexiblePositionRepo.dataClean(timeStart, timeEnd);
			investmentRecordRepo.dataClean(timeStart, timeEnd);
			investmentHourlyInterestLogRepo.dataClean(timeStart, timeEnd);
			investmentFlexibleAccountRepo.dataClean(timeStart, timeEnd);
			investmentRedeemRecordRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("investmentInterestLog")) {
			investmentInterestLogRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("memberNotification")) {
			memberNotificationRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("verificationCodeSendRecord")) {
			verificationCodeSendRecordRepo.dataClean(timeStart, timeEnd);
		}
		// ===== 交易数据 =====
		if (dataTypes.contains("spotOrder")) {
			spotOrderRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("spotPlanOrder")) {
			spotPlanOrderRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("convertOrder")) {
			convertOrderRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("perpetualOrder")) {
			perpetualOrderRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("perpetualPlanOrder")) {
			perpetualPlanOrderRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("perpetualPosition")) {
			perpetualPositionRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("perpetualFundingFee")) {
			perpetualFundingFeeRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("perpetualFundingRateHistory")) {
			perpetualFundingRateHistoryRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("secondContractOrder")) {
			secondContractOrderRepo.dataClean(timeStart, timeEnd);
		}
		// ===== 资金 / 日志 =====
		if (dataTypes.contains("assetTransferOrder")) {
			assetTransferOrderRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("rebateRecord")) {
			rebateRecordRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("accountLoginSession")) {
			accountLoginSessionRepo.dataClean(timeStart, timeEnd);
		}
		if (dataTypes.contains("memberContactBindLog")) {
			memberContactBindLogRepo.dataClean(timeStart, timeEnd);
		}
	}

}

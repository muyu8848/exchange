package com.exchange.investment.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.exchange.common.utils.SymbolPriceUtils;
import com.exchange.constants.Constant;
import com.exchange.investment.domain.InvestmentHourlyInterestLog;
import com.exchange.investment.domain.InvestmentInterestLog;
import com.exchange.investment.domain.InvestmentRecord;
import com.exchange.investment.domain.InvestmentRedeemRecord;
import com.exchange.investment.repo.InvestmentHourlyInterestLogRepo;
import com.exchange.investment.repo.InvestmentInterestLogRepo;
import com.exchange.investment.repo.InvestmentRecordRepo;
import com.exchange.investment.repo.InvestmentRedeemRecordRepo;

import lombok.extern.slf4j.Slf4j;

/**
 * 赚币流水 USDT 折算：申购、赎回、日利息、小时利息。
 * <p>
 * 写入时先试算（{@link SymbolPriceUtils#toUsdtAmount}），无行情返回 null 由调用方置为未折算；
 * 定时任务扫描未折算记录批量补偿，与充提的折算机制一致。
 */
@Slf4j
@Service
public class InvestmentUsdtConvertService {

	@Autowired
	private InvestmentRecordRepo investmentRecordRepo;

	@Autowired
	private InvestmentRedeemRecordRepo investmentRedeemRecordRepo;

	@Autowired
	private InvestmentInterestLogRepo investmentInterestLogRepo;

	@Autowired
	private InvestmentHourlyInterestLogRepo investmentHourlyInterestLogRepo;

	/** 定时任务入口：补偿四类未折算流水 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void convertPending() {
		convertPendingSubscribe();
		convertPendingRedeem();
		convertPendingInterest();
		convertPendingHourlyInterest();
	}

	private void convertPendingSubscribe() {
		List<InvestmentRecord> list = investmentRecordRepo
				.findByUsdtConvertStatusOrderByCreateTimeAsc(Constant.折算USDT状态_未折算);
		for (InvestmentRecord record : list) {
			BigDecimal usdtAmount = SymbolPriceUtils.toUsdtAmount(record.getAssetCode(), record.getPrincipal());
			if (usdtAmount == null) {
				continue;
			}
			record.setPrincipalUsdtAmount(usdtAmount);
			record.setUsdtConvertStatus(Constant.折算USDT状态_已折算);
			investmentRecordRepo.save(record);
		}
	}

	private void convertPendingRedeem() {
		List<InvestmentRedeemRecord> list = investmentRedeemRecordRepo
				.findByUsdtConvertStatusOrderByCreateTimeAsc(Constant.折算USDT状态_未折算);
		for (InvestmentRedeemRecord record : list) {
			BigDecimal usdtAmount = SymbolPriceUtils.toUsdtAmount(record.getAssetCode(), record.getRedeemAmount());
			if (usdtAmount == null) {
				continue;
			}
			record.setRedeemUsdtAmount(usdtAmount);
			record.setUsdtConvertStatus(Constant.折算USDT状态_已折算);
			investmentRedeemRecordRepo.save(record);
		}
	}

	private void convertPendingInterest() {
		List<InvestmentInterestLog> list = investmentInterestLogRepo
				.findByUsdtConvertStatusOrderByCreateTimeAsc(Constant.折算USDT状态_未折算);
		for (InvestmentInterestLog record : list) {
			BigDecimal usdtAmount = SymbolPriceUtils.toUsdtAmount(record.getAssetCode(), record.getInterestAmount());
			if (usdtAmount == null) {
				continue;
			}
			record.setInterestUsdtAmount(usdtAmount);
			record.setUsdtConvertStatus(Constant.折算USDT状态_已折算);
			investmentInterestLogRepo.save(record);
		}
	}

	private void convertPendingHourlyInterest() {
		List<InvestmentHourlyInterestLog> list = investmentHourlyInterestLogRepo
				.findByUsdtConvertStatusOrderByCreateTimeAsc(Constant.折算USDT状态_未折算);
		for (InvestmentHourlyInterestLog record : list) {
			BigDecimal usdtAmount = SymbolPriceUtils.toUsdtAmount(record.getAssetCode(), record.getInterestAmount());
			if (usdtAmount == null) {
				continue;
			}
			record.setInterestUsdtAmount(usdtAmount);
			record.setUsdtConvertStatus(Constant.折算USDT状态_已折算);
			investmentHourlyInterestLogRepo.save(record);
		}
	}
}

package com.exchange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exchange.c2c.service.C2cRiskService;
import com.exchange.c2c.service.C2cTradeService;
import com.exchange.constants.Constant;
import com.exchange.contract.service.SecondContractOrderService;
import com.exchange.deposit.service.DepositOrderService;
import com.exchange.investment.service.InvestmentAccrualService;
import com.exchange.ip.service.IpInfoService;
import com.exchange.rebate.service.RebateRecordService;
import com.exchange.notification.service.MemberNotificationBiz;
import com.exchange.runner.ContractKlineMonitor;
import com.exchange.runner.OrderBookMonitor;
import com.exchange.runner.SpotKlineMonitor;
import com.exchange.verificationcode.service.VerificationCodeService;
import com.exchange.withdraw.service.WithdrawOrderService;
import com.zengtengpeng.annotation.MQListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RedisMqListener {

	@Autowired
	private C2cTradeService tradeService;

	@Autowired
	private C2cRiskService tradeRiskService;

	@Autowired
	private MemberNotificationBiz memberNotificationBiz;

	@Autowired
	private DepositOrderService depositOrderService;

	@Autowired
	private WithdrawOrderService withdrawOrderService;

	@Autowired
	private ContractKlineMonitor contractKlineMonitor;

	@Autowired
	private SpotKlineMonitor spotKlineMonitor;

	@Autowired
	private OrderBookMonitor orderBookMonitor;

	@Autowired
	private SecondContractOrderService secondContractOrderService;

	@Autowired
	private VerificationCodeService verificationCodeService;

	@Autowired
	private RebateRecordService rebateRecordService;

	@Autowired
	private IpInfoService ipInfoService;
	
	@Autowired
	private InvestmentAccrualService investmentAccrualService;
	
	@MQListener(name = Constant.定期计息)
	public void fixedAccrual(String topicName, String value) {
		investmentAccrualService.processFixedAccrual(value);
	}
	
	@MQListener(name = Constant.活期计息)
	public void flexibleAccountAccrual(String topicName, String value) {
		investmentAccrualService.processFlexibleAccountAccrual(value);
	}

	@MQListener(name = Constant.发送验证码)
	public void sendSms(String topicName, String value) {
		verificationCodeService.sendVerificationCode(value);
	}

	@MQListener(name = Constant.秒合约平仓)
	public void secondContractClosePosition(String topicName, String value) {
		try {
			secondContractOrderService.closePosition(value);
		} catch (Exception e) {
			log.error("秒合约平仓异常:" + e.getMessage());
		}

	}

	@MQListener(name = Constant.监控新交易对)
	public void monitorNewSymbol(String topicName, String value) {
		contractKlineMonitor.monitorNewSymbol(value);
		orderBookMonitor.monitorNewContractSymbol(value);
	}

	@MQListener(name = Constant.监控新现货交易对)
	public void monitorNewSpotSymbol(String topicName, String value) {
		spotKlineMonitor.monitorNewSymbol(value);
		orderBookMonitor.monitorNewSpotSymbol(value);
	}

	@MQListener(name = Constant.C2C超时未付款)
	public void c2cTimeoutUnPaid(String topicName, String value) {
		tradeService.c2cTimeoutUnPaid(value);
	}

	@MQListener(name = Constant.C2C超时未放行)
	public void c2cTimeoutUnRelease(String topicName, String value) {
		tradeService.c2cTimeoutUnReleaseOrder(value);
	}

	@MQListener(name = Constant.C2C协议取消超时)
	public void c2cProtocolCancelTimeout(String topicName, String value) {
		tradeService.c2cProtocolCancelTimeout(value);
	}

	@MQListener(name = Constant.充币订单超时)
	public void depositOrderTimeout(String topicName, String value) {
		depositOrderService.depositOrderTimeout(value);
	}

	@MQListener(name = Constant.充币金额折算USDT)
	public void depositAmountConverted2Usdt(String topicName, String value) {
		try {
			depositOrderService.depositAmountConverted2Usdt(value);
		} catch (Exception e) {
			log.error("充币金额折算USDT异常:" + e.getMessage());
		}
	}

	@MQListener(name = Constant.提币金额折算USDT)
	public void withdrawAmountConverted2Usdt(String topicName, String value) {
		try {
			withdrawOrderService.withdrawAmountConverted2Usdt(value);
		} catch (Exception e) {
			log.error("提币打款USDT折算异常:" + e.getMessage());
		}
	}

	@MQListener(name = Constant.C2C交易提醒_提醒买家付款)
	public void tipBuyerPayment(String topicName, String value) {
		memberNotificationBiz.tipBuyerPayment(value, true);
	}

	@MQListener(name = Constant.C2C交易提醒_提醒卖家确认收款)
	public void tipSellerBuyerPaid(String topicName, String value) {
		memberNotificationBiz.tipSellerBuyerPaid(value);
	}

	@MQListener(name = Constant.C2C交易提醒_交易完成)
	public void tipTradeCompleted(String topicName, String value) {
		memberNotificationBiz.tipTradeCompleted(value);
	}

	@MQListener(name = Constant.C2C风控原因_使用非本人支付账户付款)
	public void notMySelfPaid(String topicName, String value) {
		tradeRiskService.notMySelfPaid(value);
	}

	@MQListener(name = Constant.C2C风控原因_买家未付款)
	public void buyerUnpaid(String topicName, String value) {
		tradeRiskService.buyerUnPaid(value);
	}

	@MQListener(name = Constant.C2C风控原因_买家取消交易)
	public void buyerCancelTrade(String topicName, String value) {
		tradeRiskService.buyerCancelTrade(value);
	}

	@MQListener(name = Constant.C2C风控原因_超时未付款)
	public void buyerTimeoutUnPaid(String topicName, String value) {
		tradeRiskService.buyerTimeoutUnPaid(value);
	}

	@MQListener(name = Constant.C2C风控原因_买家原因协议取消)
	public void buyerProtocolCancel(String topicName, String value) {
		tradeRiskService.buyerProtocolCancel(value);
	}

	@MQListener(name = Constant.C2C风控原因_卖家原因协议取消)
	public void sellerProtocolCancel(String topicName, String value) {
		tradeRiskService.sellerProtocolCancel(value);
	}

	@MQListener(name = Constant.现货成交返佣)
	public void rebateSpotOrder(String topicName, String value) {
		try {
			rebateRecordService.handleSpotRebate(value);
		} catch (Exception e) {
			log.error("现货成交返佣异常:" + e.getMessage());
		}
	}

	@MQListener(name = Constant.永续合约成交返佣)
	public void rebatePerpOrder(String topicName, String value) {
		try {
			rebateRecordService.handlePerpRebate(value);
		} catch (Exception e) {
			log.error("合约成交返佣异常:" + e.getMessage());
		}
	}

	@MQListener(name = Constant.记录IP信息)
	public void logIpInfo(String topicName, String value) {
		ipInfoService.logIpInfo(value);
	}

}

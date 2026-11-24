package com.exchange.controller.client.c2c;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exchange.c2c.param.BuyToSellAdParam;
import com.exchange.c2c.param.BuyerCancelTradeParam;
import com.exchange.c2c.param.BuyerMarkPaidParam;
import com.exchange.c2c.param.C2cChatRecordQueryCondParam;
import com.exchange.c2c.param.C2cOrderQueryCondParam;
import com.exchange.c2c.param.C2cPreOrderQueryCondParam;
import com.exchange.c2c.param.CancelPreOrderParam;
import com.exchange.c2c.param.ConfirmProtocolCancelParam;
import com.exchange.c2c.param.CreatePreOrderParam;
import com.exchange.c2c.param.InitiateAppealParam;
import com.exchange.c2c.param.InitiatorCancelAppealParam;
import com.exchange.c2c.param.SellToBuyAdParam;
import com.exchange.c2c.param.SellerConfirmTradeCompletedParam;
import com.exchange.c2c.param.SendMsgParam;
import com.exchange.c2c.param.SubmitProtocolCancelParam;
import com.exchange.c2c.param.SubmitReviewParam;
import com.exchange.c2c.param.SwitchPaymentAccountParam;
import com.exchange.c2c.param.UpdatePreOrderPaymentAccountParam;
import com.exchange.c2c.service.C2cAppealService;
import com.exchange.c2c.service.C2cChatService;
import com.exchange.c2c.service.C2cOrderReviewService;
import com.exchange.c2c.service.C2cOrderService;
import com.exchange.c2c.service.C2cPreOrderService;
import com.exchange.c2c.service.C2cSymbolService;
import com.exchange.c2c.service.C2cTradeService;
import com.exchange.c2c.vo.C2cEnabledSymbolVO;
import com.exchange.c2c.vo.C2cOrderReviewVO;
import com.exchange.c2c.vo.MyC2cChatRecordVO;
import com.exchange.c2c.vo.MyC2cOrderDetailVO;
import com.exchange.c2c.vo.MyC2cOrderVO;
import com.exchange.c2c.vo.MyC2cPreOrderVO;
import com.exchange.c2c.vo.TradablePreOrderVO;
import com.exchange.common.utils.FiatRateUtils;
import com.exchange.common.vo.PageResult;
import com.exchange.common.vo.Result;

import cn.dev33.satoken.stp.StpUtil;

@RestController
@RequestMapping("/c2c")
public class C2cController {

	@Autowired
	private C2cPreOrderService preOrderService;

	@Autowired
	private C2cOrderService orderService;

	@Autowired
	private C2cTradeService tradeService;

	@Autowired
	private C2cAppealService appealService;

	@Autowired
	private C2cChatService c2cChatService;

	@Autowired
	private C2cOrderReviewService reviewService;

	@Autowired
	private C2cSymbolService c2cSymbolService;

	@PostMapping("/initiatorCancelAppeal")
	public Result<String> initiatorCancelAppeal(InitiatorCancelAppealParam param) {
		param.setInitiatorId(StpUtil.getLoginIdAsString());
		appealService.initiatorCancelAppeal(param);
		return Result.success();
	}

	@PostMapping("/initiateAppeal")
	public Result<String> initiateAppeal(InitiateAppealParam param) {
		param.setInitiatorId(StpUtil.getLoginIdAsString());
		appealService.initiateAppeal(param);
		return Result.success();
	}

	@PostMapping("/findChatRecord")
	public Result<List<MyC2cChatRecordVO>> findChatRecord(C2cChatRecordQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(c2cChatService.findMyChatRecord(param));
	}

	@PostMapping("/sendMsg")
	public Result<String> sendMsg(SendMsgParam param) {
		param.setSenderId(StpUtil.getLoginIdAsString());
		c2cChatService.sendMsg(param);
		return Result.success();
	}

	@PostMapping("/markRead")
	public Result<String> markRead(String orderId) {
		c2cChatService.markRead(orderId, StpUtil.getLoginIdAsString());
		return Result.success();
	}

	@PostMapping("/unreadCount")
	public Result<Long> unreadCount(String orderId) {
		return Result.success(c2cChatService.unreadCount(orderId, StpUtil.getLoginIdAsString()));
	}

	@PostMapping("/findMyPreOrderById")
	public Result<MyC2cPreOrderVO> findMyPreOrderById(String id) {
		return Result.success(preOrderService.findMyPreOrderById(id));
	}

	@PostMapping("/findMyPreOrderByPage")
	public Result<PageResult<MyC2cPreOrderVO>> findMyPreOrderByPage(C2cPreOrderQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(preOrderService.findMyPreOrderByPage(param));
	}

	@PostMapping("/findMyOrderByPage")
	public Result<PageResult<MyC2cOrderVO>> findMyOrderByPage(C2cOrderQueryCondParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		return Result.success(orderService.findMyOrderByPage(param));
	}

	@PostMapping("/getOrderDetail")
	public Result<MyC2cOrderDetailVO> getOrderDetail(String orderId) {
		return Result.success(orderService.getOrderDetail(orderId, StpUtil.getLoginIdAsString()));
	}

	@PostMapping("/getAllowedAppealTypes")
	public Result<List<String>> getAllowedAppealTypes(String orderId) {
		return Result.success(orderService.getAllowedAppealTypes(orderId, StpUtil.getLoginIdAsString()));
	}

	@PostMapping("/findHallOrderById")
	public Result<TradablePreOrderVO> findHallOrderById(String id) {
		return Result.success(preOrderService.findHallOrderById(id));
	}

	@PostMapping("/findTradablePreOrderByPage")
	public Result<PageResult<TradablePreOrderVO>> findTradablePreOrderByPage(C2cPreOrderQueryCondParam param) {
		return Result.success(preOrderService.findTradablePreOrderByPage(param));
	}

	@PostMapping("/sellerConfirmTradeCompleted")
	public Result<String> sellerConfirmTradeCompleted(SellerConfirmTradeCompletedParam param) {
		param.setSellerId(StpUtil.getLoginIdAsString());
		tradeService.sellerConfirmTradeCompleted(param);
		return Result.success();
	}

	@PostMapping("/buyerMarkPaid")
	public Result<String> buyerMarkPaid(BuyerMarkPaidParam param) {
		param.setBuyerId(StpUtil.getLoginIdAsString());
		tradeService.buyerMarkPaid(param);
		return Result.success();
	}

	@PostMapping("/switchPaymentAccount")
	public Result<String> switchPaymentAccount(SwitchPaymentAccountParam param) {
		param.setBuyerId(StpUtil.getLoginIdAsString());
		tradeService.switchPaymentAccount(param);
		return Result.success();
	}

	@PostMapping("/buyerCancelTrade")
	public Result<String> buyerCancelTrade(BuyerCancelTradeParam param) {
		param.setBuyerId(StpUtil.getLoginIdAsString());
		tradeService.buyerCancelTrade(param);
		return Result.success();
	}

	@PostMapping("/submitBuyerProtocolCancel")
	public Result<String> submitBuyerProtocolCancel(SubmitProtocolCancelParam param) {
		param.setInitiatorId(StpUtil.getLoginIdAsString());
		tradeService.submitBuyerProtocolCancel(param);
		return Result.success();
	}

	@PostMapping("/submitSellerProtocolCancel")
	public Result<String> submitSellerProtocolCancel(SubmitProtocolCancelParam param) {
		param.setInitiatorId(StpUtil.getLoginIdAsString());
		tradeService.submitSellerProtocolCancel(param);
		return Result.success();
	}

	@PostMapping("/sellerResponseProtocolCancel")
	public Result<String> sellerResponseProtocolCancel(ConfirmProtocolCancelParam param) {
		param.setAccountId(StpUtil.getLoginIdAsString());
		tradeService.sellerResponseProtocolCancel(param);
		return Result.success();
	}

	@PostMapping("/buyerResponseProtocolCancel")
	public Result<String> buyerResponseProtocolCancel(ConfirmProtocolCancelParam param) {
		param.setAccountId(StpUtil.getLoginIdAsString());
		tradeService.buyerResponseProtocolCancel(param);
		return Result.success();
	}

	@PostMapping("/buyToSellAd")
	public Result<String> buyToSellAd(BuyToSellAdParam param) {
		param.setBuyerId(StpUtil.getLoginIdAsString());
		return Result.success(tradeService.memberBuyToSellAd(param));
	}

	@PostMapping("/sellToBuyAd")
	public Result<String> sellToBuyAd(SellToBuyAdParam param) {
		param.setSellerId(StpUtil.getLoginIdAsString());
		return Result.success(tradeService.memberSellToBuyAd(param));
	}

	@PostMapping("/cancelPreOrder")
	public Result<String> cancelPreOrder(CancelPreOrderParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		preOrderService.cancelPreOrder(param);
		return Result.success();
	}

	@PostMapping("/pausePreOrder")
	public Result<String> pausePreOrder(CancelPreOrderParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		preOrderService.pausePreOrder(param);
		return Result.success();
	}

	@PostMapping("/resumePreOrder")
	public Result<String> resumePreOrder(CancelPreOrderParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		preOrderService.resumePreOrder(param);
		return Result.success();
	}

	@PostMapping("/createPreOrder")
	public Result<String> createPreOrder(@RequestBody CreatePreOrderParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		preOrderService.createPreOrder(param);
		return Result.success();
	}

	@PostMapping("/updatePaymentAccounts")
	public Result<String> updatePaymentAccounts(@RequestBody UpdatePreOrderPaymentAccountParam param) {
		param.setMemberAccountId(StpUtil.getLoginIdAsString());
		preOrderService.updatePaymentAccounts(param);
		return Result.success();
	}

	@PostMapping("/submitReview")
	public Result<String> submitReview(SubmitReviewParam param) {
		param.setReviewerId(StpUtil.getLoginIdAsString());
		reviewService.submitReview(param);
		return Result.success();
	}

	@PostMapping("/findOrderReviews")
	public Result<List<C2cOrderReviewVO>> findOrderReviews(String orderId) {
		return Result.success(reviewService.findOrderReviews(orderId));
	}

	@PostMapping("/findAllEnabledSymbol")
	public Result<List<C2cEnabledSymbolVO>> findAllEnabledSymbol() {
		return Result.success(c2cSymbolService.findAllEnabledSymbol());
	}

	@PostMapping("/getMarketPrice")
	public Result<BigDecimal> getMarketPrice(String fiatCode, String assetCode) {
		return Result.success(FiatRateUtils.getMarketPrice(fiatCode, assetCode));
	}

}

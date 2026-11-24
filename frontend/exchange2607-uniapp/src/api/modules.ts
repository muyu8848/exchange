import type {
  AssetByAccount,
  AssetByCurrency,
  AssetItem,
  AssetTransferRecord,
  AssetTransferSubmitParam,
  C2cFiatOption,
  C2cOrderDetail,
  C2cOrderItem,
  C2cRiskSetting,
  C2cSetting,
  C2cSymbolOption,
  ChatRecord,
  ContractOrderItem,
  ContractPeriodConfig,
  ContractSymbolItem,
  ConvertOrderItem,
  ConvertQuote,
  ConvertSetting,
  CurrencyAssetDetail,
  CurrencyOption,
  DepositOrder,
  FixedPositionDetail,
  FlexibleProductDetail,
  FundingRateHistoryItem,
  InvestmentEstimate,
  InvestmentOverview,
  InvestmentProduct,
  KlineItem,
  LatestAppInfo,
  LoginLogItem,
  MemberLoginSession,
  MarginLiqPricePreview,
  MemberContractLeverageItem,
  MemberDepositChannel,
  MemberInfo,
  MemberNotification,
  MemberWithdrawChannel,
  MyAuthenticationInfo,
  MerchantProfileVO,
  ModifyOrderItem,
  MyAssetChangeLog,
  MyAssetChangeLogDetail,
  MyClosedPositionItem,
  MyDailyInterestDetail,
  MyDailyInterestRecord,
  MyDirectTeamOverviewVO,
  MyHistoryPerpetualContractOrderItem,
  MyHistoryPlanOrderItem,
  MyHourlyInterestRecord,
  MyInterestRecord,
  MyInvestmentRecord,
  MyPendingPerpetualContractOrderItem,
  MyPendingPlanOrderItem,
  MyRedeemRecord,
  MyTeamMemberVO,
  NoticeAbstract,
  NoticeDetail,
  OrderReview,
  PageResult,
  PaymentAccount,
  PaymentAccountType,
  PerpetualFundingCountdownItem,
  PerpetualOrderDetailItem,
  PerpetualPlanOrderItem,
  PerpetualPositionItem,
  PerpetualSymbolItem,
  PreOrderDetail,
  PreOrderItem,
  RebateRecordItem,
  SpotOrderItem,
  SpotPlanOrderItem,
  SpotSymbolItem,
  TokenInfo,
  TotalValue,
  TradablePreOrder,
  UsingHelpItem,
  WithdrawableAsset,
  WithdrawOrder,
} from './types'
import { apiPost, apiPostJson } from './http'

export const authApi = {
  /** 登录：双渠道 + 双方式（loginType: 1=密码 2=验证码；contactType: 1=邮箱 2=手机号，手机号时带 phoneAreaCode） */
  login: (data: { contactType: string, contact: string, loginType: string, loginPwd?: string, verificationCode?: string, phoneAreaCode?: string, googleVerCode?: string }) =>
    apiPost<TokenInfo>('/login', data),
  /** 注册：双渠道，需先调 sendRegisterVerificationCode 获取验证码 */
  register: (data: { contactType: string, contact: string, phoneAreaCode?: string, verificationCode: string, loginPwd: string, inviteCode?: string }) =>
    apiPost<string>('/register', data),
  /** 发送注册验证码 */
  sendRegisterVerificationCode: (data: { contactType: string, contact: string, phoneAreaCode?: string }) =>
    apiPost<string>('/sendRegisterVerificationCode', data),
  /** 发送找回密码验证码 */
  sendForgetVerificationCode: (data: { contactType: string, contact: string, phoneAreaCode?: string }) =>
    apiPost<string>('/sendForgetVerificationCode', data),
  /** 发送登录验证码 */
  sendLoginVerificationCode: (data: { contactType: string, contact: string, phoneAreaCode?: string }) =>
    apiPost<string>('/sendLoginVerificationCode', data),
  /** 重置密码（找回密码场景） */
  resetPwd: (data: { contactType: string, contact: string, phoneAreaCode?: string, verificationCode: string, newLoginPwd: string }) =>
    apiPost<string>('/resetPwd', data),
  logout: () =>
    apiPost<string>('/logout'),
}

export const memberApi = {
  getMemberInfo: () =>
    apiPost<MemberInfo>('/memberAccount/getMemberInfo'),
  /** 实名认证信息（状态、证件类型、脱敏号码、驳回原因），仅实名页使用 */
  getMyAuthenticationInfo: () =>
    apiPost<MyAuthenticationInfo>('/memberAccount/getMyAuthenticationInfo'),
  submitAuthenticationInfo: (data: Record<string, unknown>) =>
    apiPost<string>('/memberAccount/submitAuthenticationInfo', data),
  updateLoginPwd: (data: Record<string, unknown>) =>
    apiPost<string>('/memberAccount/updateLoginPwd', data),
  updateFundPwd: (data: Record<string, unknown>) =>
    apiPost<string>('/memberAccount/updateFundPwd', data),
  sendSetFundPwdCode: (data: { contactType: string }) =>
    apiPost<string>('/memberAccount/sendSetFundPwdCode', data),
  setFundPwd: (data: { newPwd: string, contactType: string, verifyCode: string }) =>
    apiPost<string>('/memberAccount/setFundPwd', data),
  generateGoogleSecretKey: () =>
    apiPost<string>('/memberAccount/generateGoogleSecretKey'),
  bindGoogleAuth: (data: Record<string, unknown>) =>
    apiPost<string>('/memberAccount/bindGoogleAuth', data),
  updateNickName: (nickName: string) =>
    apiPost<string>('/memberAccount/updateNickName', { nickName }),
  sendBindVerificationCode: (data: { contactType: string, contact: string, phoneAreaCode?: string }) =>
    apiPost<string>('/memberAccount/sendBindVerificationCode', data),
  bindContact: (data: { contactType: string, contact: string, phoneAreaCode?: string, verificationCode: string }) =>
    apiPost<string>('/memberAccount/bindContact', data),
  sendRebindVerificationCode: (data: { contactType: string, contact: string, phoneAreaCode?: string }) =>
    apiPost<string>('/memberAccount/sendRebindVerificationCode', data),
  changeBindContact: (data: { contactType: string, contact: string, phoneAreaCode?: string, verificationCode: string, oldVerificationCode: string }) =>
    apiPost<string>('/memberAccount/changeBindContact', data),
  findLoginLogByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<LoginLogItem>>('/memberAccount/findLoginLogByPage', params),
  updateNotifyLangCode: (notifyLangCode: string) =>
    apiPost<string>('/memberAccount/updateNotifyLangCode', { notifyLangCode }),
  findMyLoginSession: () =>
    apiPost<MemberLoginSession[]>('/memberAccount/findMyLoginSession'),
  kickoutLoginSession: (id: string) =>
    apiPost<string>('/memberAccount/kickoutLoginSession', { id }),
  kickoutOtherLoginSession: () =>
    apiPost<string>('/memberAccount/kickoutOtherLoginSession'),
}

export const memberNotificationApi = {
  findByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<MemberNotification>>('/memberNotification/findByPage', params),
  findPendingPopup: () =>
    apiPost<MemberNotification | null>('/memberNotification/findPendingPopup'),
  countUnread: () =>
    apiPost<number>('/memberNotification/countUnread'),
  markRead: (id: string) =>
    apiPost<string>('/memberNotification/markRead', { id }),
  markAllRead: () =>
    apiPost<string>('/memberNotification/markAllRead'),
}

export const settingApi = {
  getCustomerServiceUrl: () =>
    apiPost<string>('/setting/getCustomerServiceUrl'),
  getH5Url: () =>
    apiPost<string>('/setting/getH5Url'),
  getLatestAppInfo: () =>
    apiPost<LatestAppInfo>('/setting/getLatestAppInfo'),
  getC2cSetting: () =>
    apiPost<C2cSetting>('/setting/getC2cSetting'),
  getC2cRiskSetting: () =>
    apiPost<C2cRiskSetting>('/setting/getC2cRiskSetting'),
}

/** 客户端公共元数据（免登录）：启动预取并全局缓存 */
export const commonApi = {
  /** 全部未删除的法币列表（含禁用态） */
  findAllC2cFiat: () =>
    apiPost<C2cFiatOption[]>('/common/findAllC2cFiat'),
  /** 全部未删除的币种列表 */
  findAllCurrency: () =>
    apiPost<CurrencyOption[]>('/common/findAllCurrency'),
}

export const c2cApi = {
  /** 启用的交易对精简列表：页面打开时实时获取 */
  findAllEnabledSymbol: () =>
    apiPost<C2cSymbolOption[]>('/c2c/findAllEnabledSymbol'),
  getMarketPrice: (fiatCode: string, assetCode: string) =>
    apiPost<number>('/c2c/getMarketPrice', { fiatCode, assetCode }),
  findTradablePreOrderByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<TradablePreOrder>>('/c2c/findTradablePreOrderByPage', params),
  findHallOrderById: (id: string) =>
    apiPost<TradablePreOrder>('/c2c/findHallOrderById', { id }),
  buyToSellAd: (data: Record<string, unknown>) =>
    apiPost<string>('/c2c/buyToSellAd', data),
  sellToBuyAd: (data: Record<string, unknown>) =>
    apiPost<string>('/c2c/sellToBuyAd', data),
  createPreOrder: (data: Record<string, unknown>) =>
    apiPostJson<string>('/c2c/createPreOrder', data),
  findMyOrderByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<C2cOrderItem>>('/c2c/findMyOrderByPage', params),
  findMyPreOrderByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<PreOrderItem>>('/c2c/findMyPreOrderByPage', params),
  getOrderDetail: (orderId: string) =>
    apiPost<C2cOrderDetail>('/c2c/getOrderDetail', { orderId }),
  getAllowedAppealTypes: (orderId: string) =>
    apiPost<string[]>('/c2c/getAllowedAppealTypes', { orderId }),
  findMyPreOrderById: (id: string) =>
    apiPost<PreOrderDetail>('/c2c/findMyPreOrderById', { id }),
  cancelPreOrder: (orderId: string) =>
    apiPost<string>('/c2c/cancelPreOrder', { orderId }),
  pausePreOrder: (orderId: string) =>
    apiPost<string>('/c2c/pausePreOrder', { orderId }),
  resumePreOrder: (orderId: string) =>
    apiPost<string>('/c2c/resumePreOrder', { orderId }),
  buyerMarkPaid: (data: Record<string, unknown>) =>
    apiPost<string>('/c2c/buyerMarkPaid', data),
  buyerCancelTrade: (data: Record<string, unknown>) =>
    apiPost<string>('/c2c/buyerCancelTrade', data),
  sellerConfirmTradeCompleted: (data: Record<string, unknown>) =>
    apiPost<string>('/c2c/sellerConfirmTradeCompleted', data),
  initiateAppeal: (data: Record<string, unknown>) =>
    apiPost<string>('/c2c/initiateAppeal', data),
  initiatorCancelAppeal: (orderId: string) =>
    apiPost<string>('/c2c/initiatorCancelAppeal', { orderId }),
  findChatRecord: (params: Record<string, unknown>) =>
    apiPost<ChatRecord[]>('/c2c/findChatRecord', params),
  sendMsg: (data: Record<string, unknown>) =>
    apiPost<string>('/c2c/sendMsg', data),
  markRead: (orderId: string) =>
    apiPost<string>('/c2c/markRead', { orderId }),
  unreadCount: (orderId: string) =>
    apiPost<number>('/c2c/unreadCount', { orderId }),
  submitBuyerProtocolCancel: (data: Record<string, unknown>) =>
    apiPost<string>('/c2c/submitBuyerProtocolCancel', data),
  sellerResponseProtocolCancel: (data: Record<string, unknown>) =>
    apiPost<string>('/c2c/sellerResponseProtocolCancel', data),
  submitSellerProtocolCancel: (data: Record<string, unknown>) =>
    apiPost<string>('/c2c/submitSellerProtocolCancel', data),
  buyerResponseProtocolCancel: (data: Record<string, unknown>) =>
    apiPost<string>('/c2c/buyerResponseProtocolCancel', data),
  switchPaymentAccount: (data: Record<string, unknown>) =>
    apiPost<string>('/c2c/switchPaymentAccount', data),
  submitReview: (data: Record<string, unknown>) =>
    apiPost<string>('/c2c/submitReview', data),
  findOrderReviews: (orderId: string) =>
    apiPost<OrderReview[]>('/c2c/findOrderReviews', { orderId }),
  /** 商家主页信息 */
  getMerchantProfile: (userId: string) =>
    apiPost<MerchantProfileVO>('/merchant/profile', { userId }),
}

export const paymentAccountApi = {
  findAll: (params?: Record<string, unknown>) =>
    apiPost<PaymentAccount[]>('/paymentAccount/findAll', params),
  findById: (id: string) =>
    apiPost<PaymentAccount>('/paymentAccount/findById', { id }),
  /** JSON body：{ paymentAccountTypeCode, fieldValues } */
  add: (data: Record<string, unknown>) =>
    apiPostJson<string>('/paymentAccount/add', data),
  /** JSON body：{ id, paymentAccountTypeCode, fieldValues } */
  update: (data: Record<string, unknown>) =>
    apiPostJson<string>('/paymentAccount/update', data),
  del: (id: string) =>
    apiPost<string>('/paymentAccount/del', { id }),
  enable: (id: string) =>
    apiPost<string>('/paymentAccount/enable', { id }),
  disable: (id: string) =>
    apiPost<string>('/paymentAccount/disable', { id }),
}

export const paymentAccountTypeApi = {
  findAllEnabled: () =>
    apiPost<PaymentAccountType[]>('/paymentAccountType/findAllEnabled'),
  findByCode: (code: string) =>
    apiPost<PaymentAccountType>('/paymentAccountType/findByCode', { code }),
}

export const dictApi = {
  findDictItemInCache: (dictTypeCode: string) =>
    apiPost<Array<{ dictItemCode: string, dictItemName: string }>>('/dictconfig/findDictItemInCache', { dictTypeCode }),
}

export const balanceLogApi = {
  findByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<MyAssetChangeLog>>('/memberAssetChangeLog/findByPage', params),
  getDetail: (id: string) =>
    apiPost<MyAssetChangeLogDetail>('/memberAssetChangeLog/getDetail', { id }),
}

export const noticeApi = {
  findNoticeByPage: (params: { pageNum: number, pageSize: number }) =>
    apiPost<PageResult<NoticeAbstract>>('/notice/findNoticeByPage', params),
  getNoticeDetail: (id: string) =>
    apiPost<NoticeDetail>('/notice/getNoticeDetail', { id }),
}

export const usingHelpApi = {
  findUsingHelpByPage: (params: { pageNum: number, pageSize: number }) =>
    apiPost<PageResult<UsingHelpAbstract>>('/usingHelp/findUsingHelpByPage', params),
  getUsingHelpDetail: (id: string) =>
    apiPost<UsingHelpItem>('/usingHelp/getUsingHelpDetail', { id }),
}

export const memberAssetApi = {
  findByCurrencySummary: () =>
    apiPost<AssetByCurrency[]>('/memberAsset/findByCurrencySummary'),
  findByAccountTypeSummary: () =>
    apiPost<AssetByAccount[]>('/memberAsset/findByAccountTypeSummary'),
  /** fiatCode：折算展示货币（如 CNY/USD），不传由后端默认 CNY */
  getTotalValue: (fiatCode?: string) =>
    apiPost<TotalValue>('/memberAsset/getTotalValue', { fiatCode }),
  findDetailByCurrency: (assetCode: string) =>
    apiPost<CurrencyAssetDetail>('/memberAsset/findDetailByCurrency', { assetCode }),
  findSpotAssetsByCodes: (assetCodes: string) =>
    apiPost<AssetItem[]>('/memberAsset/findSpotAssetsByCodes', { assetCodes }),
  getAvailableBalance: (accountType: string, assetCode: string) =>
    apiPost<number>('/memberAsset/getAvailableBalance', { accountType, assetCode }),
}

export const depositApi = {
  findEnabledChannels: () =>
    apiPost<MemberDepositChannel[]>('/deposit/findEnabledChannels'),
  findEnabledChannelById: (id: string) =>
    apiPost<MemberDepositChannel>('/deposit/findEnabledChannelById', { id }),
  createOrder: (channelId: string, amount: number) =>
    apiPost<string>('/deposit/createOrder', { channelId, amount }),
  findOrderById: (id: string) =>
    apiPost<DepositOrder>('/deposit/findOrderById', { id }),
  submitTxHash: (orderId: string, txHash: string) =>
    apiPost<string>('/deposit/submitTxHash', { orderId, txHash }),
  findByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<DepositOrder>>('/deposit/findByPage', params),
}

export const rebateRecordApi = {
  findByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<RebateRecordItem>>('/rebate/findMyRebateRecordByPage', params),
  getRebateRate: () =>
    apiPost<number>('/rebate/getRebateRate'),
}

export const teamApi = {
  getDirectOverview: () =>
    apiPost<MyDirectTeamOverviewVO>('/team/findMyDirectOverview'),
  getDirectMembers: (params: Record<string, unknown>) =>
    apiPost<PageResult<MyTeamMemberVO>>('/team/findMyTeamDirectMemberByPage', params),
}

export const investmentApi = {
  findEnabledProducts: (assetCode?: string) =>
    apiPost<InvestmentProduct[]>('/investment/findEnabledProducts', { assetCode }),
  estimate: (productId: string, amount: number) =>
    apiPost<InvestmentEstimate>('/investment/estimate', { productId, amount }),
  subscribe: (productId: string, amount: number) =>
    apiPost<string>('/investment/subscribe', { productId, amount }),
  findMyInvestmentRecordByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<MyInvestmentRecord>>('/investment/findMyInvestmentRecordByPage', params),
  findMyInterestRecordByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<MyInterestRecord>>('/investment/findMyInterestRecordByPage', params),
  findOverview: () =>
    apiPost<InvestmentOverview>('/investment/findOverview'),
  findFlexibleProductDetail: (params: { assetCode?: string; flexibleAccountId?: string }) =>
    apiPost<FlexibleProductDetail>('/investment/findFlexibleProductDetail', params),
  findFixedPositionDetail: (positionId: string) =>
    apiPost<FixedPositionDetail>('/investment/findFixedPositionDetail', { positionId }),
  redeemFlexible: (assetCode: string, amount: number) =>
    apiPost<string>('/investment/redeemFlexible', { assetCode, amount }),
  findMyRedeemRecordByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<MyRedeemRecord>>('/investment/findMyRedeemRecordByPage', params),
  findMyHourlyInterestLogByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<MyHourlyInterestRecord>>('/investment/findMyHourlyInterestLogByPage', params),
  findMyDailyInterestLogByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<MyDailyInterestRecord>>('/investment/findMyDailyInterestLogByPage', params),
  findMyDailyInterestDetail: (params: { assetCode?: string; interestDate: string; flexibleAccountId?: string }) =>
    apiPost<MyDailyInterestDetail>('/investment/findMyDailyInterestDetail', params),
}

export const assetTransferApi = {
  transfer: (data: AssetTransferSubmitParam) =>
    apiPost<string>('/assetTransfer/transfer', { ...data }),
  findByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<AssetTransferRecord>>('/assetTransfer/findByPage', params),

}

export const withdrawApi = {
  findWithdrawableAssets: () =>
    apiPost<WithdrawableAsset[]>('/withdraw/findWithdrawableAssets'),
  findEnabledChannels: (assetCode: string) =>
    apiPost<MemberWithdrawChannel[]>('/withdraw/findEnabledChannels', { assetCode }),
  createOrder: (data: {
    channelId: string
    withdrawalAddress: string
    amount: number
    contactType: string
    verifyCode: string
  }) =>
    apiPost<string>('/withdraw/createOrder', data),
  sendVerificationCode: (contactType?: string) =>
    apiPost<string>('/withdraw/sendVerificationCode', { contactType }),
  findOrderById: (id: string) =>
    apiPost<WithdrawOrder>('/withdraw/findOrderById', { id }),
  findByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<WithdrawOrder>>('/withdraw/findByPage', params),
  cancelOrder: (orderId: string) =>
    apiPost<string>('/withdraw/cancelOrder', { orderId }),
}

export const spotApi = {
  trade: (data: {
    symbolCode: string
    side: string
    amount: number
    entrustType?: string
    price?: number
  }) =>
    apiPost<string>('/spotOrder/trade', data),
  cancelOrder: (orderId: string) =>
    apiPost<string>('/spotOrder/cancel', { orderId }),
  findMyOrderByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<SpotOrderItem>>('/spotOrder/findMyOrderByPage', params),
  findMyOrderById: (id: string) =>
    apiPost<SpotOrderItem>('/spotOrder/findMyOrderById', { id }),
}

export const spotPlanOrderApi = {
  trade: (data: {
    symbolCode: string
    side: string
    amount: number
    triggerPrice: number
    execType: string
    price?: number
  }) =>
    apiPost<string>('/spotPlanOrder/trade', data),
  cancel: (orderId: string) =>
    apiPost<string>('/spotPlanOrder/cancel', { orderId }),
  findMyOrderByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<SpotPlanOrderItem>>('/spotPlanOrder/findMyOrderByPage', params),
}

export const spotSymbolApi = {
  findEnableSymbol: () =>
    apiPost<SpotSymbolItem[]>('/spotSymbol/findEnableSymbol'),
  findSymbolInfo: (symbolCode: string) =>
    apiPost<SpotSymbolItem>('/spotSymbol/findSymbolInfo', { symbolCode }),
}

export const klineApi = {
  findKline: (params: {
    symbol: string
    period: string
    limit: number
    enddate?: string
    endtime?: string
    bizType?: string
  }) =>
    apiPost<KlineItem[]>('/kline/findKline', params),
  findOrderBook: (symbolCode: string, bizType: string) =>
    apiPost<string>('/kline/findOrderBook', { symbolCode, bizType }),
}

export const contractApi = {
  findMyOrderByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<ContractOrderItem>>('/secondContractOrder/findMyOrderByPage', params),
  findMyOrderById: (id: string) =>
    apiPost<ContractOrderItem>('/secondContractOrder/findMyOrderById', { id }),
  openPosition: (data: {
    symbolCode: string
    direction: string
    period: number
    amount: number
  }) =>
    apiPost<string>('/secondContractOrder/openPosition', data),
}

export const contractSymbolApi = {
  findEnableSymbol: () =>
    apiPost<ContractSymbolItem[]>('/secondContractSymbol/findEnableSymbol'),
  findSymbolInfo: (symbolCode: string) =>
    apiPost<ContractSymbolItem>('/secondContractSymbol/findSymbolInfo', { symbolCode }),
  findPeriodConfig: (symbolCode: string) =>
    apiPost<ContractPeriodConfig[]>('/secondContractSymbol/findPeriodConfig', { symbolCode }),
}

export const perpetualApi = {
  findEnableSymbols: () =>
    apiPost<PerpetualSymbolItem[]>('/perpetualContractSymbol/findEnable'),
  findSymbolInfo: (symbolCode: string) =>
    apiPost<PerpetualSymbolItem>('/perpetualContractSymbol/findSymbolInfo', { symbolCode }),
  openPosition: (data: { symbolCode: string, direction: string, leverage: number, margin: number, entrustType?: string, price?: number }) =>
    apiPost<string>('/perpetualContract/openPosition', data),
  marketCloseAll: (positionId: string) =>
    apiPost<string>('/perpetualContract/marketCloseAll', { positionId }),
  marketClosePart: (positionId: string, size: number) =>
    apiPost<string>('/perpetualContract/marketClosePart', { positionId, size }),
  placeCloseLimitOrder: (positionId: string, price: number, size?: number) =>
    apiPost<string>('/perpetualContract/placeCloseLimitOrder', { positionId, price, size }),
  reversePosition: (positionId: string) =>
    apiPost<string>('/perpetualContract/reversePosition', { positionId }),
  adjustMargin: (params: { positionId: string, type: 'ADD' | 'REDUCE', amount: string }) =>
    apiPost<string>('/perpetualContract/adjustMargin', params),
  /** 调整保证金预览：返回调整后参考强平价与保证金率（只读） */
  previewMarginLiqPrice: (params: { positionId: string, newMargin: string }) =>
    apiPost<MarginLiqPricePreview>('/perpetualContract/previewMarginLiqPrice', params),
  /** 资金费率历史（按交易对分页） */
  findFundingRateHistory: (params: { symbolCode: string, pageNum: number, pageSize: number }) =>
    apiPost<PageResult<FundingRateHistoryItem>>('/perpetualContract/findFundingRateHistory', params),
  cancelOrder: (orderId: string) =>
    apiPost<string>('/perpetualContract/cancelOrder', { orderId }),
  changeOrder: (data: { orderId: string, price: number, size: number }) =>
    apiPost<string>('/perpetualContract/changeOrder', data),
  planTrade: (data: { planType: string, symbolCode?: string, direction?: string, leverage?: number, margin?: number, positionId?: string, triggerPrice: number, execType: string, price?: number }) =>
    apiPost<string>('/perpetualPlanOrder/trade', data),
  cancelPlanOrder: (orderId: string) =>
    apiPost<string>('/perpetualPlanOrder/cancel', { orderId }),
  findMyPlanOrders: (params: Record<string, unknown>) =>
    apiPost<PageResult<PerpetualPlanOrderItem>>('/perpetualPlanOrder/findMyPlanOrderByPage', params),
  findMyPositions: (params: Record<string, unknown>) =>
    apiPost<PerpetualPositionItem[]>('/perpetualContract/findMyPositions', params),
  findMyClosedPositionByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<MyClosedPositionItem>>('/perpetualContract/findMyClosedPositionByPage', params),
  findMyPendingOrder: (params: Record<string, unknown>) =>
    apiPost<MyPendingPerpetualContractOrderItem[]>('/perpetualContract/findMyPendingOrder', params),
  findMyPendingPlanOrder: (params: Record<string, unknown>) =>
    apiPost<MyPendingPlanOrderItem[]>('/perpetualPlanOrder/findMyPendingPlanOrder', params),
  findMyHistoryOrderByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<MyHistoryPerpetualContractOrderItem>>('/perpetualContract/findMyHistoryOrderByPage', params),
  findMyOrderById: (id: string) =>
    apiPost<PerpetualOrderDetailItem>('/perpetualContract/findMyOrderById', { id }),
  findPerpetualLimitModifyOrderById: (id: string) =>
    apiPost<ModifyOrderItem>('/perpetualContract/findPerpetualLimitModifyOrderById', { id }),
  findMyHistoryPlanOrderByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<MyHistoryPlanOrderItem>>('/perpetualPlanOrder/findMyHistoryPlanOrderByPage', params),
  findFundingCountdown: (symbolCode: string) =>
    apiPost<PerpetualFundingCountdownItem>('/perpetualContractSymbol/findFundingCountdown', { symbolCode }),
  findMyLeverage: (symbolCode: string) =>
    apiPost<MemberContractLeverageItem>('/perpetualContractSymbol/findMyLeverage', { symbolCode }),
  saveLeverage: (data: { symbolCode: string, direction: string, longLeverage: number, shortLeverage: number }) =>
    apiPost<void>('/perpetualContractSymbol/saveLeverage', data),
  setPositionTpsl: (data: { positionId: string, takeProfitPrice?: number, stopLossPrice?: number }) =>
    apiPost<string>('/perpetualPlanOrder/setPositionTpsl', data),
}

export const convertApi = {
  getQuote: (fromAssetCode: string, toAssetCode: string, fromAmount?: number) =>
    apiPost<ConvertQuote>('/convertOrder/getQuote', { fromAssetCode, toAssetCode, fromAmount }),
  convert: (data: { fromAssetCode: string, toAssetCode: string, fromAmount: number }) =>
    apiPost<string>('/convertOrder/convert', data),
  findByPage: (params: Record<string, unknown>) =>
    apiPost<PageResult<ConvertOrderItem>>('/convertOrder/findByPage', params),
  getSetting: () =>
    apiPost<ConvertSetting>('/convertOrder/getSetting'),
  findCurrencies: () =>
    apiPost<CurrencyOption[]>('/convertOrder/findCurrencies'),
}

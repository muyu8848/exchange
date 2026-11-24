export interface ApiResult<T = unknown> {
  code: number
  msg?: string
  data?: T
  total?: number
  more?: boolean
}

export interface PageResult<T> {
  content: T[]
  totalPage: number
  total: number
}

export interface CountryAreaCode {
  areaCode: string
  countryName: string
  countryNameEn?: string
  flagImage?: string
}

export interface TokenInfo {
  tokenName: string
  tokenValue: string
  accountId: string
  accountType?: string
}

export interface MemberInfo {
  uid?: number
  nickName?: string
  /** 注册方式：phone / email */
  contactType?: string
  realName?: string
  latelyLoginTime?: string
  balance?: number
  sellBalance?: number
  forbidSellBalance?: number
  walletAddr?: string
  authenticationStatus?: string
  authenticationStatusName?: string
  createTime?: string
  googleAuthBindTime?: string
  /** 手机号 */
  phone?: string
  /** 手机区号，如 86/1 */
  phoneAreaCode?: string
  /** 邮箱 */
  email?: string
  /** 是否已设置资金密码 */
  hasFundPwdFlag?: boolean
  mashangFlag?: boolean
  inviteCode?: string
  /** 通知语言 */
  notifyLangCode?: string
  notifyLangName?: string
}

/** 实名认证信息（仅实名页使用，与账号基本信息分离） */
export interface MyAuthenticationInfo {
  /** 认证状态：1 未认证 / 2 审核中 / 3 已认证 / 4 审核不通过 */
  status?: string
  statusName?: string
  /** 证件类型：idCard / passport / driverLicense */
  idType?: string
  idTypeName?: string
  /** 脱敏后的证件号码，如 11********34 */
  idNumberMasked?: string
  realName?: string
  /** 证件有效期 yyyy-MM-dd */
  expiryDate?: string
  /** 提交时间 */
  createTime?: string
  /** 认证通过时间 */
  passedTime?: string
  /** 最近一次驳回原因 */
  failReason?: string
  /** 最近一次驳回时间 */
  failTime?: string
}

export interface AssetItem {
  id: string
  memberAccountId?: string
  accountType: string
  accountTypeName?: string
  assetCode: string
  available: number
  frozen: number
  total?: number
  fiatValue?: number
  usdtValue?: number
}

export interface AssetByCurrency {
  currencyCode: string
  currencyName?: string
  currencyLogo?: string
  available: number
  frozen: number
  total?: number
  usdtValue?: number
  fiatValue?: number
}

export interface AssetByAccount {
  accountType: string
  accountTypeName?: string
  usdtValue: number
  fiatValue?: number
}

/** /memberAsset/getTotalValue 返回：totalUsdt 为基准，totalFiat 按请求法币折算 */
export interface TotalValue {
  totalUsdt: number
  /** 折算后的法币金额；该法币汇率暂不可用时为 null */
  totalFiat: number | null
  /** 实际使用的法币代码（后端会在入参非法时回落 CNY） */
  fiatCode: string
  /** 该法币兑 USDT 的汇率；不可用时为 null */
  fiatRate?: number | null
}

export interface AccountDistribution {
  accountType: string
  accountTypeName?: string
  total: number
  usdtValue?: number
  percentage: number
}

export interface CurrencyAssetDetail {
  assetCode: string
  totalBalance: number
  usdtValue?: number
  accounts: AccountDistribution[]
}

export interface TradablePreOrder {
  id: string
  availableAmount: number
  assetCode: string
  nickName?: string
  supportedPaymentAccountTypes?: SupportedPaymentAccountType[]
  singleMinFiatAmount?: number
  singleMaxFiatAmount?: number
  price?: number
  priceScale?: number
  amountScale?: number
  priceType?: string
  premiumRate?: number
  orderType?: string
  tradeDeadline?: number
  fiatCode?: string
  fiatAmount?: number
  remark?: string
  memberAccountId?: string
}

/** /common/findAllC2cFiat 返回的法币选项（全部未删除，含禁用态；客户端启动预取缓存） */
export interface C2cFiatOption {
  fiatCode: string
  fiatName?: string
  fiatSymbol?: string
  logo?: string
  /** 法币金额精度（小数位数，未配置默认 2） */
  fiatScale?: number
  sortNum?: number
  /** 启用状态：'1' 启用 / '0' 停用 */
  status?: string
  /** 该法币兑 USDT 的汇率；暂不可用为 null */
  fiatRate?: number | null
}

/** /c2c/findAllEnabledSymbol 返回的启用交易对精简选项（页面打开时实时获取） */
export interface C2cSymbolOption {
  id: string
  symbolCode?: string
  fiatCode: string
  currencyCode: string
  fiatName?: string
  currencyName?: string
  floatAllowed?: boolean
  minBuyFiatAmount?: number
  minSellFiatAmount?: number
  maxQuantity?: number
  priceScale?: number
  /** 数量精度（小数位数，未配置默认 2） */
  amountScale?: number
  /** 发布广告价格偏离幅度（百分数），如 20 表示固定价限制在市场价 ±20%、浮动溢价率限制在 ±20% 内 */
  priceDeviationPercent?: number
}

export interface OrderReview {
  id: string
  orderId: string
  reviewerId: string
  reviewerAccount?: string
  revieweeId: string
  revieweeAccount?: string
  orderType?: string
  orderTypeName?: string
  transactionSpeed: number
  serviceAttitude: number
  priceReasonable: number
  fundSafety: number
  content?: string
  createTime: string
}

/** 商家主页统计数据 */
export interface MerchantStatsVO {
  /** 30日成单量 */
  orderCount30d: number
  /** 30日完成率（如 98 表示 98%） */
  completionRate30d: number
  /** 总成单量 */
  totalOrderCount: number
  /** 总完成率 */
  completionRateTotal: number
  /** 总交易用户数 */
  totalTradeUsers: number
  /** 30日内买完成率 */
  completionRateBuy: number
  /** 30日内卖完成率 */
  completionRateSell: number
  /** 30日平均放行时间（分钟，保留2位小数） */
  avgReleaseTime30d: number
  /** 30日平均付款时间（分钟，保留2位小数） */
  avgPaymentTime30d: number
}

/** 商家主页信息 */
export interface MerchantProfileVO {
  userId: string
  nickname: string
  registerTime: string
  verifiedEmail: boolean
  verifiedPhone: boolean
  verifiedIdentity: boolean
  verifiedAdvanced: boolean
  stats: MerchantStatsVO
}

export interface C2cOrderItem {
  id: string
  sellerId: string
  buyerId: string
  /** 数量 */
  amount: number
  /** 数字货币币种（如 USDT、BTC） */
  assetCode?: string
  status?: string
  statusName: string
  /** @deprecated 支付方式编码，新列表不再展示 */
  channelCode?: string
  /** @deprecated 支付方式名称 */
  channelName?: string
  createTime: string
  buyerRealName?: string
  payerRealName?: string
  sellerRealName?: string
  /** 单价（法币） */
  price?: number
  /** 法币币种（如 USD、CNY） */
  fiatCode?: string
  /** 交易总额（法币） */
  fiatAmount?: number
}

export interface PreOrderItem {
  id: string
  amount: number
  assetCode?: string
  availableAmount?: number
  completedAmount?: number
  tradingAmount?: number
  singleMinFiatAmount?: number
  singleMaxFiatAmount?: number
  status?: string
  statusName: string
  createTime: string
  supportedPaymentAccountTypes?: SupportedPaymentAccountType[]
  price?: number
  priceType?: string
  premiumRate?: number
  orderType?: string
  orderTypeName?: string
  fiatCode?: string
  fiatAmount?: number
  remark?: string
}

export interface LatestAppInfo {
  appDownloadUrl?: string
  appVersion?: number
}

/** /common/findAllCurrency 返回的币种选项（全部未删除；客户端启动预取缓存） */
export interface CurrencyOption {
  currencyCode?: string
  currencyName?: string
  currencyLogo?: string
  scale?: number
  sortNum?: number
}

export interface C2cSetting {
  tradeDeadlineMin?: number
  tradeDeadlineMax?: number
  auditReceiptInfoFun?: boolean
  maxActivePreOrders?: number
  perOrderMaxPaymentAccounts?: number
  appealIntervalMinutes?: number
}

export interface PaymentAccountFormField {
  key: string
  label: string
  /** text | textarea | number | image */
  type: string
  required?: boolean
  placeholder?: string
  maxLength?: number
  searchable?: boolean
  /** 主要字段：true 时在客户端收款方式列表等场景展示（image 以小图展示） */
  keyField?: boolean
}

export interface PaymentAccountType {
  code: string
  name: string
  logo?: string
  sortNum?: number
  formFields?: PaymentAccountFormField[]
}

export interface SupportedPaymentAccountType {
  code: string
  name: string
  logo?: string
}

export interface PaymentAccountFieldLine {
  /** 模板字段 key（表单回填与提交使用） */
  key: string
  label: string
  value: string
  /** text / textarea / image / number */
  type?: string
  /** 主要字段：true 时客户端收款方式列表展示 */
  keyField?: boolean
}

export interface PaymentAccount {
  id: string
  paymentAccountTypeCode: string
  paymentAccountTypeName?: string
  /** 类型 logo（直出） */
  paymentAccountTypeLogo?: string
  /** 展示字段行：后端按类型模板合成，客户端据 keyField / type 渲染、key / value 回填 */
  fieldLines?: PaymentAccountFieldLine[]
  realName?: string
  status?: string
  statusName?: string
  todayReceivedAmount?: number
  todayReceivedCount?: number
  createTime?: string
}

export interface ReceiptPaymentFormField {
  key: string
  label: string
  type: string
  required?: boolean
  placeholder?: string
}

/** C2C 风控设置 */
export interface C2cRiskSetting {}

export interface C2cOrderDetail {
  id: string
  orderNo: string
  amount: number
  status: string
  statusName: string
  paymentAccountTypeCode?: string
  paymentAccountTypeName?: string
  paymentAccount?: PaymentAccount
  /** 广告单支持的支付方式列表（用于买家切换支付方式），对应后端 SupportedPaymentAccountTypeVO */
  supportedPaymentTypes?: SupportedPaymentAccountType[]
  createTime?: string
  tradeDeadline?: string
  tradeRemainingSecondCount?: number
  assetCode?: string
  price?: number
  fiatCode?: string
  fiatAmount?: number
  sellerId?: string
  sellerNickName?: string
  buyerId?: string
  buyerNickName?: string
  buyerRealName?: string
  sellerRealName?: string
  cancelReason?: string
  /** 取消原因显示名 */
  cancelReasonName?: string
  /** 协议取消发起方 buyer/seller */
  protocolCancelInitiator?: string
  appealType?: string
  appealTypeName?: string
  appealInitiatorId?: string
  myReviewed?: boolean
  counterpartyReviewed?: boolean
}

export interface PreOrderDetail {
  id: string
  orderNo: string
  amount: number
  availableAmount?: number
  singleMinFiatAmount?: number
  singleMaxFiatAmount?: number
  completedAmount?: number
  tradingAmount?: number
  status: string
  statusName: string
  /** 待商户买币残留清理后移除（后端已无该字段） */
  allowMerchantBuyFlag?: boolean
  supportedPaymentAccountTypes?: SupportedPaymentAccountType[]
  createTime: string
  price?: number
  priceType?: string
  premiumRate?: number
  orderType?: string
  fiatCode?: string
  assetCode?: string
  fiatAmount?: number
  remark?: string
}

export interface ChatRecord {
  id: string
  msgType: string
  content: string
  timestamp: number
  createTime?: string
  senderId: string
  senderNickName?: string
}

export interface MyAssetChangeLog {
  id: string
  changeTime: string
  changeType: string
  changeTypeName: string
  balanceChange: number
  balanceAfter?: number
  assetCode?: string
  symbolCode?: string
  /** 账户类型编码（如 SPOT/FIAT/FUTURES），后端未返回时为空 */
  accountType?: string
  /** 账户类型名称（如 现货账户/法币账户），后端未返回时为空 */
  accountTypeName?: string
}

export interface MyAssetChangeLogDetail {
  id: string
  changeTime: string
  changeType: string
  changeTypeName: string
  assetCode?: string
  balanceChange: number
  balanceAfter: number
  note?: string
  merchantName?: string
  /** 账户类型编码（如 SPOT/FIAT/FUTURES），后端未返回时为空 */
  accountType?: string
  /** 账户类型名称（如 现货账户/法币账户），后端未返回时为空 */
  accountTypeName?: string
  /** 交易对（永续平仓/强平/资金费/手续费流水携带，如 BTC-USDT） */
  symbolCode?: string
}

export interface NoticeAbstract {
  id: string
  title: string
  publishTime: string
}

export interface NoticeDetail extends NoticeAbstract {
  content: string
}

export interface MemberNotification {
  id: string
  type?: string
  typeName?: string
  title?: string
  content?: string
  bizId?: string
  readFlag?: boolean
  readTime?: string
  popupFlag?: boolean
  createTime?: string
}

export interface UsingHelpItem {
  id: string
  title: string
  content?: string
  latelyUpdateTime?: string
}

/** 帮助列表项（不含正文，减小列表响应体） */
export interface UsingHelpAbstract {
  id: string
  title: string
  latelyUpdateTime?: string
}

export interface MemberLoginSession {
  id: string
  deviceType?: string
  deviceTypeName?: string
  ip?: string
  loginTime?: string
  current?: boolean
}



export interface LoginLogItem {
  id: string
  loginTime: string
  status: string
  statusName: string
  ip: string
  browser?: string
  os?: string
  msg?: string
}

export interface MemberDepositChannel {
  id: string
  chainType?: string
  assetCode?: string
  currencyLogo?: string
  minAmount?: number
  maxAmount?: number
  available?: boolean
  orderExpireMinutes?: number
}

export interface DepositChannel {
  id: string
  chainType?: string
  orderExpireMinutes?: number
  minAmount?: number
  maxAmount?: number
  idleAddressCount?: number
}

export interface DepositOrder {
  id: string
  orderNo: string
  channelId?: string
  chainType?: string
  depositAddress?: string
  assetCode?: string
  amount?: number
  actualAmount?: number
  fee?: number
  txHash?: string
  fromAddress?: string
  status?: string
  statusName?: string
  createTime?: string
  createTimeShort?: string
  expireTime?: string
  creditTime?: string
  closeReason?: string
  expireRemainingSeconds?: number
}

export interface InvestmentProductRateTier {
  id?: string
  productId?: string
  minAmount?: number
  maxAmount?: number | null
  annualRate?: number
}

export interface InvestmentProduct {
  id: string
  assetCode?: string
  productType?: string
  productTypeName?: string
  annualRate?: number
  subsidizedRate?: number
  rewardLimit?: number
  accrualFrequency?: string
  accrualFrequencyName?: string
  newUserExclusive?: boolean
  lockDays?: number
  minAmount?: number
  totalQuota?: number
  remainingQuota?: number
  personalRemainingQuota?: number
  /** 用户当前已持有该产品的本金（含复投利息） */
  heldAmount?: number
  maxHoldPerUser?: number
}

export interface InvestmentEstimate {
  dailyInterest?: string
  totalInterest?: string
  comprehensiveRate?: string
  /** 是否含平台补贴（后端按当前用户本笔申购是否能享受补贴利率判定） */
  hasSubsidyFlag?: boolean
}

export interface InvestmentSummary {
  investingPrincipal?: number
  totalAccruedInterest?: number
  yesterdayInterest?: number
}

export interface InvestmentPosition {
  id: string
  orderNo: string
  productId?: string
  productName?: string
  assetCode?: string
  principal?: number
  productType?: string
  productTypeName?: string
  annualRate?: number
  accrualFrequency?: string
  accrualFrequencyName?: string
  lockDays?: number
  accruedInterest?: number
  status?: string
  statusName?: string
  investTime?: string
  valueDate?: string
  maturityDate?: string | null
  lastAccrualDate?: string
  nextAccrualTime?: string
  redeemTime?: string
}

/** 赚币订单-投资tab列表项 */
export interface MyInvestmentRecord {
  id: string
  orderNo?: string
  assetCode?: string
  productType?: string
  productTypeName?: string
  principal?: number
  /** 格式: MM/dd HH:mm */
  createTime?: string
}

/** 赚币订单-赎回tab列表项 */
export interface MyRedeemRecord {
  id: string
  assetCode?: string
  productType?: string
  productTypeName?: string
  redeemAmount?: number
  redeemType?: string
  redeemTypeName?: string
  /** 格式: MM/dd HH:mm */
  createTime?: string
}

/** 赚币订单-收益tab列表项 */
export interface MyInterestRecord {
  id: string
  assetCode?: string
  productType?: string
  productTypeName?: string
  principal?: number
  interestAmount?: number
  interestType?: string
  interestTypeName?: string
  /** 格式: MM/dd HH:mm */
  createTime?: string
}

export interface InvestmentOverview {
  totalUsdt?: string
  totalAccruedInterestUsdt?: string
  yesterdayInterestUsdt?: string
  currencySummaries: InvestmentCurrencySummary[]
  productSummaries: InvestmentProductSummary[]
}

export interface InvestmentCurrencySummary {
  assetCode: string
  totalValue?: string
  usdtValue?: string
  positionCount: number
  details: InvestmentCurrencyDetail[]
}

export interface InvestmentCurrencyDetail {
  positionId?: string
  /** 活期账户 ID（活期行才有值，用于跳转活期详情） */
  flexibleAccountId?: string
  productType?: string
  lockDays?: number
  productTypeName?: string
  totalPrincipal?: string
  totalAccruedInterest?: string
  estimatedTotalInterest?: string
  confirmedInterest?: string
  annualRate?: string
  elapsedDays?: number
  yesterdayInterest?: string
}

export interface InvestmentProductSummary {
  productType?: string
  lockDays?: number
  productTypeName?: string
  usdtValue?: string
  positionCount: number
  details: InvestmentProductDetail[]
}

export interface InvestmentProductDetail {
  positionId?: string
  /** 活期账户 ID（活期行才有值，用于跳转活期详情） */
  flexibleAccountId?: string
  assetCode?: string
  lockDays?: number
  totalPrincipal?: string
  totalAccruedInterest?: string
  estimatedTotalInterest?: string
  confirmedInterest?: string
  annualRate?: string
  elapsedDays?: number
  yesterdayInterest?: string
}

export interface InvestmentInterestLog {
  id: string
  positionId?: string
  flexibleAccountId?: string
  memberAccountId?: string
  assetCode?: string
  productType?: string
  productTypeName?: string
  interestType?: string
  interestTypeName?: string
  orderNo?: string
  interestDate?: string
  interestHour?: number | null
  principal?: number
  interestAmount?: number
  createTime?: string
}

export interface FlexibleProductDetail {
  assetCode: string
  productId?: string
  flexibleAccountId?: string
  totalPrincipal?: string
  todayInterest?: string
  redeemableAmount?: string
  totalAccruedInterest?: string
  yesterdayInterest?: string
  annualRate?: string
  subsidizedRate?: string
  rewardLimit?: string
  comprehensiveRate?: string
  productEnabled?: boolean
}

export interface InvestmentRedeemRecord {
  id: string
  memberAccountId?: string
  assetCode?: string
  productType?: string
  productTypeName?: string
  redeemAmount?: string
  principalAmount?: string
  interestAmount?: string
  redeemType?: string
  redeemTypeName?: string
  createTime?: string
}

export interface RebateRecordItem {
  id: string
  sourceMemberId?: string
  sourceMemberUid?: number
  level?: number
  amount?: string
  assetCode?: string
  bizType?: string
  bizTypeName?: string
  createTime?: string
  createTimeShort?: string
}

export interface MyDirectTeamOverviewVO {
  directCount: number
  todayNewDirect: number
}

export interface MyTeamMemberVO {
  id?: string
  uid?: number
  nickName?: string
  status?: string
  statusName?: string
  createTime?: string
  createDate?: string
  level?: number
}

export interface AssetTransferRecord {
  id: string
  orderNo: string
  memberAccountId?: string
  userName?: string
  fromAccountType: string
  fromAccountTypeName?: string
  toAccountType: string
  toAccountTypeName?: string
  assetCode: string
  amount: number
  createTime?: string
}

export interface AssetTransferSubmitParam {
  fromAccountType: string
  toAccountType: string
  assetCode: string
  amount: number
}

export interface WithdrawableAsset {
  id: string
  assetCode?: string
  available?: number
  frozen?: number
}

export interface MemberWithdrawChannel {
  id: string
  chainType?: string
  assetCode?: string
  currencyLogo?: string
  minAmount?: number
  fixedFee?: number
  withdrawScale?: number
  arrivalTimeDesc?: string
}

export interface WithdrawOrder {
  id: string
  orderNo: string
  chainType?: string
  withdrawalAddress?: string
  assetCode?: string
  amount?: number
  fee?: number
  actualAmount?: number
  status?: string
  statusName?: string
  auditRemark?: string
  addressExplorerUrl?: string
  createTime?: string
  createTimeShort?: string
  completeTime?: string
}

export interface SpotOrderItem {
  id: string
  orderNo: string
  symbolCode?: string
  baseCurrencyCode?: string
  quoteCurrencyCode?: string
  side?: string
  sideName?: string
  price?: number
  amount?: number
  total?: number
  dealPrice?: number
  dealAmount?: number
  dealTotal?: number
  fee?: number
  feeAssetCode?: string
  status?: string
  statusName?: string
  entrustType?: string
  entrustTypeName?: string
  createTime?: string
  dealTime?: string
}

export interface SpotPlanOrderItem {
  id: string
  orderNo: string
  symbolCode?: string
  baseCurrencyCode?: string
  quoteCurrencyCode?: string
  side?: string
  sideName?: string
  triggerPrice?: number
  triggerType?: string
  execType?: string
  price?: number
  amount?: number
  status?: string
  statusName?: string
  spotOrderId?: string
  createTime?: string
  triggerTime?: string
}

export interface SpotSymbolItem {
  id: string
  symbolCode: string
  baseCurrencyCode?: string
  quoteCurrencyCode?: string
  minOrderAmount?: number
  maxOrderAmount?: number
  priceScale?: number
  amountScale?: number
  buyFeeRate?: number
  sellFeeRate?: number
  spread?: number
  closePrice?: string
  openPrice?: string
  highestPrice?: string
  lowestPrice?: string
  chg?: string
}

export interface ContractOrderItem {
  id: string
  orderNo: string
  symbolCode?: string
  amount?: number
  openPrice?: number
  closePrice?: number
  period?: number
  direction?: string
  directionName?: string
  openTime?: string
  expectCloseTime?: string
  closeTime?: string
  status?: string
  statusName?: string
  /** 盈亏净额：赢 = 下注金额×(赔率-1)，平 = 0，输 = -下注金额 */
  pnl?: number
}

export interface ContractSymbolItem {
  id: string
  symbolCode: string
  baseCurrencyCode?: string
  quoteCurrencyCode?: string
  currencyName?: string
  currencyLogo?: string
  priceScale?: number
  closePrice?: string
  openPrice?: string
  highestPrice?: string
  lowestPrice?: string
  chg?: string
}

export interface ContractPeriodConfig {
  period: number
  minAmount: number
  maxAmount: number
  odds: number
}

export interface KlineItem {
  id?: string
  symbol?: string
  period?: string
  openTime?: number
  openPrice?: number
  highPrice?: number
  lowPrice?: number
  closePrice?: number
  volume?: number
}

// ===== 永续合约 =====
export interface PerpetualSymbolItem {
  id: string
  symbolCode: string
  baseCurrencyCode?: string
  quoteCurrencyCode?: string
  maxLeverage?: number
  defaultLeverage?: number
  maintenanceMarginRate?: number
  takerFeeRate?: number
  fundingInterval?: number
  defaultFundingRate?: number
  priceScale?: number
  amountScale?: number
  minOrderValue?: number
  closePrice?: string
  openPrice?: string
  highestPrice?: string
  lowestPrice?: string
  chg?: string
}

/** 资金费率历史记录 */
export interface FundingRateHistoryItem {
  id: string
  symbolCode?: string
  /** 资金费率（小数，展示需 ×100） */
  fundingRate?: number
  /** 结算时间（yyyy-MM-dd HH:mm:ss） */
  fundingTime?: string
}

/** 调整保证金预览结果 */
export interface MarginLiqPricePreview {
  /** 调整后参考强平价 */
  liquidationPrice?: number
  /** 调整后保证金率（8 位小数） */
  marginRatio?: number
}

export interface PerpetualPositionItem {
  id: string
  symbolCode?: string
  direction?: string
  leverage?: number
  margin?: number
  size?: number
  openPrice?: number
  liquidationPrice?: number
  unrealizedPnl?: number
  pnlRate?: number
  marginRatio?: number
  markPrice?: number
  takeProfitPrice?: number
  stopLossPrice?: number
  /** 最大可减少保证金（安全线约束） */
  maxReduceMargin?: number
  /** 价格精度（止盈止损等价格输入按此限制小数位） */
  priceScale?: number
  /** 维持保证金率 */
  maintenanceMarginRate?: number
}

export interface MyClosedPositionItem {
  id: string
  symbolCode?: string
  direction?: string
  directionName?: string
  leverage?: number
  size?: number
  openPrice?: number
  closePrice?: number
  realizedPnl?: number
  openTime?: string
  closeTime?: string
}

export interface PerpetualOrderItem {
  id: string
  orderNo: string
  symbolCode?: string
  type?: string
  typeName?: string
  entrustType?: string
  status?: string
  statusName?: string
  direction?: string
  directionName?: string
  leverage?: number
  margin?: number
  size?: number
  price?: number
  fee?: number
  fundingAmount?: number
  pnl?: number
  positionId?: string
  createTime?: string
}

export interface PerpetualFundingCountdownItem {
  fundingRate?: number
  remainingMillis?: number
  nextSettleTime?: string
}

export interface MemberContractLeverageItem {
  longLeverage?: number
  shortLeverage?: number
  direction?: 'long' | 'short'
}

export interface PerpetualPlanOrderItem {
  id: string
  orderNo: string
  symbolCode?: string
  type?: string
  typeName?: string
  direction?: string
  directionName?: string
  leverage?: number
  margin?: number
  positionId?: string
  triggerPrice?: number
  triggerType?: string
  execType?: string
  price?: number
  status?: string
  statusName?: string
  perpetualOrderId?: string
  createTime?: string
  triggerTime?: string
}

export interface MyPendingPerpetualContractOrderItem {
  id: string
  symbolCode?: string
  type?: string
  orderDirectionName?: string
  direction?: string
  entrustTypeName?: string
  entrustType?: string
  leverage?: number
  price?: number
  size?: number
  filledSize?: number
  createTime?: string
}

export interface ModifyOrderItem {
  id: string
  symbolCode?: string
  type?: string
  direction?: string
  orderDirectionName?: string
  entrustType?: string
  leverage?: number
  price?: number
  size?: number
  status?: string
  latestPrice?: string
}

export interface MyPendingPlanOrderItem {
  id: string
  symbolCode?: string
  type?: string
  orderDirectionName?: string
  direction?: string
  entrustTypeName?: string
  leverage?: number
  price?: number
  size?: number
  triggerPrice?: number
  triggerType?: string
  source?: string
  sourceName?: string
  createTime?: string
}

export interface MyHistoryPerpetualContractOrderItem {
  id: string
  symbolCode?: string
  type?: string
  orderDirectionName?: string
  direction?: string
  entrustType?: string
  entrustTypeName?: string
  leverage?: number
  price?: number
  size?: number
  filledSize?: number
  avgPrice?: number
  pnl?: number
  status?: string
  statusName?: string
  createTime?: string
}

export interface PerpetualOrderDetailItem {
  id: string
  orderNo?: string
  positionId?: string
  symbolCode?: string
  type?: string
  typeName?: string
  entrustType?: string
  entrustTypeName?: string
  status?: string
  statusName?: string
  direction?: string
  orderDirectionName?: string
  leverage?: number
  margin?: number
  size?: number
  price?: number
  filledSize?: number
  avgPrice?: number
  fee?: number
  fundingAmount?: number
  pnl?: number
  createTime?: string
  fillTime?: string
}

export interface MyHistoryPlanOrderItem {
  id: string
  symbolCode?: string
  type?: string
  orderDirectionName?: string
  direction?: string
  entrustType?: string
  leverage?: number
  price?: number
  size?: number
  triggerPrice?: number
  triggerType?: string
  execType?: string
  source?: string
  sourceName?: string
  status?: string
  statusName?: string
  createTime?: string
}

export interface FixedPositionDetail {
  positionId: string
  assetCode: string
  productId?: string
  productEnabled: boolean
  principal?: string
  lockDays?: number
  annualRate?: string
  accruedInterest?: string
  estimatedTotalInterest?: string
  confirmedInterest?: string
  investTime?: string
  valueDate?: string
  maturityDate?: string
  arrivalDate?: string
}

/** 小时收益记录 */
export interface MyHourlyInterestRecord {
  id: string
  interestHour?: number
  interestAmount?: string
  principal?: string
  comprehensiveRate?: string
  /** 格式: HH:mm yyyy/MM/dd */
  interestTime?: string
}

/** 日收益记录 */
export interface MyDailyInterestRecord {
  id: string
  interestAmount?: string
  /** 格式: yyyy-MM-dd */
  interestDate?: string
  /** 是否为当天实时汇总数据（尚未跨天结算） */
  realtime?: boolean
}

/** 日收益明细（某日小时收益汇总 + 列表） */
export interface MyDailyInterestDetail {
  dailyInterestAmount?: string
  hourlyRecords?: MyHourlyInterestRecord[]
}

// ===== 闪兑 =====
export interface ConvertQuote {
  expireAt?: number
  fromAmount?: number
  toAmount?: number
  exchangeRate: number
}

export interface ConvertOrderItem {
  id: string
  fromAssetCode?: string
  toAssetCode?: string
  fromAmount?: number
  toAmount?: number
  exchangeRate?: number
  createTime?: string
}

export interface ConvertSetting {
  enabled?: boolean
  spread?: number
  minAmount?: number
  maxAmount?: number
  quoteTtlSeconds?: number
}

package com.exchange.constants;

import java.util.Arrays;
import java.util.List;

public class Constant {

	public static final String 联系方式_邮箱 = "email";

	public static final String 联系方式_手机号码 = "phone";

	/** 联系方式流水动作：注册即绑定 */
	public static final String 联系方式流水动作_注册 = "register";

	/** 联系方式流水动作：绑定 / 换绑新号 */
	public static final String 联系方式流水动作_绑定 = "bind";

	/** 联系方式流水动作：换绑旧号解绑 */
	public static final String 联系方式流水动作_解绑 = "unbind";

	/** 登录方式：1=密码登录 2=验证码登录 */
	public static final String 登录方式_密码 = "1";

	/** 登录方式：验证码登录 */
	public static final String 登录方式_验证码 = "2";

	public static final String 验证码发送限制 = "verificationCodeSendLock";

	/** 登录验证码错误次数限制 key 前缀 */

	public static final String 验证码类型_注册 = "register";

	public static final String 验证码类型_忘记密码 = "forgetPwd";

	/** 登录验证码类型 */
	public static final String 验证码类型_登录 = "login";

	/** 绑定联系方式验证码类型 */
	public static final String 验证码类型_绑定 = "bind";

	/** 换绑联系方式验证码类型（验证原绑定） */
	public static final String 验证码类型_换绑 = "rebind";

	/** 设置资金密码验证码类型（首次设置，需短信/邮箱验证码二次认证） */
	public static final String 验证码类型_设置资金密码 = "setFundPwd";

	/** 提币验证码类型 */
	public static final String 验证码类型_提币 = "withdraw";

	public static final String 验证码 = "verificationCode";

	/** 验证码有效期（分钟），Redis TTL 与邮件文案共用，避免两处不一致 */
	public static final int 验证码有效期分钟 = 10;

	public static final String 发送验证码 = "sendVerificationCode";

	public static final String 验证码发送状态_未发送 = "1";

	public static final String 验证码发送状态_发送成功 = "2";

	public static final String 验证码发送状态_发送失败 = "3";

	public static final String 邮件加密方式_SSL = "SSL";

	public static final String 邮件加密方式_STARTTLS = "STARTTLS";

	public static final String 邮件加密方式_NONE = "NONE";

	public static final String 汇率数据源_Binance = "Binance";

	public static final String 汇率数据源_OKX = "OKX";

	public static final Integer 默认同步K线数量 = 150;

	public static final String K线时间粒度_1分 = "1m";

	public static final String K线时间粒度_15分 = "15m";

	public static final String K线时间粒度_1时 = "1H";

	public static final String K线时间粒度_4时 = "4H";

	public static final String K线时间粒度_1日 = "1D";

	public static final String 监控新交易对 = "monitorNewSymbol";

	public static final String 监控新现货交易对 = "monitorNewSpotSymbol";

	public static final String 开盘价 = "openPrice";

	public static final String 收盘价 = "closePrice";

	public static final String 最高价 = "highPrice";

	public static final String 最低价 = "lowPrice";

	public static final String 最后更新价格时间 = "lastUpdatePriceTime";

	/** 5档盘口数据前缀，完整Key = 盘口数据 + instId（如 orderBook:BTC-USDT-SWAP） */
	public static final String 盘口数据 = "orderBook:";

	// 行情业务类型：区分现货 / 合约（秒合约跟踪永续合约行情）
	public static final String 行情业务类型_现货 = "spot";

	public static final String 行情业务类型_合约 = "contract";

	public static final String 秒合约平仓 = "secondContractClosePosition";

	public static final String 秒合约方向_买涨 = "long";

	public static final String 秒合约方向_买跌 = "short";

	public static final String 秒合约订单队列 = "secondContractOrderQueue";

	public static final String 秒合约订单状态_进行中 = "1";

	public static final String 秒合约订单状态_已完结 = "2";

	public static final String 永续合约方向_多 = "long";

	public static final String 永续合约方向_空 = "short";

	public static final String 永续持仓状态_持仓中 = "1";

	public static final String 永续持仓状态_已平仓 = "2";

	public static final String 永续持仓状态_已强平 = "3";

	public static final String 永续订单类型_开仓 = "open";

	public static final String 永续订单类型_平仓 = "close";

	public static final String 永续订单类型_强平 = "liquidate";

	public static final String 永续订单状态_待成交 = "0";

	public static final String 永续订单状态_已成交 = "1";

	public static final String 永续订单状态_已撤销 = "2";

	public static final String 永续委托类型_市价 = "market";

	public static final String 永续委托类型_限价 = "limit";

	public static final String 永续委托类型_计划委托 = "plan";

	public static final String 永续计划委托状态_委托中 = "0";

	public static final String 永续计划委托状态_已委托 = "1";

	public static final String 永续计划委托状态_已撤销 = "2";

	public static final String 永续计划委托状态_已失效 = "4";

	public static final String 永续计划委托状态_委托失败 = "3";

	public static final String 当前项目 = "exchange";

	public static final String 会员认证状态_未认证 = "1";

	public static final String 会员认证状态_认证待审核 = "2";

	public static final String 会员认证状态_已认证 = "3";

	// ===== 实名证件类型（与字典 idType 的 dict_item_code 严格一致） =====
	public static final String 证件类型_身份证 = "idCard";

	public static final String 证件类型_护照 = "passport";

	public static final String 证件类型_驾驶证 = "driverLicense";

	public static final String 会员认证状态_审核不通过 = "4";

	// ===== 会员账号状态（字典 memberAccountStatus） =====
	public static final String 账号状态_正常 = "NORMAL";

	public static final String 账号状态_冻结 = "FROZEN";

	public static final String 账号状态_已注销 = "CANCELLED";

	public static final String 会员登录密码强度校验 = "^[A-Za-z0-9]{5,15}$";

	public static final String 会员资金密码强度校验 = "^[0-9]{6}$";

	public static final String C2C交易提醒_提醒买家付款 = "tipBuyerPayment";

	public static final String C2C交易提醒_提醒卖家确认收款 = "tipSellerBuyerPaid";

	public static final String C2C交易提醒_交易完成 = "tipTradeCompleted";

	public static final String C2C超时未付款 = "c2cTimeoutUnPaid";

	public static final String C2C协议取消超时 = "c2cProtocolCancelTimeout";

	public static final String C2C风控处罚_限制买入 = "limitBuy";

	public static final String C2C风控处罚_限制卖出 = "limitSell";

	public static final String C2C风控原因_买家取消交易 = "buyerCancelTrade";

	/** 系统原因：买家超时未付款，系统自动取消交易 */
	public static final String C2C风控原因_超时未付款 = "timeoutUnPaid";

	public static final String C2C风控原因_买家未付款 = "buyerUnPaid";

	public static final String C2C风控原因_使用非本人支付账户付款 = "notMySelfPaid";

	/** 买家责任协议取消（买家原因）：达到阈值限制买入 */
	public static final String C2C风控原因_买家原因协议取消 = "buyerProtocolCancelCause";

	/** 卖家责任协议取消（卖家原因）：达到阈值限制卖出 */
	public static final String C2C风控原因_卖家原因协议取消 = "sellerProtocolCancelCause";

	// ===== C2C 取消原因 =====
	/** 买方原因：我确认还没有付款给对方 */
	public static final String C2C取消原因_买家确认未付款 = "BUYER_NO_PAY";
	/** 卖方原因：卖家要额外收取费用 */
	public static final String C2C取消原因_卖家额外收费 = "SELLER_EXTRA_FEE";
	/** 卖方原因：卖家支付方式有问题无法成功打款 */
	public static final String C2C取消原因_卖家支付方式有问题 = "SELLER_PAY_PROBLEM";
	/** 卖方原因：卖家无法放行/卖家已退款给我 */
	public static final String C2C取消原因_卖家已退款 = "SELLER_REFUNDED";
	/** 卖方原因：无法联系到卖家 */
	public static final String C2C取消原因_联系不上卖家 = "SELLER_UNREACHABLE";
	/** 系统原因：买家超时未付款，系统自动取消 */
	public static final String C2C取消原因_超时未付款 = "BUYER_TIMEOUT_UNPAID";
	/** 客服原因：申诉结案取消交易 */
	public static final String C2C取消原因_申诉取消 = "APPEAL_CANCELED";
	/** 买方原因：买家要求线下/脱离平台交易（卖家发起协议取消） */
	public static final String C2C取消原因_买家要求线下交易 = "BUYER_OFF_PLATFORM";
	/** 买方原因：无法联系到买家（卖家发起协议取消） */
	public static final String C2C取消原因_联系不上买家 = "BUYER_UNREACHABLE";

	/** 协议取消原因集合（卖方原因，买家发起协议取消时使用） */
	public static final List<String> C2C协议取消卖家原因 = List.of(C2C取消原因_卖家额外收费, C2C取消原因_卖家支付方式有问题, C2C取消原因_卖家已退款, C2C取消原因_联系不上卖家);

	/** 协议取消原因集合（买方原因，卖家发起协议取消时使用） */
	public static final List<String> C2C协议取消买家原因 = List.of(C2C取消原因_买家要求线下交易, C2C取消原因_联系不上买家);

	public static final String 操作方_系统 = "system";

	public static final String 操作方_客服 = "customerService";

	public static final String 操作方_卖家 = "seller";

	public static final String 操作方_买家 = "buyer";

	public static final String C2C申诉状态_待处理 = "1";

	public static final String C2C申诉状态_已完结 = "2";

	public static final String C2C申诉状态_已撤销 = "3";

	public static final String C2C申诉处理方式_放行交易 = "RELEASE";

	public static final String C2C申诉处理方式_取消订单 = "CANCEL";

	public static final String C2C申诉类型_钱付多了 = "OVER_PAID";

	public static final String C2C申诉类型_买方未付款 = "BUYER_UNPAID";

	public static final String C2C申诉类型_使用非本人支付账户付款 = "NON_OWNER_ACCOUNT";

	// 买家仅保留"钱付多了"：已付款后卖家未放行由系统自动放行兜底，无需申诉
	public static final List<String> 买家申诉类型 = Arrays.asList(C2C申诉类型_钱付多了);

	// 卖家保留"买方未付款/少付"与"非本人账户付款"；"钱付少了"已并入"买方未付款"
	public static final List<String> 卖家申诉类型 = Arrays.asList(C2C申诉类型_买方未付款, C2C申诉类型_使用非本人支付账户付款);

	public static final String 会员余额变动类型_系统 = "SYSTEM";

	// ===== C2C 法币交易（账户类型固定为法币）：买家收币余额+ / 卖家付币余额- =====
	public static final String 会员余额变动类型_C2C买币 = "C2C_BUY";

	public static final String 会员余额变动类型_C2C卖币 = "C2C_SELL";

	// ===== 资金进出 =====
	public static final String 会员余额变动类型_充币 = "DEPOSIT";

	public static final String 会员余额变动类型_提币 = "WITHDRAW";

	public static final String 会员余额变动类型_资金划转 = "TRANSFER_OUT";

	public static final String 会员余额变动类型_资金划转入 = "TRANSFER_IN";

	public static final String 会员余额变动类型_资金冻结 = "FREEZE";

	public static final String 会员余额变动类型_资金解冻 = "UNFREEZE";

	// ===== 理财（赚币） =====
	public static final String 会员余额变动类型_理财申购 = "INVEST_SUBSCRIBE";

	public static final String 会员余额变动类型_理财收益 = "INVEST_INTEREST";

	public static final String 会员余额变动类型_理财赎回 = "INVEST_REDEEM";

	// ===== 闪兑 =====
	public static final String 会员余额变动类型_闪兑 = "CONVERT";

	// ===== 永续合约 =====
	public static final String 会员资产变动类型_永续平仓 = "PERP_CLOSE";

	public static final String 会员资产变动类型_永续强平 = "PERP_LIQUIDATION";

	public static final String 会员资产变动类型_永续资金费 = "FUNDING_FEE";

	public static final String 会员资产变动类型_永续手续费 = "PERP_FEE";

	// ===== 秒合约 =====
	public static final String 会员余额变动类型_秒合约开仓 = "SECOND_CONTRACT_OPEN";

	/** 平仓时返还本金（与平仓盈亏分开记账，便于审计亏损单） */
	public static final String 会员余额变动类型_秒合约本金返还 = "SECOND_CONTRACT_REFUND";

	/** 平仓时结算盈亏净额（正=盈利，负=亏损） */
	public static final String 会员余额变动类型_秒合约平仓 = "SECOND_CONTRACT_CLOSE";

	// ===== 返佣 =====
	public static final String 会员资产变动类型_返佣 = "REBATE";

	// ===== 现货 =====
	public static final String 会员余额变动类型_现货买入 = "SPOT_BUY";

	public static final String 会员余额变动类型_现货卖出 = "SPOT_SELL";

	public static final String 闪兑报价缓存前缀 = "convert:quote:";

	public static final String 提币订单状态_待审核 = "1";

	public static final String 提币订单状态_审核通过 = "2";

	public static final String 提币订单状态_已提币 = "3";

	public static final String 提币订单状态_已驳回 = "4";

	public static final String 提币订单状态_已取消 = "5";
	
	public static final String 定期计息 = "fixedAccrual";
	
	public static final String 活期计息 = "flexibleAccountAccrual";

	public static final String 理财持仓状态_持仓中 = "1";

	public static final String 理财持仓状态_已赎回 = "2";

	public static final String 理财赎回方式_到期赎回 = "1";

	public static final String 理财赎回方式_手动赎回 = "2";

	public static final String 理财产品类型_活期 = "FLEXIBLE";

	public static final String 理财产品类型_定期 = "FIXED";

	public static final String 理财计息频率_每小时 = "HOURLY";

	public static final String 理财计息频率_每日 = "DAILY";

	public static final String 收益类型_收益复投 = "COMPOUND";

	public static final String 收益类型_收益发放 = "DISTRIBUTE";

	public static final String 会员消息类型_理财申购 = "invest_subscribe";

	public static final String 会员消息类型_理财赎回 = "invest_redeemed";

	public static final String 会员消息类型_C2C交易 = "c2c_trade";

	public static final String 会员消息类型_C2C风控 = "c2c_risk";

	public static final String 会员消息类型_实名通过 = "auth_passed";

	public static final String 会员消息类型_实名未通过 = "auth_rejected";

	public static final String 会员消息类型_充币 = "deposit";

	public static final String 会员消息类型_系统 = "system";

	public static final String 会员消息类型_提币 = "withdraw";
	
	/** 保证金调整类型：增加 */
	public static final String 保证金调整类型_增加 = "ADD";
	/** 保证金调整类型：减少 */
	public static final String 保证金调整类型_减少 = "REDUCE";
	
	public static final String 会员消息类型_永续强平 = "perp_liquidation";

	public static final String 会员消息业务类型_永续持仓 = "perpetual_position";

	public static final String 充币金额折算USDT = "depositAmountConverted2Usdt";

	public static final String 提币金额折算USDT = "withdrawAmountConverted2Usdt";

	public static final String 折算USDT状态_未折算 = "0";

	public static final String 折算USDT状态_已折算 = "1";
	
	public static final String 充币订单超时 = "depositOrderTimeout";

	public static final String 充币订单状态_待支付 = "1";

	public static final String 充币订单状态_确认中 = "2";

	public static final String 充币订单状态_已完成 = "3";

	public static final String 充币订单状态_已过期 = "4";

	public static final String 充币订单状态_已关闭 = "5";

	public static final String 充币地址状态_空闲 = "1";

	public static final String 充币地址状态_占用中 = "2";

	public static final String 充币地址状态_停用 = "3";

	public static final String C2C订单状态_未付款 = "1";

	public static final String C2C订单状态_已付款 = "2";

	public static final String C2C订单状态_已完成 = "3";

	public static final String C2C订单状态_已取消 = "4";

	public static final String C2C订单状态_申诉中 = "5";

	/** 协议取消申请中 */
	public static final String C2C订单状态_协议取消中 = "6";

	public static final String C2C广告单状态_进行中 = "1";

	public static final String C2C广告单状态_已完成 = "2";

	public static final String C2C广告单状态_已取消 = "3";

	public static final String C2C广告单状态_已暂停 = "4";

	// ===== C2C 浮动定价 =====
	public static final String 定价类型_固定 = "fixed";

	public static final String 定价类型_浮动 = "float";

	// ===== C2C 双向广告市场 =====
	public static final String C2C广告单类型_出售 = "sell";

	public static final String C2C广告单类型_购买 = "buy";

	/** 每用户同时进行中（含暂停）的广告数量上限 */
	public static final int C2C最大进行中广告数 = 5;

	public static final String C2C超时未放行 = "c2cTimeoutUnRelease";

	public static final String 支付方式状态_启用 = "1";

	public static final String 支付方式状态_停用 = "2";

	public static final String 菜单类型_一级菜单 = "menu_1";

	public static final String 菜单类型_二级菜单 = "menu_2";

	public static final String 菜单类型_权限 = "menu_3";

	public static final String 子系统_客户端 = "client";

	public static final String 子系统_后台管理 = "admin";

	public static final String 登录提示_登录成功 = "登录成功";

	public static final String 登录状态_成功 = "1";

	public static final String 登录状态_失败 = "0";

	// 登录会话账号类型
	public static final String 登录会话账号类型_会员 = "MEMBER";

	public static final String 登录会话账号类型_后台 = "ADMIN";

	// 登录会话状态
	public static final String 登录会话状态_在线 = "ONLINE";

	public static final String 登录会话状态_已登出 = "LOGGED_OUT";

	public static final String 登录会话状态_已踢出 = "KICKED";

	public static final String 登录会话状态_已顶下线 = "REPLACED";

	public static final String 登录会话状态_已过期 = "EXPIRED";

	// 登录设备类型
	public static final String 登录设备类型_APP = "APP";

	public static final String 登录设备类型_WEB = "WEB";

	// 强制下线原因（用于区分 NotLoginException 的提示文案）
	public static final String 强制下线原因_权限变更 = "permissionChanged";

	public static final String 强制下线原因_密码变更 = "passwordChanged";

	public static final String 强制下线原因_账号冻结 = "accountFrozen";

	public static final String 强制下线原因_账号删除 = "accountDeleted";

	public static final String 强制下线原因_账号注销 = "accountCancelled";

	public static final String 强制下线原因缓存key前缀 = "kickout:reason:";

	// 登录失败默认配置
	public static final int 账号登录失败默认次数 = 5;

	public static final int 账号登录失败默认锁定分钟 = 15;

	public static final int IP登录失败默认次数 = 10;

	public static final int IP登录失败默认锁定分钟 = 15;

	public static final int 会员默认最大在线端数 = 5;

	// 操作日志状态
	public static final String 操作日志状态_成功 = "SUCCESS";

	public static final String 操作日志状态_失败 = "FAIL";

	public static final String 启用状态_启用 = "1";

	public static final String 启用状态_停用 = "0";

	public static final String 存储类型_本地 = "LOCAL";

	public static final String 客户端接口前缀 = "/client";

	public static final String 后台接口前缀 = "/admin";

	// ===== 多资产账户 / 资产代码 =====
	public static final String 账户类型_现货 = "SPOT";

	public static final String 账户类型_合约 = "FUTURES";

	public static final String 账户类型_法币 = "FIAT";

	public static final String 数字货币_USDT = "USDT";

	public static final String 法币_CNY = "CNY";

	// ===== C2C 信用体系 =====
	/** 30天交易统计缓存前缀 */
	public static final String C2C_交易统计缓存 = "c2cTradeStats:";

	/** C2C 法币兑 USDT 汇率 Redis key 前缀，完整 key = c2c:fiatRate:{fiatCode} */
	public static final String C2C法币汇率 = "c2c:fiatRate:";

	// ===== 现货交易 =====
	public static final String 现货方向_买入 = "buy";

	public static final String 现货方向_卖出 = "sell";

	public static final String 现货订单状态_已完成 = "1";

	public static final String 现货订单状态_已撤销 = "2";

	public static final String 现货订单状态_待成交 = "0";

	public static final String 现货委托类型_市价 = "market";

	public static final String 现货委托类型_限价 = "limit";

	public static final String 现货委托类型_计划委托 = "plan";

	/** 现货计划委托状态：委托中 */
	public static final String 现货计划委托状态_委托中 = "0";

	/** 现货计划委托状态：已委托 */
	public static final String 现货计划委托状态_已委托 = "1";

	/** 现货计划委托状态：已撤销 */
	public static final String 现货计划委托状态_已撤销 = "2";

	/** 现货计划委托状态：委托失败 */
	public static final String 现货计划委托状态_委托失败 = "3";

	/** 计划委托触发条件：市价 ≥ 触发价 */
	public static final String 计划触发_大于等于 = "ge";

	/** 计划委托触发条件：市价 ≤ 触发价 */
	public static final String 计划触发_小于等于 = "le";

	/** 计划委托触发后执行方式：限价 */
	public static final String 计划执行_限价 = "limit";

	/** 计划委托触发后执行方式：市价 */
	public static final String 计划执行_市价 = "market";

	/** 计划委托来源：普通条件单 */
	public static final String 永续计划来源_普通 = "plan";

	/** 计划委托来源：止盈单 */
	public static final String 永续计划来源_止盈 = "tp";

	/** 计划委托来源：止损单 */
	public static final String 永续计划来源_止损 = "sl";

	public static final int 数值总位数 = 32;

	public static final int 小数部分位数 = 8;

	public static final int 整数部分位数 = 24;

	public static final int 折合USDT精度 = 2;

	// ===== 返佣业务类型（HTX 式：现货/合约交易手续费返佣给推荐人） =====
	public static final String 返佣业务类型_现货 = "1";

	public static final String 返佣业务类型_合约 = "2";

	// ===== 返佣生成（afterCommit 发 Topic 信号 + 定时扫库补偿，参考充币折算USDT） =====
	/** 现货成交后待返佣订单信号（Redisson Topic，参考充币金额折算USDT） */
	public static final String 现货成交返佣 = "spotRebate";

	/** 合约成交后待返佣订单信号（Redisson Topic，参考充币金额折算USDT） */
	public static final String 永续合约成交返佣 = "perpRebate";

	// ===== IP 归属地记录（afterCommit 发 Topic 信号，由 RedisMqListener 异步处理） =====
	/** 记录登录 IP 归属地（Redisson Topic） */
	public static final String 记录IP信息 = "logIpInfo";

	/** 订单返佣状态：待返佣 */
	public static final String 返佣状态_待返佣 = "0";

	/** 订单返佣状态：已返佣（含无需返佣：无推荐人 / 无手续费） */
	public static final String 返佣状态_已返佣 = "1";

	/** 会员 UID 自增序列 Redis key */
	public static final String 会员UID序列号 = "member:uid:seq";

	/** 会员 UID 初始偏移量（首次 INCR 得到 1000000） */
	public static final long 会员UID初始偏移量 = 999999L;

	// ===== 多语言 =====
	/** 系统默认语言码：未传 Lang 或解析失败时使用 */
	public static final String 默认语言_简体中文 = "zh-CN";

	public static final String 语言_简体中文 = "zh-CN";

	public static final String 语言_繁体中文 = "zh-TW";

	public static final String 语言_英文 = "en-US";
	
	public static final String 模块_充币订单 = "depositOrder";

	public static final String 模块_充币通道 = "depositChannel";

	public static final String 模块_充币地址 = "depositAddress";

	public static final String 模块_提币通道 = "withdrawChannel";

	public static final String 模块_提币订单 = "withdrawOrder";

	public static final String 模块_返佣记录 = "rebateRecord";

	public static final String 模块_参数配置 = "setting";

	public static final String 模块_支付方式类型 = "paymentAccountType";

	public static final String 模块_币种 = "currency";

	public static final String 模块_区块链网络 = "chainNetwork";

	public static final String 模块_后台账号 = "backgroundAccount";
	
	public static final String 模块_后台账号会话 = "backgroundAccountLoginSession";

	public static final String 模块_超级管理员 = "superAdmin";

	public static final String 模块_菜单管理 = "menu";

	public static final String 模块_角色管理 = "role";

	public static final String 模块_会员消息 = "memberNotification";

	public static final String 模块_支付方式 = "paymentAccount";

	public static final String 模块_会员账号 = "memberAccount";
	
	public static final String 模块_会员账号会话 = "memberAccountLoginSession";

	public static final String 模块_会员资产 = "memberAsset";

	public static final String 模块_实名认证 = "memberAuthenticationInfo";

	public static final String 模块_字典管理 = "dictConfig";

	public static final String 模块_理财产品 = "investmentProduct";

	public static final String 模块_定期持仓 = "investmentFixedPosition";

	public static final String 模块_活期持仓 = "investmentFlexiblePosition";

	public static final String 模块_理财收益流水 = "investmentInterestLog";

	public static final String 模块_活期赚币账户 = "investmentFlexibleAccount";

	public static final String 模块_投资记录 = "investmentRecord";

	public static final String 模块_C2C订单 = "c2cOrder";

	public static final String 模块_挂单记录 = "c2cPreOrder";

	public static final String 模块_C2C法币配置 = "c2cFiat";

	public static final String 模块_C2C币种配置 = "c2cCurrency";

	public static final String 模块_C2C交易对配置 = "c2cSymbol";

	public static final String 模块_永续合约交易对 = "perpetualContractSymbol";

	public static final String 模块_永续持仓 = "perpetualPosition";

	/** 多语言维护模块 */
	public static final String 模块_多语言 = "multiLang";

	/** 现货交易对模块 */
	public static final String 模块_现货交易对 = "spotSymbol";

	/** 秒合约交易对模块 */
	public static final String 模块_秒合约交易对 = "secondContractSymbol";

	/** 现货计划委托模块 */
	public static final String 模块_现货计划委托 = "spotPlanOrder";

	/** 永续计划委托模块 */
	public static final String 模块_永续计划委托 = "perpetualPlanOrder";

	/** 公告模块 */
	public static final String 模块_公告 = "notice";

	/** 帮助中心模块 */
	public static final String 模块_帮助中心 = "usingHelp";

	/** 国家区号模块 */
	public static final String 模块_国家区号 = "countryAreaCode";

	/** 数据清理模块 */
	public static final String 模块_数据清理 = "dataClean";

}

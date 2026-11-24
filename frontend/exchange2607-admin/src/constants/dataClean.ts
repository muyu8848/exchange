/**
 * 数据清理可选项。
 * <p>
 * 必须与后端 DataCleanService.clean 支持的 dataTypes 一一对应：
 * 后端按字符串精确匹配，罗列了但后端不处理的类型会被静默忽略（点了没反应），
 * 新增/删除清理项时前后端需同步修改。
 */
export const DATA_CLEAN_TYPES = [
  // ===== 交易数据 =====
  { value: 'spotOrder', label: '现货订单' },
  { value: 'spotPlanOrder', label: '现货计划委托' },
  { value: 'perpetualOrder', label: '永续订单' },
  { value: 'perpetualPlanOrder', label: '永续计划委托' },
  { value: 'perpetualPosition', label: '永续持仓' },
  { value: 'perpetualFundingFee', label: '资金费记录' },
  { value: 'perpetualFundingRateHistory', label: '资金费率历史' },
  { value: 'secondContractOrder', label: '秒合约订单' },
  { value: 'convertOrder', label: '闪兑订单' },
  // ===== 资金 / 记录 =====
  { value: 'assetTransferOrder', label: '资产划转记录' },
  { value: 'rebateRecord', label: '返佣记录' },
  { value: 'c2cOrder', label: 'C2C 订单（含聊天、申诉、评价记录）' },
  { value: 'c2cPreOrder', label: 'C2C 广告单（含收款账户）' },
  { value: 'c2cRiskRecord', label: 'C2C 风控记录' },
  { value: 'depositOrder', label: '充币订单' },
  { value: 'withdrawOrder', label: '提币订单' },
  { value: 'investmentPosition', label: '理财持仓（含利息、投资记录、赎回、活期账户）' },
  { value: 'investmentInterestLog', label: '理财利息记录' },
  { value: 'memberAuthenticationInfo', label: '实名认证信息' },
  { value: 'memberAssetChangeLog', label: '会员资金流水' },
  { value: 'memberNotification', label: '会员消息通知' },
  // ===== 日志 / 安全 =====
  { value: 'loginLog', label: '登录日志' },
  { value: 'operLog', label: '操作日志' },
  { value: 'verificationCodeSendRecord', label: '验证码发送记录' },
  { value: 'accountLoginSession', label: '登录会话记录' },
  { value: 'memberContactBindLog', label: '联系方式绑定日志' },
] as const;

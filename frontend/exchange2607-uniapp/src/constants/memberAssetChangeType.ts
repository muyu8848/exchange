/**
 * 会员资产变动类型：与后端 Constant.会员余额变动类型_* / 会员资产变动类型_* 严格一致。
 * 后端已由数字 code 改为英文枚举，新增/修改时两边需同步。
 */
export const MEMBER_ASSET_CHANGE_TYPE = {
  /** 系统 */
  SYSTEM: 'SYSTEM',
  /** C2C 买币（买家收币） */
  C2C_BUY: 'C2C_BUY',
  /** C2C 卖币（卖家付币） */
  C2C_SELL: 'C2C_SELL',
  /** 充币 */
  DEPOSIT: 'DEPOSIT',
  /** 提币 */
  WITHDRAW: 'WITHDRAW',
  /** 资金划转（转出） */
  TRANSFER_OUT: 'TRANSFER_OUT',
  /** 资金划转入 */
  TRANSFER_IN: 'TRANSFER_IN',
  /** 资金冻结 */
  FREEZE: 'FREEZE',
  /** 资金解冻 */
  UNFREEZE: 'UNFREEZE',
  /** 理财申购 */
  INVEST_SUBSCRIBE: 'INVEST_SUBSCRIBE',
  /** 理财收益 */
  INVEST_INTEREST: 'INVEST_INTEREST',
  /** 理财赎回 */
  INVEST_REDEEM: 'INVEST_REDEEM',
  /** 闪兑 */
  CONVERT: 'CONVERT',
  /** 永续平仓 */
  PERP_CLOSE: 'PERP_CLOSE',
  /** 永续强平 */
  PERP_LIQUIDATION: 'PERP_LIQUIDATION',
  /** 永续资金费 */
  FUNDING_FEE: 'FUNDING_FEE',
  /** 永续手续费 */
  PERP_FEE: 'PERP_FEE',
  /** 秒合约开仓 */
  SECOND_CONTRACT_OPEN: 'SECOND_CONTRACT_OPEN',
  /** 秒合约平仓（盈亏结算净额，正=盈利 负=亏损） */
  SECOND_CONTRACT_CLOSE: 'SECOND_CONTRACT_CLOSE',
  /** 秒合约平仓-本金返还 */
  SECOND_CONTRACT_REFUND: 'SECOND_CONTRACT_REFUND',
  /** 返佣 */
  REBATE: 'REBATE',
  /** 现货买入 */
  SPOT_BUY: 'SPOT_BUY',
  /** 现货卖出 */
  SPOT_SELL: 'SPOT_SELL',
} as const

export type MemberAssetChangeType = typeof MEMBER_ASSET_CHANGE_TYPE[keyof typeof MEMBER_ASSET_CHANGE_TYPE]

/** 永续账单「资金流水」tab 展示的四类账变 */
export const PERP_FUNDING_CHANGE_TYPES: MemberAssetChangeType[] = [
  MEMBER_ASSET_CHANGE_TYPE.PERP_CLOSE,
  MEMBER_ASSET_CHANGE_TYPE.PERP_LIQUIDATION,
  MEMBER_ASSET_CHANGE_TYPE.FUNDING_FEE,
  MEMBER_ASSET_CHANGE_TYPE.PERP_FEE,
]

/** 永续「资金流水」默认查询条件（接口 changeType 参数，逗号分隔） */
export const PERP_FUNDING_CHANGE_TYPES_PARAM = PERP_FUNDING_CHANGE_TYPES.join(',')

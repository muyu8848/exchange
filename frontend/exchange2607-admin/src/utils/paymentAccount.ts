export interface PaymentAccountFormField {
  key: string;
  label?: string;
  type?: string;
  required?: boolean;
  placeholder?: string;
  maxLength?: number;
  searchable?: boolean;
}

export interface PaymentAccountTypeInfo {
  code?: string;
  name?: string;
  formFields?: PaymentAccountFormField[];
}

export interface PaymentAccountFieldLine {
  key: string;
  label: string;
  value: string;
  /** text / textarea / image / number */
  type?: string;
  /** 主要字段：true 时在客户端收款方式列表等场景展示（image 以小图展示） */
  keyField?: boolean;
}

export interface PaymentAccountVO {
  id?: string;
  paymentAccountTypeCode?: string;
  paymentAccountTypeName?: string;
  /** 类型模板（含 formFields），前端据此渲染字段 label */
  paymentAccountType?: PaymentAccountTypeInfo;
  /** 收款字段值（key 由类型模板定义） */
  fieldValues?: Record<string, string>;
  /** 后端按类型模板合成，前端列表/详情直接据此渲染 */
  fieldLines?: PaymentAccountFieldLine[];
  realName?: string;
  status?: string;
  statusName?: string;
  memberAccount?: string;
  todayReceivedAmount?: number;
  todayReceivedCount?: number;
  createTime?: string;
  // 以下为历史固定字段，仅兼容遗留数据结构展示，新数据不再返回
  cardNumber?: string;
  bankName?: string;
  alipayAccount?: string;
  wechatNickName?: string;
  walletNumber?: string;
  qrcode?: string;
}

export function resolvePaymentAccount(info: unknown): PaymentAccountVO | null {
  if (!info || typeof info !== 'object') {
    return null;
  }
  return info as PaymentAccountVO;
}



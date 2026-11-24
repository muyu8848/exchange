import type { PageResult } from '@/api/common';
import { request } from '@/utils/request';

export interface InvestmentHourlyInterestLogRow {
  id: string;
  flexibleAccountId?: string;
  memberAccountId?: string;
  memberUid?: number;
  assetCode?: string;
  interestDate?: string;
  interestHour?: number;
  principal?: number;
  interestAmount?: number;
  createTime?: string;
}

export interface InvestmentHourlyInterestLogQuery {
  memberUid?: string;
  assetCode?: string;
  /** 活期账户 ID（精确过滤某个在投周期） */
  flexibleAccountId?: string;
  interestDateStart?: string;
  interestDateEnd?: string;
  pageNum?: number;
  pageSize?: number;
}

export function findInvestmentHourlyInterestLogByPage(params: InvestmentHourlyInterestLogQuery) {
  return request.post<PageResult<InvestmentHourlyInterestLogRow>>({
    url: '/investmentHourlyInterestLog/findByPage',
    data: params,
  });
}

export interface HourlyInterestLogSubtotalItem {
  assetCode: string;
  recordCount: number;
  totalInterestAmount: number;
}

export interface InvestmentHourlyInterestLogSubtotal {
  items: HourlyInterestLogSubtotalItem[];
}

export function investmentHourlyInterestLogSubtotal(params: Omit<InvestmentHourlyInterestLogQuery, 'pageNum' | 'pageSize'>) {
  return request.post<InvestmentHourlyInterestLogSubtotal>({
    url: '/investmentHourlyInterestLog/subtotal',
    data: params,
  });
}

import { request } from '@/utils/request';

export interface SpotDashboardOverview {
  dealCount: number;
  buyDealTotal: number;
  sellDealTotal: number;
  totalDealTotal: number;
  feeUsdtAmount: number;
  pendingPlanCount: number;
  pendingPlanAmount: number;
}

export interface SpotDashboardTrendItem {
  date: string;
  buyDealTotal: number;
  sellDealTotal: number;
  feeUsdtAmount: number;
}

export interface SpotDashboardDailyItem {
  date: string;
  dealCount: number;
  buyDealTotal: number;
  sellDealTotal: number;
  totalDealTotal: number;
  feeUsdtAmount: number;
}

export interface SpotDashboardSummary {
  overview: SpotDashboardOverview;
  trend: SpotDashboardTrendItem[];
}

export interface SpotDashboardQueryParam {
  timeStart?: string;
  timeEnd?: string;
  symbolCode?: string;
  side?: string;
  granularity?: string;
}

export function getSpotDashboardSummary(params: SpotDashboardQueryParam) {
  return request.post<SpotDashboardSummary>({ url: '/spotDashboard/summary', data: params });
}

export function getSpotDashboardDaily(params: SpotDashboardQueryParam) {
  return request.post<SpotDashboardDailyItem[]>({ url: '/spotDashboard/daily', data: params });
}

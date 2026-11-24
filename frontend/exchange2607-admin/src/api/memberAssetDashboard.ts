import { request } from '@/utils/request';

export interface MemberAssetDashboardOverview {
  totalUsdtValue: number;
  availableUsdtValue: number;
  frozenUsdtValue: number;
  memberCount: number;
  accountCount: number;
  avgUsdtValue: number;
  assetCount: number;
}

export interface MemberAssetDashboardAssetItem {
  assetCode: string;
  available: number;
  frozen: number;
  total: number;
  usdtValue: number;
  accountCount: number;
  ratio: number;
}

export interface MemberAssetDashboardTypeItem {
  accountType: string;
  accountTypeName: string;
  usdtValue: number;
  accountCount: number;
  ratio: number;
}

export interface MemberAssetDashboardSummary {
  overview: MemberAssetDashboardOverview;
  assetDistribution: MemberAssetDashboardAssetItem[];
  accountTypeDistribution: MemberAssetDashboardTypeItem[];
}

export interface MemberAssetDashboardQueryParam {
  assetCode?: string;
  accountType?: string;
  memberAccountStatus?: string;
}

export function getMemberAssetDashboardSummary(params: MemberAssetDashboardQueryParam) {
  return request.post<MemberAssetDashboardSummary>({ url: '/memberAssetDashboard/summary', data: params });
}

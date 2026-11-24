import { request } from '@/utils/request';

export interface MemberDashboardOverview {
  newMemberCount: number;
  totalRegisteredCount: number;
  totalMemberCount: number;
  activeMemberCount: number;
  verifiedMemberCount: number;
  newVerifiedCount: number;
  disabledMemberCount: number;
}

export interface MemberDashboardTrendItem {
  date: string;
  newMemberCount: number;
  activeMemberCount: number;
}

export interface MemberDashboardDailyItem {
  date: string;
  newMemberCount: number;
  activeMemberCount: number;
  newVerifiedCount: number;
}

export interface MemberDashboardDistributionItem {
  name: string;
  value: number;
}

export interface MemberDashboardSummary {
  overview: MemberDashboardOverview;
  trend: MemberDashboardTrendItem[];
  contactTypeDistribution: MemberDashboardDistributionItem[];
  authenticationStatusDistribution: MemberDashboardDistributionItem[];
}

export interface MemberDashboardQueryParam {
  timeStart?: string;
  timeEnd?: string;
  status?: string;
  authenticationStatus?: string;
  contactType?: string;
  granularity?: string;
}

export function getMemberDashboardSummary(params: MemberDashboardQueryParam) {
  return request.post<MemberDashboardSummary>({ url: '/memberDashboard/summary', data: params });
}

export function getMemberDashboardDaily(params: MemberDashboardQueryParam) {
  return request.post<MemberDashboardDailyItem[]>({ url: '/memberDashboard/daily', data: params });
}

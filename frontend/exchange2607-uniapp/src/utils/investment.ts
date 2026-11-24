import type { InvestmentPosition } from '@/api/types'

export interface AccrualProgress {
  earnedDays: number
  totalDays: number
  percent: number
}

function daysBetween(start: string, end: string) {
  const startDate = new Date(start.replace(/-/g, '/'))
  const endDate = new Date(end.replace(/-/g, '/'))
  return Math.round((endDate.getTime() - startDate.getTime()) / 86400000)
}

export function calcAccrualProgress(position?: InvestmentPosition | null): AccrualProgress {
  const totalDays = position?.lockDays ?? 0
  if (!position || totalDays <= 0) {
    return { earnedDays: 0, totalDays: 0, percent: 0 }
  }

  let earnedDays = 0
  if (position.status === '2' || position.status === '3') {
    earnedDays = totalDays
  }
  else if (position.lastAccrualDate && position.valueDate) {
    earnedDays = daysBetween(position.valueDate, position.lastAccrualDate) + 1
  }

  earnedDays = Math.min(Math.max(earnedDays, 0), totalDays)
  const percent = Math.round((earnedDays / totalDays) * 100)
  return { earnedDays, totalDays, percent }
}

import dayjs from 'dayjs';
import type { DateRangePickerProps } from 'tdesign-vue-next';

const presetRanges = {
  今天: [dayjs().startOf('day').format('YYYY-MM-DD HH:mm:ss'), dayjs().endOf('day').format('YYYY-MM-DD HH:mm:ss')],
  昨天: [
    dayjs().subtract(1, 'day').startOf('day').format('YYYY-MM-DD HH:mm:ss'),
    dayjs().subtract(1, 'day').endOf('day').format('YYYY-MM-DD HH:mm:ss'),
  ],
  最近7天: [
    dayjs().subtract(6, 'day').startOf('day').format('YYYY-MM-DD HH:mm:ss'),
    dayjs().endOf('day').format('YYYY-MM-DD HH:mm:ss'),
  ],
  本月: [dayjs().startOf('month').format('YYYY-MM-DD HH:mm:ss'), dayjs().endOf('day').format('YYYY-MM-DD HH:mm:ss')],
  上个月: [
    dayjs().subtract(1, 'month').startOf('month').format('YYYY-MM-DD HH:mm:ss'),
    dayjs().subtract(1, 'month').endOf('month').format('YYYY-MM-DD HH:mm:ss'),
  ],
  本年: [dayjs().startOf('year').format('YYYY-MM-DD HH:mm:ss'), dayjs().endOf('day').format('YYYY-MM-DD HH:mm:ss')],
} satisfies Record<string, [string, string]>;

export const tableDateRangePresets = presetRanges as DateRangePickerProps['presets'];

export const staticTableDateRangePresets = {
  今天: [dayjs().startOf('day').format('YYYY-MM-DD'), dayjs().endOf('day').format('YYYY-MM-DD')],
  昨天: [
    dayjs().subtract(1, 'day').startOf('day').format('YYYY-MM-DD'),
    dayjs().subtract(1, 'day').endOf('day').format('YYYY-MM-DD'),
  ],
  最近7天: [dayjs().subtract(6, 'day').startOf('day').format('YYYY-MM-DD'), dayjs().endOf('day').format('YYYY-MM-DD')],
  本月: [dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().endOf('day').format('YYYY-MM-DD')],
  上个月: [
    dayjs().subtract(1, 'month').startOf('month').format('YYYY-MM-DD'),
    dayjs().subtract(1, 'month').endOf('month').format('YYYY-MM-DD'),
  ],
  本年: [dayjs().startOf('year').format('YYYY-MM-DD'), dayjs().endOf('day').format('YYYY-MM-DD')],
} satisfies Record<string, [string, string]>;

export const tableDateOnlyRangePresets = staticTableDateRangePresets as DateRangePickerProps['presets'];

export function getDefaultDateRange(): string[] {
  return [...staticTableDateRangePresets['今天']];
}

export function getDefaultDateTimeRange(): string[] {
  return [...presetRanges['今天']];
}

export function buildTimeRangeQuery(dateRange: string[]) {
  if (!dateRange?.length) {
    return { timeStart: '', timeEnd: '' };
  }
  return {
    timeStart: dateRange[0],
    timeEnd: dateRange[1],
  };
}

export function buildDateRangeQuery(dateRange: string[], options?: { withTime?: boolean }) {
  if (!dateRange?.length) {
    return { timeStart: '', timeEnd: '' };
  }
  if (options?.withTime === false) {
    const startDate = dateRange[0].slice(0, 10);
    const endDate = dateRange[1].slice(0, 10);
    return {
      timeStart: `${startDate} 00:00:00`,
      timeEnd: `${endDate} 23:59:59`,
    };
  }
  return buildTimeRangeQuery(dateRange);
}

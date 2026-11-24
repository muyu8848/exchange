import dayjs from 'dayjs';
import { DateRangePickerProps } from 'tdesign-vue-next';

import { staticTableDateRangePresets, tableDateRangePresets } from '@/utils/dateRangePresets';

export { tableDateRangePresets } from '@/utils/dateRangePresets';

const isBlank = (text) => {
  return text === null || text === '' || text === undefined;
};

const globalTimeRangePresets: DateRangePickerProps['presets'] = {
  ...staticTableDateRangePresets,
  昨天: [
    dayjs().subtract(1, 'day').startOf('day').format('YYYY-MM-DD'),
    dayjs().subtract(1, 'day').endOf('day').format('YYYY-MM-DD'),
  ],
  最近7天: [dayjs().subtract(6, 'day').startOf('day').format('YYYY-MM-DD'), dayjs().endOf('day').format('YYYY-MM-DD')],
  上个月: [
    dayjs().subtract(1, 'month').startOf('month').format('YYYY-MM-DD'),
    dayjs().subtract(1, 'month').endOf('month').format('YYYY-MM-DD'),
  ],
};
const defaultTimeRange = [dayjs().startOf('day').format('YYYY-MM-DD'), dayjs().endOf('day').format('YYYY-MM-DD')];

export default {
  isBlank,
  globalTimeRangePresets,
  defaultTimeRange,
};

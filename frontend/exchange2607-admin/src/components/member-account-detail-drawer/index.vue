<template>
  <t-drawer v-model:visible="drawerVisible" header="会员详情" size="1200px" :footer="false" attach="body" destroy-on-close>
    <t-loading :loading="loading" size="small">
      <template v-if="member">
        <t-tabs v-model="activeTab" placement="left" class="detail-tabs">
          <t-tab-panel value="info" label="会员信息">
            <!-- 账号信息 -->
            <t-descriptions bordered :column="2" :table-layout="'fixed'" :colon="false" :label-style="labelStyle"
              :content-style="contentStyle" title="账号信息" class="detail-block detail-block--first">
              <t-descriptions-item label="昵称">{{ member.nickName || '—' }}</t-descriptions-item>
              <t-descriptions-item label="UID">
                <template v-if="member.uid">
                  <span class="detail-mono">{{ member.uid }}</span>
                  <copy-icon-button :text="String(member.uid)" />
                </template>
                <span v-else class="text-placeholder">—</span>
              </t-descriptions-item>
              <t-descriptions-item label="账号状态">
                <t-tag size="small" :theme="statusTagTheme(member.status)">
                  {{ member.statusName || member.status || '—' }}
                </t-tag>
              </t-descriptions-item>
              <t-descriptions-item label="注册方式">{{ member.contactTypeName || '—' }}</t-descriptions-item>
              <t-descriptions-item label="通知语言">{{ member.notifyLangName || '—' }}</t-descriptions-item>
              <t-descriptions-item label="手机号">
                <span v-if="member.phone" class="detail-mono">{{ (member.phoneAreaCode ? '+' + member.phoneAreaCode + '' : '') +
                  member.phone }}</span>
                <span v-else class="text-placeholder">—</span>
              </t-descriptions-item>
              <t-descriptions-item label="邮箱">{{ member.email || '—' }}</t-descriptions-item>
              <t-descriptions-item label="邀请码">
                <template v-if="member.inviteCode">
                  <span class="detail-mono">{{ member.inviteCode }}</span>
                  <copy-icon-button :text="member.inviteCode" />
                </template>
                <span v-else class="text-placeholder">—</span>
              </t-descriptions-item>
              <t-descriptions-item label="直属上级">{{ member.inviterUid || '—' }}</t-descriptions-item>
              <t-descriptions-item label="创建时间">{{ member.createTime || '—' }}</t-descriptions-item>
              <t-descriptions-item label="最后登录">{{ member.latelyLoginTime || '—' }}</t-descriptions-item>
              <t-descriptions-item label="谷歌">
                <template v-if="member.googleAuthBindTime">已设置（{{ member.googleAuthBindTime }}）</template>
                <span v-else class="text-placeholder">未设置</span>
              </t-descriptions-item>
            </t-descriptions>

            <!-- 功能状态 -->
            <t-descriptions bordered :column="2" :table-layout="'fixed'" :colon="false" :label-style="labelStyle"
              :content-style="contentStyle" title="功能状态" class="detail-block">
              <t-descriptions-item label="c2c买入">
                <t-space><t-tag size="small" :theme="member.c2cBuyStatus === '1' ? 'primary' : 'warning'">{{
                  member.c2cBuyStatusName
                    }}</t-tag>
                  <t-tag v-if="member.buyRiskLimit" size="small" theme="danger">风控中</t-tag></t-space>

              </t-descriptions-item>
              <t-descriptions-item label="c2c卖出">
                <t-space><t-tag size="small" :theme="member.c2cSellStatus === '1' ? 'primary' : 'warning'">{{
                  member.c2cSellStatusName }}</t-tag>
                  <t-tag v-if="member.sellRiskLimit" size="small" theme="danger">风控中</t-tag></t-space>

              </t-descriptions-item>
            </t-descriptions>

            <!-- 实名信息：未认证/审核不通过时仅显示标题+状态标签 -->
            <div v-if="!authInfo || (member.authenticationStatus !== '3' && member.authenticationStatus !== '2')"
              class="auth-title-only detail-block">
              <span class="auth-title-inline">
                <span class="auth-title-inline__label">实名信息</span>
                <t-tag size="small" :theme="authStatusTheme(member.authenticationStatus)">{{
                  member.authenticationStatusName || '—' }}</t-tag>
              </span>
            </div>
            <!-- 已认证/认证待审核：展示证件资料 -->
            <t-descriptions v-else bordered :column="2" :table-layout="'fixed'" :colon="false" :label-style="labelStyle"
              :content-style="contentStyle" class="detail-block">
              <template #title>
                <span class="auth-title-inline">
                  <span class="auth-title-inline__label">实名信息</span>
                  <t-tag size="small" :theme="authStatusTheme(member.authenticationStatus)">{{
                    member.authenticationStatusName || '—' }}</t-tag>
                </span>
              </template>
              <t-descriptions-item label="真实姓名">{{ authInfo.realName || '—' }}</t-descriptions-item>
              <t-descriptions-item v-if="member.authenticationStatus === '3' && member.authenticationPassedTime"
                label="认证通过">{{ member.authenticationPassedTime }}</t-descriptions-item>
              <t-descriptions-item label="证件类型">{{ authInfo.idTypeName || authInfo.idType || '—' }}</t-descriptions-item>
              <t-descriptions-item label="证件号码">
                {{ authInfo.idNumber || '—' }}
              </t-descriptions-item>
              <t-descriptions-item label="有效期">{{ authInfo.expiryDate || '—' }}</t-descriptions-item>
              <t-descriptions-item label="证件照" :span="2">
                <div v-if="authPics.length" class="auth-pics">
                  <div v-for="(pic, index) in authPics" :key="index" class="auth-pic" @click="openAuthViewer(index)">
                    <t-image :src="pic" fit="cover" class="auth-pic__img" />
                    <span class="auth-pic__tag">{{ authPicLabel(index) }}</span>
                  </div>
                </div>
                <span v-else class="text-placeholder">未上传证件照片</span>
              </t-descriptions-item>
            </t-descriptions>
            <t-image-viewer v-model:visible="authViewerVisible" v-model:index="authViewerIndex" :images="authPics"
              :z-index="3000" />
          </t-tab-panel>

          <t-tab-panel value="asset" label="会员资产">
            <t-loading :loading="assetLoading" size="small">
              <template v-if="assetSummary">
                <div class="asset-total">
                  <span class="asset-total__item">总资产折合 USDT：<b>{{ assetSummary.totalValue?.totalUsdt ?? '—'
                      }}</b></span>
                  <span class="asset-total__item asset-total__item--fiat">
                    折合
                    <t-select v-model="fiatCode" :options="fiatOptions" size="small" style="width: 84px" />
                    <b>{{ assetSummary.totalValue?.totalFiat ?? '—' }}</b>
                  </span>
                </div>
                <div class="asset-block">
                  <div class="asset-block__title">按账户类型</div>
                  <t-table :data="assetSummary.byAccountType || []" :columns="accountColumns" row-key="accountType"
                    size="small" table-layout="auto" bordered />
                </div>
                <div class="asset-block">
                  <div class="asset-block__title">按币种</div>
                  <t-table :data="assetSummary.byCurrency || []" :columns="currencyColumns" row-key="currencyCode"
                    size="small" table-layout="auto" bordered />
                </div>
              </template>
              <div v-else class="text-placeholder">暂无资产数据</div>
            </t-loading>
          </t-tab-panel>

          <t-tab-panel value="accountSecurity" label="账户安全" lazy>
            <AccountSecurityTab :member-uid="member.uid" :member-id="member.id" />
          </t-tab-panel>

          <t-tab-panel value="fundFlow" label="资金流水" lazy>
            <FundFlowTab :member-uid="member.uid" />
          </t-tab-panel>

          <t-tab-panel value="depositWithdraw" label="充提记录" lazy>
            <DepositWithdrawTab :member-uid="member.uid" />
          </t-tab-panel>

          <t-tab-panel value="spotTrade" label="现货交易" lazy>
            <SpotTradeTab :member-uid="member.uid" />
          </t-tab-panel>

          <t-tab-panel value="secondContract" label="秒合约交易" lazy>
            <SecondContractTradeTab :member-uid="member.uid" />
          </t-tab-panel>

          <t-tab-panel value="perpTrade" label="永续合约交易" lazy>
            <PerpTradeTab :member-uid="member.uid" />
          </t-tab-panel>

          <t-tab-panel value="c2c" label="C2C交易" lazy>
            <C2cTab :member-id="member.id" :member-uid="member.uid" />
          </t-tab-panel>

          <t-tab-panel value="investment" label="理财赚币" lazy>
            <InvestmentTab :member-uid="member.uid" />
          </t-tab-panel>

          <t-tab-panel value="team" label="邀请关系" lazy>
            <TeamTab :member-id="member.id" />
          </t-tab-panel>

          <t-tab-panel value="notification" label="会员消息" lazy>
            <NotificationTab :member-uid="member.uid" />
          </t-tab-panel>
        </t-tabs>
      </template>
    </t-loading>
  </t-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import { findC2cFiatAll } from '@/api/c2c';
import { findLatestAuthInfoByMemberAccountId, findMemberById, getMemberAssetSummary } from '@/api/member';
import type { LatestAuthInfo, MemberAssetSummaryVO } from '@/api/member';
import CopyIconButton from '@/components/copy-icon-button/index.vue';

import AccountSecurityTab from './account-security-tab.vue';
import C2cTab from './c2c-tab.vue';
import DepositWithdrawTab from './deposit-withdraw-tab.vue';
import FundFlowTab from './fund-flow-tab.vue';
import InvestmentTab from './investment-tab.vue';
import NotificationTab from './notification-tab.vue';
import PerpTradeTab from './perp-trade-tab.vue';
import SecondContractTradeTab from './second-contract-trade-tab.vue';
import SpotTradeTab from './spot-trade-tab.vue';
import TeamTab from './team-tab.vue';

const labelStyle = { width: '100px', verticalAlign: 'middle' };
const contentStyle = { verticalAlign: 'middle' };

/** 账号状态标签配色（后端取值为 NORMAL / FROZEN，与会员列表页保持一致） */
function statusTagTheme(status?: string): 'primary' | 'warning' | 'danger' {
  if (status === 'NORMAL') {
    return 'primary';
  }
  if (status === 'FROZEN') {
    return 'warning';
  }
  return 'danger';
}

/** 实名认证状态标签配色（1 未认证 / 2 认证待审核 / 3 已认证 / 4 审核不通过） */
function authStatusTheme(status?: string): 'success' | 'warning' | 'default' | 'danger' {
  if (status === '3') {
    return 'success';
  }
  if (status === '2') {
    return 'warning';
  }
  if (status === '4') {
    return 'danger';
  }
  return 'default';
}

interface MemberDetail {
  id: string;
  uid?: number | string;
  nickName?: string;
  realName?: string;
  status: string;
  statusName: string;
  c2cBuyStatus?: string;
  c2cBuyStatusName?: string;
  c2cSellStatus?: string;
  c2cSellStatusName?: string;
  buyRiskLimit?: boolean;
  sellRiskLimit?: boolean;
  inviteCode?: string;
  authenticationStatus?: string;
  authenticationStatusName?: string;
  authenticationPassedTime?: string;
  inviterUid?: string | number;
  latelyLoginTime?: string;
  googleAuthBindTime?: string;
  createTime?: string;
  // 联系方式
  email?: string;
  phone?: string;
  phoneAreaCode?: string;
  contactType?: string;
  contactTypeName?: string;
  // 通知语言
  notifyLangCode?: string;
  notifyLangName?: string;
}

const props = defineProps<{
  visible: boolean;
  memberId: string;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
}>();

const loading = ref(false);
const member = ref<MemberDetail | null>(null);

/** 最近一次实名记录（已认证/认证待审核时有值，用于证件资料展示） */
const authInfo = ref<LatestAuthInfo | null>(null);
const authPics = computed(() => [authInfo.value?.identityCardPic1, authInfo.value?.identityCardPic2].filter(Boolean) as string[]);
function authPicLabel(index: number) {
  if (authInfo.value?.idType === 'passport') {
    return '资料页';
  }
  return index === 0 ? '正面' : '反面';
}
const authViewerVisible = ref(false);
const authViewerIndex = ref(0);
function openAuthViewer(index: number) {
  authViewerIndex.value = index;
  authViewerVisible.value = true;
}

const activeTab = ref('info');
const assetSummary = ref<MemberAssetSummaryVO | null>(null);
const assetLoading = ref(false);
const assetLoaded = ref(false);
/** 折算货币（默认 CNY，与后端 getTotalValue 的回落值一致） */
const fiatCode = ref('CNY');
const fiatOptions = ref<{ label: string, value: string }[]>([]);

const accountColumns = [
  { colKey: 'accountTypeName', title: '账户类型' },
  { colKey: 'usdtValue', title: '折合 USDT' },
];

const currencyColumns = [
  { colKey: 'currencyCode', title: '币种' },
  { colKey: 'available', title: '可用' },
  { colKey: 'frozen', title: '冻结' },
  { colKey: 'total', title: '总量' },
  { colKey: 'usdtValue', title: '折合 USDT' },
];

const drawerVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
});

watch(
  () => [props.visible, props.memberId] as const,
  async ([visible, memberId]) => {
    if (!visible || !memberId) {
      member.value = null;
      authInfo.value = null;
      activeTab.value = 'info';
      assetSummary.value = null;
      assetLoaded.value = false;
      return;
    }
    loading.value = true;
    try {
      const data = await findMemberById(memberId);
      member.value = data as unknown as MemberDetail;
      if (member.value?.id) {
        try {
          authInfo.value = await findLatestAuthInfoByMemberAccountId(member.value.id);
        } catch {
          authInfo.value = null;
        }
      }
      else {
        authInfo.value = null;
      }
    } finally {
      loading.value = false;
    }
  },
);

const loadAssets = async () => {
  if (!props.memberId) {
    return;
  }
  assetLoading.value = true;
  try {
    assetSummary.value = await getMemberAssetSummary(props.memberId, fiatCode.value);
    assetLoaded.value = true;
  } finally {
    assetLoading.value = false;
  }
};

/** 加载可选法币（复用 c2c 法币配置，只需加载一次） */
async function loadFiatOptions() {
  if (fiatOptions.value.length) {
    return;
  }
  try {
    const list = await findC2cFiatAll();
    fiatOptions.value = (list || [])
      .filter(f => f.fiatCode)
      .map(f => ({ label: f.fiatCode as string, value: f.fiatCode as string }));
  } catch {
    // 忽略：下拉退化为仅显示当前值
  }
}

// 切换折算货币后重新拉取（仅抽屉打开且当前在资产 tab 时）
watch(fiatCode, () => {
  if (props.visible && activeTab.value === 'asset') {
    loadAssets();
  }
});

watch(activeTab, (tab) => {
  if (tab === 'asset' && props.memberId) {
    loadFiatOptions();
    if (!assetLoaded.value) {
      loadAssets();
    }
  }
});
</script>

<style lang="less" scoped>
.detail-block {
  margin-bottom: 20px;
}

/* 首个描述块与 tab 标题拉开间距，避免标题贴得太近 */
.detail-block--first {
  margin-top: 16px;
}

.detail-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  word-break: break-all;
}

.text-placeholder {
  color: var(--td-text-color-placeholder);
}

/* 实名信息标题（含状态标签） */
.auth-title-only {
  padding: 12px 16px;
  border: 1px solid var(--td-component-stroke);
  border-radius: var(--td-radius-medium);
  background: var(--td-bg-color-container);
}

/* 标题文字与状态标签之间留出间距 */
.auth-title-inline {
  display: inline-flex;
  align-items: center;
  gap: 12px;

  &__label {
    font-size: 14px;
    font-weight: 600;
    color: var(--td-text-color-primary);
  }
}

/* 证件照缩略图（点击查看大图） */
.auth-pics {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.auth-pic {
  position: relative;
  width: 200px;
  height: 130px;
  overflow: hidden;
  border: 1px solid var(--td-border-level-1-color);
  border-radius: 6px;
  background: var(--td-bg-color-secondarycontainer);
  cursor: zoom-in;

  &__img {
    width: 100%;
    height: 100%;
  }

  &__tag {
    position: absolute;
    right: 0;
    bottom: 0;
    left: 0;
    background: rgba(0, 0, 0, 0.55);
    color: #fff;
    font-size: 12px;
    line-height: 22px;
    text-align: center;
  }
}

.detail-tabs {
  margin-top: 4px;

  /* 左侧竖排 tab 与右侧内容之间留出间距（只作用于一级内容区，避免影响子组件里的二级 tab） */
  > :deep(.t-tabs__content) {
    padding-left: 24px;
  }
}

.asset-total {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 24px;
  margin-top: 12px;
  margin-bottom: 16px;
  font-size: 14px;
}

.asset-total__item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.asset-total__item--fiat {
  gap: 4px;
}

.asset-total__item b {
  color: var(--td-text-color-primary);
}

.asset-block {
  margin-bottom: 16px;
}

.asset-block__title {
  font-weight: 600;
  margin-bottom: 8px;
}
</style>

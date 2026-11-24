import { defineStore } from 'pinia'
import type { MemberInfo } from '@/api/types'
import { apiData } from '@/api/http'
import { memberApi } from '@/api/modules'
import { useAuthStore } from '@/store/auth'

/** Cache freshness window (ms). Within this period, no refetch occurs. */
const FRESH_TTL = 10_000

let lastFetchTime = 0
let inflight: Promise<void> | null = null

export const useMemberStore = defineStore('member', {
  state: () => ({
    memberInfo: {} as MemberInfo,
  }),
  actions: {
    /**
     * SWR strategy:
     * 1. Fresh cache (< FRESH_TTL) → return immediately, no network.
     * 2. Stale cache → return immediately AND trigger background refresh.
     * 3. No cache → fetch and wait.
     */
    async ensureMemberInfo(): Promise<MemberInfo> {
      const now = Date.now()
      if (lastFetchTime && now - lastFetchTime < FRESH_TTL) {
        return this.memberInfo
      }
      if (lastFetchTime && Object.keys(this.memberInfo).length > 0) {
        this._fetch()
        return this.memberInfo
      }
      await this._fetch()
      return this.memberInfo
    },

    /** Force a fresh fetch regardless of cache state. */
    async refreshMemberInfo(): Promise<void> {
      await this._fetch()
    },

    async _fetch(): Promise<void> {
      // 游客态不请求会员信息：该接口需登录，请求会触发全局「未登录」处理把用户踢回登录页，
      // 导致首页等免登录页面留不住人。重置时间戳，避免登录后命中空缓存。
      const authStore = useAuthStore()
      if (!authStore.isLoggedIn) {
        this.memberInfo = {} as MemberInfo
        lastFetchTime = 0
        return
      }
      if (inflight) {
        return inflight
      }
      inflight = (async () => {
        try {
          const data = await apiData(memberApi.getMemberInfo())
          this.memberInfo = data || {}
          lastFetchTime = Date.now()
        }
        catch {
          // keep existing data on error
        }
        finally {
          inflight = null
        }
      })()
      return inflight
    },

    clearMemberInfo() {
      this.memberInfo = {} as MemberInfo
      lastFetchTime = 0
    },
  },
})

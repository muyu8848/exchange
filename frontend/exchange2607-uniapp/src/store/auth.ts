import { defineStore } from 'pinia'
import type { TokenInfo } from '@/api/types'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    tokenName: '' as string,
    tokenValue: '' as string,
    accountId: '' as string,
  }),
  getters: {
    isLoggedIn: state => !!state.tokenName && !!state.tokenValue,
  },
  actions: {
    loadFromStorage() {
      this.tokenName = uni.getStorageSync('tokenName') || ''
      this.tokenValue = uni.getStorageSync('tokenValue') || ''
      this.accountId = uni.getStorageSync('accountId') || ''
    },
    setToken(info: TokenInfo) {
      this.tokenName = info.tokenName
      this.tokenValue = info.tokenValue
      this.accountId = info.accountId
      uni.setStorageSync('tokenName', info.tokenName)
      uni.setStorageSync('tokenValue', info.tokenValue)
      uni.setStorageSync('accountId', info.accountId)
    },
    clearAuth() {
      this.tokenName = ''
      this.tokenValue = ''
      this.accountId = ''
      uni.removeStorageSync('tokenName')
      uni.removeStorageSync('tokenValue')
      uni.removeStorageSync('accountId')
    },
  },
})

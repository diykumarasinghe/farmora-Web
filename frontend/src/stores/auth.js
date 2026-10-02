import { defineStore } from 'pinia'
import { authService } from '../services/authService'

export const useAuthStore = defineStore('auth', {
  state: () => {
    let savedUser = null
    try {
      savedUser = JSON.parse(localStorage.getItem('farmora_user'))
    } catch (e) {
      savedUser = null
    }

    return {
      token: localStorage.getItem('farmora_token') || null,
      user: savedUser,
      loading: false,
      error: null
    }
  },

  getters: {
    isAuthenticated: (state) => !!state.token && !!state.user,
    role: (state) => state.user ? state.user.role : null,
    isFarmer: (state) => state.user?.role === 'FARMER',
    isBuyer: (state) => state.user?.role === 'BUYER',
    isDeliveryPartner: (state) => state.user?.role === 'DELIVERY_PARTNER',
    isAdmin: (state) => state.user?.role === 'ADMIN'
  },

  actions: {
    async register(userData) {
      this.loading = true
      this.error = null
      try {
        const response = await authService.register(userData)
        this.setAuthData(response.token, response.user)
        return response
      } catch (err) {
        this.error = err.response?.data?.message || 'Registration failed'
        throw err
      } finally {
        this.loading = false
      }
    },

    async login(credentials) {
      this.loading = true
      this.error = null
      try {
        const response = await authService.login(credentials)
        this.setAuthData(response.token, response.user)
        return response
      } catch (err) {
        this.error = err.response?.data?.message || 'Invalid credentials'
        throw err
      } finally {
        this.loading = false
      }
    },

    setAuthData(token, user) {
      this.token = token
      this.user = user
      localStorage.setItem('farmora_token', token)
      localStorage.setItem('farmora_user', JSON.stringify(user))
    },

    async restoreSession() {
      if (!this.token) return null
      try {
        const user = await authService.getCurrentUser()
        this.user = user
        localStorage.setItem('farmora_user', JSON.stringify(user))
        return user
      } catch (err) {
        this.logout()
        return null
      }
    },

    logout() {
      this.token = null
      this.user = null
      this.error = null
      localStorage.removeItem('farmora_token')
      localStorage.removeItem('farmora_user')
    }
  }
})

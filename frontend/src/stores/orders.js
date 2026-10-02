import { defineStore } from 'pinia'
import { orderService } from '../services/orderService'

export const useOrderStore = defineStore('orders', {
  state: () => ({
    orders: [],
    currentOrder: null,
    loading: false,
    error: null
  }),

  actions: {
    async fetchMyOrders() {
      this.loading = true
      this.error = null
      try {
        this.orders = await orderService.getMyOrders()
      } catch (err) {
        this.error = err.response?.data?.message || 'Failed to load orders'
      } finally {
        this.loading = false
      }
    },

    async fetchOrderById(id) {
      this.loading = true
      this.error = null
      try {
        this.currentOrder = await orderService.getOrderById(id)
        return this.currentOrder
      } catch (err) {
        this.error = err.response?.data?.message || 'Failed to load order'
        throw err
      } finally {
        this.loading = false
      }
    },

    async updateStatus(id, status) {
      try {
        const updated = await orderService.updateOrderStatus(id, status)
        const idx = this.orders.findIndex(o => o.id === id)
        if (idx !== -1) {
          this.orders[idx] = updated
        }
        if (this.currentOrder && this.currentOrder.id === id) {
          this.currentOrder = updated
        }
        return updated
      } catch (err) {
        throw err
      }
    }
  }
})

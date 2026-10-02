import api from './api'

export const bulkOrderService = {
  async createBulkOrder(data) {
    const res = await api.post('/bulk-orders', data)
    return res.data
  },

  async getMyBulkOrders() {
    const res = await api.get('/bulk-orders/my')
    return res.data
  },

  async getBulkOrderById(id) {
    const res = await api.get(`/bulk-orders/${id}`)
    return res.data
  },

  async modifyQuantity(id, data) {
    const res = await api.put(`/bulk-orders/${id}/quantity`, data)
    return res.data
  },

  async updateStatus(id, status, notes = '') {
    const res = await api.put(`/bulk-orders/${id}/status`, null, {
      params: { status, notes }
    })
    return res.data
  },

  async cancelBulkOrder(id, reason = '') {
    const res = await api.put(`/bulk-orders/${id}/cancel`, null, {
      params: { reason }
    })
    return res.data
  }
}

import api from './api'

export const orderService = {
  async createDirectOrder(data) {
    const res = await api.post('/orders', data)
    return res.data
  },

  async getMyOrders() {
    const res = await api.get('/orders/my')
    return res.data
  },

  async getOrderById(id) {
    const res = await api.get(`/orders/${id}`)
    return res.data
  },

  async updateOrderStatus(id, status) {
    const res = await api.put(`/orders/${id}/status`, null, { params: { status } })
    return res.data
  },

  async cancelOrder(id, reason = '') {
    const res = await api.put(`/orders/${id}/cancel`, { reason })
    return res.data
  }
}

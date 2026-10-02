import api from './api'

export const orderService = {
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
  }
}

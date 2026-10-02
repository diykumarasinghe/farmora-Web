import api from './api'

export const paymentService = {
  async processPayment(data) {
    const res = await api.post('/payments/process', data)
    return res.data
  },

  async getMyPayments() {
    const res = await api.get('/payments/my')
    return res.data
  },

  async getPaymentsByOrderId(orderId) {
    const res = await api.get(`/payments/order/${orderId}`)
    return res.data
  },

  async refundPayment(id, data) {
    const res = await api.post(`/payments/${id}/refund`, data)
    return res.data
  },

  async cancelTransaction(id) {
    const res = await api.post(`/payments/${id}/cancel`)
    return res.data
  }
}

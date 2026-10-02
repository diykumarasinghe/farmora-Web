import api from './api'

export const deliveryService = {
  async createDeliveryRequest(data) {
    const res = await api.post('/deliveries/request', data)
    return res.data
  },

  async getAvailableDeliveries() {
    const res = await api.get('/deliveries/available')
    return res.data
  },

  async getMyDeliveries() {
    const res = await api.get('/deliveries/my')
    return res.data
  },

  async getDeliveryById(id) {
    const res = await api.get(`/deliveries/${id}`)
    return res.data
  },

  async getDeliveryByOrderId(orderId) {
    const res = await api.get(`/deliveries/order/${orderId}`)
    return res.data
  },

  async acceptDelivery(id) {
    const res = await api.put(`/deliveries/${id}/accept`)
    return res.data
  },

  async updateDeliveryStatus(id, status, notes = '') {
    const res = await api.put(`/deliveries/${id}/status`, null, {
      params: { status, notes }
    })
    return res.data
  },

  async redirectDelivery(id, data) {
    const res = await api.put(`/deliveries/${id}/redirect`, data)
    return res.data
  },

  async cancelDelivery(id, reason = '') {
    const res = await api.put(`/deliveries/${id}/cancel`, null, {
      params: { reason }
    })
    return res.data
  },

  async updateLocation(id, data) {
    const res = await api.put(`/deliveries/${id}/location`, data)
    return res.data
  }
}

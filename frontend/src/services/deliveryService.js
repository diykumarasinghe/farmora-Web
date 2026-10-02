import api from './api'

export const deliveryService = {
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

  async acceptDelivery(id) {
    const res = await api.put(`/deliveries/${id}/accept`)
    return res.data
  },

  async updateDeliveryStatus(id, status, notes = '') {
    const res = await api.put(`/deliveries/${id}/status`, null, {
      params: { status, notes }
    })
    return res.data
  }
}

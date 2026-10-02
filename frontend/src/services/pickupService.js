import api from './api'

export const pickupService = {
  async schedulePickup(data) {
    const res = await api.post('/pickups/schedule', data)
    return res.data
  },

  async getMyPickups() {
    const res = await api.get('/pickups/my')
    return res.data
  },

  async getPickupByOrderId(orderId) {
    const res = await api.get(`/pickups/order/${orderId}`)
    return res.data
  },

  async reschedulePickup(id, data) {
    const res = await api.put(`/pickups/${id}/reschedule`, data)
    return res.data
  },

  async cancelPickup(id, reason = '') {
    const res = await api.put(`/pickups/${id}/cancel`, null, { params: { reason } })
    return res.data
  },

  async updateStatus(id, status, passcode = '') {
    const res = await api.put(`/pickups/${id}/status`, null, { params: { status, passcode } })
    return res.data
  }
}

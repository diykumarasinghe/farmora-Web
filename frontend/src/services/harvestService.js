import api from './api'

export const harvestService = {
  async postDailyHarvest(data) {
    const res = await api.post('/harvest/daily', data)
    return res.data
  },

  async getHarvestDetails(harvestId) {
    const res = await api.get(`/harvest/${harvestId}`)
    return res.data
  },

  async updateQuantity(harvestId, quantity) {
    const res = await api.put(`/harvest/${harvestId}/quantity?quantity=${quantity}`)
    return res.data
  },

  async archiveHarvest(harvestId) {
    const res = await api.delete(`/harvest/${harvestId}`)
    return res.data
  },

  async getFarmerTodayPosts(farmerId) {
    const res = await api.get(`/harvest/farmer/${farmerId}/today`)
    return res.data
  },

  async getFarmerAllPosts(farmerId) {
    const res = await api.get(`/harvest/farmer/${farmerId}/all`)
    return res.data
  },

  async updateStatus(harvestId, status) {
    const res = await api.put(`/harvest/${harvestId}/status?status=${status}`)
    return res.data
  },

  async getAvailableNow() {
    const res = await api.get('/harvest/available/now')
    return res.data
  },

  async getHarvestAnalytics(farmerId) {
    const res = await api.get(`/harvest/${farmerId}/analytics`)
    return res.data
  },

  async extendHarvestDate(harvestId, days = 2) {
    const res = await api.put(`/harvest/${harvestId}/extend?days=${days}`)
    return res.data
  },

  async cleanupExpired() {
    const res = await api.delete('/harvest/cleanup')
    return res.data
  }
}

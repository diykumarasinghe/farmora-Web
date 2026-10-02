import api from './api'

export const bidService = {
  // Group 3 CRUDs
  async createBid(data) {
    const res = await api.post('/bids', data)
    return res.data
  },

  async getBidById(bidId) {
    const res = await api.get(`/bids/${bidId}`)
    return res.data
  },

  async updateBid(bidId, action = 'accept', counterData = null) {
    const res = await api.put(`/bids/${bidId}?action=${action}`, counterData)
    return res.data
  },

  async withdrawBid(bidId) {
    const res = await api.delete(`/bids/${bidId}`)
    return res.data
  },

  async getFarmerBids(farmerId) {
    const res = await api.get(`/bids/farmer/${farmerId}`)
    return res.data
  },

  async getBuyerBids(buyerId) {
    const res = await api.get(`/bids/buyer/${buyerId}`)
    return res.data
  },

  async sendCounterOffer(bidId, data) {
    const res = await api.post(`/bids/${bidId}/counter`, data)
    return res.data
  },

  async getBidHistory(bidId) {
    const res = await api.get(`/bids/${bidId}/history`)
    return res.data
  },

  async extendBidExpiry(bidId, hours = 24) {
    const res = await api.put(`/bids/${bidId}/extend?hours=${hours}`)
    return res.data
  },

  async archiveBid(bidId) {
    const res = await api.delete(`/bids/${bidId}/archive`)
    return res.data
  },

  // Convenience methods
  async getMyBids() {
    const res = await api.get('/bids/my')
    return res.data
  },

  async getFarmerReceivedBids() {
    const res = await api.get('/bids/farmer/received')
    return res.data
  },

  async getProductBids(productId) {
    const res = await api.get(`/bids/product/${productId}`)
    return res.data
  },

  async acceptCounterOffer(bidId) {
    const res = await api.put(`/bids/${bidId}/accept-counter`)
    return res.data
  },

  async acceptBid(bidId) {
    const res = await api.put(`/bids/${bidId}/accept`)
    return res.data
  },

  async rejectBid(bidId) {
    const res = await api.put(`/bids/${bidId}/reject`)
    return res.data
  },

  async cancelBid(bidId) {
    const res = await api.put(`/bids/${bidId}/cancel`)
    return res.data
  }
}

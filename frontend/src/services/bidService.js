import api from './api'

export const bidService = {
  async createBid(data) {
    const res = await api.post('/bids', data)
    return res.data
  },

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

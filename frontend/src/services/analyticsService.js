import api from './api'

export const analyticsService = {
  async getFarmerAnalytics() {
    const res = await api.get('/analytics/farmer')
    return res.data
  },

  async getBuyerAnalytics() {
    const res = await api.get('/analytics/buyer')
    return res.data
  },

  async getSavedProducts() {
    const res = await api.get('/buyer/saved-products')
    return res.data
  },

  async saveProduct(productId) {
    const res = await api.post(`/buyer/saved-products/${productId}`)
    return res.data
  },

  async removeSavedProduct(productId) {
    const res = await api.delete(`/buyer/saved-products/${productId}`)
    return res.data
  }
}

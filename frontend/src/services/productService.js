import api from './api'
import { MARKET_PRICE_BENCHMARKS } from '../utils/agriculturalData'

export const productService = {
  async getProducts(params = {}) {
    const res = await api.get('/products', { params })
    return res.data
  },

  async getProductById(id) {
    const res = await api.get(`/products/${id}`)
    return res.data
  },

  async getFarmerProducts() {
    const res = await api.get('/products/farmer/my')
    return res.data
  },

  async createProduct(data) {
    const res = await api.post('/products', data)
    return res.data
  },

  async updateProduct(id, data) {
    const res = await api.put(`/products/${id}`, data)
    return res.data
  },

  async updateProductStatus(id, status) {
    const res = await api.put(`/products/${id}/status`, null, { params: { status } })
    return res.data
  },

  async deleteProduct(id) {
    const res = await api.delete(`/products/${id}`)
    return res.data
  },

  async archiveProduct(id) {
    // Try updating status to ARCHIVED or SOLD_OUT
    try {
      return await this.updateProductStatus(id, 'ARCHIVED')
    } catch {
      return await this.updateProductStatus(id, 'SOLD_OUT')
    }
  },

  async getMarketPriceBenchmarks() {
    return MARKET_PRICE_BENCHMARKS
  }
}

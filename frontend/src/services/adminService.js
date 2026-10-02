import api from './api'

export const adminService = {
  async getStats() {
    const res = await api.get('/admin/stats')
    return res.data
  },

  async getUsers() {
    const res = await api.get('/admin/users')
    return res.data
  },

  async toggleUserStatus(id) {
    const res = await api.put(`/admin/users/${id}/toggle-status`)
    return res.data
  },

  async getProducts() {
    const res = await api.get('/admin/products')
    return res.data
  },

  async updateProductStatus(id, status) {
    const res = await api.put(`/admin/products/${id}/status`, null, { params: { status } })
    return res.data
  },

  async getOrders() {
    const res = await api.get('/admin/orders')
    return res.data
  }
}

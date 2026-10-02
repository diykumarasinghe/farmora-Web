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
  },

  // Farmer Verification
  async getVerifications() {
    const res = await api.get('/admin/verifications')
    return res.data
  },

  async approveFarmerVerification(id, notes) {
    const res = await api.put(`/admin/verifications/${id}/approve?notes=${encodeURIComponent(notes || 'Approved by Admin')}`)
    return res.data
  },

  async rejectFarmerVerification(id, notes) {
    const res = await api.put(`/admin/verifications/${id}/reject?notes=${encodeURIComponent(notes || 'Documentation requirements not met')}`)
    return res.data
  },

  // Transactions Monitoring
  async getTransactions() {
    const res = await api.get('/admin/transactions')
    return res.data
  },

  // Complaints / Dispute Handling
  async getComplaints() {
    const res = await api.get('/admin/complaints')
    return res.data
  },

  async updateComplaintStatus(id, status, adminNotes) {
    const res = await api.put(`/admin/complaints/${id}/status?status=${status}&adminNotes=${encodeURIComponent(adminNotes || '')}`)
    return res.data
  }
}

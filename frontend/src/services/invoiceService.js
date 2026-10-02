import api from './api'

export const invoiceService = {
  async generateInvoice(orderId) {
    const res = await api.post(`/invoices/generate/${orderId}`)
    return res.data
  },

  async getInvoiceByOrderId(orderId) {
    const res = await api.get(`/invoices/order/${orderId}`)
    return res.data
  },

  async getMyInvoices(archived = null) {
    const params = archived !== null ? { archived } : {}
    const res = await api.get('/invoices/my', { params })
    return res.data
  },

  async updateInvoice(id, data) {
    const res = await api.put(`/invoices/${id}`, data)
    return res.data
  },

  async toggleArchive(id) {
    const res = await api.put(`/invoices/${id}/archive`)
    return res.data
  }
}

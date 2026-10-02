import api from './api'

export const complaintService = {
  async submitComplaint(data) {
    const res = await api.post('/complaints', data)
    return res.data
  },

  async getMyComplaints() {
    const res = await api.get('/complaints/my')
    return res.data
  },

  async getAllComplaints() {
    const res = await api.get('/admin/complaints')
    return res.data
  },

  async updateComplaintStatus(id, status, adminNotes) {
    const res = await api.put(`/admin/complaints/${id}/status?status=${status}&adminNotes=${encodeURIComponent(adminNotes || '')}`)
    return res.data
  }
}

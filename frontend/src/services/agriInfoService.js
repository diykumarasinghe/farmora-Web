import api from './api'

export const agriInfoService = {
  async getPosts(category, province) {
    let url = '/agri-info'
    const params = new URLSearchParams()
    if (category) params.append('category', category)
    if (province) params.append('province', province)
    if (params.toString()) {
      url += `?${params.toString()}`
    }
    const res = await api.get(url)
    return res.data
  },

  async getPostById(id) {
    const res = await api.get(`/agri-info/${id}`)
    return res.data
  },

  async createPost(data) {
    const res = await api.post('/agri-info', data)
    return res.data
  },

  async deletePost(id) {
    const res = await api.delete(`/agri-info/${id}`)
    return res.data
  }
}

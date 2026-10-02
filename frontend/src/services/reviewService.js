import api from './api'

export const reviewService = {
  async createReview(data) {
    const res = await api.post('/reviews', data)
    return res.data
  },

  async updateReview(id, data) {
    const res = await api.put(`/reviews/${id}`, data)
    return res.data
  },

  async deleteReview(id) {
    const res = await api.delete(`/reviews/${id}`)
    return res.data
  },

  async getReviewsForUser(userId) {
    const res = await api.get(`/reviews/user/${userId}`)
    return res.data
  },

  async getProductReviews(productId) {
    const res = await api.get(`/reviews/product/${productId}`)
    return res.data
  },

  async getMyReviews() {
    const res = await api.get('/reviews/my')
    return res.data
  },

  async getRatingSummary(farmerId) {
    const res = await api.get(`/reviews/summary/${farmerId}`)
    return res.data
  }
}

import api from './api'

export const reviewService = {
  async createReview(data) {
    const res = await api.post('/reviews', data)
    return res.data
  },

  async getReviewsForUser(userId) {
    const res = await api.get(`/reviews/user/${userId}`)
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

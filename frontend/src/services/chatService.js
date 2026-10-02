import api from './api'

export const chatService = {
  async sendMessage(data) {
    const res = await api.post('/messages', data)
    return res.data
  },

  async getConversations() {
    const res = await api.get('/messages/conversations')
    return res.data
  },

  async getConversation(otherUserId) {
    const res = await api.get(`/messages/conversation/${otherUserId}`)
    return res.data
  },

  async markAsRead(messageId) {
    const res = await api.put(`/messages/${messageId}/read`)
    return res.data
  },

  async markConversationAsRead(otherUserId) {
    const res = await api.put(`/messages/conversation/${otherUserId}/read`)
    return res.data
  },

  async deleteMessage(messageId) {
    const res = await api.delete(`/messages/${messageId}`)
    return res.data
  }
}

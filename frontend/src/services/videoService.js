import api from './api'

export const videoService = {
  async initiateCall(data) {
    const res = await api.post('/video-calls/initiate', data)
    return res.data
  },

  async scheduleMeeting(data) {
    const res = await api.post('/video-calls/schedule', data)
    return res.data
  },

  async getMyCalls() {
    const res = await api.get('/video-calls/my')
    return res.data
  },

  async endCall(callId, durationSeconds) {
    const res = await api.put(`/video-calls/${callId}/end?durationSeconds=${durationSeconds || 120}`)
    return res.data
  },

  async cancelCall(callId) {
    const res = await api.put(`/video-calls/${callId}/cancel`)
    return res.data
  }
}

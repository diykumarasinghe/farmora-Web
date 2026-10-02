import api from './api'
import { SAMPLE_FARMS, calculateDistance } from '../utils/agriculturalData'

export const farmService = {
  // Farmer actions
  getMyFarmProfile: async () => {
    const res = await api.get('/farm-profiles/my')
    return res.data
  },

  saveFarmProfile: async (data) => {
    const res = await api.post('/farm-profiles/my', data)
    return res.data
  },

  updateFarmLocation: async (latitude, longitude, farmAddress) => {
    let currentProfile = {}
    try {
      currentProfile = await farmService.getMyFarmProfile()
    } catch (e) {
      console.warn('Could not fetch existing profile before updating location', e)
    }
    const payload = {
      ...currentProfile,
      latitude,
      longitude,
      ...(farmAddress ? { farmAddress } : {})
    }
    return await farmService.saveFarmProfile(payload)
  },

  requestVerification: async () => {
    const res = await api.post('/farm-profiles/my/request-verification')
    return res.data
  },

  toggleArchiveProfile: async () => {
    const res = await api.patch('/farm-profiles/my/archive')
    return res.data
  },

  // Public views
  getFarmProfileByFarmerId: async (farmerId) => {
    const res = await api.get(`/farm-profiles/farmer/${farmerId}`)
    return res.data
  },

  getFarmProfileById: async (id) => {
    const res = await api.get(`/farm-profiles/${id}`)
    return res.data
  },

  getFarms: async (params = {}) => {
    const res = await api.get('/farm-profiles', { params })
    const farms = res.data
    // If backend returns farms, return them. If empty, return sample Sri Lankan verified farms.
    if (Array.isArray(farms) && farms.length > 0) {
      return farms
    }
    return SAMPLE_FARMS
  },

  // Admin actions
  adminVerifyFarm: async (farmId, status, notes = '') => {
    const res = await api.put(`/farm-profiles/${farmId}/verify`, null, {
      params: { status, notes }
    })
    return res.data
  },

  calculateDistance
}

import api from './api'

export const buyerService = {
  // Delivery address management
  getMyAddresses: async () => {
    const res = await api.get('/buyer/addresses')
    return res.data
  },

  addAddress: async (data) => {
    const res = await api.post('/buyer/addresses', data)
    return res.data
  },

  updateAddress: async (id, data) => {
    const res = await api.put(`/buyer/addresses/${id}`, data)
    return res.data
  },

  deleteAddress: async (id) => {
    const res = await api.delete(`/buyer/addresses/${id}`)
    return res.data
  },

  setDefaultAddress: async (id) => {
    const res = await api.put(`/buyer/addresses/${id}/default`)
    return res.data
  },

  // Favourite farms
  getFavouriteFarms: async () => {
    const res = await api.get('/buyer/favourites')
    return res.data
  },

  addFavouriteFarm: async (farmerId) => {
    const res = await api.post(`/buyer/favourites/${farmerId}`)
    return res.data
  },

  removeFavouriteFarm: async (farmerId) => {
    const res = await api.delete(`/buyer/favourites/${farmerId}`)
    return res.data
  },

  checkIsFavourite: async (farmerId) => {
    const res = await api.get(`/buyer/favourites/check/${farmerId}`)
    return res.data
  }
}

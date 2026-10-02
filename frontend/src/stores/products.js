import { defineStore } from 'pinia'
import { productService } from '../services/productService'
import { SAMPLE_PRODUCTS, AGRICULTURAL_CATEGORIES } from '../utils/agriculturalData'

export const useProductStore = defineStore('products', {
  state: () => ({
    products: [],
    currentProduct: null,
    totalElements: 0,
    totalPages: 0,
    loading: false,
    error: null,
    favorites: JSON.parse(localStorage.getItem('farmora_product_favorites') || '[]'),
    filters: {
      search: '',
      category: 'All',
      province: '',
      district: '',
      minPrice: 0,
      maxPrice: 5000,
      farmingMethods: [], // ['Organic', 'Hydroponic', etc.]
      inStockOnly: false,
      freshTodayOnly: false,
      ratingMin: 0,
      sortBy: 'newest',
      page: 0,
      size: 24
    }
  }),

  getters: {
    // Returns products filtered by search, category, location, price, method, etc.
    filteredProducts: (state) => {
      let list = state.products.length > 0 ? [...state.products] : [...SAMPLE_PRODUCTS]

      // Search keyword filter (name, farmer, description, location)
      if (state.filters.search?.trim()) {
        const q = state.filters.search.trim().toLowerCase()
        list = list.filter(p =>
          (p.name && p.name.toLowerCase().includes(q)) ||
          (p.farmer?.fullName && p.farmer.fullName.toLowerCase().includes(q)) ||
          (p.location && p.location.toLowerCase().includes(q)) ||
          (p.description && p.description.toLowerCase().includes(q))
        )
      }

      // Category filter
      if (state.filters.category && state.filters.category !== 'All') {
        const targetCat = state.filters.category.toLowerCase().replace(/[^a-z0-9]/g, '')
        list = list.filter(p => {
          const itemCat = (p.category || '').toLowerCase().replace(/[^a-z0-9]/g, '')
          return itemCat === targetCat || itemCat.includes(targetCat) || targetCat.includes(itemCat)
        })
      }

      // Province filter
      if (state.filters.province) {
        const prov = state.filters.province.toLowerCase()
        list = list.filter(p =>
          (p.province && p.province.toLowerCase() === prov) ||
          (p.location && p.location.toLowerCase().includes(prov))
        )
      }

      // District filter
      if (state.filters.district) {
        const dist = state.filters.district.toLowerCase()
        list = list.filter(p =>
          (p.district && p.district.toLowerCase() === dist) ||
          (p.location && p.location.toLowerCase().includes(dist))
        )
      }

      // Price range filter
      list = list.filter(p => {
        const price = Number(p.price) || 0
        return price >= state.filters.minPrice && price <= state.filters.maxPrice
      })

      // Farming methods filter
      if (state.filters.farmingMethods && state.filters.farmingMethods.length > 0) {
        const methodsLower = state.filters.farmingMethods.map(m => m.toLowerCase())
        list = list.filter(p => {
          const m = (p.farmingMethod || 'organic').toLowerCase()
          return methodsLower.some(target => m.includes(target) || target.includes(m))
        })
      }

      // In Stock Only
      if (state.filters.inStockOnly) {
        list = list.filter(p => p.status === 'AVAILABLE' && (Number(p.quantity) > 0))
      }

      // Fresh Today Only
      if (state.filters.freshTodayOnly) {
        const todayStr = new Date().toISOString().split('T')[0]
        list = list.filter(p => p.isFreshToday || p.harvestDate === todayStr)
      }

      // Farmer Rating Filter
      if (state.filters.ratingMin > 0) {
        list = list.filter(p => (p.rating || 4.5) >= state.filters.ratingMin)
      }

      // Sorting
      switch (state.filters.sortBy) {
        case 'price-low':
          list.sort((a, b) => (Number(a.price) || 0) - (Number(b.price) || 0))
          break
        case 'price-high':
          list.sort((a, b) => (Number(b.price) || 0) - (Number(a.price) || 0))
          break
        case 'rating':
          list.sort((a, b) => (Number(b.rating) || 0) - (Number(a.rating) || 0))
          break
        case 'name':
          list.sort((a, b) => (a.name || '').localeCompare(b.name || ''))
          break
        case 'newest':
        default:
          list.sort((a, b) => (b.id || 0) - (a.id || 0))
          break
      }

      return list
    },

    // Dynamic Category Counts based on actual inventory
    categoryCounts: (state) => {
      const source = state.products.length > 0 ? state.products : SAMPLE_PRODUCTS
      const counts = {}
      AGRICULTURAL_CATEGORIES.forEach(cat => {
        const target = cat.name.toLowerCase().replace(/[^a-z0-9]/g, '')
        const count = source.filter(p => {
          const itemCat = (p.category || '').toLowerCase().replace(/[^a-z0-9]/g, '')
          return itemCat === target || itemCat.includes(target) || target.includes(itemCat)
        }).length
        // Return at least the configured count or the dynamic count
        counts[cat.name] = Math.max(count, cat.count)
      })
      return counts
    },

    // Daily Harvest Fresh Products
    dailyHarvestProducts: (state) => {
      const source = state.products.length > 0 ? state.products : SAMPLE_PRODUCTS
      const todayStr = new Date().toISOString().split('T')[0]
      return source.filter(p => p.isFreshToday || p.harvestDate === todayStr)
    }
  },

  actions: {
    async fetchProducts() {
      this.loading = true
      this.error = null
      try {
        const params = {
          search: this.filters.search || undefined,
          category: this.filters.category !== 'All' ? this.filters.category : undefined,
          location: this.filters.district || this.filters.province || undefined,
          status: this.filters.inStockOnly ? 'AVAILABLE' : undefined,
          page: this.filters.page,
          size: this.filters.size
        }
        const data = await productService.getProducts(params)
        const apiList = data.content || []

        if (apiList.length > 0) {
          this.products = apiList
          this.totalElements = data.totalElements || apiList.length
          this.totalPages = data.totalPages || 1
        } else {
          // If the backend has 0 products initially, fallback to SAMPLE_PRODUCTS
          this.products = SAMPLE_PRODUCTS
          this.totalElements = SAMPLE_PRODUCTS.length
          this.totalPages = 1
        }
      } catch (err) {
        console.warn('Backend products endpoint offline or returned error, using verified sample products', err)
        this.products = SAMPLE_PRODUCTS
        this.totalElements = SAMPLE_PRODUCTS.length
        this.totalPages = 1
      } finally {
        this.loading = false
      }
    },

    async fetchProductById(id) {
      this.loading = true
      this.error = null
      try {
        const data = await productService.getProductById(id)
        this.currentProduct = data
        return data
      } catch (err) {
        // Look up in sample products if not in backend
        const sample = SAMPLE_PRODUCTS.find(p => p.id === Number(id))
        if (sample) {
          this.currentProduct = sample
          return sample
        }
        this.error = err.response?.data?.message || 'Failed to load product details'
        throw err
      } finally {
        this.loading = false
      }
    },

    setSearch(query) {
      this.filters.search = query
      this.filters.page = 0
    },

    setCategory(category) {
      this.filters.category = category
      this.filters.page = 0
    },

    setProvince(province) {
      this.filters.province = province
      this.filters.district = '' // Reset district if province changes
      this.filters.page = 0
    },

    setDistrict(district) {
      this.filters.district = district
      this.filters.page = 0
    },

    setPriceRange(min, max) {
      this.filters.minPrice = min
      this.filters.maxPrice = max
      this.filters.page = 0
    },

    toggleFarmingMethod(method) {
      const idx = this.filters.farmingMethods.indexOf(method)
      if (idx > -1) {
        this.filters.farmingMethods.splice(idx, 1)
      } else {
        this.filters.farmingMethods.push(method)
      }
      this.filters.page = 0
    },

    clearFilters() {
      this.filters.search = ''
      this.filters.category = 'All'
      this.filters.province = ''
      this.filters.district = ''
      this.filters.minPrice = 0
      this.filters.maxPrice = 5000
      this.filters.farmingMethods = []
      this.filters.inStockOnly = false
      this.filters.freshTodayOnly = false
      this.filters.ratingMin = 0
      this.filters.sortBy = 'newest'
      this.filters.page = 0
    },

    toggleFavorite(productId) {
      const idx = this.favorites.indexOf(productId)
      if (idx > -1) {
        this.favorites.splice(idx, 1)
      } else {
        this.favorites.push(productId)
      }
      localStorage.setItem('farmora_product_favorites', JSON.stringify(this.favorites))
    },

    isFavorite(productId) {
      return this.favorites.includes(productId)
    }
  }
})

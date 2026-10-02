<script setup>
import { ref, onMounted, computed } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import { productService } from '../../services/productService'
import { harvestService } from '../../services/harvestService'
import { AGRICULTURAL_CATEGORIES, SAMPLE_PRODUCTS } from '../../utils/agriculturalData'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const isFarmer = computed(() => authStore.role === 'FARMER')

const harvestListings = ref([])
const loading = ref(true)
const posting = ref(false)
const message = ref('')
const activeFilter = ref('today') // 'today' | 'all'
const analytics = ref(null)

const categories = AGRICULTURAL_CATEGORIES.map(c => c.name)
const todayStr = new Date().toISOString().split('T')[0]

// Quick Post Today's Harvest Form
const quickForm = ref({
  name: '',
  category: 'Vegetables',
  price: '',
  quantity: '',
  unit: 'kg',
  location: 'Nuwara Eliya',
  harvestDate: todayStr,
  imageUrl: ''
})

const showQuickPostModal = ref(false)
const editingProduct = ref(null)
const updateQtyVal = ref('')

const loadHarvests = async () => {
  loading.value = true
  try {
    let prods = []
    if (isFarmer.value && authStore.user?.id) {
      const [fHarvests, fAnalytics] = await Promise.all([
        harvestService.getFarmerAllPosts(authStore.user.id).catch(() => []),
        harvestService.getHarvestAnalytics(authStore.user.id).catch(() => null)
      ])
      prods = fHarvests
      analytics.value = fAnalytics
    }
    if (!prods || prods.length === 0) {
      prods = await harvestService.getAvailableNow().catch(() => [])
    }
    if (!prods || prods.length === 0) {
      const data = await productService.getProducts()
      prods = data.content || []
    }

    if (prods && prods.length > 0) {
      harvestListings.value = prods.map(p => ({
        ...p,
        name: p.name || p.cropName,
        price: p.price || p.pricePerUnit,
        isFreshToday: p.harvestDate === todayStr || p.freshnessIndicator === 'Picked Today' || p.isFreshToday
      }))
    } else {
      harvestListings.value = SAMPLE_PRODUCTS.map(p => ({
        ...p,
        isFreshToday: p.harvestDate === todayStr || p.isFreshToday
      }))
    }
  } catch (err) {
    console.warn('Error loading harvests, using sample list', err)
    harvestListings.value = SAMPLE_PRODUCTS
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadHarvests()
})

const filteredHarvests = computed(() => {
  if (activeFilter.value === 'today') {
    return harvestListings.value.filter(item => item.isFreshToday || item.harvestDate === todayStr)
  }
  return harvestListings.value
})

const handleQuickPost = async () => {
  posting.value = true
  message.value = ''
  try {
    const payload = {
      cropName: quickForm.value.name,
      name: quickForm.value.name,
      description: 'Fresh daily harvest picked this morning for immediate fulfillment.',
      category: quickForm.value.category,
      pricePerUnit: parseFloat(quickForm.value.price),
      price: parseFloat(quickForm.value.price),
      quantity: parseFloat(quickForm.value.quantity),
      unit: quickForm.value.unit,
      location: quickForm.value.location,
      harvestDate: todayStr,
      imageUrl: quickForm.value.imageUrl || 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&w=600&q=80'
    }

    let created
    try {
      created = await harvestService.postDailyHarvest(payload)
    } catch (e) {
      created = await productService.createProduct(payload)
    }

    harvestListings.value.unshift({
      ...created,
      name: created.name || created.cropName,
      price: created.price || created.pricePerUnit,
      isFreshToday: true
    })
    message.value = `Successfully published today's harvest of ${quickForm.value.name}!`
    showQuickPostModal.value = false
    quickForm.value = {
      name: '',
      category: 'Vegetables',
      price: '',
      quantity: '',
      unit: 'kg',
      location: 'Nuwara Eliya',
      harvestDate: todayStr,
      imageUrl: ''
    }
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to post harvest.')
  } finally {
    posting.value = false
  }
}

const openQuantityModal = (item) => {
  editingProduct.value = item
  updateQtyVal.value = item.quantity
}

const handleUpdateQuantity = async () => {
  if (!editingProduct.value) return
  try {
    const newQty = parseFloat(updateQtyVal.value)
    editingProduct.value.quantity = newQty
    
    try {
      await harvestService.updateQuantity(editingProduct.value.id, newQty)
    } catch (e) {
      await productService.updateProduct(editingProduct.value.id, {
        name: editingProduct.value.name,
        price: editingProduct.value.price,
        quantity: newQty,
        unit: editingProduct.value.unit,
        category: editingProduct.value.category,
        location: editingProduct.value.location
      })
    }

    message.value = `Updated inventory for ${editingProduct.value.name} to ${newQty} ${editingProduct.value.unit}.`
    editingProduct.value = null
  } catch (err) {
    alert('Failed to update quantity.')
  }
}

const handleArchive = async (item) => {
  if (confirm(`Archive harvest listing for ${item.name}?`)) {
    try {
      await harvestService.archiveHarvest(item.id).catch(() => productService.archiveProduct(item.id))
      harvestListings.value = harvestListings.value.filter(p => p.id !== item.id)
      message.value = `Archived ${item.name} harvest listing.`
    } catch (err) {
      console.error(err)
    }
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <!-- Farmer Dashboard Sidebar if logged in as Farmer -->
      <div v-if="isFarmer" class="col-lg-3">
        <DashboardSidebar activeMenu="daily-harvest" />
      </div>

      <!-- Main Daily Harvest Content -->
      <div :class="isFarmer ? 'col-lg-9' : 'col-12'">
        <!-- Header Banner -->
        <div class="harvest-banner p-4 p-md-5 rounded-4 mb-4 text-white shadow-sm d-flex flex-wrap justify-content-between align-items-center">
          <div>
            <div class="badge bg-warning text-dark px-3 py-1-5 rounded-pill mb-2 fw-bold text-uppercase" style="font-size: 0.72rem;">
              <i class="bi bi-clock-history me-1"></i> Real-time Produce
            </div>
            <h2 class="fw-bold mb-1">Today's Daily Harvest</h2>
            <p class="text-white-75 mb-0" style="max-width: 580px;">
              Direct farm listings plucked in the last 24 hours. Zero cold storage delays. Farm fresh guarantee.
            </p>
          </div>

          <div class="mt-3 mt-md-0 d-flex gap-2">
            <button
              v-if="isFarmer"
              class="btn btn-warning fw-bold px-3 py-2 shadow-sm text-dark d-flex align-items-center gap-2"
              @click="showQuickPostModal = true"
            >
              <i class="bi bi-plus-circle-fill"></i>
              <span>Post Today's Harvest</span>
            </button>
            <router-link to="/products" class="btn btn-outline-light px-3 py-2">
              Browse All Produce &rarr;
            </router-link>
          </div>
        </div>

        <!-- Success Toast Notification -->
        <div v-if="message" class="alert alert-success alert-dismissible fade show rounded-3 small" role="alert">
          <i class="bi bi-check-circle-fill me-1"></i> {{ message }}
          <button type="button" class="btn-close" @click="message = ''"></button>
        </div>

        <!-- Control Bar: Filter Tabs & Stats -->
        <div class="card border-0 shadow-sm rounded-4 p-3 mb-4 bg-white d-flex flex-wrap justify-content-between align-items-center flex-row gap-3">
          <div class="btn-group shadow-none" role="group">
            <button
              type="button"
              class="btn btn-sm px-3 fw-semibold d-flex align-items-center gap-1"
              :class="activeFilter === 'today' ? 'btn-success' : 'btn-outline-secondary'"
              @click="activeFilter = 'today'"
            >
              <i class="bi bi-patch-check-fill text-white"></i>
              <span>Harvested Today Only</span>
            </button>
            <button
              type="button"
              class="btn btn-sm px-3 fw-semibold"
              :class="activeFilter === 'all' ? 'btn-success' : 'btn-outline-secondary'"
              @click="activeFilter = 'all'"
            >
              All Recent Harvests
            </button>
          </div>

          <div class="small text-muted fw-medium">
            Active Listings: <strong class="text-dark">{{ filteredHarvests.length }}</strong>
          </div>
        </div>

        <LoadingSpinner v-if="loading" message="Loading today's fresh harvests..." />

        <!-- Harvest Table / Cards -->
        <div v-else-if="filteredHarvests.length > 0" class="row g-3">
          <div
            v-for="item in filteredHarvests"
            :key="item.id"
            class="col-md-6 col-xl-4"
          >
            <div class="card h-100 border-0 shadow-sm rounded-4 overflow-hidden harvest-card bg-white position-relative">
              <!-- Produce Image with Badge -->
              <div class="position-relative" style="height: 180px;">
                <img
                  :src="item.imageUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=500'"
                  :alt="item.name"
                  class="w-100 h-100 object-fit-cover"
                />

                <span class="position-absolute top-0 start-0 m-2 badge bg-success px-2 py-1-5 rounded-pill shadow-sm small fw-semibold d-flex align-items-center gap-1">
                  <i class="bi bi-patch-check-fill"></i>
                  <span>Fresh Today</span>
                </span>

                <span class="position-absolute bottom-0 start-0 m-2 badge bg-dark bg-opacity-75 text-white px-2 py-1 rounded">
                  {{ item.category }}
                </span>
              </div>

              <!-- Body -->
              <div class="card-body p-3 d-flex flex-column">
                <div class="d-flex justify-content-between align-items-start mb-1">
                  <h6 class="fw-bold text-dark text-truncate mb-0" :title="item.name">{{ item.name }}</h6>
                  <span class="badge bg-light text-dark border">{{ item.farmingMethod || 'Organic' }}</span>
                </div>

                <div class="text-muted small mb-2">
                  <i class="bi bi-geo-alt-fill text-danger me-1"></i>{{ item.location || 'Local Farm' }}
                  <span class="mx-1">•</span>
                  <i class="bi bi-calendar-check me-1"></i>{{ item.harvestDate || todayStr }}
                </div>

                <!-- Quantity & Price -->
                <div class="p-2 rounded-3 bg-light mb-3 d-flex justify-content-between align-items-center small">
                  <div>
                    <span class="text-muted d-block" style="font-size: 0.72rem;">Available Stock</span>
                    <strong class="text-dark fs-6">{{ item.quantity }} {{ item.unit }}</strong>
                  </div>
                  <div class="text-end">
                    <span class="text-muted d-block" style="font-size: 0.72rem;">Daily Price</span>
                    <strong class="text-success fs-6">Rs. {{ Number(item.price).toLocaleString() }}/{{ item.unit }}</strong>
                  </div>
                </div>

                <!-- Actions -->
                <div class="mt-auto d-flex gap-2">
                  <template v-if="isFarmer">
                    <button
                      class="btn btn-sm btn-outline-success flex-grow-1 fw-semibold"
                      @click="openQuantityModal(item)"
                    >
                      <i class="bi bi-pencil me-1"></i> Update Stock
                    </button>
                    <button
                      class="btn btn-sm btn-outline-danger"
                      @click="handleArchive(item)"
                      title="Archive old listing"
                    >
                      <i class="bi bi-archive"></i>
                    </button>
                  </template>

                  <router-link
                    v-else
                    :to="`/products/${item.id}`"
                    class="btn btn-farmora btn-sm w-100 fw-semibold"
                  >
                    View & Order Today's Stock
                  </router-link>
                </div>
              </div>
            </div>
          </div>
        </div>

        <EmptyState
          v-else
          icon="bi-cloud-sun"
          title="No Daily Harvest Posted Yet"
          description="Check back shortly or click Post Today's Harvest if you are a registered farmer."
        />
      </div>
    </div>

    <!-- Quick Post Today's Harvest Modal -->
    <div
      v-if="showQuickPostModal"
      class="modal-backdrop-custom d-flex align-items-center justify-content-center p-3"
    >
      <div class="card border-0 shadow-lg rounded-4 p-4 bg-white" style="max-width: 520px; width: 100%;">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="fw-bold text-dark mb-0 d-flex align-items-center gap-2">
            <i class="bi bi-sun-fill text-warning"></i>
            <span>Post Today's Harvest</span>
          </h5>
          <button type="button" class="btn-close" @click="showQuickPostModal = false"></button>
        </div>

        <form @submit.prevent="handleQuickPost">
          <div class="mb-3">
            <label class="form-label small fw-bold">Produce Name</label>
            <input
              type="text"
              v-model="quickForm.name"
              class="form-control"
              placeholder="e.g. Fresh Morning Carrots"
              required
            />
          </div>

          <div class="row g-2 mb-3">
            <div class="col-6">
              <label class="form-label small fw-bold">Category</label>
              <select v-model="quickForm.category" class="form-select" required>
                <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
              </select>
            </div>
            <div class="col-6">
              <label class="form-label small fw-bold">Farm Location</label>
              <input
                type="text"
                v-model="quickForm.location"
                class="form-control"
                placeholder="e.g. Nuwara Eliya"
                required
              />
            </div>
          </div>

          <div class="row g-2 mb-3">
            <div class="col-4">
              <label class="form-label small fw-bold">Price (Rs.)</label>
              <input
                type="number"
                step="0.01"
                v-model="quickForm.price"
                class="form-control"
                placeholder="250"
                required
              />
            </div>
            <div class="col-4">
              <label class="form-label small fw-bold">Quantity</label>
              <input
                type="number"
                step="0.1"
                v-model="quickForm.quantity"
                class="form-control"
                placeholder="100"
                required
              />
            </div>
            <div class="col-4">
              <label class="form-label small fw-bold">Unit</label>
              <select v-model="quickForm.unit" class="form-select">
                <option value="kg">kg</option>
                <option value="ton">ton</option>
                <option value="crate">crate</option>
                <option value="bunch">bunch</option>
              </select>
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label small fw-bold">Produce Photo URL (Optional)</label>
            <input
              type="url"
              v-model="quickForm.imageUrl"
              class="form-control"
              placeholder="https://..."
            />
          </div>

          <div class="d-flex justify-content-end gap-2 pt-2 border-top">
            <button
              type="button"
              class="btn btn-outline-secondary"
              @click="showQuickPostModal = false"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="btn btn-farmora fw-bold"
              :disabled="posting"
            >
              <span v-if="posting" class="spinner-border spinner-border-sm me-1"></span>
              Post Fresh Stock Now
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Update Quantity Modal -->
    <div
      v-if="editingProduct"
      class="modal-backdrop-custom d-flex align-items-center justify-content-center p-3"
    >
      <div class="card border-0 shadow-lg rounded-4 p-4 bg-white" style="max-width: 420px; width: 100%;">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="fw-bold text-dark mb-0">Update Stock Quantity</h5>
          <button type="button" class="btn-close" @click="editingProduct = null"></button>
        </div>

        <p class="small text-muted mb-3">
          Update the available harvest stock for <strong>{{ editingProduct.name }}</strong>.
        </p>

        <form @submit.prevent="handleUpdateQuantity">
          <div class="mb-3">
            <label class="form-label small fw-bold">New Available Quantity ({{ editingProduct.unit }})</label>
            <input
              type="number"
              step="0.1"
              min="0"
              class="form-control"
              v-model="updateQtyVal"
              required
            />
          </div>

          <div class="d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-outline-secondary" @click="editingProduct = null">
              Cancel
            </button>
            <button type="submit" class="btn btn-farmora fw-semibold">
              Save Quantity
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.harvest-banner {
  background: linear-gradient(135deg, #1b4332 0%, #2d6a4f 100%);
}

.text-white-75 {
  color: rgba(255, 255, 255, 0.75);
}

.harvest-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border: 1px solid rgba(0, 0, 0, 0.06) !important;
}

.harvest-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 24px rgba(45, 106, 79, 0.12) !important;
}

.py-1-5 {
  padding-top: 0.35rem;
  padding-bottom: 0.35rem;
}

.modal-backdrop-custom {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(4px);
  z-index: 1050;
}
</style>

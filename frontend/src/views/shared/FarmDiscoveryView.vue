<script setup>
import { onMounted, ref, computed } from 'vue'
import { farmService } from '../../services/farmService'
import { SAMPLE_FARMS, calculateDistance } from '../../utils/agriculturalData'
import { PROVINCES, getDistrictsForProvince, SRI_LANKA_REGIONS } from '../../utils/sriLankaRegions'
import { useAuthStore } from '../../stores/auth'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'

const authStore = useAuthStore()
const isFarmer = computed(() => authStore.role === 'FARMER')

const farms = ref([])
const loading = ref(true)
const searchQuery = ref('')
const selectedProvince = ref('')
const selectedDistrict = ref('')
const selectedMethod = ref('')
const verifiedOnly = ref(false)

const userCoords = ref({ lat: 6.9271, lng: 79.8612 }) // Default Colombo
const locating = ref(false)
const locationMessage = ref('')

// Farmer GPS Update Modal
const showLocationModal = ref(false)
const updateLat = ref('')
const updateLng = ref('')
const updateAddress = ref('')
const savingLocation = ref(false)
const modalMessage = ref('')

const farmingMethods = ['ORGANIC', 'HYDROPONIC', 'TRADITIONAL', 'GREENHOUSE', 'CONVENTIONAL']

const availableDistricts = computed(() => {
  if (selectedProvince.value) {
    return getDistrictsForProvince(selectedProvince.value)
  }
  return Object.values(SRI_LANKA_REGIONS).flat()
})

const loadFarms = async () => {
  loading.value = true
  try {
    const list = await farmService.getFarms()
    farms.value = list && list.length > 0 ? list : SAMPLE_FARMS
  } catch (err) {
    console.warn('Using sample farms', err)
    farms.value = SAMPLE_FARMS
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFarms()
})

const filteredFarms = computed(() => {
  let list = [...farms.value]

  // Search
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(f =>
      (f.farmName && f.farmName.toLowerCase().includes(q)) ||
      (f.farmerName && f.farmerName.toLowerCase().includes(q)) ||
      (f.district && f.district.toLowerCase().includes(q)) ||
      (f.crops && f.crops.some(c => c.toLowerCase().includes(q)))
    )
  }

  // Province
  if (selectedProvince.value) {
    list = list.filter(f => f.province === selectedProvince.value)
  }

  // District
  if (selectedDistrict.value) {
    list = list.filter(f => f.district === selectedDistrict.value)
  }

  // Method
  if (selectedMethod.value) {
    list = list.filter(f => (f.farmingMethod || '').toUpperCase() === selectedMethod.value)
  }

  // Verified Only
  if (verifiedOnly.value) {
    list = list.filter(f => f.verificationStatus === 'VERIFIED')
  }

  // Sort by proximity
  list.sort((a, b) => {
    const distA = getDistance(a) || 9999
    const distB = getDistance(b) || 9999
    return distA - distB
  })

  return list
})

const getDistance = (farm) => {
  if (!farm.latitude || !farm.longitude || !userCoords.value) return null
  return calculateDistance(userCoords.value.lat, userCoords.value.lng, farm.latitude, farm.longitude)
}

const handleDetectLocation = () => {
  if (navigator.geolocation) {
    locating.value = true
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        userCoords.value = {
          lat: pos.coords.latitude,
          lng: pos.coords.longitude
        }
        locating.value = false
        locationMessage.value = 'Proximity calculated from your current GPS position!'
        setTimeout(() => { locationMessage.value = '' }, 4000)
      },
      () => {
        locating.value = false
        locationMessage.value = 'Using Colombo center as reference origin.'
        setTimeout(() => { locationMessage.value = '' }, 4000)
      }
    )
  }
}

const openLocationModal = async () => {
  showLocationModal.value = true
  modalMessage.value = ''
  try {
    const myProfile = await farmService.getMyFarmProfile()
    if (myProfile) {
      updateLat.value = myProfile.latitude || 6.9497
      updateLng.value = myProfile.longitude || 80.7891
      updateAddress.value = myProfile.farmAddress || ''
    }
  } catch (e) {
    updateLat.value = 6.9497
    updateLng.value = 80.7891
  }
}

const handleSaveLocation = async () => {
  savingLocation.value = true
  modalMessage.value = ''
  try {
    await farmService.updateFarmLocation(
      parseFloat(updateLat.value),
      parseFloat(updateLng.value),
      updateAddress.value
    )
    modalMessage.value = 'Farm GPS coordinates updated successfully!'
    setTimeout(() => {
      showLocationModal.value = false
      loadFarms()
    }, 1500)
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to update farm location.')
  } finally {
    savingLocation.value = false
  }
}
</script>

<template>
  <div class="farm-discovery-view bg-light min-vh-100 py-4">
    <div class="container-xxl">
      <!-- Hero Header -->
      <div class="discovery-hero p-4 p-md-5 rounded-4 mb-4 text-white shadow-sm d-flex flex-wrap justify-content-between align-items-center">
        <div>
          <div class="badge bg-success-subtle text-success border border-success-subtle px-3 py-1-5 rounded-pill mb-2 fw-bold text-uppercase" style="font-size: 0.72rem;">
            <i class="bi bi-compass-fill me-1"></i> Agricultural Mapping
          </div>
          <h2 class="fw-bold mb-2">Discover Sri Lankan Farms</h2>
          <p class="text-white-75 mb-0" style="max-width: 600px;">
            Locate sustainable farms across all districts. Connect directly with verified growers, check cultivation methods, and plan farm visits.
          </p>
        </div>

        <div class="d-flex gap-2 mt-3 mt-md-0 flex-wrap">
          <button
            class="btn btn-warning fw-bold px-3 py-2 text-dark shadow-sm d-flex align-items-center gap-2"
            @click="handleDetectLocation"
            :disabled="locating"
          >
            <i class="bi bi-crosshair"></i>
            <span>{{ locating ? 'Locating...' : 'Sort by Nearest' }}</span>
          </button>

          <button
            v-if="isFarmer"
            class="btn btn-outline-light px-3 py-2 fw-semibold d-flex align-items-center gap-2"
            @click="openLocationModal"
          >
            <i class="bi bi-pin-map-fill"></i>
            <span>Update My Farm GPS</span>
          </button>

          <router-link to="/agricultural-map" class="btn btn-farmora px-3 py-2">
            <i class="bi bi-map me-1"></i> Interactive Map
          </router-link>
        </div>
      </div>

      <!-- Toast Feedback -->
      <div v-if="locationMessage" class="alert alert-info py-2 small rounded-3 mb-4" role="alert">
        <i class="bi bi-info-circle-fill me-1"></i> {{ locationMessage }}
      </div>

      <!-- Filter Controls Bar -->
      <div class="card border-0 shadow-sm rounded-4 p-3 mb-4 bg-white">
        <div class="row g-2 align-items-center">
          <!-- Keyword Search -->
          <div class="col-lg-4">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-white border-end-0"><i class="bi bi-search text-muted"></i></span>
              <input
                type="text"
                class="form-control border-start-0"
                placeholder="Search by farm name, farmer, crop, or district..."
                v-model="searchQuery"
              />
            </div>
          </div>

          <!-- Province -->
          <div class="col-sm-6 col-lg-2">
            <select v-model="selectedProvince" class="form-select form-select-sm">
              <option value="">All Provinces</option>
              <option v-for="prov in PROVINCES" :key="prov" :value="prov">{{ prov }}</option>
            </select>
          </div>

          <!-- District -->
          <div class="col-sm-6 col-lg-2">
            <select v-model="selectedDistrict" class="form-select form-select-sm">
              <option value="">All Districts</option>
              <option v-for="dist in availableDistricts" :key="dist" :value="dist">{{ dist }}</option>
            </select>
          </div>

          <!-- Method -->
          <div class="col-sm-6 col-lg-2">
            <select v-model="selectedMethod" class="form-select form-select-sm">
              <option value="">All Methods</option>
              <option v-for="m in farmingMethods" :key="m" :value="m">{{ m }}</option>
            </select>
          </div>

          <!-- Verified Checkbox -->
          <div class="col-sm-6 col-lg-2 d-flex align-items-center">
            <div class="form-check small mb-0">
              <input class="form-check-input" type="checkbox" id="verifiedCheck" v-model="verifiedOnly" />
              <label class="form-check-label text-dark fw-semibold" for="verifiedCheck">
                Verified Only
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- Results Grid -->
      <LoadingSpinner v-if="loading" message="Locating certified farms across Sri Lanka..." />

      <div v-else-if="filteredFarms.length > 0" class="row g-4">
        <div
          v-for="farm in filteredFarms"
          :key="farm.id"
          class="col-md-6 col-lg-4"
        >
          <div class="card h-100 border-0 shadow-sm rounded-4 overflow-hidden farm-discover-card bg-white position-relative">
            <!-- Cover image -->
            <div class="position-relative" style="height: 190px;">
              <img
                :src="farm.imageUrl || 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=600'"
                :alt="farm.farmName"
                class="w-100 h-100 object-fit-cover"
              />

              <!-- Verified Badge -->
              <span
                v-if="farm.verificationStatus === 'VERIFIED'"
                class="position-absolute top-0 start-0 m-2 badge bg-success-subtle text-success border border-success-subtle px-2 py-1-5 rounded-pill shadow-sm small fw-semibold d-flex align-items-center gap-1"
              >
                <i class="bi bi-patch-check-fill text-success"></i>
                <span>Farmora Verified</span>
              </span>

              <!-- Distance Badge -->
              <span
                v-if="getDistance(farm)"
                class="position-absolute top-0 end-0 m-2 badge bg-dark bg-opacity-75 text-white px-2 py-1 rounded-pill small"
              >
                <i class="bi bi-cursor-fill me-1 text-warning"></i>
                {{ getDistance(farm) }} km away
              </span>
            </div>

            <!-- Body -->
            <div class="card-body p-3-5 d-flex flex-column">
              <!-- Title & Rating -->
              <div class="d-flex justify-content-between align-items-start mb-1">
                <h5 class="fw-bold text-dark text-truncate mb-0" :title="farm.farmName">{{ farm.farmName }}</h5>
                <span class="text-warning small fw-bold flex-shrink-0 ms-1">
                  ★ {{ farm.averageRating || '4.8' }} ({{ farm.totalReviews || 40 }})
                </span>
              </div>

              <!-- Farmer Name & Location -->
              <div class="text-muted small mb-2">
                <i class="bi bi-person-fill text-success me-1"></i>{{ farm.farmerName || farm.farmer?.fullName || 'Certified Farmer' }}
                <span class="mx-1">•</span>
                <i class="bi bi-geo-alt-fill text-danger me-1"></i>{{ farm.district }}, {{ farm.province }}
              </div>

              <!-- Description -->
              <p class="text-secondary small leading-normal mb-3 flex-grow-1" style="display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                {{ farm.farmerDetails || 'Sustainable Sri Lankan farm cultivating high-standard agricultural produce.' }}
              </p>

              <!-- Crops Tag pills -->
              <div v-if="farm.crops && farm.crops.length > 0" class="d-flex flex-wrap gap-1 mb-3">
                <span
                  v-for="(c, cidx) in farm.crops.slice(0, 4)"
                  :key="cidx"
                  class="badge bg-light text-dark border px-2 py-1 rounded"
                  style="font-size: 0.72rem;"
                >
                  {{ c }}
                </span>
              </div>

              <!-- GPS Coordinates & Directions Action -->
              <div class="border-top pt-3 mt-auto d-flex justify-content-between align-items-center">
                <span class="font-monospace small text-muted" style="font-size: 0.72rem;">
                  <i class="bi bi-pin-map text-danger me-1"></i>{{ farm.latitude?.toFixed(2) }}, {{ farm.longitude?.toFixed(2) }}
                </span>

                <div class="d-flex gap-2">
                  <a
                    :href="`https://www.google.com/maps/dir/?api=1&destination=${farm.latitude},${farm.longitude}`"
                    target="_blank"
                    class="btn btn-sm btn-outline-success px-2 py-1"
                    title="Get Route Directions"
                  >
                    <i class="bi bi-arrow-up-right-circle"></i>
                  </a>
                  <router-link
                    :to="`/farms/${farm.id}`"
                    class="btn btn-sm btn-farmora px-3 py-1 fw-semibold"
                  >
                    View Details
                  </router-link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <EmptyState
        v-else
        icon="bi-geo-alt"
        title="No Farms Found"
        description="Try expanding your location or method filters to discover more farms."
      />
    </div>

    <!-- Farmer GPS Location Update Modal -->
    <div
      v-if="showLocationModal"
      class="modal-backdrop-custom d-flex align-items-center justify-content-center p-3"
    >
      <div class="card border-0 shadow-lg rounded-4 p-4 bg-white" style="max-width: 480px; width: 100%;">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="fw-bold text-dark mb-0 d-flex align-items-center gap-2">
            <i class="bi bi-geo-alt-fill text-farmora-green"></i>
            <span>Update Farm GPS Location</span>
          </h5>
          <button type="button" class="btn-close" @click="showLocationModal = false"></button>
        </div>

        <div v-if="modalMessage" class="alert alert-success py-2 small rounded-3">
          <i class="bi bi-check-circle-fill me-1"></i> {{ modalMessage }}
        </div>

        <form @submit.prevent="handleSaveLocation">
          <p class="small text-muted mb-3">
            Enter your farm gate latitude and longitude coordinates to appear accurately on the Sri Lanka Agricultural Discovery Map.
          </p>

          <div class="row g-2 mb-3">
            <div class="col-6">
              <label class="form-label small fw-bold">Latitude</label>
              <input
                type="number"
                step="0.0001"
                class="form-control"
                v-model="updateLat"
                required
              />
            </div>
            <div class="col-6">
              <label class="form-label small fw-bold">Longitude</label>
              <input
                type="number"
                step="0.0001"
                class="form-control"
                v-model="updateLng"
                required
              />
            </div>
          </div>

          <div class="mb-4">
            <label class="form-label small fw-bold">Farm Gate Address</label>
            <input
              type="text"
              class="form-control"
              placeholder="e.g. Kandapola Road, Nuwara Eliya"
              v-model="updateAddress"
            />
          </div>

          <div class="d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-outline-secondary" @click="showLocationModal = false">
              Cancel
            </button>
            <button type="submit" class="btn btn-farmora fw-bold" :disabled="savingLocation">
              <span v-if="savingLocation" class="spinner-border spinner-border-sm me-1"></span>
              Save Farm Location
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.discovery-hero {
  background: linear-gradient(135deg, #1b4332 0%, #2d6a4f 100%);
}

.text-white-75 {
  color: rgba(255, 255, 255, 0.75);
}

.farm-discover-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border: 1px solid rgba(0, 0, 0, 0.06) !important;
}

.farm-discover-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(45, 106, 79, 0.12) !important;
}

.p-3-5 {
  padding: 1.15rem;
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

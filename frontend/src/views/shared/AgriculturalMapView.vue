<script setup>
import { onMounted, ref, computed, watch, onBeforeUnmount } from 'vue'
import L from 'leaflet'
import { farmService } from '../../services/farmService'
import { SAMPLE_FARMS, DISTRICT_COORDINATES, calculateDistance } from '../../utils/agriculturalData'
import { PROVINCES, getDistrictsForProvince, SRI_LANKA_REGIONS } from '../../utils/sriLankaRegions'

const mapContainer = ref(null)
let mapInstance = null
let markersGroup = null

const farms = ref([])
const loading = ref(true)
const selectedProvince = ref('')
const selectedDistrict = ref('')
const selectedCrop = ref('')
const selectedFarm = ref(null)

const userLocation = ref(null) // { lat, lng }
const locationStatus = ref('')

const cropOptions = [
  'All Crops',
  'Carrots',
  'Tomatoes',
  'Potatoes',
  'Mangoes',
  'Ceylon Cinnamon',
  'Suwandel Rice',
  'Iceberg Lettuce',
  'Cashew Nuts',
  'Red Onions',
  'Pumpkin'
]

const availableDistricts = computed(() => {
  if (selectedProvince.value) {
    return getDistrictsForProvince(selectedProvince.value)
  }
  return Object.values(SRI_LANKA_REGIONS).flat()
})

const filteredFarms = computed(() => {
  return farms.value.filter(farm => {
    // Province filter
    if (selectedProvince.value && farm.province !== selectedProvince.value) {
      return false
    }
    // District filter
    if (selectedDistrict.value && farm.district !== selectedDistrict.value) {
      return false
    }
    // Crop filter
    if (selectedCrop.value && selectedCrop.value !== 'All Crops') {
      const q = selectedCrop.value.toLowerCase()
      const farmCrops = (farm.crops || []).map(c => c.toLowerCase())
      const match = farmCrops.some(c => c.includes(q) || q.includes(c))
      if (!match) return false
    }
    return true
  })
})

const getFarmDistance = (farm) => {
  if (userLocation.value && farm.latitude && farm.longitude) {
    return calculateDistance(userLocation.value.lat, userLocation.value.lng, farm.latitude, farm.longitude)
  }
  // If user selected a district, calculate from that district center
  if (selectedDistrict.value && DISTRICT_COORDINATES[selectedDistrict.value]) {
    const center = DISTRICT_COORDINATES[selectedDistrict.value]
    return calculateDistance(center.lat, center.lng, farm.latitude, farm.longitude)
  }
  // Default from Colombo
  return calculateDistance(6.9271, 79.8612, farm.latitude, farm.longitude)
}

const initMap = () => {
  if (!mapContainer.value) return

  mapInstance = L.map(mapContainer.value, {
    center: [7.8731, 80.7718],
    zoom: 7.5,
    attributionControl: false
  })

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 18,
    subdomains: ['a', 'b', 'c']
  }).addTo(mapInstance)

  markersGroup = L.layerGroup().addTo(mapInstance)
}

const renderMarkers = () => {
  if (!mapInstance || !markersGroup) return

  markersGroup.clearLayers()

  const farmIcon = L.divIcon({
    className: 'custom-farm-pin',
    html: `<div style="background: linear-gradient(135deg, #10b981, #059669); color: white; width: 34px; height: 34px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 3px solid white; box-shadow: 0 4px 10px rgba(0,0,0,0.3); font-size: 16px;">🌱</div>`,
    iconSize: [34, 34],
    iconAnchor: [17, 17],
    popupAnchor: [0, -17]
  })

  filteredFarms.value.forEach(farm => {
    if (farm.latitude && farm.longitude) {
      const distance = getFarmDistance(farm)
      const cropsHtml = (farm.crops || []).slice(0, 3).map(c => `<span style="background: #e8f5e9; color: #2e7d32; padding: 2px 6px; border-radius: 4px; font-size: 10px; margin-right: 4px;">${c}</span>`).join('')

      const popupContent = `
        <div style="font-family: inherit; font-size: 13px; max-width: 240px; padding: 2px;">
          <img src="${farm.imageUrl || 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=300'}" style="width: 100%; height: 90px; object-fit: cover; border-radius: 6px; margin-bottom: 6px;" />
          <strong style="color: #1b4332; font-size: 14px; display: block; margin-bottom: 2px;">${farm.farmName}</strong>
          <div style="color: #6b7280; font-size: 11px; margin-bottom: 4px;">
            <i class="bi bi-geo-alt-fill text-danger"></i> ${farm.district}, ${farm.province}
            ${distance ? ` &bull; <strong>${distance} km away</strong>` : ''}
          </div>
          <div style="margin-bottom: 6px;">${cropsHtml}</div>
          <div style="color: #f59e0b; font-weight: 700; font-size: 12px; margin-bottom: 8px;">
            ★ ${farm.averageRating || '4.8'} (${farm.totalReviews || 80}+ reviews)
          </div>
          <div style="display: flex; gap: 6px;">
            <a href="https://www.google.com/maps/dir/?api=1&destination=${farm.latitude},${farm.longitude}" target="_blank" style="flex: 1; background: #2d6a4f; color: white; padding: 6px 10px; border-radius: 6px; text-decoration: none; font-size: 11px; font-weight: 600; text-align: center;">Directions &rarr;</a>
            <a href="/farms/${farm.id}" style="flex: 1; background: #f3f4f6; color: #1f2937; padding: 6px 10px; border-radius: 6px; text-decoration: none; font-size: 11px; font-weight: 600; text-align: center;">View Farm</a>
          </div>
        </div>
      `

      const marker = L.marker([farm.latitude, farm.longitude], { icon: farmIcon })
      marker.bindPopup(popupContent)
      marker.on('click', () => {
        selectedFarm.value = farm
      })
      markersGroup.addLayer(marker)
    }
  })

  // Fit bounds if markers exist
  if (filteredFarms.value.length > 0) {
    const latLngs = filteredFarms.value
      .filter(f => f.latitude && f.longitude)
      .map(f => [f.latitude, f.longitude])
    if (latLngs.length > 0) {
      mapInstance.fitBounds(latLngs, { padding: [40, 40], maxZoom: 11 })
    }
  }
}

onMounted(async () => {
  initMap()
  try {
    const data = await farmService.getFarms()
    farms.value = data && data.length > 0 ? data : SAMPLE_FARMS
  } catch (err) {
    console.warn('Error fetching farms', err)
    farms.value = SAMPLE_FARMS
  } finally {
    loading.value = false
    renderMarkers()
  }
})

watch([selectedProvince, selectedDistrict, selectedCrop], () => {
  renderMarkers()
})

const panToFarm = (farm) => {
  selectedFarm.value = farm
  if (mapInstance && farm.latitude && farm.longitude) {
    mapInstance.setView([farm.latitude, farm.longitude], 12, { animate: true })
    // Open popup
    markersGroup.eachLayer(layer => {
      const latlng = layer.getLatLng()
      if (latlng.lat === farm.latitude && latlng.lng === farm.longitude) {
        layer.openPopup()
      }
    })
  }
}

const detectCurrentLocation = () => {
  if (navigator.geolocation) {
    locationStatus.value = 'Locating...'
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        userLocation.value = {
          lat: pos.coords.latitude,
          lng: pos.coords.longitude
        }
        locationStatus.value = 'Location set!'

        // User location pin
        const userIcon = L.divIcon({
          className: 'user-pin',
          html: `<div style="background-color: #3b82f6; width: 22px; height: 22px; border-radius: 50%; border: 3px solid white; box-shadow: 0 0 10px rgba(59,130,246,0.8);"></div>`,
          iconSize: [22, 22]
        })
        L.marker([pos.coords.latitude, pos.coords.longitude], { icon: userIcon })
          .addTo(mapInstance)
          .bindPopup('<strong>Your Location</strong>')
          .openPopup()

        renderMarkers()
        setTimeout(() => { locationStatus.value = '' }, 3000)
      },
      (err) => {
        console.warn('Geolocation error', err)
        locationStatus.value = 'Location access denied'
        setTimeout(() => { locationStatus.value = '' }, 3000)
      }
    )
  }
}

const resetFilters = () => {
  selectedProvince.value = ''
  selectedDistrict.value = ''
  selectedCrop.value = ''
}

onBeforeUnmount(() => {
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
})
</script>

<template>
  <div class="agricultural-map-view bg-light min-vh-100 py-4">
    <div class="container-xxl">
      <!-- Title Header -->
      <div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
        <div>
          <h3 class="fw-bold text-dark mb-1 d-flex align-items-center gap-2">
            <i class="bi bi-map-fill text-farmora-green"></i>
            <span>Sri Lanka Agricultural Map</span>
          </h3>
          <p class="text-muted small mb-0">
            Interactive GIS map of certified farms, organic orchards, and harvest fields across all 9 provinces.
          </p>
        </div>

        <div class="d-flex gap-2 mt-2 mt-md-0">
          <button
            class="btn btn-outline-success btn-sm d-flex align-items-center gap-1 shadow-sm"
            @click="detectCurrentLocation"
          >
            <i class="bi bi-crosshair"></i>
            <span>{{ locationStatus || 'Find Farms Near Me' }}</span>
          </button>
          <router-link to="/products" class="btn btn-farmora btn-sm">
            Browse Produce Catalog &rarr;
          </router-link>
        </div>
      </div>

      <!-- Filters Ribbon -->
      <div class="card border-0 shadow-sm rounded-4 p-3 mb-4 bg-white">
        <div class="row g-2 align-items-center">
          <!-- Province Filter -->
          <div class="col-md-3">
            <label class="form-label small fw-bold mb-1">Province</label>
            <select v-model="selectedProvince" class="form-select form-select-sm">
              <option value="">All Provinces (9)</option>
              <option v-for="prov in PROVINCES" :key="prov" :value="prov">{{ prov }}</option>
            </select>
          </div>

          <!-- District Filter -->
          <div class="col-md-3">
            <label class="form-label small fw-bold mb-1">District</label>
            <select v-model="selectedDistrict" class="form-select form-select-sm">
              <option value="">All Districts (25)</option>
              <option v-for="dist in availableDistricts" :key="dist" :value="dist">{{ dist }}</option>
            </select>
          </div>

          <!-- Crop Filter -->
          <div class="col-md-3">
            <label class="form-label small fw-bold mb-1">Crop / Product Filter</label>
            <select v-model="selectedCrop" class="form-select form-select-sm">
              <option v-for="crop in cropOptions" :key="crop" :value="crop">{{ crop }}</option>
            </select>
          </div>

          <!-- Reset & Counter -->
          <div class="col-md-3 d-flex align-items-end justify-content-between pt-md-3">
            <button class="btn btn-sm btn-outline-secondary" @click="resetFilters">
              <i class="bi bi-arrow-counterclockwise me-1"></i> Reset
            </button>
            <span class="small fw-semibold text-farmora-green">
              Showing {{ filteredFarms.length }} Farms
            </span>
          </div>
        </div>
      </div>

      <!-- Map & Sidebar Grid -->
      <div class="row g-4">
        <!-- Main Interactive Leaflet Map -->
        <div class="col-lg-8 col-xl-9">
          <div class="card border-0 shadow-sm rounded-4 overflow-hidden position-relative bg-white" style="height: 600px;">
            <div ref="mapContainer" style="height: 100%; width: 100%; z-index: 1;"></div>

            <!-- Floating Legend -->
            <div
              class="position-absolute bottom-0 start-0 m-3 p-2 bg-white rounded-3 shadow-sm small text-muted d-flex align-items-center gap-3"
              style="z-index: 500; font-size: 0.75rem;"
            >
              <span class="d-flex align-items-center gap-1">
                <span class="rounded-circle" style="width: 10px; height: 10px; background-color: #10b981;"></span>
                <span>Verified Farm</span>
              </span>
              <span class="d-flex align-items-center gap-1">
                <span class="rounded-circle" style="width: 10px; height: 10px; background-color: #3b82f6;"></span>
                <span>Your Location</span>
              </span>
            </div>
          </div>
        </div>

        <!-- Right Side: Farm Cards List -->
        <div class="col-lg-4 col-xl-3">
          <div class="card border-0 shadow-sm rounded-4 p-3 bg-white" style="max-height: 600px; display: flex; flex-direction: column;">
            <h6 class="fw-bold mb-3 d-flex justify-content-between align-items-center">
              <span>Discovered Farms</span>
              <span class="badge bg-farmora-light text-farmora-green">{{ filteredFarms.length }}</span>
            </h6>

            <div class="overflow-y-auto d-flex flex-column gap-2 pe-1 flex-grow-1">
              <div
                v-for="farm in filteredFarms"
                :key="farm.id"
                class="farm-card-item p-2-5 rounded-3 border"
                :class="{ active: selectedFarm?.id === farm.id }"
                @click="panToFarm(farm)"
              >
                <div class="d-flex gap-2 align-items-center mb-1">
                  <img
                    :src="farm.imageUrl || 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=100'"
                    :alt="farm.farmName"
                    class="rounded-3 object-fit-cover"
                    style="width: 48px; height: 48px; min-width: 48px;"
                  />
                  <div class="overflow-hidden">
                    <div class="fw-bold text-dark text-truncate small">{{ farm.farmName }}</div>
                    <div class="text-muted" style="font-size: 0.72rem;">
                      {{ farm.district }}, {{ farm.province }}
                    </div>
                    <div class="text-success fw-semibold" style="font-size: 0.72rem;">
                      {{ getFarmDistance(farm) }} km away
                    </div>
                  </div>
                </div>

                <!-- Crops & Actions -->
                <div class="d-flex justify-content-between align-items-center mt-2 pt-2 border-top">
                  <span class="badge bg-light text-secondary border" style="font-size: 0.7rem;">
                    {{ farm.farmingMethod || 'ORGANIC' }}
                  </span>

                  <div class="d-flex gap-1">
                    <a
                      :href="`https://www.google.com/maps/dir/?api=1&destination=${farm.latitude},${farm.longitude}`"
                      target="_blank"
                      class="btn btn-sm btn-outline-success py-0 px-2"
                      style="font-size: 0.75rem;"
                      title="Directions"
                      @click.stop
                    >
                      <i class="bi bi-compass"></i>
                    </a>
                    <router-link
                      :to="`/farms/${farm.id}`"
                      class="btn btn-sm btn-farmora py-0 px-2"
                      style="font-size: 0.75rem;"
                      @click.stop
                    >
                      Details
                    </router-link>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-farmora-green {
  color: #2d6a4f !important;
}

.farm-card-item {
  cursor: pointer;
  background: #fdfdfd;
  transition: all 0.2s ease;
}

.farm-card-item:hover {
  background: #f0fdf4;
  border-color: #86efac !important;
  transform: translateX(2px);
}

.farm-card-item.active {
  background: #ecfdf5;
  border-color: #2d6a4f !important;
  box-shadow: 0 2px 8px rgba(45, 106, 79, 0.15);
}

.p-2-5 {
  padding: 0.65rem;
}
</style>

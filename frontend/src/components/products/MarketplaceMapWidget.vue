<script setup>
import { onMounted, ref, onBeforeUnmount } from 'vue'
import L from 'leaflet'
import { SAMPLE_FARMS } from '../../utils/agriculturalData'
import { farmService } from '../../services/farmService'

const mapContainer = ref(null)
let mapInstance = null

onMounted(async () => {
  if (!mapContainer.value) return

  // Center of Sri Lanka
  const sriLankaCenter = [7.8731, 80.7718]
  
  mapInstance = L.map(mapContainer.value, {
    center: sriLankaCenter,
    zoom: 6.8,
    zoomControl: false,
    attributionControl: false
  })

  // Add zoom control to top-left
  L.control.zoom({ position: 'topleft' }).addTo(mapInstance)

  // Clean OpenStreetMap tiles
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 18,
    subdomains: ['a', 'b', 'c']
  }).addTo(mapInstance)

  // Custom green pin icon
  const farmIcon = L.divIcon({
    className: 'custom-map-pin',
    html: `<div style="background-color: #2d6a4f; color: white; width: 26px; height: 26px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 2px solid white; box-shadow: 0 2px 6px rgba(0,0,0,0.3); font-size: 13px;">🌱</div>`,
    iconSize: [26, 26],
    iconAnchor: [13, 13],
    popupAnchor: [0, -13]
  })

  // Load farms
  let farms = SAMPLE_FARMS
  try {
    const fetchedFarms = await farmService.getFarms()
    if (fetchedFarms && fetchedFarms.length > 0) {
      farms = fetchedFarms
    }
  } catch (e) {
    console.warn('Using sample farms on map widget', e)
  }

  farms.forEach(farm => {
    if (farm.latitude && farm.longitude) {
      const marker = L.marker([farm.latitude, farm.longitude], { icon: farmIcon }).addTo(mapInstance)
      marker.bindPopup(`
        <div style="font-family: inherit; font-size: 12px; max-width: 180px;">
          <strong style="color: #1b4332; font-size: 13px;">${farm.farmName}</strong>
          <div style="color: #6b7280; margin: 2px 0;">${farm.district}, ${farm.province}</div>
          <div style="color: #059669; font-weight: 600;">⭐ ${farm.averageRating || '4.8'} (${farm.totalReviews || 50}+)</div>
          <a href="/farms/${farm.id}" style="color: #2d6a4f; text-decoration: none; font-weight: 600; display: inline-block; margin-top: 4px;">View Farm &rarr;</a>
        </div>
      `)
    }
  })

  setTimeout(() => {
    mapInstance?.invalidateSize()
  }, 400)
})

onBeforeUnmount(() => {
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
})
</script>

<template>
  <div class="card border-0 shadow-sm rounded-4 overflow-hidden mb-4">
    <div class="card-header bg-white border-0 pt-3 px-3 pb-2 d-flex justify-content-between align-items-center">
      <h6 class="fw-bold text-dark mb-0 d-flex align-items-center gap-2">
        <i class="bi bi-geo-alt-fill text-farmora-green"></i>
        <span>Farm Discovery Map</span>
      </h6>
      <router-link to="/agricultural-map" class="text-farmora-green fw-semibold text-decoration-none small d-flex align-items-center gap-1">
        <span>View Full Map</span>
        <i class="bi bi-arrow-right-short fs-6"></i>
      </router-link>
    </div>

    <!-- Map container -->
    <div class="position-relative">
      <div ref="mapContainer" style="height: 260px; width: 100%; z-index: 1;"></div>
      
      <!-- Legend -->
      <div class="map-legend position-absolute bottom-0 start-0 m-2 px-2 py-1 bg-white bg-opacity-90 rounded-pill shadow-sm small text-muted d-flex align-items-center gap-1" style="z-index: 500; font-size: 0.72rem;">
        <span class="rounded-circle" style="width: 8px; height: 8px; background-color: #2d6a4f;"></span>
        <span>Farm Location</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-farmora-green {
  color: #2d6a4f !important;
}
.text-farmora-green:hover {
  color: #1b4332 !important;
}
</style>

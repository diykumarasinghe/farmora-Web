<script setup>
import { onMounted, onBeforeUnmount, ref, watch, nextTick } from 'vue'
import L from 'leaflet'

const props = defineProps({
  latitude: {
    type: Number,
    default: 7.8731 // Sri Lanka central latitude default
  },
  longitude: {
    type: Number,
    default: 80.7718 // Sri Lanka central longitude default
  },
  readonly: {
    type: Boolean,
    default: false
  },
  height: {
    type: String,
    default: '340px'
  }
})

const emit = defineEmits(['update:location'])

const mapContainer = ref(null)
const mapId = 'map-' + Math.random().toString(36).substring(2, 9)
let mapInstance = null
let markerInstance = null

const locating = ref(false)
const geoError = ref('')

const customIcon = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
})

const initMap = () => {
  if (mapInstance || !document.getElementById(mapId)) return

  const initialLat = props.latitude || 7.8731
  const initialLng = props.longitude || 80.7718
  const initialZoom = (props.latitude && props.longitude) ? 12 : 7

  mapInstance = L.map(mapId, {
    center: [initialLat, initialLng],
    zoom: initialZoom,
    scrollWheelZoom: !props.readonly
  })

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19
  }).addTo(mapInstance)

  markerInstance = L.marker([initialLat, initialLng], {
    icon: customIcon,
    draggable: !props.readonly
  }).addTo(mapInstance)

  if (!props.readonly) {
    markerInstance.on('dragend', (e) => {
      const pos = e.target.getLatLng()
      emit('update:location', { lat: Number(pos.lat.toFixed(6)), lng: Number(pos.lng.toFixed(6)) })
    })

    mapInstance.on('click', (e) => {
      const { lat, lng } = e.latlng
      markerInstance.setLatLng([lat, lng])
      emit('update:location', { lat: Number(lat.toFixed(6)), lng: Number(lng.toFixed(6)) })
    })
  }

  // Ensure map tiles redraw cleanly
  setTimeout(() => {
    mapInstance?.invalidateSize()
  }, 250)
}

const detectCurrentLocation = () => {
  if (!navigator.geolocation) {
    geoError.value = 'Geolocation is not supported by your browser.'
    return
  }

  locating.value = true
  geoError.value = ''

  navigator.geolocation.getCurrentPosition(
    (position) => {
      locating.value = false
      const lat = Number(position.coords.latitude.toFixed(6))
      const lng = Number(position.coords.longitude.toFixed(6))

      if (mapInstance && markerInstance) {
        markerInstance.setLatLng([lat, lng])
        mapInstance.setView([lat, lng], 14)
      }
      emit('update:location', { lat, lng })
    },
    (err) => {
      locating.value = false
      geoError.value = 'Could not retrieve GPS location: ' + err.message
    },
    { enableHighAccuracy: true, timeout: 10000 }
  )
}

watch(
  () => [props.latitude, props.longitude],
  ([newLat, newLng]) => {
    if (mapInstance && markerInstance && newLat && newLng) {
      const cur = markerInstance.getLatLng()
      if (Math.abs(cur.lat - newLat) > 0.0001 || Math.abs(cur.lng - newLng) > 0.0001) {
        markerInstance.setLatLng([newLat, newLng])
        mapInstance.setView([newLat, newLng], mapInstance.getZoom() || 12)
      }
    }
  }
)

onMounted(() => {
  nextTick(() => {
    initMap()
  })
})

onBeforeUnmount(() => {
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
})
</script>

<template>
  <div class="gps-map-wrapper">
    <div v-if="!readonly" class="d-flex justify-content-between align-items-center mb-2">
      <div class="small text-muted">
        <i class="bi bi-geo-alt-fill text-danger me-1"></i>
        Click on the map or drag the marker to pinpoint exact farm entrance.
      </div>
      <button
        type="button"
        class="btn btn-sm btn-outline-success d-flex align-items-center gap-1"
        @click="detectCurrentLocation"
        :disabled="locating"
      >
        <span v-if="locating" class="spinner-border spinner-border-sm"></span>
        <i v-else class="bi bi-crosshair"></i>
        <span>Use My GPS</span>
      </button>
    </div>

    <div v-if="geoError" class="alert alert-warning py-1 small mb-2">
      <i class="bi bi-exclamation-circle me-1"></i>{{ geoError }}
    </div>

    <div
      :id="mapId"
      ref="mapContainer"
      class="rounded-3 border shadow-sm"
      :style="{ height, width: '100%', zIndex: 1 }"
    ></div>
  </div>
</template>

<style scoped>
.gps-map-wrapper {
  position: relative;
  width: 100%;
}
</style>

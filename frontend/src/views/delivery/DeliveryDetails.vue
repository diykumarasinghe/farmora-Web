<script setup>
import { onMounted, ref, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { deliveryService } from '../../services/deliveryService'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const route = useRoute()

const delivery = ref(null)
const loading = ref(true)
const updating = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const statusNotes = ref('')

const mapContainer = ref(null)
let leafletMap = null
let driverMarker = null

const loadDelivery = async () => {
  try {
    delivery.value = await deliveryService.getDeliveryById(route.params.id)
    nextTick(() => {
      initMap()
    })
  } catch (err) {
    errorMsg.value = 'Could not load delivery details.'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDelivery()
})

const initMap = () => {
  if (!mapContainer.value || !delivery.value) return
  if (leafletMap) {
    leafletMap.remove()
    leafletMap = null
  }

  const defaultLat = delivery.value.currentLat || 6.9271
  const defaultLng = delivery.value.currentLng || 79.8612

  leafletMap = L.map(mapContainer.value).setView([defaultLat, defaultLng], 12)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap'
  }).addTo(leafletMap)

  L.marker([defaultLat - 0.05, defaultLng - 0.04]).addTo(leafletMap)
    .bindPopup('<b>Farm Origin:</b> ' + (delivery.value.pickupAddress || 'Farm Depot'))

  L.marker([defaultLat + 0.04, defaultLng + 0.03]).addTo(leafletMap)
    .bindPopup('<b>Destination:</b> ' + (delivery.value.deliveryAddress || 'Recipient'))

  const driverIcon = L.divIcon({
    className: 'custom-driver-icon',
    html: `<div class="bg-success text-white rounded-circle p-2 shadow-lg d-flex align-items-center justify-content-center" style="width: 36px; height: 36px;"><i class="bi bi-truck fs-5"></i></div>`,
    iconSize: [36, 36],
    iconAnchor: [18, 18]
  })

  driverMarker = L.marker([defaultLat, defaultLng], { icon: driverIcon }).addTo(leafletMap)
    .bindPopup('<b>Current Van Location</b>')
    .openPopup()
}

const updateStatus = async (newStatus) => {
  updating.value = true
  errorMsg.value = ''
  try {
    delivery.value = await deliveryService.updateDeliveryStatus(delivery.value.id, newStatus, statusNotes.value)
    successMsg.value = `Status updated to ${newStatus}.`
    statusNotes.value = ''
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to update status.'
  } finally {
    updating.value = false
  }
}

const simulateLocationPing = async () => {
  if (!delivery.value) return
  const currentLat = delivery.value.currentLat || 6.9271
  const currentLng = delivery.value.currentLng || 79.8612

  const newLat = currentLat + 0.005
  const newLng = currentLng + 0.004

  try {
    delivery.value = await deliveryService.updateLocation(delivery.value.id, {
      currentLat: newLat,
      currentLng: newLng,
      trackingNotes: 'GPS ping: vehicle approaching drop-off destination.'
    })
    if (driverMarker && leafletMap) {
      driverMarker.setLatLng([newLat, newLng])
      leafletMap.panTo([newLat, newLng])
    }
    successMsg.value = 'Live vehicle GPS coordinate pinged successfully!'
  } catch (err) {
    errorMsg.value = 'Failed to update GPS ping.'
  }
}

const statusColor = (status) => {
  switch (status) {
    case 'AVAILABLE': return 'bg-success'
    case 'ASSIGNED': return 'bg-warning text-dark'
    case 'PICKED_UP': return 'bg-info text-dark'
    case 'IN_TRANSIT': return 'bg-primary'
    case 'DELIVERED': return 'bg-success'
    case 'CANCELLED': return 'bg-danger'
    default: return 'bg-secondary'
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="my" />
      </div>

      <div class="col-lg-9">
        <LoadingSpinner v-if="loading" message="Loading delivery route details..." />

        <div v-else-if="delivery" class="card border-0 shadow-sm p-4 rounded-4">
          <!-- Header -->
          <div class="d-flex justify-content-between align-items-start mb-4 pb-3 border-bottom">
            <div>
              <span class="badge px-3 py-2 rounded-pill me-2" :class="statusColor(delivery.status)">
                {{ delivery.status }}
              </span>
              <h4 class="fw-bold d-inline align-middle text-dark">Delivery Dispatch Run #{{ delivery.id }}</h4>
              <div class="small text-muted mt-1">Linked Order: #{{ delivery.order?.orderNumber || delivery.order?.id }}</div>
            </div>
            <router-link to="/delivery/my-deliveries" class="btn btn-outline-secondary btn-sm">
              <i class="bi bi-arrow-left me-1"></i> Back to Runs
            </router-link>
          </div>

          <div v-if="successMsg" class="alert alert-success small alert-dismissible" role="alert">
            <i class="bi bi-check-circle-fill me-1"></i> {{ successMsg }}
            <button type="button" class="btn-close" @click="successMsg = ''"></button>
          </div>
          <div v-if="errorMsg" class="alert alert-danger small" role="alert">{{ errorMsg }}</div>

          <!-- Interactive Leaflet Route Map -->
          <div class="card border-0 shadow-sm rounded-4 overflow-hidden mb-4">
            <div class="p-3 bg-light border-bottom d-flex justify-content-between align-items-center">
              <span class="fw-bold small text-dark"><i class="bi bi-map text-success me-1"></i> Live Delivery Route Map</span>
              <button class="btn btn-sm btn-outline-success py-1 px-2 fw-semibold" @click="simulateLocationPing">
                <i class="bi bi-geo-alt-fill me-1"></i> Ping Current GPS
              </button>
            </div>
            <div ref="mapContainer" style="height: 320px; width: 100%;"></div>
          </div>

          <!-- Route Info -->
          <div class="row g-4 mb-4">
            <div class="col-md-6">
              <div class="card border-0 bg-light p-3 h-100 rounded-3">
                <div class="fw-bold mb-2 text-danger">
                  <i class="bi bi-geo-alt-fill me-2"></i>Pickup Location (Farm Depot)
                </div>
                <p class="small text-dark mb-1">{{ delivery.pickupAddress }}</p>
                <div class="small text-muted">
                  Farmer: {{ delivery.order?.farmer?.fullName }} ({{ delivery.order?.farmer?.phone }})
                </div>
              </div>
            </div>
            <div class="col-md-6">
              <div class="card border-0 bg-light p-3 h-100 rounded-3">
                <div class="fw-bold mb-2 text-success">
                  <i class="bi bi-pin-map-fill me-2"></i>Drop-off Destination
                </div>
                <p class="small text-dark mb-1">{{ delivery.deliveryAddress }}</p>
                <div class="small text-muted">
                  Recipient: {{ delivery.order?.buyer?.fullName }} ({{ delivery.recipientPhone || delivery.order?.buyer?.phone }})
                </div>
                <div v-if="delivery.specialInstructions" class="small text-info mt-1 fst-italic">
                  Note: "{{ delivery.specialInstructions }}"
                </div>
              </div>
            </div>
          </div>

          <!-- Order Summary Details -->
          <div class="mb-4 p-3 bg-light rounded-3">
            <h6 class="fw-bold mb-3 text-dark">Produce Parcel Details</h6>
            <div class="row g-3">
              <div class="col-sm-6">
                <div class="small text-muted">Produce Item</div>
                <div class="fw-bold text-dark">{{ delivery.order?.product?.name }}</div>
              </div>
              <div class="col-sm-6">
                <div class="small text-muted">Weight / Quantity</div>
                <div class="fw-bold text-dark">{{ delivery.order?.quantity }} {{ delivery.order?.product?.unit || 'kg' }}</div>
              </div>
              <div class="col-sm-6">
                <div class="small text-muted">Delivery Fee Payout</div>
                <div class="fw-bold text-success fs-6">LKR {{ Number(delivery.deliveryFee || 350).toLocaleString() }}</div>
              </div>
              <div class="col-sm-6">
                <div class="small text-muted">Estimated Window</div>
                <div class="fw-bold text-dark">{{ delivery.estimatedDeliveryTime || 'Standard Delivery' }}</div>
              </div>
            </div>
          </div>

          <!-- Status Update Remarks Input -->
          <div v-if="delivery.status !== 'DELIVERED' && delivery.status !== 'CANCELLED'" class="mb-3">
            <label class="form-label small fw-bold text-dark">Status Notes / Proof of Delivery Remarks</label>
            <input
              type="text"
              class="form-control form-control-sm"
              placeholder="e.g. Package verified, collected from farm gate, in transit on A1..."
              v-model="statusNotes"
            />
          </div>

          <!-- Action Buttons -->
          <div class="d-flex flex-wrap gap-2 pt-3 border-top">
            <button
              v-if="delivery.status === 'ASSIGNED'"
              class="btn btn-warning fw-semibold"
              :disabled="updating"
              @click="updateStatus('PICKED_UP')"
            >
              <span v-if="updating" class="spinner-border spinner-border-sm me-1"></span>
              <i class="bi bi-box-arrow-up me-1"></i> Confirm Pickup at Farm
            </button>
            <button
              v-if="delivery.status === 'PICKED_UP'"
              class="btn btn-info text-dark fw-semibold"
              :disabled="updating"
              @click="updateStatus('IN_TRANSIT')"
            >
              <span v-if="updating" class="spinner-border spinner-border-sm me-1"></span>
              <i class="bi bi-truck me-1"></i> Start Transit (En Route)
            </button>
            <button
              v-if="delivery.status === 'IN_TRANSIT'"
              class="btn btn-success fw-semibold px-4"
              :disabled="updating"
              @click="updateStatus('DELIVERED')"
            >
              <span v-if="updating" class="spinner-border spinner-border-sm me-1"></span>
              <i class="bi bi-check2-all me-1"></i> Mark Delivered & Complete
            </button>
            <span
              v-if="delivery.status === 'DELIVERED'"
              class="badge bg-success px-4 py-3 fw-bold fs-6"
            >
              <i class="bi bi-patch-check-fill me-1"></i> Delivery Complete!
            </span>
          </div>
        </div>

        <div v-else class="alert alert-danger">{{ errorMsg || 'Delivery not found.' }}</div>
      </div>
    </div>
  </div>
</template>

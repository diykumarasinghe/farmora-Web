<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import { deliveryService } from '../../services/deliveryService'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const props = defineProps({
  show: Boolean,
  order: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'deliveryUpdated'])

const delivery = ref(null)
const loading = ref(true)
const errorMsg = ref('')
const successMsg = ref('')
const isRedirecting = ref(false)

const newAddress = ref('')
const newPhone = ref('')
const redirectNotes = ref('')
const submittingRedirect = ref(false)

// Map variables
const mapContainer = ref(null)
let leafletMap = null
let driverMarker = null

const loadDelivery = async () => {
  if (!props.order) return
  loading.value = true
  errorMsg.value = ''
  try {
    delivery.value = await deliveryService.getDeliveryByOrderId(props.order.id)
    newAddress.value = delivery.value.deliveryAddress || ''
    newPhone.value = delivery.value.recipientPhone || ''
  } catch (err) {
    // If not created yet, try creating it
    try {
      delivery.value = await deliveryService.createDeliveryRequest({
        orderId: props.order.id,
        recipientPhone: props.order.buyer?.phone,
        deliveryAddress: props.order.deliveryAddress
      })
    } catch (e) {
      errorMsg.value = 'Could not load delivery tracking information.'
    }
  } finally {
    loading.value = false
    nextTick(() => {
      initMap()
    })
  }
}

watch(() => props.show, (newVal) => {
  if (newVal) {
    isRedirecting.value = false
    loadDelivery()
  } else {
    destroyMap()
  }
})

const initMap = () => {
  if (!mapContainer.value) return
  destroyMap()

  // Default coordinate center (Sri Lanka center / Colombo)
  const defaultLat = delivery.value?.currentLat || 6.9271
  const defaultLng = delivery.value?.currentLng || 79.8612

  leafletMap = L.map(mapContainer.value).setView([defaultLat, defaultLng], 12)

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap'
  }).addTo(leafletMap)

  // Pickup Marker (Farm origin)
  const pickupMarker = L.marker([defaultLat - 0.05, defaultLng - 0.04], {
    title: 'Farm Origin'
  }).addTo(leafletMap)
    .bindPopup('<b>Farm Origin:</b> ' + (delivery.value?.pickupAddress || 'Farm Depot'))

  // Dropoff Marker (Destination)
  const destMarker = L.marker([defaultLat + 0.04, defaultLng + 0.03], {
    title: 'Drop-off Destination'
  }).addTo(leafletMap)
    .bindPopup('<b>Destination:</b> ' + (delivery.value?.deliveryAddress || 'Recipient Address'))

  // Driver Live Marker
  const driverIcon = L.divIcon({
    className: 'custom-driver-icon',
    html: `<div class="bg-success text-white rounded-circle p-2 shadow-lg d-flex align-items-center justify-content-center" style="width: 38px; height: 38px;"><i class="bi bi-truck fs-5"></i></div>`,
    iconSize: [38, 38],
    iconAnchor: [19, 19]
  })

  driverMarker = L.marker([defaultLat, defaultLng], { icon: driverIcon }).addTo(leafletMap)
    .bindPopup('<b>Driver Position:</b> ' + (delivery.value?.deliveryPartner?.fullName || 'Delivery Partner'))
    .openPopup()

  // Draw simulated route line
  const routePoints = [
    [defaultLat - 0.05, defaultLng - 0.04],
    [defaultLat, defaultLng],
    [defaultLat + 0.04, defaultLng + 0.03]
  ]
  L.polyline(routePoints, { color: '#2d6a4f', weight: 4, dashArray: '6, 8' }).addTo(leafletMap)
}

const destroyMap = () => {
  if (leafletMap) {
    leafletMap.remove()
    leafletMap = null
  }
}

// Live simulation step
const simulateDriverMove = async () => {
  if (!delivery.value) return
  const currentLat = delivery.value.currentLat || 6.9271
  const currentLng = delivery.value.currentLng || 79.8612

  // Nudge coordinate slightly towards destination
  const newLat = currentLat + 0.005
  const newLng = currentLng + 0.004

  try {
    const updated = await deliveryService.updateLocation(delivery.value.id, {
      currentLat: newLat,
      currentLng: newLng,
      trackingNotes: 'Driver moving along highway A1 towards destination.'
    })
    delivery.value = updated
    if (driverMarker && leafletMap) {
      driverMarker.setLatLng([newLat, newLng])
      leafletMap.panTo([newLat, newLng])
    }
    successMsg.value = 'Driver GPS coordinates updated in real-time!'
  } catch (err) {
    console.error('Failed to update live coordinates', err)
  }
}

const handleRedirect = async () => {
  if (!newAddress.value) {
    errorMsg.value = 'Please enter a valid destination address.'
    return
  }
  submittingRedirect.value = true
  errorMsg.value = ''
  try {
    const updated = await deliveryService.redirectDelivery(delivery.value.id, {
      newDeliveryAddress: newAddress.value,
      newRecipientPhone: newPhone.value,
      instructions: redirectNotes.value
    })
    delivery.value = updated
    isRedirecting.value = false
    successMsg.value = 'Delivery address successfully updated and redirected!'
    emit('deliveryUpdated', updated)
    initMap()
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to redirect delivery.'
  } finally {
    submittingRedirect.value = false
  }
}

const handleCancelDelivery = async () => {
  if (!confirm('Are you sure you want to cancel this delivery request?')) return
  try {
    const updated = await deliveryService.cancelDelivery(delivery.value.id, 'Cancelled by recipient')
    delivery.value = updated
    successMsg.value = 'Delivery request cancelled.'
    emit('deliveryUpdated', updated)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to cancel delivery.'
  }
}

const canRedirect = () => {
  return delivery.value && delivery.value.status !== 'DELIVERED' && delivery.value.status !== 'CANCELLED'
}
</script>

<template>
  <div v-if="show && order" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.6); z-index: 1060;">
    <div class="modal-dialog modal-dialog-centered modal-xl">
      <div class="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
        <!-- Header -->
        <div class="modal-header bg-farmora-green text-white p-3 px-4">
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-geo-alt-fill fs-5"></i>
            <h5 class="modal-title fw-bold mb-0">Live Delivery Route & Real-Time Tracking</h5>
          </div>
          <button type="button" class="btn-close btn-close-white" @click="$emit('close')"></button>
        </div>

        <div class="modal-body p-4">
          <div v-if="successMsg" class="alert alert-success py-2 small mb-3 alert-dismissible" role="alert">
            <i class="bi bi-check-circle-fill me-1"></i> {{ successMsg }}
            <button type="button" class="btn-close" @click="successMsg = ''"></button>
          </div>
          <div v-if="errorMsg" class="alert alert-danger py-2 small mb-3">
            <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ errorMsg }}
          </div>

          <div v-if="loading" class="text-center py-5">
            <span class="spinner-border text-success"></span>
            <div class="small text-muted mt-2">Connecting to GPS dispatch satellite...</div>
          </div>

          <div v-else-if="delivery" class="row g-4">
            <!-- Left Column: Map & Route Tracker -->
            <div class="col-lg-7">
              <div class="card border-0 shadow-sm rounded-4 overflow-hidden mb-3">
                <div class="p-3 bg-light border-bottom d-flex justify-content-between align-items-center">
                  <span class="fw-bold small text-dark">
                    <i class="bi bi-broadcast text-danger me-1"></i> Live Satellite Dispatch View
                  </span>
                  <button
                    class="btn btn-sm btn-outline-success py-1 px-2 fw-semibold"
                    @click="simulateDriverMove"
                    title="Simulate live GPS driver ping"
                  >
                    <i class="bi bi-play-fill"></i> Simulate Ping
                  </button>
                </div>
                <div ref="mapContainer" style="height: 360px; width: 100%;"></div>
              </div>

              <!-- Live Status Stepper -->
              <div class="p-3 bg-light rounded-3">
                <div class="d-flex justify-content-between text-center small">
                  <div :class="['AVAILABLE', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED'].includes(delivery.status) ? 'text-success fw-bold' : 'text-muted'">
                    <i class="bi bi-clipboard-check fs-5 d-block mb-1"></i> Dispatched
                  </div>
                  <div :class="['ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED'].includes(delivery.status) ? 'text-success fw-bold' : 'text-muted'">
                    <i class="bi bi-person-check fs-5 d-block mb-1"></i> Partner Assigned
                  </div>
                  <div :class="['PICKED_UP', 'IN_TRANSIT', 'DELIVERED'].includes(delivery.status) ? 'text-success fw-bold' : 'text-muted'">
                    <i class="bi bi-box-arrow-up fs-5 d-block mb-1"></i> Picked Up
                  </div>
                  <div :class="['IN_TRANSIT', 'DELIVERED'].includes(delivery.status) ? 'text-success fw-bold' : 'text-muted'">
                    <i class="bi bi-truck fs-5 d-block mb-1"></i> On the Way
                  </div>
                  <div :class="delivery.status === 'DELIVERED' ? 'text-success fw-bold' : 'text-muted'">
                    <i class="bi bi-check2-circle fs-5 d-block mb-1"></i> Delivered
                  </div>
                </div>
              </div>
            </div>

            <!-- Right Column: Dispatch Details & Redirect Form -->
            <div class="col-lg-5">
              <!-- Driver Info Card -->
              <div class="card border-0 bg-light p-3 rounded-3 mb-3">
                <div class="d-flex align-items-center gap-3">
                  <div class="bg-success text-white rounded-circle p-2 d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
                    <i class="bi bi-person-badge fs-4"></i>
                  </div>
                  <div class="flex-grow-1">
                    <div class="small text-muted">Assigned Courier</div>
                    <h6 class="fw-bold mb-0 text-dark">{{ delivery.deliveryPartner?.fullName || 'Assigning nearest partner...' }}</h6>
                    <span class="small text-secondary">{{ delivery.deliveryPartner?.phone || '+94 77 987 6543' }}</span>
                  </div>
                  <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-1-5 rounded-pill">
                    {{ delivery.status }}
                  </span>
                </div>
              </div>

              <!-- Estimated Time & Notes -->
              <div class="card border-0 p-3 rounded-3 shadow-sm mb-3">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <span class="small text-muted">Estimated Delivery Window:</span>
                  <span class="fw-bold text-farmora-green fs-6">{{ delivery.estimatedDeliveryTime || 'Within 2 hours' }}</span>
                </div>
                <div class="small text-muted">
                  <strong>Latest GPS Note:</strong>
                  <span class="fst-italic text-dark ms-1">{{ delivery.trackingNotes || 'Order verified and queued for fast dispatch.' }}</span>
                </div>
              </div>

              <!-- Address summary -->
              <div v-if="!isRedirecting" class="card border-0 p-3 rounded-3 shadow-sm mb-3">
                <div class="small text-muted fw-bold mb-1">
                  <i class="bi bi-geo-alt-fill text-danger me-1"></i> Drop-off Destination:
                </div>
                <p class="small text-dark mb-2">{{ delivery.deliveryAddress }}</p>
                <div class="small text-muted">
                  <strong>Recipient Mobile:</strong> {{ delivery.recipientPhone || 'Primary Contact' }}
                </div>
                <div v-if="delivery.specialInstructions" class="small text-muted mt-1 fst-italic">
                  "{{ delivery.specialInstructions }}"
                </div>

                <div class="d-flex gap-2 mt-3 pt-2 border-top">
                  <button
                    v-if="canRedirect()"
                    class="btn btn-outline-warning text-dark btn-sm flex-fill fw-semibold"
                    @click="isRedirecting = true"
                  >
                    <i class="bi bi-pencil-square me-1"></i> Redirect / Change Address
                  </button>
                  <button
                    v-if="canRedirect()"
                    class="btn btn-outline-danger btn-sm"
                    @click="handleCancelDelivery"
                  >
                    <i class="bi bi-x-circle me-1"></i> Cancel Delivery
                  </button>
                </div>
              </div>

              <!-- REDIRECT FORM -->
              <div v-else class="card border border-warning bg-warning-subtle p-3 rounded-3 mb-3">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <h6 class="fw-bold text-dark mb-0"><i class="bi bi-geo text-danger me-1"></i> Redirect Destination</h6>
                  <button type="button" class="btn-close" @click="isRedirecting = false"></button>
                </div>
                <p class="small text-dark mb-3">Update your delivery address or contact instructions before the driver reaches your location.</p>

                <div class="mb-2">
                  <label class="form-label small fw-bold text-dark">New Drop-off Address</label>
                  <input
                    type="text"
                    class="form-control form-control-sm"
                    v-model="newAddress"
                    required
                  />
                </div>
                <div class="mb-2">
                  <label class="form-label small fw-bold text-dark">Updated Contact Phone</label>
                  <input
                    type="tel"
                    class="form-control form-control-sm"
                    v-model="newPhone"
                  />
                </div>
                <div class="mb-3">
                  <label class="form-label small fw-bold text-dark">Driver Notes / Instructions</label>
                  <input
                    type="text"
                    class="form-control form-control-sm"
                    placeholder="e.g. Leave with security guard at front gate..."
                    v-model="redirectNotes"
                  />
                </div>

                <div class="d-flex justify-content-end gap-2">
                  <button class="btn btn-sm btn-light" @click="isRedirecting = false">Cancel</button>
                  <button
                    class="btn btn-sm btn-success fw-bold"
                    @click="handleRedirect"
                    :disabled="submittingRedirect"
                  >
                    <span v-if="submittingRedirect" class="spinner-border spinner-border-sm me-1"></span>
                    Save New Destination
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer bg-light px-4 py-3">
          <button type="button" class="btn btn-outline-secondary btn-sm px-4" @click="$emit('close')">Close</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bg-farmora-green {
  background-color: #2d6a4f !important;
}
.text-farmora-green {
  color: #2d6a4f !important;
}
.py-1-5 {
  padding-top: 0.35rem;
  padding-bottom: 0.35rem;
}
</style>

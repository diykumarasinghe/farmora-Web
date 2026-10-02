<script setup>
import { ref, onMounted, watch } from 'vue'
import { pickupService } from '../../services/pickupService'

const props = defineProps({
  show: Boolean,
  order: {
    type: Object,
    required: true
  },
  isFarmer: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'pickupScheduled'])

const appointment = ref(null)
const loading = ref(true)
const submitting = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

const isRescheduling = ref(false)
const verificationPasscode = ref('')

// Form fields
const pickupDate = ref('')
const timeSlot = ref('08:00 AM - 10:00 AM')
const vehicleNumber = ref('')
const contactPhone = ref('')
const instructions = ref('')
const rescheduleReason = ref('')

const timeSlots = [
  '08:00 AM - 10:00 AM',
  '10:00 AM - 12:00 PM',
  '01:00 PM - 03:00 PM',
  '03:00 PM - 05:00 PM',
  '05:00 PM - 07:00 PM'
]

const loadAppointment = async () => {
  if (!props.order) return
  loading.value = true
  errorMsg.value = ''
  try {
    appointment.value = await pickupService.getPickupByOrderId(props.order.id)
  } catch (err) {
    appointment.value = null
  } finally {
    loading.value = false
  }
}

watch(() => props.show, (newVal) => {
  if (newVal) {
    const tomorrow = new Date()
    tomorrow.setDate(tomorrow.getDate() + 1)
    pickupDate.value = tomorrow.toISOString().split('T')[0]
    isRescheduling.value = false
    loadAppointment()
  }
})

const handleSchedule = async () => {
  submitting.value = true
  errorMsg.value = ''
  successMsg.value = ''
  try {
    const res = await pickupService.schedulePickup({
      orderId: props.order.id,
      pickupDate: pickupDate.value,
      timeSlot: timeSlot.value,
      vehicleNumber: vehicleNumber.value,
      contactPhone: contactPhone.value,
      instructions: instructions.value
    })
    appointment.value = res
    successMsg.value = 'Farm pickup appointment scheduled successfully!'
    emit('pickupScheduled', res)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to schedule pickup.'
  } finally {
    submitting.value = false
  }
}

const handleReschedule = async () => {
  submitting.value = true
  errorMsg.value = ''
  successMsg.value = ''
  try {
    const res = await pickupService.reschedulePickup(appointment.value.id, {
      pickupDate: pickupDate.value,
      timeSlot: timeSlot.value,
      reason: rescheduleReason.value || 'Schedule change requested by buyer'
    })
    appointment.value = res
    isRescheduling.value = false
    successMsg.value = 'Pickup rescheduled successfully!'
    emit('pickupScheduled', res)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to reschedule pickup.'
  } finally {
    submitting.value = false
  }
}

const handleCancelPickup = async () => {
  if (!confirm('Are you sure you want to cancel this farm pickup appointment?')) return
  submitting.value = true
  errorMsg.value = ''
  try {
    const res = await pickupService.cancelPickup(appointment.value.id, 'Cancelled by user')
    appointment.value = res
    successMsg.value = 'Pickup appointment cancelled.'
    emit('pickupScheduled', res)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to cancel appointment.'
  } finally {
    submitting.value = false
  }
}

const handleFarmerVerifyAndComplete = async () => {
  submitting.value = true
  errorMsg.value = ''
  try {
    const res = await pickupService.updateStatus(appointment.value.id, 'COMPLETED', verificationPasscode.value)
    appointment.value = res
    successMsg.value = 'Passcode verified! Farm pickup completed and marked delivered.'
    emit('pickupScheduled', res)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Passcode verification failed.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div v-if="show && order" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.6); z-index: 1060;">
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
        <!-- Header -->
        <div class="modal-header bg-farmora-green text-white p-3 px-4">
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-shop fs-5"></i>
            <h5 class="modal-title fw-bold mb-0">Farm Pickup Logistics & Appointments</h5>
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

          <!-- Loading state -->
          <div v-if="loading" class="text-center py-4">
            <span class="spinner-border text-success"></span>
            <div class="small text-muted mt-2">Checking pickup schedule...</div>
          </div>

          <!-- VIEW EXISTING APPOINTMENT -->
          <div v-else-if="appointment && !isRescheduling">
            <!-- Pickup Header card -->
            <div class="card border-0 bg-light p-3 rounded-3 mb-4">
              <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                <div>
                  <span
                    class="badge px-3 py-1 rounded-pill me-2"
                    :class="appointment.status === 'COMPLETED' ? 'bg-success' : (appointment.status === 'CANCELLED' ? 'bg-danger' : 'bg-warning text-dark')"
                  >
                    {{ appointment.status }}
                  </span>
                  <span class="fw-bold text-dark">Appointment #{{ appointment.id }}</span>
                </div>
                <!-- Verification Passcode & QR Tag -->
                <div class="d-flex align-items-center gap-2">
                  <div class="bg-white border rounded-3 px-3 py-1 text-center shadow-sm">
                    <div class="small text-muted" style="font-size: 0.72rem;">GATE PASSCODE</div>
                    <div class="fw-bold text-farmora-green fs-5 tracking-wide">{{ appointment.passcode }}</div>
                  </div>
                  <div class="bg-white border rounded-3 p-1 text-center shadow-sm" style="width: 52px; height: 52px;" title="Scan QR Code at Gate">
                    <img
                      :src="'https://api.qrserver.com/v1/create-qr-code/?size=120x120&data=FARMORA-PICKUP-' + appointment.id + '-' + appointment.passcode"
                      alt="Pickup QR Code"
                      class="w-100 h-100 object-fit-contain"
                      @error="(e) => e.target.style.display = 'none'"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- Details Grid -->
            <div class="row g-3 mb-4">
              <div class="col-md-6">
                <div class="p-3 border rounded-3 h-100 bg-white shadow-sm">
                  <div class="small text-muted fw-bold text-uppercase mb-2">
                    <i class="bi bi-calendar-event text-success me-1"></i> Scheduled Schedule
                  </div>
                  <div class="fw-bold text-dark fs-6">{{ appointment.pickupDate }}</div>
                  <div class="text-muted small">{{ appointment.timeSlot }}</div>
                  <div v-if="appointment.rescheduledReason" class="small text-warning-emphasis mt-2">
                    * Reschedule note: {{ appointment.rescheduledReason }}
                  </div>
                </div>
              </div>

              <div class="col-md-6">
                <div class="p-3 border rounded-3 h-100 bg-white shadow-sm d-flex flex-column justify-content-between">
                  <div>
                    <div class="small text-muted fw-bold text-uppercase mb-2">
                      <i class="bi bi-geo-alt-fill text-danger me-1"></i> Collection Point
                    </div>
                    <div class="fw-bold text-dark fs-6">{{ appointment.pickupLocation }}</div>
                    <div class="small text-muted mt-1">
                      Producer: {{ appointment.farmer?.fullName }} ({{ appointment.farmer?.phone || appointment.farmer?.email }})
                    </div>
                  </div>
                  <div class="mt-2">
                    <a
                      :href="'https://www.google.com/maps/search/?api=1&query=' + encodeURIComponent(appointment.pickupLocation)"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="btn btn-sm btn-outline-primary d-inline-flex align-items-center gap-1"
                    >
                      <i class="bi bi-compass"></i> Farm GPS Navigation
                    </a>
                  </div>
                </div>
              </div>

              <div class="col-md-6">
                <div class="p-3 border rounded-3 h-100 bg-white shadow-sm">
                  <div class="small text-muted fw-bold text-uppercase mb-2">
                    <i class="bi bi-truck-front text-primary me-1"></i> Vehicle & Contact
                  </div>
                  <div class="small text-dark">
                    <strong>Vehicle Plate:</strong> {{ appointment.vehicleNumber || 'Standard Transport' }}
                  </div>
                  <div class="small text-dark mt-1">
                    <strong>Driver Contact:</strong> {{ appointment.contactPhone || appointment.buyer?.phone }}
                  </div>
                </div>
              </div>

              <div class="col-md-6">
                <div class="p-3 border rounded-3 h-100 bg-white shadow-sm">
                  <div class="small text-muted fw-bold text-uppercase mb-2">
                    <i class="bi bi-info-circle text-info me-1"></i> Special Instructions
                  </div>
                  <p class="small text-dark mb-0">
                    {{ appointment.instructions || 'Show your gate passcode at farm reception to verify and collect your harvest crates.' }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Farmer Passcode Verification Section (for Farmer role) -->
            <div v-if="isFarmer && appointment.status !== 'COMPLETED' && appointment.status !== 'CANCELLED'" class="p-3 border border-success-subtle bg-light-green rounded-3 mb-3">
              <h6 class="fw-bold text-dark mb-1"><i class="bi bi-shield-check text-success me-1"></i> Farmer Verification & Handover</h6>
              <p class="small text-muted mb-2">Enter the buyer's 6-digit gate passcode to verify collection and complete the order.</p>
              <div class="input-group input-group-sm mb-2" style="max-width: 320px;">
                <input
                  type="text"
                  class="form-control"
                  placeholder="Enter passcode (e.g. PK-84920)"
                  v-model="verificationPasscode"
                />
                <button
                  class="btn btn-success fw-bold"
                  type="button"
                  @click="handleFarmerVerifyAndComplete"
                  :disabled="submitting || !verificationPasscode"
                >
                  <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
                  Verify & Complete
                </button>
              </div>
            </div>

            <!-- Buyer Reschedule/Cancel Action Buttons -->
            <div v-if="appointment.status !== 'COMPLETED' && appointment.status !== 'CANCELLED'" class="d-flex justify-content-end gap-2 border-top pt-3">
              <button class="btn btn-outline-danger btn-sm" @click="handleCancelPickup" :disabled="submitting">
                <i class="bi bi-x-circle me-1"></i> Cancel Pickup
              </button>
              <button class="btn btn-outline-success btn-sm fw-semibold" @click="isRescheduling = true">
                <i class="bi bi-calendar2-range me-1"></i> Reschedule Appointment
              </button>
            </div>
          </div>

          <!-- SCHEDULE OR RESCHEDULE FORM -->
          <form v-else @submit.prevent="isRescheduling ? handleReschedule() : handleSchedule()">
            <div class="mb-3">
              <h6 class="fw-bold text-dark mb-1">
                {{ isRescheduling ? 'Reschedule Farm Pickup' : 'Schedule Farm Gate Pickup' }}
              </h6>
              <p class="small text-muted mb-3">
                Choose a suitable date and pickup window. The farm manager will have your produce packed and weighed for arrival.
              </p>
            </div>

            <div class="row g-3">
              <!-- Pickup Date -->
              <div class="col-md-6">
                <label class="form-label small fw-bold text-dark">Preferred Pickup Date</label>
                <input
                  type="date"
                  class="form-control"
                  v-model="pickupDate"
                  required
                />
              </div>

              <!-- Time Slot -->
              <div class="col-md-6">
                <label class="form-label small fw-bold text-dark">Collection Time Window</label>
                <select class="form-select" v-model="timeSlot" required>
                  <option v-for="slot in timeSlots" :key="slot" :value="slot">{{ slot }}</option>
                </select>
              </div>

              <!-- Vehicle Plate -->
              <div class="col-md-6">
                <label class="form-label small fw-bold text-dark">Vehicle Plate / Transport Mode</label>
                <input
                  type="text"
                  class="form-control"
                  placeholder="e.g. WP CAA-4819, Small Lorry, Van"
                  v-model="vehicleNumber"
                />
              </div>

              <!-- Contact Phone -->
              <div class="col-md-6">
                <label class="form-label small fw-bold text-dark">Driver / Collector Mobile</label>
                <input
                  type="tel"
                  class="form-control"
                  placeholder="e.g. +94 77 123 4567"
                  v-model="contactPhone"
                />
              </div>

              <!-- Reason if Rescheduling -->
              <div class="col-12" v-if="isRescheduling">
                <label class="form-label small fw-bold text-dark">Reason for Rescheduling</label>
                <input
                  type="text"
                  class="form-control"
                  placeholder="e.g. Transport delay, adjusting harvest schedule..."
                  v-model="rescheduleReason"
                  required
                />
              </div>

              <!-- Instructions -->
              <div class="col-12" v-else>
                <label class="form-label small fw-bold text-dark">Logistics Notes / Special Packaging</label>
                <textarea
                  class="form-control"
                  rows="2"
                  placeholder="e.g. Need loading assistance, bringing reusable crates..."
                  v-model="instructions"
                ></textarea>
              </div>
            </div>

            <div class="d-flex justify-content-end gap-2 border-top pt-3 mt-4">
              <button
                v-if="isRescheduling"
                type="button"
                class="btn btn-light btn-sm"
                @click="isRescheduling = false"
              >
                Back
              </button>
              <button type="button" class="btn btn-outline-secondary btn-sm" @click="$emit('close')">
                Cancel
              </button>
              <button
                type="submit"
                class="btn btn-farmora btn-sm px-4 fw-bold"
                :disabled="submitting"
              >
                <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
                {{ isRescheduling ? 'Confirm Reschedule' : 'Confirm Appointment' }}
              </button>
            </div>
          </form>
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
.bg-light-green {
  background-color: #f0fdf4;
}
.tracking-wide {
  letter-spacing: 0.12em;
}
</style>

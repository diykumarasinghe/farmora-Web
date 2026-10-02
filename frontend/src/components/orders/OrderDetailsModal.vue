<script setup>
import { computed, ref } from 'vue'
import { orderService } from '../../services/orderService'

const props = defineProps({
  show: Boolean,
  order: {
    type: Object,
    default: null
  },
  role: {
    type: String,
    default: 'BUYER'
  }
})

const emit = defineEmits(['close', 'orderUpdated', 'trackDelivery', 'schedulePickup', 'viewReceipt', 'payNow'])

const cancelling = ref(false)
const showCancelPrompt = ref(false)
const cancelReason = ref('')
const errorMsg = ref('')

const isPickup = computed(() => {
  return props.order?.fulfillmentType === 'PICKUP'
})

const currentStep = computed(() => {
  if (!props.order) return 1
  switch (props.order.status) {
    case 'PENDING': return 1
    case 'CONFIRMED': return 1
    case 'PROCESSING': return 2
    case 'READY_FOR_PICKUP':
    case 'OUT_FOR_DELIVERY': return 3
    case 'DELIVERED': return 4
    case 'CANCELLED': return 0
    default: return 1
  }
})

const canCancel = computed(() => {
  if (!props.order) return false
  return props.order.status !== 'DELIVERED' && props.order.status !== 'CANCELLED' && props.order.status !== 'OUT_FOR_DELIVERY'
})

const handleCancelOrder = async () => {
  cancelling.value = true
  errorMsg.value = ''
  try {
    const updated = await orderService.cancelOrder(props.order.id, cancelReason.value || 'Cancelled by buyer')
    emit('orderUpdated', updated)
    showCancelPrompt.value = false
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to cancel order.'
  } finally {
    cancelling.value = false
  }
}
</script>

<template>
  <div v-if="show && order" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.6); z-index: 1055;">
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
        <!-- Header -->
        <div class="modal-header bg-light border-bottom p-3 px-4">
          <div>
            <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-1 rounded-pill me-2 fw-semibold">
              {{ order.status }}
            </span>
            <h5 class="modal-title fw-bold d-inline align-middle text-dark">Order #{{ order.orderNumber || order.id }}</h5>
            <div class="text-muted small mt-1">Placed on {{ new Date(order.createdAt).toLocaleString() }}</div>
          </div>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>

        <div class="modal-body p-4">
          <div v-if="errorMsg" class="alert alert-danger py-2 small mb-3">
            <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ errorMsg }}
          </div>

          <!-- Order Status Timeline (Progress Stepper) -->
          <div v-if="order.status !== 'CANCELLED'" class="mb-4 p-3 bg-light rounded-3">
            <h6 class="fw-bold text-dark small text-uppercase tracking-wider mb-3">
              <i class="bi bi-clock-history text-success me-1"></i> Fulfillment Progress
            </h6>
            <div class="position-relative d-flex justify-content-between align-items-center px-2">
              <div class="progress position-absolute w-100" style="height: 4px; top: 18px; z-index: 0;">
                <div
                  class="progress-bar bg-success"
                  role="progressbar"
                  :style="{ width: ((currentStep - 1) / 3 * 100) + '%' }"
                ></div>
              </div>

              <!-- Step 1 -->
              <div class="text-center position-relative" style="z-index: 1;">
                <div
                  class="rounded-circle d-flex align-items-center justify-content-center mx-auto shadow-sm"
                  :class="currentStep >= 1 ? 'bg-success text-white' : 'bg-white text-muted border'"
                  style="width: 36px; height: 36px;"
                >
                  <i class="bi bi-check2"></i>
                </div>
                <div class="small fw-semibold mt-1" :class="currentStep >= 1 ? 'text-success' : 'text-muted'">Order Placed</div>
              </div>

              <!-- Step 2 -->
              <div class="text-center position-relative" style="z-index: 1;">
                <div
                  class="rounded-circle d-flex align-items-center justify-content-center mx-auto shadow-sm"
                  :class="currentStep >= 2 ? 'bg-success text-white' : 'bg-white text-muted border'"
                  style="width: 36px; height: 36px;"
                >
                  <i class="bi bi-box-seam"></i>
                </div>
                <div class="small fw-semibold mt-1" :class="currentStep >= 2 ? 'text-success' : 'text-muted'">Processing / Farm Pack</div>
              </div>

              <!-- Step 3 -->
              <div class="text-center position-relative" style="z-index: 1;">
                <div
                  class="rounded-circle d-flex align-items-center justify-content-center mx-auto shadow-sm"
                  :class="currentStep >= 3 ? 'bg-success text-white' : 'bg-white text-muted border'"
                  style="width: 36px; height: 36px;"
                >
                  <i :class="isPickup ? 'bi bi-shop' : 'bi bi-truck'"></i>
                </div>
                <div class="small fw-semibold mt-1" :class="currentStep >= 3 ? 'text-success' : 'text-muted'">
                  {{ isPickup ? 'Ready for Pickup' : 'Out for Delivery' }}
                </div>
              </div>

              <!-- Step 4 -->
              <div class="text-center position-relative" style="z-index: 1;">
                <div
                  class="rounded-circle d-flex align-items-center justify-content-center mx-auto shadow-sm"
                  :class="currentStep >= 4 ? 'bg-success text-white' : 'bg-white text-muted border'"
                  style="width: 36px; height: 36px;"
                >
                  <i class="bi bi-patch-check-fill"></i>
                </div>
                <div class="small fw-semibold mt-1" :class="currentStep >= 4 ? 'text-success' : 'text-muted'">Delivered</div>
              </div>
            </div>
          </div>

          <!-- Cancelled Notice -->
          <div v-else class="alert alert-danger mb-4">
            <h6 class="fw-bold mb-1"><i class="bi bi-x-octagon-fill me-1"></i> Order Cancelled</h6>
            <p class="small mb-0">Reason: {{ order.cancellationReason || 'Order was cancelled and inventory restocked.' }}</p>
          </div>

          <!-- Order Item Breakdown -->
          <div class="card border-0 bg-light p-3 rounded-3 mb-4">
            <div class="d-flex align-items-center gap-3">
              <img
                :src="order.product?.imageUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=150&q=80'"
                :alt="order.product?.name"
                class="rounded-3 object-fit-cover shadow-sm"
                style="width: 64px; height: 64px;"
              />
              <div class="flex-grow-1">
                <h6 class="fw-bold text-dark mb-1">{{ order.product?.name }}</h6>
                <div class="small text-muted">
                  {{ order.quantity }} {{ order.product?.unit || 'kg' }} &times; LKR {{ Number(order.unitPrice).toLocaleString() }}
                </div>
                <div class="small text-secondary mt-1">
                  <i class="bi bi-person-fill text-success me-1"></i> Farmer: <strong>{{ order.farmer?.fullName }}</strong> ({{ order.farmer?.phone || order.farmer?.email }})
                </div>
              </div>
              <div class="text-end">
                <div class="fs-5 fw-bold text-farmora-green">
                  LKR {{ Number(order.totalAmount).toLocaleString() }}
                </div>
                <span
                  class="badge small mt-1"
                  :class="order.paymentStatus === 'PAID' ? 'bg-success' : (order.paymentStatus === 'REFUNDED' ? 'bg-info text-dark' : 'bg-warning text-dark')"
                >
                  Payment: {{ order.paymentStatus || 'PAID' }}
                </span>
              </div>
            </div>
          </div>

          <!-- Logistics & Fulfillment Details -->
          <div class="row g-3 mb-4">
            <div class="col-md-6">
              <div class="p-3 border rounded-3 h-100 bg-white shadow-sm">
                <div class="fw-bold small text-muted mb-2 text-uppercase">
                  <i class="bi bi-geo-alt-fill text-danger me-1"></i> Origin (Farm Depot)
                </div>
                <p class="small text-dark mb-0">{{ order.pickupAddress || order.product?.location || 'Local Farm' }}</p>
              </div>
            </div>
            <div class="col-md-6">
              <div class="p-3 border rounded-3 h-100 bg-white shadow-sm">
                <div class="fw-bold small text-muted mb-2 text-uppercase">
                  <i :class="isPickup ? 'bi bi-shop text-primary me-1' : 'bi bi-truck text-success me-1'"></i>
                  {{ isPickup ? 'Pickup Method' : 'Delivery Destination' }}
                </div>
                <p class="small text-dark mb-0">
                  {{ isPickup ? 'Direct Farm Gate Collection' : (order.deliveryAddress || 'Standard Delivery Address') }}
                </p>
                <div v-if="order.notes" class="small text-muted mt-2 fst-italic">
                  "{{ order.notes }}"
                </div>
              </div>
            </div>
          </div>

          <!-- Cancellation Confirmation Box -->
          <div v-if="showCancelPrompt" class="p-3 border border-danger-subtle bg-danger-subtle rounded-3 mb-3">
            <h6 class="fw-bold text-danger mb-2">Cancel Order Confirmation</h6>
            <p class="small text-dark mb-2">Are you sure you want to cancel this order? The crop quantity will be returned to stock, and any completed payment will be marked for refund.</p>
            <input
              type="text"
              class="form-control form-control-sm mb-3"
              placeholder="Reason for cancellation (e.g. Changed plans, order placed by error)..."
              v-model="cancelReason"
            />
            <div class="d-flex justify-content-end gap-2">
              <button class="btn btn-sm btn-light" @click="showCancelPrompt = false">Keep Order</button>
              <button class="btn btn-sm btn-danger fw-bold" @click="handleCancelOrder" :disabled="cancelling">
                <span v-if="cancelling" class="spinner-border spinner-border-sm me-1"></span>
                Yes, Cancel Order
              </button>
            </div>
          </div>
        </div>

        <!-- Footer Actions -->
        <div class="modal-footer bg-light p-3 px-4 d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div>
            <button
              v-if="canCancel && !showCancelPrompt"
              class="btn btn-outline-danger btn-sm"
              @click="showCancelPrompt = true"
            >
              <i class="bi bi-x-circle me-1"></i> Cancel Order
            </button>
          </div>

          <div class="d-flex gap-2 flex-wrap">
            <!-- If UNPAID, Pay Now button -->
            <button
              v-if="order.paymentStatus === 'UNPAID'"
              class="btn btn-warning btn-sm fw-bold"
              @click="$emit('payNow', order)"
            >
              <i class="bi bi-credit-card me-1"></i> Pay Now (Online)
            </button>

            <!-- Track Delivery if DELIVERY -->
            <button
              v-if="!isPickup && order.status !== 'CANCELLED'"
              class="btn btn-outline-primary btn-sm fw-semibold"
              @click="$emit('trackDelivery', order)"
            >
              <i class="bi bi-geo-alt me-1"></i> Track Live Delivery
            </button>

            <!-- Schedule / View Pickup if PICKUP -->
            <button
              v-if="isPickup && order.status !== 'CANCELLED'"
              class="btn btn-outline-success btn-sm fw-semibold"
              @click="$emit('schedulePickup', order)"
            >
              <i class="bi bi-calendar-check me-1"></i> Pickup Appointment
            </button>

            <!-- View Invoice & Digital Receipt -->
            <button
              class="btn btn-farmora btn-sm fw-bold px-3"
              @click="$emit('viewReceipt', order)"
            >
              <i class="bi bi-receipt me-1"></i> Digital Invoice & Receipt
            </button>
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
.tracking-wider {
  letter-spacing: 0.05em;
}
</style>

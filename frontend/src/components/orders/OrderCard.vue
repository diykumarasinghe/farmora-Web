<script setup>
import { computed } from 'vue'

const props = defineProps({
  order: {
    type: Object,
    required: true
  },
  role: {
    type: String,
    default: 'BUYER'
  }
})

const emit = defineEmits([
  'updateStatus',
  'leaveReview',
  'viewDetails',
  'trackDelivery',
  'schedulePickup',
  'viewReceipt',
  'payNow',
  'cancelOrder'
])

const isPickup = computed(() => {
  return props.order.fulfillmentType === 'PICKUP'
})

const statusClass = computed(() => {
  switch (props.order.status) {
    case 'CONFIRMED':
      return 'badge-confirmed text-primary'
    case 'PROCESSING':
      return 'bg-info-subtle text-info border border-info-subtle'
    case 'READY_FOR_PICKUP':
      return 'badge-pending text-warning-emphasis'
    case 'OUT_FOR_DELIVERY':
      return 'bg-info text-dark'
    case 'DELIVERED':
      return 'badge-delivered text-success'
    case 'CANCELLED':
      return 'badge-sold-out text-danger'
    default:
      return 'bg-secondary text-white'
  }
})

const canCancel = computed(() => {
  return props.order.status !== 'DELIVERED' && props.order.status !== 'CANCELLED' && props.order.status !== 'OUT_FOR_DELIVERY'
})
</script>

<template>
  <div class="card shadow-sm border-0 mb-3 rounded-3 overflow-hidden">
    <div class="card-body p-4">
      <!-- Header row -->
      <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <div class="d-flex align-items-center flex-wrap gap-2">
          <span class="badge px-3 py-1-5 rounded-pill" :class="statusClass">{{ order.status }}</span>
          <span class="badge rounded-pill" :class="isPickup ? 'bg-primary-subtle text-primary border border-primary-subtle' : 'bg-success-subtle text-success border border-success-subtle'">
            <i :class="isPickup ? 'bi bi-shop me-1' : 'bi bi-truck me-1'"></i>
            {{ isPickup ? 'FARM PICKUP' : 'EXPRESS DELIVERY' }}
          </span>
          <span
            v-if="order.paymentStatus"
            class="badge rounded-pill"
            :class="order.paymentStatus === 'PAID' ? 'bg-success text-white' : (order.paymentStatus === 'REFUNDED' ? 'bg-info text-dark' : 'bg-warning text-dark')"
          >
            {{ order.paymentStatus }}
          </span>
          <span class="fw-bold text-dark">Order #{{ order.orderNumber || order.id }}</span>
          <span class="text-muted small">&bull; {{ new Date(order.createdAt).toLocaleDateString() }}</span>
        </div>
        <div class="text-success fw-bold fs-5">
          LKR {{ Number(order.totalAmount).toLocaleString() }}
        </div>
      </div>

      <!-- Produce & Logistics Row -->
      <div class="row g-3 mb-3">
        <div class="col-md-6">
          <div class="fw-bold fs-6 mb-1 text-dark">{{ order.product?.name }}</div>
          <div class="text-muted small mb-1">
            <span>{{ order.quantity }} {{ order.product?.unit || 'kg' }} &times; LKR {{ Number(order.unitPrice).toLocaleString() }}</span>
          </div>
          <div class="small text-secondary">
            <div><i class="bi bi-person-fill text-success me-1"></i> Farmer: <strong>{{ order.farmer?.fullName }}</strong></div>
            <div><i class="bi bi-person-badge text-primary me-1"></i> Buyer: <strong>{{ order.buyer?.fullName }}</strong></div>
          </div>
        </div>

        <div class="col-md-6 border-start-md ps-md-3">
          <div class="small text-muted mb-1">
            <strong><i class="bi bi-geo-alt me-1 text-danger"></i> Origin:</strong> {{ order.pickupAddress || 'Farm Depot' }}
          </div>
          <div class="small text-muted mb-1">
            <strong><i :class="isPickup ? 'bi bi-shop me-1 text-primary' : 'bi bi-truck me-1 text-success'"></i> Fulfillment:</strong>
            {{ isPickup ? 'Self Collection at Farm Gate' : (order.deliveryAddress || 'Standard Delivery') }}
          </div>
          <div v-if="order.cancellationReason" class="small text-danger mt-1">
            <strong>Cancellation:</strong> {{ order.cancellationReason }}
          </div>
        </div>
      </div>

      <!-- Action Buttons Toolbar -->
      <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 border-top pt-3">
        <div>
          <!-- Cancel Button (if allowed) -->
          <button
            v-if="canCancel"
            @click="$emit('cancelOrder', order)"
            class="btn btn-outline-danger btn-sm"
          >
            <i class="bi bi-x-circle me-1"></i> Cancel Order
          </button>
        </div>

        <div class="d-flex flex-wrap gap-2">
          <!-- Pay Now if Unpaid -->
          <button
            v-if="order.paymentStatus === 'UNPAID'"
            @click="$emit('payNow', order)"
            class="btn btn-warning btn-sm fw-bold"
          >
            <i class="bi bi-credit-card me-1"></i> Pay Now
          </button>

          <!-- Delivery Tracking (if Delivery) -->
          <button
            v-if="!isPickup && order.status !== 'CANCELLED'"
            @click="$emit('trackDelivery', order)"
            class="btn btn-outline-primary btn-sm"
          >
            <i class="bi bi-geo-alt me-1"></i> Live Tracker
          </button>

          <!-- Farm Pickup Appointment (if Pickup) -->
          <button
            v-if="isPickup && order.status !== 'CANCELLED'"
            @click="$emit('schedulePickup', order)"
            class="btn btn-outline-success btn-sm"
          >
            <i class="bi bi-calendar-check me-1"></i> Farm Pickup Pass
          </button>

          <!-- Digital Invoice & Receipt -->
          <button
            @click="$emit('viewReceipt', order)"
            class="btn btn-outline-secondary btn-sm"
          >
            <i class="bi bi-receipt me-1"></i> Invoice & Receipt
          </button>

          <!-- Order Details Modal button -->
          <button
            @click="$emit('viewDetails', order)"
            class="btn btn-farmora-outline btn-sm"
          >
            <i class="bi bi-info-circle me-1"></i> Details
          </button>

          <!-- Review button for Buyer when delivered -->
          <button
            v-if="role === 'BUYER' && order.status === 'DELIVERED'"
            @click="$emit('leaveReview', order)"
            class="btn btn-warning btn-sm text-dark fw-semibold"
          >
            <i class="bi bi-star-fill me-1"></i> Review
          </button>

          <!-- Farmer Actions -->
          <template v-if="role === 'FARMER'">
            <button
              v-if="order.status === 'CONFIRMED'"
              @click="$emit('updateStatus', order.id, 'PROCESSING')"
              class="btn btn-info text-dark btn-sm fw-semibold"
            >
              <i class="bi bi-box-seam me-1"></i> Start Packaging
            </button>
            <button
              v-if="order.status === 'PROCESSING'"
              @click="$emit('updateStatus', order.id, 'READY_FOR_PICKUP')"
              class="btn btn-warning btn-sm fw-semibold"
            >
              <i class="bi bi-check2-circle me-1"></i> Mark Ready for Pickup
            </button>
            <button
              v-if="order.status === 'READY_FOR_PICKUP' || order.status === 'OUT_FOR_DELIVERY'"
              @click="$emit('updateStatus', order.id, 'DELIVERED')"
              class="btn btn-success btn-sm fw-semibold"
            >
              <i class="bi bi-check2-all me-1"></i> Mark Delivered
            </button>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.py-1-5 {
  padding-top: 0.35rem;
  padding-bottom: 0.35rem;
}
.btn-farmora-outline {
  border-color: #2d6a4f;
  color: #2d6a4f;
}
.btn-farmora-outline:hover {
  background-color: #2d6a4f;
  color: #fff;
}
</style>

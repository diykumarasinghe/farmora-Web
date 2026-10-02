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

defineEmits(['updateStatus', 'leaveReview'])

const statusClass = computed(() => {
  switch (props.order.status) {
    case 'CONFIRMED':
      return 'badge-confirmed text-primary'
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
</script>

<template>
  <div class="card shadow-sm border-0 mb-3">
    <div class="card-body">
      <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <div>
          <span class="badge px-3 py-1 rounded-pill me-2" :class="statusClass">{{ order.status }}</span>
          <span class="fw-bold">Order #{{ order.id }}</span>
          <span class="text-muted small ms-2">&bull; {{ new Date(order.createdAt).toLocaleDateString() }}</span>
        </div>
        <div class="text-success fw-bold fs-5">
          ${{ Number(order.totalAmount).toFixed(2) }}
        </div>
      </div>

      <div class="row g-3 mb-3">
        <div class="col-md-6">
          <div class="fw-bold fs-6 mb-1">{{ order.product?.name }}</div>
          <div class="text-muted small mb-1">
            <span>{{ order.quantity }} {{ order.product?.unit }} @ ${{ Number(order.unitPrice).toFixed(2) }}</span>
          </div>
          <div class="small text-secondary">
            <div><i class="bi bi-person-fill text-success me-1"></i> Farmer: {{ order.farmer?.fullName }}</div>
            <div><i class="bi bi-person-badge text-primary me-1"></i> Buyer: {{ order.buyer?.fullName }}</div>
          </div>
        </div>

        <div class="col-md-6 border-start-md ps-md-3">
          <div class="small text-muted mb-1">
            <strong><i class="bi bi-geo-alt me-1 text-danger"></i> Pickup:</strong> {{ order.pickupAddress || 'Farm Location' }}
          </div>
          <div class="small text-muted mb-1">
            <strong><i class="bi bi-truck me-1 text-primary"></i> Deliver to:</strong> {{ order.deliveryAddress || 'Standard Delivery' }}
          </div>
        </div>
      </div>

      <!-- Actions based on role and status -->
      <div class="d-flex flex-wrap justify-content-end gap-2 border-top pt-3">
        <!-- Farmer actions -->
        <template v-if="role === 'FARMER'">
          <button
            v-if="order.status === 'CONFIRMED'"
            @click="$emit('updateStatus', order.id, 'READY_FOR_PICKUP')"
            class="btn btn-warning btn-sm fw-semibold"
          >
            <i class="bi bi-box-seam me-1"></i> Mark Ready for Pickup
          </button>
        </template>

        <!-- Buyer actions -->
        <template v-if="role === 'BUYER'">
          <button
            v-if="order.status === 'DELIVERED'"
            @click="$emit('leaveReview', order)"
            class="btn btn-farmora-outline btn-sm"
          >
            <i class="bi bi-star-fill me-1 text-warning"></i> Write Review
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  delivery: {
    type: Object,
    required: true
  },
  isAvailableView: {
    type: Boolean,
    default: false
  }
})

defineEmits(['accept', 'updateStatus'])

const statusBadge = computed(() => {
  switch (props.delivery.status) {
    case 'AVAILABLE':
      return 'bg-success text-white'
    case 'ASSIGNED':
      return 'bg-warning text-dark'
    case 'PICKED_UP':
    case 'IN_TRANSIT':
      return 'bg-info text-dark'
    case 'DELIVERED':
      return 'bg-secondary text-white'
    default:
      return 'bg-light text-dark'
  }
})
</script>

<template>
  <div class="card shadow-sm border-0 mb-3">
    <div class="card-body">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
          <span class="badge px-3 py-1 rounded-pill me-2" :class="statusBadge">{{ delivery.status }}</span>
          <span class="fw-bold">Delivery Run #{{ delivery.id }}</span>
        </div>
        <div class="text-muted small">
          Order #{{ delivery.order?.id }}
        </div>
      </div>

      <div class="row g-3 mb-3">
        <div class="col-md-6">
          <div class="p-2 rounded bg-light mb-2">
            <div class="small fw-bold text-muted"><i class="bi bi-geo-alt-fill text-danger me-1"></i> Pickup Location</div>
            <div class="small text-dark">{{ delivery.pickupAddress }}</div>
          </div>
          <div class="small text-muted">
            Produce: <strong>{{ delivery.order?.product?.name }}</strong> ({{ delivery.order?.quantity }} {{ delivery.order?.product?.unit }})
          </div>
        </div>

        <div class="col-md-6">
          <div class="p-2 rounded bg-light mb-2">
            <div class="small fw-bold text-muted"><i class="bi bi-pin-map-fill text-success me-1"></i> Drop-off Destination</div>
            <div class="small text-dark">{{ delivery.deliveryAddress }}</div>
          </div>
          <div class="small text-muted">
            Recipient: <strong>{{ delivery.order?.buyer?.fullName }}</strong> ({{ delivery.order?.buyer?.phone || 'No phone' }})
          </div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="d-flex justify-content-end gap-2 border-top pt-3">
        <template v-if="isAvailableView">
          <button @click="$emit('accept', delivery.id)" class="btn btn-farmora btn-sm px-4">
            <i class="bi bi-check-circle me-1"></i> Accept This Delivery
          </button>
        </template>

        <template v-else>
          <button
            v-if="delivery.status === 'ASSIGNED'"
            @click="$emit('updateStatus', delivery.id, 'PICKED_UP')"
            class="btn btn-warning btn-sm fw-semibold"
          >
            <i class="bi bi-box-arrow-up me-1"></i> Confirm Pickup
          </button>
          <button
            v-if="delivery.status === 'PICKED_UP'"
            @click="$emit('updateStatus', delivery.id, 'IN_TRANSIT')"
            class="btn btn-info btn-sm text-dark fw-semibold"
          >
            <i class="bi bi-truck me-1"></i> Start Transit
          </button>
          <button
            v-if="delivery.status === 'IN_TRANSIT'"
            @click="$emit('updateStatus', delivery.id, 'DELIVERED')"
            class="btn btn-success btn-sm px-4 fw-semibold"
          >
            <i class="bi bi-check2-all me-1"></i> Confirm Delivered
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

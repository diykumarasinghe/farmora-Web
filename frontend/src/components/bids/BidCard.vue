<script setup>
import { computed } from 'vue'

const props = defineProps({
  bid: {
    type: Object,
    required: true
  },
  isFarmerView: {
    type: Boolean,
    default: false
  }
})

defineEmits(['accept', 'reject', 'cancel'])

const statusClass = computed(() => {
  switch (props.bid.status) {
    case 'ACCEPTED':
      return 'badge-confirmed text-primary'
    case 'REJECTED':
    case 'CANCELLED':
      return 'badge-sold-out text-danger'
    default:
      return 'badge-pending text-warning-emphasis'
  }
})
</script>

<template>
  <div class="card shadow-sm border-0 mb-3">
    <div class="card-body">
      <div class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2 mb-3">
        <div>
          <span class="badge px-3 py-1 rounded-pill me-2" :class="statusClass">{{ bid.status }}</span>
          <span class="text-muted small">Bid #{{ bid.id }} &bull; {{ new Date(bid.createdAt).toLocaleDateString() }}</span>
        </div>
        <div class="fs-5 fw-bold text-success">
          Offer: ${{ Number(bid.amount).toFixed(2) }} / {{ bid.product?.unit }}
        </div>
      </div>

      <div class="row g-3 align-items-center mb-3">
        <div class="col-md-7">
          <h5 class="fw-bold mb-1">{{ bid.product?.name }}</h5>
          <div class="text-muted small mb-2">
            <span v-if="isFarmerView">
              <i class="bi bi-person me-1 text-primary"></i> Buyer: <strong>{{ bid.buyer?.fullName }}</strong> ({{ bid.buyer?.phone || bid.buyer?.email }})
            </span>
            <span v-else>
              <i class="bi bi-shop me-1 text-success"></i> Farmer: <strong>{{ bid.product?.farmer?.fullName }}</strong>
            </span>
          </div>
          <p v-if="bid.note" class="small fst-italic text-secondary mb-0">
            "{{ bid.note }}"
          </p>
        </div>

        <div class="col-md-5 text-md-end">
          <div class="small text-muted">Requested Quantity</div>
          <div class="fw-bold fs-6">{{ bid.quantity }} {{ bid.product?.unit }}</div>
          <div class="small text-muted mt-1">Total Valuation: <strong>${{ Number(bid.totalOffer).toFixed(2) }}</strong></div>
        </div>
      </div>

      <!-- Action Buttons -->
      <div v-if="bid.status === 'PENDING'" class="d-flex justify-content-end gap-2 border-top pt-3">
        <template v-if="isFarmerView">
          <button @click="$emit('reject', bid.id)" class="btn btn-outline-danger btn-sm px-3">
            <i class="bi bi-x-circle me-1"></i> Decline
          </button>
          <button @click="$emit('accept', bid.id)" class="btn btn-farmora btn-sm px-4">
            <i class="bi bi-check2-circle me-1"></i> Accept & Create Order
          </button>
        </template>
        <template v-else>
          <button @click="$emit('cancel', bid.id)" class="btn btn-outline-secondary btn-sm px-3">
            <i class="bi bi-trash me-1"></i> Cancel Bid
          </button>
        </template>
      </div>
    </div>
  </div>
</template>

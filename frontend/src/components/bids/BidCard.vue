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

const emit = defineEmits(['accept', 'reject', 'cancel', 'counter', 'acceptCounter'])

const statusClass = computed(() => {
  switch (props.bid.status) {
    case 'ACCEPTED':
      return 'badge-confirmed text-primary'
    case 'COUNTERED':
      return 'bg-warning text-dark'
    case 'REJECTED':
    case 'CANCELLED':
    case 'WITHDRAWN':
      return 'badge-sold-out text-danger'
    default:
      return 'badge-pending text-warning-emphasis'
  }
})

const isPendingOrCountered = computed(() => {
  return props.bid.status === 'PENDING' || props.bid.status === 'COUNTERED'
})

const isMyTurnToRespond = computed(() => {
  if (props.bid.status !== 'COUNTERED') return false
  if (props.isFarmerView) {
    return props.bid.counterBy === 'BUYER'
  } else {
    return props.bid.counterBy === 'FARMER'
  }
})
</script>

<template>
  <div class="card shadow-sm border-0 mb-3 rounded-3 overflow-hidden">
    <div class="card-body p-4">
      <!-- Top header line -->
      <div class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-2 mb-3">
        <div>
          <span class="badge px-3 py-1 rounded-pill me-2" :class="statusClass">
            {{ bid.status === 'COUNTERED' ? 'COUNTER-OFFER PENDING' : bid.status }}
          </span>
          <span class="text-muted small">Bid #{{ bid.id }} &bull; {{ new Date(bid.createdAt).toLocaleDateString() }}</span>
        </div>
        <div class="fs-5 fw-bold text-success">
          Offer: LKR {{ Number(bid.amount).toFixed(2) }} / {{ bid.product?.unit || 'kg' }}
        </div>
      </div>

      <!-- Main produce info -->
      <div class="row g-3 align-items-center mb-3">
        <div class="col-md-7">
          <h5 class="fw-bold mb-1 text-dark">{{ bid.product?.name }}</h5>
          <div class="text-muted small mb-2">
            <span v-if="isFarmerView">
              <i class="bi bi-person me-1 text-primary"></i> Buyer: <strong>{{ bid.buyer?.fullName }}</strong> ({{ bid.buyer?.phone || bid.buyer?.email }})
            </span>
            <span v-else>
              <i class="bi bi-shop me-1 text-success"></i> Farmer: <strong>{{ bid.product?.farmer?.fullName }}</strong>
            </span>
          </div>
          <p v-if="bid.note" class="small fst-italic text-secondary mb-0 bg-light p-2 rounded-2">
            "{{ bid.note }}"
          </p>
        </div>

        <div class="col-md-5 text-md-end">
          <div class="small text-muted">Requested Quantity</div>
          <div class="fw-bold fs-6">{{ bid.quantity }} {{ bid.product?.unit || 'kg' }}</div>
          <div class="small text-muted mt-1">Total Valuation: <strong>LKR {{ Number(bid.totalOffer).toLocaleString() }}</strong></div>
        </div>
      </div>

      <!-- Counter-Offer Banner if COUNTERED -->
      <div v-if="bid.status === 'COUNTERED'" class="p-3 mb-3 rounded-3 border border-warning bg-warning-subtle">
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div>
            <span class="badge bg-warning text-dark fw-bold me-2">Counter Proposal</span>
            <span class="small text-dark">
              Proposed by <strong>{{ bid.counterBy }}</strong>:
              <strong class="text-success fs-6">LKR {{ Number(bid.counterAmount).toFixed(2) }}</strong> / {{ bid.product?.unit || 'kg' }}
            </span>
          </div>
          <div class="small fw-semibold text-dark">
            Revised Total: LKR {{ (Number(bid.counterAmount) * Number(bid.quantity)).toLocaleString() }}
          </div>
        </div>
        <div v-if="bid.counterNote" class="small text-muted mt-2 fst-italic">
          Note: "{{ bid.counterNote }}"
        </div>
      </div>

      <!-- Action Buttons -->
      <div v-if="isPendingOrCountered" class="d-flex flex-wrap justify-content-end gap-2 border-top pt-3">
        <!-- Farmer Actions -->
        <template v-if="isFarmerView">
          <button @click="$emit('reject', bid.id)" class="btn btn-outline-danger btn-sm px-3">
            <i class="bi bi-x-circle me-1"></i> Decline
          </button>

          <!-- Counter-Offer button -->
          <button @click="$emit('counter', bid)" class="btn btn-outline-warning text-dark btn-sm px-3 fw-semibold">
            <i class="bi bi-arrow-left-right me-1"></i> Counter-Offer
          </button>

          <!-- Accept Counter if buyer countered -->
          <button
            v-if="bid.status === 'COUNTERED' && bid.counterBy === 'BUYER'"
            @click="$emit('acceptCounter', bid.id)"
            class="btn btn-farmora btn-sm px-4 fw-bold"
          >
            <i class="bi bi-check2-circle me-1"></i> Accept Counter & Confirm Order
          </button>

          <!-- Accept Original if PENDING -->
          <button
            v-if="bid.status === 'PENDING'"
            @click="$emit('accept', bid.id)"
            class="btn btn-farmora btn-sm px-4 fw-bold"
          >
            <i class="bi bi-check2-circle me-1"></i> Accept & Create Order
          </button>
        </template>

        <!-- Buyer Actions -->
        <template v-else>
          <!-- Accept Counter if farmer countered -->
          <button
            v-if="bid.status === 'COUNTERED' && bid.counterBy === 'FARMER'"
            @click="$emit('acceptCounter', bid.id)"
            class="btn btn-farmora btn-sm px-3 fw-bold"
          >
            <i class="bi bi-check2-all me-1"></i> Accept Counter-Offer & Place Order
          </button>

          <!-- Counter back -->
          <button @click="$emit('counter', bid)" class="btn btn-outline-warning text-dark btn-sm px-3 fw-semibold">
            <i class="bi bi-arrow-left-right me-1"></i> Send Counter-Offer
          </button>

          <!-- Withdraw / Cancel -->
          <button @click="$emit('cancel', bid.id)" class="btn btn-outline-secondary btn-sm px-3">
            <i class="bi bi-x-circle me-1"></i> Withdraw Bid
          </button>
        </template>
      </div>

      <!-- If accepted, notice -->
      <div v-else-if="bid.status === 'ACCEPTED'" class="border-top pt-3 d-flex justify-content-between align-items-center">
        <span class="small text-success fw-semibold">
          <i class="bi bi-check-circle-fill me-1"></i> Deal Finalized! Active Order in Progress.
        </span>
        <router-link :to="isFarmerView ? '/farmer/orders' : '/buyer/orders'" class="btn btn-sm btn-outline-success">
          View Linked Order &rarr;
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.btn-farmora {
  background-color: #2d6a4f;
  color: #fff;
}
.btn-farmora:hover {
  background-color: #1b4332;
  color: #fff;
}
</style>

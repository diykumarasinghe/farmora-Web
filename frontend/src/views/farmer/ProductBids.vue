<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import BidCard from '../../components/bids/BidCard.vue'
import CounterOfferModal from '../../components/bids/CounterOfferModal.vue'
import { bidService } from '../../services/bidService'

const bids = ref([])
const loading = ref(true)
const actionMessage = ref('')

const showCounterModal = ref(false)
const selectedBid = ref(null)

const loadBids = async () => {
  loading.value = true
  try {
    bids.value = await bidService.getFarmerReceivedBids()
  } catch (err) {
    console.error('Failed to load bids', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadBids()
})

const handleOpenCounter = (bid) => {
  selectedBid.value = bid
  showCounterModal.value = true
}

const handleAccept = async (id) => {
  try {
    await bidService.acceptBid(id)
    actionMessage.value = 'Bid accepted! Order has been created and confirmed.'
    loadBids()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to accept bid.')
  }
}

const handleAcceptCounter = async (id) => {
  try {
    const order = await bidService.acceptCounterOffer(id)
    actionMessage.value = 'Buyer counter-offer accepted! Order #' + (order.orderNumber || order.id) + ' created.'
    loadBids()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to accept counter-offer.')
  }
}

const handleReject = async (id) => {
  try {
    await bidService.rejectBid(id)
    actionMessage.value = 'Bid offer declined.'
    loadBids()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to decline bid.')
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="bids" />
      </div>

      <div class="col-lg-9">
        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
          <div>
            <h4 class="fw-bold mb-1">
              <i class="bi bi-tag-fill text-success me-2"></i>Incoming Buyer Bids & Negotiations
            </h4>
            <p class="text-muted small mb-0">Review price offers from buyers, propose counter-offers, or accept the best rate to create an order.</p>
          </div>
        </div>

        <div v-if="actionMessage" class="alert alert-success alert-dismissible fade show small mb-3" role="alert">
          <i class="bi bi-check-circle-fill me-1"></i> {{ actionMessage }}
          <button type="button" class="btn-close" @click="actionMessage = ''"></button>
        </div>

        <LoadingSpinner v-if="loading" message="Loading received bids and negotiation offers..." />

        <div v-else-if="bids.length > 0">
          <BidCard
            v-for="bid in bids"
            :key="bid.id"
            :bid="bid"
            :isFarmerView="true"
            @accept="handleAccept"
            @counter="handleOpenCounter"
            @acceptCounter="handleAcceptCounter"
            @reject="handleReject"
          />
        </div>

        <EmptyState
          v-else
          icon="bi-tag"
          title="No Bids Received Yet"
          description="When prospective buyers submit pricing offers for your crops, they will appear here for your review and negotiation."
        />
      </div>
    </div>

    <!-- Counter Offer Modal -->
    <CounterOfferModal
      :show="showCounterModal"
      :bid="selectedBid"
      :isFarmer="true"
      @close="showCounterModal = false"
      @counterSent="loadBids"
    />
  </div>
</template>

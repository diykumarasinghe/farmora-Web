<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import BidCard from '../../components/bids/BidCard.vue'
import { bidService } from '../../services/bidService'

const bids = ref([])
const loading = ref(true)
const actionMessage = ref('')

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

const handleAccept = async (id) => {
  try {
    await bidService.acceptBid(id)
    actionMessage.value = 'Bid accepted! Order has been created and confirmed.'
    loadBids()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to accept bid.')
  }
}

const handleReject = async (id) => {
  try {
    await bidService.rejectBid(id)
    actionMessage.value = 'Bid declined.'
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
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="fw-bold mb-1">Incoming Buyer Bids</h4>
            <p class="text-muted small mb-0">Review price offers from buyers. Accepting a bid immediately generates an active order.</p>
          </div>
        </div>

        <div v-if="actionMessage" class="alert alert-success alert-dismissible fade show small" role="alert">
          {{ actionMessage }}
          <button type="button" class="btn-close" @click="actionMessage = ''"></button>
        </div>

        <LoadingSpinner v-if="loading" message="Loading received bids..." />

        <div v-else-if="bids.length > 0">
          <BidCard
            v-for="bid in bids"
            :key="bid.id"
            :bid="bid"
            :isFarmerView="true"
            @accept="handleAccept"
            @reject="handleReject"
          />
        </div>

        <EmptyState
          v-else
          icon="bi-tag"
          title="No Bids Received Yet"
          description="When prospective buyers submit pricing offers for your crops, they will appear here for your review."
        />
      </div>
    </div>
  </div>
</template>

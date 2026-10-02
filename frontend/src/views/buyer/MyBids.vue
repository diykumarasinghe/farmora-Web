<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import BidCard from '../../components/bids/BidCard.vue'
import { bidService } from '../../services/bidService'

const bids = ref([])
const loading = ref(true)

const loadMyBids = async () => {
  loading.value = true
  try {
    bids.value = await bidService.getMyBids()
  } catch (err) {
    console.error('Failed to load my bids', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMyBids()
})

const handleCancelBid = async (id) => {
  if (confirm('Are you sure you want to cancel this bid offer?')) {
    try {
      await bidService.cancelBid(id)
      loadMyBids()
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel bid.')
    }
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
            <h4 class="fw-bold mb-1">My Submitted Bids</h4>
            <p class="text-muted small mb-0">Track all offers you have placed on fresh agricultural listings.</p>
          </div>
          <router-link to="/products" class="btn btn-farmora btn-sm">
            <i class="bi bi-search me-1"></i> Place New Bid
          </router-link>
        </div>

        <LoadingSpinner v-if="loading" message="Loading your bids..." />

        <div v-else-if="bids.length > 0">
          <BidCard
            v-for="bid in bids"
            :key="bid.id"
            :bid="bid"
            :isFarmerView="false"
            @cancel="handleCancelBid"
          />
        </div>

        <EmptyState
          v-else
          icon="bi-tag"
          title="No Bids Placed Yet"
          description="You haven't submitted any offers on produce yet. Explore the marketplace to find fresh crops!"
          actionText="Browse Marketplace"
          actionRoute="/products"
        />
      </div>
    </div>
  </div>
</template>

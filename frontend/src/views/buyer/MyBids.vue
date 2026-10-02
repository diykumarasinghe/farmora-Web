<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import BidCard from '../../components/bids/BidCard.vue'
import CounterOfferModal from '../../components/bids/CounterOfferModal.vue'
import { bidService } from '../../services/bidService'

const router = useRouter()

const bids = ref([])
const loading = ref(true)
const actionMessage = ref('')

const showCounterModal = ref(false)
const selectedBid = ref(null)

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

const handleOpenCounter = (bid) => {
  selectedBid.value = bid
  showCounterModal.value = true
}

const handleAcceptCounter = async (id) => {
  try {
    const order = await bidService.acceptCounterOffer(id)
    actionMessage.value = 'Counter-offer accepted! Order #' + (order.orderNumber || order.id) + ' has been created.'
    loadMyBids()
    setTimeout(() => {
      router.push('/buyer/orders')
    }, 1500)
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to accept counter-offer.')
  }
}

const handleCancelBid = async (id) => {
  if (confirm('Are you sure you want to withdraw this bid?')) {
    try {
      await bidService.withdrawBid(id)
      actionMessage.value = 'Bid offer withdrawn.'
      loadMyBids()
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to withdraw bid.')
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
        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
          <div>
            <h4 class="fw-bold mb-1">
              <i class="bi bi-tag-fill text-success me-2"></i>My Bids & Price Negotiations
            </h4>
            <p class="text-muted small mb-0">Track all offers you have placed on fresh crops, respond to farmer counter-offers, and finalize purchases.</p>
          </div>
          <router-link to="/products" class="btn btn-farmora btn-sm fw-bold">
            <i class="bi bi-search me-1"></i> Place New Bid
          </router-link>
        </div>

        <div v-if="actionMessage" class="alert alert-success alert-dismissible fade show small mb-3" role="alert">
          <i class="bi bi-check-circle-fill me-1"></i> {{ actionMessage }}
          <button type="button" class="btn-close" @click="actionMessage = ''"></button>
        </div>

        <LoadingSpinner v-if="loading" message="Loading your bids and counter-offers..." />

        <div v-else-if="bids.length > 0">
          <BidCard
            v-for="bid in bids"
            :key="bid.id"
            :bid="bid"
            :isFarmerView="false"
            @counter="handleOpenCounter"
            @acceptCounter="handleAcceptCounter"
            @cancel="handleCancelBid"
          />
        </div>

        <EmptyState
          v-else
          icon="bi-tag"
          title="No Bids Placed Yet"
          description="You haven't submitted any offers on produce yet. Explore the marketplace to find fresh crops and start negotiating!"
          actionText="Browse Marketplace"
          actionRoute="/products"
        />
      </div>
    </div>

    <!-- Counter Offer Modal -->
    <CounterOfferModal
      :show="showCounterModal"
      :bid="selectedBid"
      :isFarmer="false"
      @close="showCounterModal = false"
      @counterSent="loadMyBids"
    />
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

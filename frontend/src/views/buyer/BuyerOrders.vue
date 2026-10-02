<script setup>
import { onMounted, ref, computed } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import OrderCard from '../../components/orders/OrderCard.vue'
import { orderService } from '../../services/orderService'
import { reviewService } from '../../services/reviewService'

const orders = ref([])
const loading = ref(true)
const selectedStatus = ref('ALL')
const searchQuery = ref('')

// Review Modal State
const showReviewModal = ref(false)
const selectedOrder = ref(null)
const reviewRating = ref(5)
const reviewComment = ref('')
const submittingReview = ref(false)
const reviewSuccess = ref('')
const reviewError = ref('')

const loadOrders = async () => {
  loading.value = true
  try {
    orders.value = await orderService.getMyOrders()
  } catch (err) {
    console.error('Failed to load buyer orders', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOrders()
})

// Metrics calculations
const totalSpent = computed(() => {
  return orders.value
    .filter(o => o.status !== 'CANCELLED')
    .reduce((sum, o) => sum + (o.totalAmount || 0), 0)
})

const deliveredOrdersCount = computed(() => {
  return orders.value.filter(o => o.status === 'DELIVERED').length
})

const filteredOrders = computed(() => {
  return orders.value.filter(o => {
    const matchesStatus = selectedStatus.value === 'ALL' || o.status === selectedStatus.value
    const query = searchQuery.value.toLowerCase().trim()
    const matchesSearch = !query ||
      o.id.toString().includes(query) ||
      o.product?.name?.toLowerCase().includes(query) ||
      o.farmer?.fullName?.toLowerCase().includes(query)
    return matchesStatus && matchesSearch
  })
})

const openReviewModal = (order) => {
  selectedOrder.value = order
  reviewRating.value = 5
  reviewComment.value = ''
  reviewSuccess.value = ''
  reviewError.value = ''
  showReviewModal.value = true
}

const submitReview = async () => {
  submittingReview.value = true
  reviewError.value = ''
  try {
    await reviewService.createReview({
      orderId: selectedOrder.value.id,
      rating: reviewRating.value,
      comment: reviewComment.value
    })
    reviewSuccess.value = 'Thank you! Your review has been recorded.'
    setTimeout(() => {
      showReviewModal.value = false
    }, 1500)
  } catch (err) {
    reviewError.value = err.response?.data?.message || 'Failed to submit review.'
  } finally {
    submittingReview.value = false
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="orders" />
      </div>

      <div class="col-lg-9">
        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
          <div>
            <h4 class="fw-bold mb-1"><i class="bi bi-bag-check-fill text-success me-2"></i>My Purchases & Orders</h4>
            <p class="text-muted small mb-0">Track order progress from farm preparation to final delivery.</p>
          </div>
          <router-link to="/products" class="btn btn-farmora btn-sm">
            <i class="bi bi-search me-1"></i> Buy Produce
          </router-link>
        </div>

        <!-- Metrics KPI Cards -->
        <div class="row g-3 mb-4">
          <div class="col-md-4">
            <div class="card border-0 shadow-sm p-3 bg-white rounded-3">
              <div class="small text-muted mb-1">Total Purchases Placed</div>
              <h4 class="fw-bold text-dark mb-0">{{ orders.length }} Orders</h4>
            </div>
          </div>
          <div class="col-md-4">
            <div class="card border-0 shadow-sm p-3 bg-white rounded-3">
              <div class="small text-muted mb-1">Successfully Delivered</div>
              <h4 class="fw-bold text-success mb-0">{{ deliveredOrdersCount }} Delivered</h4>
            </div>
          </div>
          <div class="col-md-4">
            <div class="card border-0 shadow-sm p-3 bg-white rounded-3">
              <div class="small text-muted mb-1">Total Spent</div>
              <h4 class="fw-bold text-primary mb-0">LKR {{ totalSpent.toLocaleString() }}</h4>
            </div>
          </div>
        </div>

        <!-- Filters & Search Toolbar -->
        <div class="card border-0 shadow-sm p-3 mb-4 bg-white rounded-3">
          <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
            <div class="d-flex gap-1 flex-wrap">
              <button
                class="btn btn-sm"
                :class="selectedStatus === 'ALL' ? 'btn-success' : 'btn-outline-secondary'"
                @click="selectedStatus = 'ALL'"
              >
                All ({{ orders.length }})
              </button>
              <button
                class="btn btn-sm"
                :class="selectedStatus === 'DELIVERED' ? 'btn-success' : 'btn-outline-secondary'"
                @click="selectedStatus = 'DELIVERED'"
              >
                Delivered
              </button>
              <button
                class="btn btn-sm"
                :class="selectedStatus === 'IN_TRANSIT' ? 'btn-success' : 'btn-outline-secondary'"
                @click="selectedStatus = 'IN_TRANSIT'"
              >
                In Transit
              </button>
              <button
                class="btn btn-sm"
                :class="selectedStatus === 'CONFIRMED' ? 'btn-success' : 'btn-outline-secondary'"
                @click="selectedStatus = 'CONFIRMED'"
              >
                Confirmed
              </button>
              <button
                class="btn btn-sm"
                :class="selectedStatus === 'PENDING' ? 'btn-success' : 'btn-outline-secondary'"
                @click="selectedStatus = 'PENDING'"
              >
                Pending
              </button>
            </div>

            <div class="input-group input-group-sm" style="max-width: 260px;">
              <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
              <input
                type="text"
                class="form-control"
                placeholder="Search produce or order..."
                v-model="searchQuery"
              />
            </div>
          </div>
        </div>

        <LoadingSpinner v-if="loading" message="Loading your purchase orders..." />

        <div v-else-if="filteredOrders.length > 0">
          <OrderCard
            v-for="order in filteredOrders"
            :key="order.id"
            :order="order"
            role="BUYER"
            @leaveReview="openReviewModal"
          />
        </div>

        <EmptyState
          v-else
          icon="bi-bag-x"
          title="No Orders Match Your Filter"
          description="Try changing the status filter or search query, or discover fresh farm produce."
          actionText="Find Farm Produce"
          actionRoute="/products"
        />
      </div>
    </div>

    <!-- Review Modal -->
    <div
      v-if="showReviewModal"
      class="modal fade show d-block"
      tabindex="-1"
      style="background-color: rgba(0, 0, 0, 0.5);"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header border-bottom">
            <h5 class="modal-title fw-bold"><i class="bi bi-star-fill text-warning me-2"></i>Review & Rate Farmer</h5>
            <button type="button" class="btn-close" @click="showReviewModal = false"></button>
          </div>
          <div class="modal-body">
            <div v-if="reviewSuccess" class="alert alert-success small">{{ reviewSuccess }}</div>
            <div v-if="reviewError" class="alert alert-danger small">{{ reviewError }}</div>

            <p class="small text-muted mb-3">
              Share feedback on produce freshness, packaging, and fulfillment with farmer
              <strong>{{ selectedOrder?.farmer?.fullName }}</strong> for <strong>{{ selectedOrder?.product?.name }}</strong>.
            </p>

            <div class="mb-3">
              <label class="form-label small fw-semibold">Quality & Fulfillment Rating</label>
              <div class="d-flex gap-2">
                <button
                  v-for="star in 5"
                  :key="star"
                  type="button"
                  class="btn btn-sm"
                  :class="star <= reviewRating ? 'btn-warning text-white' : 'btn-outline-secondary'"
                  @click="reviewRating = star"
                >
                  <i class="bi bi-star-fill"></i> {{ star }}
                </button>
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label small fw-semibold">Review Comments</label>
              <textarea
                class="form-control"
                rows="3"
                v-model="reviewComment"
                placeholder="How was the produce quality, timely dispatch, and taste?..."
              ></textarea>
            </div>
          </div>
          <div class="modal-footer border-top-0">
            <button type="button" class="btn btn-light btn-sm" @click="showReviewModal = false">Cancel</button>
            <button
              type="button"
              class="btn btn-farmora btn-sm"
              @click="submitReview"
              :disabled="submittingReview"
            >
              <span v-if="submittingReview" class="spinner-border spinner-border-sm me-1"></span>
              Submit Review
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

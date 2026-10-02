<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productService } from '../../services/productService'
import { bidService } from '../../services/bidService'
import { useAuthStore } from '../../stores/auth'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import DirectBuyModal from '../../components/orders/DirectBuyModal.vue'
import BulkOrderModal from '../../components/orders/BulkOrderModal.vue'
import { reviewService } from '../../services/reviewService'
import { analyticsService } from '../../services/analyticsService'
import { SAMPLE_PRODUCTS } from '../../utils/agriculturalData'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const product = ref(null)
const loading = ref(true)
const submittingBid = ref(false)
const bidSuccess = ref(false)
const errorMessage = ref('')
const selectedTab = ref('overview')

const showDirectBuyModal = ref(false)
const showBulkOrderModal = ref(false)

const reviews = ref([])
const reviewModalShow = ref(false)
const editingReviewId = ref(null)
const newReview = ref({ rating: 5, comment: '' })
const isSaved = ref(false)

const bidForm = ref({
  amount: '',
  quantity: 1,
  note: '',
  isWholesaleOrder: false
})

const defaultImage = 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=800&q=80'

onMounted(async () => {
  const id = route.params.id
  try {
    const data = await productService.getProductById(id)
    product.value = data
  } catch (err) {
    console.warn('Product not found in backend, searching sample catalog', err)
    const sample = SAMPLE_PRODUCTS.find(p => p.id === Number(id))
    if (sample) {
      product.value = sample
    } else {
      errorMessage.value = 'Produce listing could not be found.'
    }
  } finally {
    if (product.value) {
      bidForm.value.amount = product.value.price
      bidForm.value.quantity = 1
    }
    loading.value = false
  }

  // Load reviews
  try {
    reviews.value = await reviewService.getProductReviews(id)
  } catch (e) {
    reviews.value = []
  }

  // Check saved state
  if (authStore.isAuthenticated && authStore.role === 'BUYER') {
    try {
      const savedList = await analyticsService.getSavedProducts()
      isSaved.value = savedList.some(s => s.id === Number(id))
    } catch (e) {}
  }
})

const avgReviewScore = computed(() => {
  if (!reviews.value || reviews.value.length === 0) return 4.8
  const sum = reviews.value.reduce((acc, r) => acc + (r.rating || 5), 0)
  return (sum / reviews.value.length).toFixed(1)
})

const handleToggleSave = async () => {
  if (!authStore.isAuthenticated) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    if (isSaved.value) {
      await analyticsService.removeSavedProduct(product.value.id)
      isSaved.value = false
    } else {
      await analyticsService.saveProduct(product.value.id)
      isSaved.value = true
    }
  } catch (e) {
    console.error('Save toggle error', e)
  }
}

const openReviewModal = (existing = null) => {
  if (!authStore.isAuthenticated) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (existing) {
    editingReviewId.value = existing.id
    newReview.value = { rating: existing.rating, comment: existing.comment }
  } else {
    editingReviewId.value = null
    newReview.value = { rating: 5, comment: '' }
  }
  reviewModalShow.value = true
}

const handleSubmitReview = async () => {
  if (!newReview.value.comment.trim()) return
  try {
    if (editingReviewId.value) {
      const updated = await reviewService.updateReview(editingReviewId.value, {
        rating: newReview.value.rating,
        comment: newReview.value.comment
      })
      const idx = reviews.value.findIndex(r => r.id === editingReviewId.value)
      if (idx !== -1) reviews.value[idx] = updated
    } else {
      const created = await reviewService.createReview({
        productId: product.value.id,
        rating: newReview.value.rating,
        comment: newReview.value.comment
      })
      reviews.value.unshift(created)
    }
    reviewModalShow.value = false
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to submit review.')
  }
}

const handleDeleteReview = async (revId) => {
  if (!confirm('Are you sure you want to delete your review?')) return
  try {
    await reviewService.deleteReview(revId)
    reviews.value = reviews.value.filter(r => r.id !== revId)
  } catch (e) {
    console.error('Failed to delete review', e)
  }
}

const isFreshToday = computed(() => {
  if (!product.value) return false
  if (product.value.isFreshToday) return true
  const todayStr = new Date().toISOString().split('T')[0]
  return product.value.harvestDate === todayStr
})

const wholesaleSavings = computed(() => {
  if (!product.value?.wholesalePrice || !product.value?.price) return 0
  const retail = Number(product.value.price)
  const wholesale = Number(product.value.wholesalePrice)
  return Math.round(((retail - wholesale) / retail) * 100)
})

const totalValuation = computed(() => {
  const amt = parseFloat(bidForm.value.amount) || 0
  const qty = parseFloat(bidForm.value.quantity) || 0
  return (amt * qty).toFixed(2)
})

const applyWholesaleRate = () => {
  if (product.value?.wholesalePrice) {
    bidForm.value.amount = product.value.wholesalePrice
    bidForm.value.isWholesaleOrder = true
    if (bidForm.value.quantity < 20) {
      bidForm.value.quantity = 25
    }
  }
}

const handlePlaceBid = async () => {
  if (!authStore.isAuthenticated) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  if (authStore.role !== 'BUYER') {
    errorMessage.value = 'Only registered BUYERS can submit bids or place orders. Sign in with a Buyer account.'
    return
  }

  errorMessage.value = ''
  submittingBid.value = true
  try {
    await bidService.createBid({
      productId: product.value.id,
      amount: bidForm.value.amount,
      quantity: bidForm.value.quantity,
      note: bidForm.value.note
    })
    bidSuccess.value = true
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Failed to submit bid. Please check your quantity and amount.'
  } finally {
    submittingBid.value = false
  }
}
</script>

<template>
  <div class="product-details-view bg-light min-vh-100 py-4">
    <div class="container">
      <!-- Breadcrumb -->
      <nav aria-label="breadcrumb" class="mb-3">
        <ol class="breadcrumb small">
          <li class="breadcrumb-item"><router-link to="/" class="text-decoration-none text-muted">Home</router-link></li>
          <li class="breadcrumb-item"><router-link to="/products" class="text-decoration-none text-muted">Marketplace</router-link></li>
          <li class="breadcrumb-item active text-dark fw-semibold" aria-current="page">{{ product?.name || 'Produce Details' }}</li>
        </ol>
      </nav>

      <LoadingSpinner v-if="loading" message="Loading produce information and pricing trends..." />

      <div v-else-if="product" class="row g-4">
        <!-- Main Product Card & Media -->
        <div class="col-lg-7">
          <div class="card border-0 shadow-sm rounded-4 overflow-hidden mb-4 bg-white">
            <div class="position-relative">
              <img
                :src="product.imageUrl || defaultImage"
                :alt="product.name"
                class="img-fluid w-100 object-fit-cover"
                style="max-height: 440px;"
                @error="$event.target.src = defaultImage"
              />

              <!-- Fresh Today Ribbon -->
              <span
                v-if="isFreshToday"
                class="position-absolute top-0 start-0 m-3 badge bg-success px-3 py-2 rounded-pill shadow-sm fs-6 d-flex align-items-center gap-1"
              >
                <i class="bi bi-patch-check-fill text-white"></i>
                <span>Fresh Today</span>
              </span>

              <!-- Bookmark / Save Product Button -->
              <button
                class="position-absolute top-0 end-0 m-3 btn rounded-circle p-2 shadow-sm d-flex align-items-center justify-content-center"
                :class="isSaved ? 'btn-danger' : 'btn-light text-muted'"
                @click="handleToggleSave"
                title="Save Produce"
                style="z-index: 10; width: 38px; height: 38px;"
              >
                <i class="bi" :class="isSaved ? 'bi-heart-fill' : 'bi-heart'"></i>
              </button>
            </div>

            <!-- Details Body -->
            <div class="card-body p-4">
              <!-- Category & Farming Method -->
              <div class="d-flex align-items-center gap-2 mb-2 flex-wrap">
                <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-1-5 rounded-pill fw-semibold">
                  {{ product.category }}
                </span>
                <span class="badge bg-light text-dark border px-3 py-1-5 rounded-pill">
                  🌱 {{ product.farmingMethod || 'Organic Cultivation' }}
                </span>
                <span v-if="product.rating" class="badge bg-warning-subtle text-dark border px-2 py-1-5 rounded-pill fw-bold">
                  ★ {{ product.rating }} ({{ product.reviewsCount || 10 }}+ reviews)
                </span>
              </div>

              <!-- Title -->
              <h2 class="fw-bold mb-2 text-dark">{{ product.name }}</h2>

              <!-- Dynamic Pricing Bar -->
              <div class="p-3 rounded-3 bg-light-green border border-success-subtle mb-4 d-flex flex-wrap justify-content-between align-items-center">
                <div>
                  <div class="small text-muted mb-1">Daily Retail Price:</div>
                  <div class="fs-3 fw-bold text-farmora-green">
                    Rs. {{ Number(product.price).toLocaleString() }}
                    <span class="fs-6 text-muted fw-normal">/ {{ product.unit || 'kg' }}</span>
                  </div>
                </div>

                <div v-if="product.wholesalePrice" class="text-md-end mt-2 mt-md-0">
                  <div class="small text-muted mb-1">
                    Wholesale Price (Bulk Tier):
                    <span class="badge bg-danger text-white ms-1">Save {{ wholesaleSavings }}%</span>
                  </div>
                  <div class="fs-4 fw-bold text-dark">
                    Rs. {{ Number(product.wholesalePrice).toLocaleString() }}
                    <span class="fs-6 text-muted fw-normal">/ {{ product.unit || 'kg' }} (25kg+)</span>
                  </div>
                </div>
              </div>

              <!-- Specifications Grid -->
              <div class="row g-3 p-3 rounded-3 bg-light text-muted small mb-4">
                <div class="col-sm-6">
                  <strong><i class="bi bi-geo-alt-fill text-danger me-1"></i> Origin:</strong>
                  {{ product.location || product.district || 'Local Farm' }}
                </div>
                <div class="col-sm-6">
                  <strong><i class="bi bi-stack text-primary me-1"></i> Available Stock:</strong>
                  {{ product.quantity }} {{ product.unit || 'kg' }}
                </div>
                <div class="col-sm-6">
                  <strong><i class="bi bi-calendar-event text-warning me-1"></i> Harvest Date:</strong>
                  {{ product.harvestDate || 'Harvested Fresh' }}
                </div>
                <div v-if="product.qualityGrade" class="col-sm-6">
                  <strong><i class="bi bi-award text-warning me-1"></i> Quality Grade:</strong>
                  {{ product.qualityGrade }}
                </div>
                <div v-if="product.packagingInfo" class="col-sm-6">
                  <strong><i class="bi bi-box-seam text-secondary me-1"></i> Packaging:</strong>
                  {{ product.packagingInfo }}
                </div>
                <div v-if="product.minOrderQuantity" class="col-sm-6">
                  <strong><i class="bi bi-cart-check text-info me-1"></i> Min Order (MOQ):</strong>
                  {{ product.minOrderQuantity }} {{ product.unit || 'kg' }}
                </div>
                <div v-if="product.farmingMethod" class="col-sm-6">
                  <strong><i class="bi bi-flower1 text-success me-1"></i> Method:</strong>
                  {{ product.farmingMethod }}
                </div>
                <div class="col-sm-6">
                  <strong><i class="bi bi-person-check text-success me-1"></i> Farmer / Producer:</strong>
                  <router-link
                    v-if="product.farmer?.id"
                    :to="`/farms/${product.farmer.id}`"
                    class="text-success fw-bold text-decoration-none ms-1"
                  >
                    {{ product.farmer?.fullName || 'Verified Farmer' }} &rarr;
                  </router-link>
                  <span v-else class="fw-semibold text-dark">{{ product.farmer?.fullName || 'Sunrise Farm' }}</span>
                </div>
              </div>

              <!-- Description -->
              <div class="mb-4">
                <h6 class="fw-bold text-dark mb-2">Produce Description & Quality Notes</h6>
                <p class="text-secondary leading-relaxed mb-0">
                  {{ product.description || 'Pristine Sri Lankan harvest cultivated with care according to sustainable agricultural practices.' }}
                </p>
              </div>

              <!-- Video Embed if available -->
              <div v-if="product.videoUrl" class="mb-4">
                <h6 class="fw-bold text-dark mb-2"><i class="bi bi-play-circle-fill text-danger me-1"></i> Harvest Video</h6>
                <div class="ratio ratio-16x9 rounded-3 overflow-hidden shadow-sm">
                  <iframe :src="product.videoUrl" allowfullscreen></iframe>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Bidding / Buying Offer Box -->
        <div class="col-lg-5">
          <div class="card border-0 shadow-sm rounded-4 p-4 sticky-top bg-white" style="top: 85px;">
            <h4 class="fw-bold mb-2 text-dark d-flex align-items-center gap-2">
              <i class="bi bi-tag-fill text-farmora-green"></i>
              <span>Purchase or Bid</span>
            </h4>
            <p class="small text-muted mb-3">
              Place a direct purchase request, order wholesale in bulk, or submit a competitive bid.
            </p>

            <!-- Direct Buy & Bulk Order Primary Buttons -->
            <div class="d-flex gap-2 mb-3">
              <button
                type="button"
                class="btn btn-farmora flex-fill fw-bold py-2 shadow-sm"
                @click="showDirectBuyModal = true"
                :disabled="product.status !== 'AVAILABLE'"
              >
                <i class="bi bi-lightning-charge-fill me-1"></i> Buy Now (Direct)
              </button>
              <button
                type="button"
                class="btn btn-outline-success fw-bold py-2 px-3"
                @click="showBulkOrderModal = true"
                title="Wholesale B2B Bulk Order"
              >
                <i class="bi bi-box-seam me-1"></i> Bulk Order
              </button>
            </div>

            <div class="position-relative text-center my-3">
              <hr class="text-muted opacity-25">
              <span class="position-absolute top-50 start-50 translate-middle bg-white px-3 small text-muted fw-semibold">
                OR NEGOTIATE / BID
              </span>
            </div>

            <!-- Success Alert -->
            <div v-if="bidSuccess" class="alert alert-success rounded-3 p-3" role="alert">
              <h6 class="fw-bold mb-1"><i class="bi bi-check-circle-fill me-1"></i> Offer Submitted Successfully!</h6>
              <p class="small mb-3">Your purchase offer has been dispatched. You can track all responses in My Bids.</p>
              <router-link to="/buyer/my-bids" class="btn btn-sm btn-success fw-semibold">
                Go to My Placed Bids &rarr;
              </router-link>
            </div>

            <!-- Error Alert -->
            <div v-if="errorMessage" class="alert alert-danger py-2 small rounded-3" role="alert">
              <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ errorMessage }}
            </div>

            <!-- Form -->
            <form v-if="!bidSuccess" @submit.prevent="handlePlaceBid">
              <!-- Wholesale quick discount banner -->
              <div
                v-if="product.wholesalePrice"
                class="p-2-5 rounded-3 mb-3 bg-light border d-flex justify-content-between align-items-center"
              >
                <div class="small">
                  <span class="fw-bold text-success">Wholesale rate available:</span>
                  <span class="text-muted ms-1">Rs. {{ product.wholesalePrice }}/{{ product.unit }}</span>
                </div>
                <button
                  type="button"
                  class="btn btn-sm btn-outline-success py-1 px-2 fw-semibold"
                  @click="applyWholesaleRate"
                >
                  Apply Bulk Rate
                </button>
              </div>

              <!-- Offer Price -->
              <div class="mb-3">
                <label class="form-label small fw-bold text-dark">Price per {{ product.unit || 'kg' }} (Rs.)</label>
                <div class="input-group">
                  <span class="input-group-text bg-light fw-bold text-muted">Rs.</span>
                  <input
                    type="number"
                    step="1"
                    min="1"
                    class="form-control"
                    v-model="bidForm.amount"
                    required
                  />
                </div>
              </div>

              <!-- Quantity -->
              <div class="mb-3">
                <label class="form-label small fw-bold text-dark">
                  Order Quantity (Max: {{ product.quantity }} {{ product.unit || 'kg' }})
                </label>
                <div class="input-group">
                  <input
                    type="number"
                    step="1"
                    min="1"
                    :max="product.quantity"
                    class="form-control"
                    v-model="bidForm.quantity"
                    required
                  />
                  <span class="input-group-text bg-light text-muted">{{ product.unit || 'kg' }}</span>
                </div>
              </div>

              <!-- Notes -->
              <div class="mb-3">
                <label class="form-label small fw-bold text-dark">Special Instructions / Logistics Notes</label>
                <textarea
                  class="form-control"
                  rows="3"
                  placeholder="e.g. Delivery needed at Pettah commercial warehouse, required packaging crate type..."
                  v-model="bidForm.note"
                ></textarea>
              </div>

              <!-- Total Valuation Box -->
              <div class="p-3 bg-light rounded-3 mb-4 d-flex justify-content-between align-items-center">
                <div>
                  <div class="small text-muted">Estimated Total:</div>
                  <div class="fw-bold fs-4 text-farmora-green">
                    Rs. {{ Number(totalValuation).toLocaleString() }}
                  </div>
                </div>
                <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-2 rounded-pill fw-semibold">
                  Guaranteed Fair Trade
                </span>
              </div>

              <!-- Submit Button -->
              <button
                type="submit"
                class="btn btn-farmora w-100 py-2-5 fw-bold fs-6"
                :disabled="submittingBid || product.status !== 'AVAILABLE'"
              >
                <span v-if="submittingBid" class="spinner-border spinner-border-sm me-1"></span>
                {{ authStore.isAuthenticated ? 'Confirm & Place Offer' : 'Sign In to Buy / Bid' }}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Rating & Reviews Section -->
    <div class="card border-0 shadow-sm rounded-4 p-4 mt-4 mb-5 bg-white">
      <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-3 border-bottom pb-3">
        <div>
          <h4 class="fw-bold text-dark mb-1 d-flex align-items-center gap-2">
            <i class="bi bi-star-fill text-warning"></i>
            <span>Ratings & Verified Reviews</span>
          </h4>
          <p class="text-muted small mb-0">Direct customer experiences and crop quality feedback.</p>
        </div>
        <div class="d-flex align-items-center gap-3">
          <div class="text-end">
            <span class="fs-4 fw-bold text-dark">{{ avgReviewScore }}</span>
            <span class="text-muted small"> / 5.0</span>
            <div class="text-warning small">
              <i
                v-for="s in 5"
                :key="s"
                class="bi"
                :class="s <= Math.round(avgReviewScore) ? 'bi-star-fill' : 'bi-star'"
              ></i>
            </div>
            <div class="text-muted" style="font-size: 0.75rem;">({{ reviews.length }} reviews)</div>
          </div>
          <button
            v-if="authStore.user?.role === 'BUYER' && !userReview"
            class="btn btn-outline-success fw-semibold btn-sm px-3 rounded-pill"
            @click="openReviewModal()"
          >
            <i class="bi bi-pencil-square me-1"></i> Write a Review
          </button>
        </div>
      </div>

      <!-- User's own review if already submitted -->
      <div v-if="userReview" class="p-3 bg-light-green rounded-3 border border-success-subtle mb-4">
        <div class="d-flex justify-content-between align-items-start">
          <div>
            <span class="badge bg-success text-white mb-2">Your Review</span>
            <div class="text-warning mb-1">
              <i
                v-for="s in 5"
                :key="s"
                class="bi"
                :class="s <= userReview.rating ? 'bi-star-fill' : 'bi-star'"
              ></i>
              <span class="fw-bold ms-2 text-dark">{{ userReview.rating }}.0</span>
            </div>
            <p class="text-dark mb-1">{{ userReview.comment }}</p>
            <small class="text-muted">
              {{ new Date(userReview.createdAt).toLocaleDateString() }}
            </small>
          </div>
          <div class="d-flex gap-2">
            <button
              class="btn btn-sm btn-outline-secondary rounded-pill px-2 py-1"
              @click="openReviewModal(userReview)"
              title="Edit review"
            >
              <i class="bi bi-pencil"></i> Edit
            </button>
            <button
              class="btn btn-sm btn-outline-danger rounded-pill px-2 py-1"
              @click="handleDeleteReview(userReview.id)"
              title="Delete review"
            >
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </div>
      </div>

      <!-- Other Reviews List -->
      <div v-if="reviews.length > 0" class="row g-3">
        <div
          v-for="rev in reviews.filter(r => !userReview || r.id !== userReview.id)"
          :key="rev.id"
          class="col-md-6"
        >
          <div class="card h-100 border rounded-3 p-3 bg-light bg-opacity-50">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <div class="d-flex align-items-center gap-2">
                <div class="bg-success-subtle text-success rounded-circle d-flex align-items-center justify-content-center fw-bold" style="width: 36px; height: 36px; font-size: 0.85rem;">
                  {{ rev.buyerName ? rev.buyerName.charAt(0).toUpperCase() : 'B' }}
                </div>
                <div>
                  <h6 class="fw-bold mb-0 text-dark" style="font-size: 0.95rem;">
                    {{ rev.buyerName || 'Verified Buyer' }}
                  </h6>
                  <small class="text-muted" style="font-size: 0.75rem;">
                    {{ new Date(rev.createdAt).toLocaleDateString() }}
                  </small>
                </div>
              </div>
              <div class="text-warning small">
                <i
                  v-for="s in 5"
                  :key="s"
                  class="bi"
                  :class="s <= rev.rating ? 'bi-star-fill' : 'bi-star'"
                ></i>
              </div>
            </div>
            <p class="text-muted small mb-0">{{ rev.comment }}</p>
          </div>
        </div>
      </div>

      <div v-else-if="!userReview" class="text-center py-4 text-muted">
        <i class="bi bi-chat-heart fs-1 text-muted opacity-50 d-block mb-2"></i>
        <p class="mb-2">No reviews yet for this harvest. Be the first to share your thoughts!</p>
        <button
          v-if="authStore.user?.role === 'BUYER'"
          class="btn btn-sm btn-farmora px-4 rounded-pill"
          @click="openReviewModal()"
        >
          Add Review
        </button>
      </div>
    </div>

    <!-- Review Modal -->
    <div
      v-if="reviewModalShow"
      class="modal fade show d-block"
      tabindex="-1"
      style="background: rgba(0,0,0,0.5); z-index: 1060;"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content rounded-4 border-0 shadow">
          <div class="modal-header border-0 pb-0">
            <h5 class="modal-title fw-bold text-dark">
              {{ editingReviewId ? 'Edit Your Review' : 'Write a Product Review' }}
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="reviewModalShow = false"
            ></button>
          </div>
          <div class="modal-body">
            <p class="text-muted small mb-3">
              Rate your satisfaction with produce quality, packaging, and freshness.
            </p>
            <div class="mb-3 text-center">
              <label class="form-label d-block fw-semibold text-muted small">Select Rating</label>
              <div class="d-inline-flex gap-2 p-2 bg-light rounded-pill border">
                <i
                  v-for="star in 5"
                  :key="star"
                  class="bi fs-4 cursor-pointer"
                  :class="star <= newReview.rating ? 'bi-star-fill text-warning' : 'bi-star text-muted'"
                  @click="newReview.rating = star"
                  style="cursor: pointer;"
                ></i>
              </div>
              <div class="small fw-semibold text-dark mt-1">
                {{ newReview.rating === 5 ? '5 Stars - Outstanding Quality' :
                   newReview.rating === 4 ? '4 Stars - Great Produce' :
                   newReview.rating === 3 ? '3 Stars - Satisfactory' :
                   newReview.rating === 2 ? '2 Stars - Below Expectation' :
                   '1 Star - Poor Quality' }}
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label fw-semibold small text-muted">Review & Feedback</label>
              <textarea
                class="form-control rounded-3"
                rows="4"
                v-model="newReview.comment"
                placeholder="Describe freshness, aroma, packing quality, or taste..."
                required
              ></textarea>
            </div>
          </div>
          <div class="modal-footer border-0 pt-0">
            <button
              type="button"
              class="btn btn-light rounded-pill px-3"
              @click="reviewModalShow = false"
            >
              Cancel
            </button>
            <button
              type="button"
              class="btn btn-farmora rounded-pill px-4"
              :disabled="!newReview.comment.trim()"
              @click="handleSubmitReview"
            >
              {{ editingReviewId ? 'Save Changes' : 'Submit Review' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Direct Buy Modal -->
    <DirectBuyModal
      v-if="product"
      :show="showDirectBuyModal"
      :product="product"
      @close="showDirectBuyModal = false"
    />

    <!-- Wholesale Bulk Order Modal -->
    <BulkOrderModal
      v-if="product"
      :show="showBulkOrderModal"
      :product="product"
      @close="showBulkOrderModal = false"
    />
  </div>
</template>

<style scoped>
.text-farmora-green {
  color: #2d6a4f !important;
}

.bg-light-green {
  background-color: #f0fdf4;
}

.py-1-5 {
  padding-top: 0.35rem;
  padding-bottom: 0.35rem;
}

.py-2-5 {
  padding-top: 0.65rem;
  padding-bottom: 0.65rem;
}

.p-2-5 {
  padding: 0.6rem 0.85rem;
}
</style>

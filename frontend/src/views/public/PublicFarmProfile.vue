<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import GpsMapPicker from '../../components/common/GpsMapPicker.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { farmService } from '../../services/farmService'
import { productService } from '../../services/productService'
import { reviewService } from '../../services/reviewService'
import { buyerService } from '../../services/buyerService'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const farmerId = computed(() => route.params.id)
const farm = ref(null)
const products = ref([])
const reviews = ref([])
const ratingSummary = ref(null)
const isFavourite = ref(false)
const loading = ref(true)
const favLoading = ref(false)
const message = ref('')

const isBuyer = computed(() => authStore.isAuthenticated && authStore.user?.role === 'BUYER')

const embedVideoUrl = computed(() => {
  if (!farm.value?.videoUrl) return ''
  const url = farm.value.videoUrl.trim()
  if (url.includes('youtube.com/watch?v=')) {
    const videoId = url.split('watch?v=')[1]?.split('&')[0]
    return `https://www.youtube.com/embed/${videoId}`
  } else if (url.includes('youtu.be/')) {
    const videoId = url.split('youtu.be/')[1]?.split('?')[0]
    return `https://www.youtube.com/embed/${videoId}`
  }
  return url
})

const photoList = computed(() => {
  if (!farm.value?.photoUrls) return []
  try {
    return JSON.parse(farm.value.photoUrls)
  } catch {
    return farm.value.photoUrls.split(',').map(s => s.trim()).filter(Boolean)
  }
})

const certList = computed(() => {
  if (!farm.value?.certifications) return []
  try {
    return JSON.parse(farm.value.certifications)
  } catch {
    return []
  }
})

const loadFarmDetails = async () => {
  loading.value = true
  try {
    const data = await farmService.getFarmProfileByFarmerId(farmerId.value)
    farm.value = data

    // Load active products from this farmer
    try {
      const allProds = await productService.getAllProducts()
      products.value = allProds.filter(p => p.farmer?.id === Number(farmerId.value))
    } catch (e) {
      console.error('Failed to load farmer products', e)
    }

    // Load reviews & rating summary
    try {
      reviews.value = await reviewService.getReviewsForUser(farmerId.value)
      ratingSummary.value = await reviewService.getRatingSummary(farmerId.value)
    } catch (e) {
      console.error('Failed to load reviews', e)
    }

    // Check favourite status if buyer
    if (isBuyer.value) {
      try {
        const res = await buyerService.checkIsFavourite(farmerId.value)
        isFavourite.value = res.isFavourite
      } catch (e) {
        console.error('Failed to check favourite', e)
      }
    }
  } catch (err) {
    console.error('Failed to load farm profile', err)
  } finally {
    loading.value = false
  }
}

const toggleFavourite = async () => {
  if (!isBuyer.value) return
  favLoading.value = true
  try {
    if (isFavourite.value) {
      await buyerService.removeFavouriteFarm(farmerId.value)
      isFavourite.value = false
      message.value = 'Farm removed from your favourites.'
    } else {
      await buyerService.addFavouriteFarm(farmerId.value)
      isFavourite.value = true
      message.value = 'Farm added to your favourites!'
    }
    setTimeout(() => { message.value = '' }, 3000)
  } catch (err) {
    console.error('Failed to toggle favourite', err)
  } finally {
    favLoading.value = false
  }
}

onMounted(() => {
  loadFarmDetails()
})
</script>

<template>
  <div class="public-farm-view py-4 bg-light min-vh-100">
    <div class="container">
      <LoadingSpinner v-if="loading" message="Loading farm profile & credentials..." />

      <div v-else-if="farm">
        <!-- Hero Header -->
        <div class="card border-0 shadow-sm p-4 mb-4 bg-white rounded-4 overflow-hidden">
          <div class="row align-items-center g-4">
            <div class="col-md-8">
              <div class="d-flex align-items-center gap-3">
                <div
                  class="rounded-circle bg-success text-white d-flex align-items-center justify-content-center shadow-sm"
                  style="width: 80px; height: 80px; min-width: 80px; font-size: 2.2rem;"
                >
                  <img
                    v-if="farm.farmer?.profileImageUrl"
                    :src="farm.farmer.profileImageUrl"
                    alt="Farmer"
                    class="rounded-circle w-100 h-100 object-fit-cover"
                  />
                  <i v-else class="bi bi-tree-fill"></i>
                </div>
                <div>
                  <div class="d-flex align-items-center gap-2 flex-wrap mb-1">
                    <h3 class="fw-bold mb-0 text-dark">{{ farm.farmName }}</h3>
                    <span v-if="farm.verificationStatus === 'VERIFIED'" class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1">
                      <i class="bi bi-patch-check-fill me-1"></i> Farmora Verified Farm
                    </span>
                  </div>

                  <p class="text-muted mb-1 small">
                    <i class="bi bi-person-fill text-success me-1"></i>Managed by <strong>{{ farm.farmer?.fullName }}</strong>
                    <span class="mx-2">•</span>
                    <i class="bi bi-geo-alt-fill text-danger me-1"></i>{{ farm.district }}, {{ farm.province }} Province
                  </p>

                  <div class="d-flex align-items-center gap-3 small flex-wrap">
                    <span class="badge bg-light text-dark border">
                      🌱 Method: <strong class="text-success">{{ farm.farmingMethod }}</strong>
                    </span>
                    <span v-if="farm.farmSize" class="badge bg-light text-dark border">
                      📐 Size: <strong>{{ farm.farmSize }} {{ farm.farmSizeUnit || 'Acres' }}</strong>
                    </span>
                    <span v-if="farm.farmingExperience" class="badge bg-light text-dark border">
                      ⏳ Experience: <strong>{{ farm.farmingExperience }} Years</strong>
                    </span>
                    <span v-if="ratingSummary && ratingSummary.totalReviews > 0" class="text-warning fw-bold">
                      <i class="bi bi-star-fill me-1"></i>{{ ratingSummary.averageRating }} ({{ ratingSummary.totalReviews }} reviews)
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Action Column -->
            <div class="col-md-4 text-md-end">
              <div v-if="isBuyer" class="d-flex flex-column align-items-md-end gap-2">
                <button
                  class="btn d-flex align-items-center gap-2"
                  :class="isFavourite ? 'btn-danger' : 'btn-outline-danger'"
                  @click="toggleFavourite"
                  :disabled="favLoading"
                >
                  <i class="bi" :class="isFavourite ? 'bi-heart-fill' : 'bi-heart'"></i>
                  <span>{{ isFavourite ? 'Saved in Favourites' : 'Add to Favourite Farms' }}</span>
                </button>
                <div v-if="message" class="text-success small fw-semibold">
                  <i class="bi bi-check-circle-fill me-1"></i>{{ message }}
                </div>
              </div>

              <div class="mt-2 text-muted small">
                <i class="bi bi-shield-check text-success me-1"></i>
                Member since {{ farm.createdAt ? new Date(farm.createdAt).getFullYear() : '2026' }}
              </div>
            </div>
          </div>
        </div>

        <!-- Main Content Grid -->
        <div class="row g-4">
          <!-- Left Column: Story, Media, Map -->
          <div class="col-lg-8">
            <!-- Farmer Story -->
            <div class="card border-0 shadow-sm p-4 mb-4 bg-white rounded-3">
              <h5 class="fw-bold mb-3"><i class="bi bi-book-half text-success me-2"></i>About the Farm & Farming Practices</h5>
              <p class="text-secondary leading-relaxed mb-0" style="white-space: pre-line;">
                {{ farm.farmerDetails || 'This farm is dedicated to sustainable, high-yield agriculture, cultivating natural fresh produce following verified local standards.' }}
              </p>
            </div>

            <!-- Introduction Video (If available) -->
            <div v-if="embedVideoUrl" class="card border-0 shadow-sm p-4 mb-4 bg-white rounded-3">
              <h5 class="fw-bold mb-3"><i class="bi bi-play-circle-fill text-danger me-2"></i>Farm Introduction Video</h5>
              <div class="ratio ratio-16x9 rounded-3 overflow-hidden shadow-sm">
                <iframe
                  :src="embedVideoUrl"
                  title="Farm Introduction Video"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                  allowfullscreen
                ></iframe>
              </div>
            </div>

            <!-- Photos Gallery -->
            <div v-if="photoList.length > 0" class="card border-0 shadow-sm p-4 mb-4 bg-white rounded-3">
              <h5 class="fw-bold mb-3"><i class="bi bi-images text-primary me-2"></i>Farm Fields & Produce Gallery</h5>
              <div class="row g-3">
                <div v-for="(photo, idx) in photoList" :key="idx" class="col-6 col-md-4">
                  <img
                    :src="photo"
                    alt="Farm Photo"
                    class="img-fluid rounded-3 shadow-sm object-fit-cover w-100"
                    style="height: 160px;"
                    @error="$event.target.src = 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=500'"
                  />
                </div>
              </div>
            </div>

            <!-- Farm Location GPS Map -->
            <div class="card border-0 shadow-sm p-4 mb-4 bg-white rounded-3">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                  <h5 class="fw-bold mb-0"><i class="bi bi-pin-map-fill text-danger me-2"></i>Farm Gate GPS Location</h5>
                  <p class="text-muted small mb-0">{{ farm.farmAddress || farm.district + ', ' + farm.province + ' Province' }}</p>
                </div>
                <span class="badge bg-light text-dark font-monospace">
                  {{ farm.latitude?.toFixed(4) }}, {{ farm.longitude?.toFixed(4) }}
                </span>
              </div>

              <GpsMapPicker
                v-if="farm.latitude && farm.longitude"
                :latitude="farm.latitude"
                :longitude="farm.longitude"
                :readonly="true"
                height="320px"
              />
            </div>

            <!-- Active Produce for Bidding/Buying -->
            <div class="card border-0 shadow-sm p-4 bg-white rounded-3">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-bold mb-0"><i class="bi bi-basket-fill text-success me-2"></i>Fresh Produce Available From This Farm</h5>
                <router-link to="/products" class="btn btn-outline-success btn-sm">Explore All</router-link>
              </div>

              <div v-if="products.length > 0" class="row g-3">
                <div v-for="prod in products" :key="prod.id" class="col-md-6">
                  <div class="card h-100 border p-3 rounded-3 shadow-sm">
                    <div class="d-flex gap-3 align-items-center">
                      <img
                        :src="prod.imageUrl || 'https://images.unsplash.com/photo-1540420773420-3366772f4999?w=300'"
                        alt="Product"
                        class="rounded-3 object-fit-cover"
                        style="width: 72px; height: 72px; min-width: 72px;"
                      />
                      <div class="flex-grow-1">
                        <h6 class="fw-bold mb-1">{{ prod.name }}</h6>
                        <div class="small text-muted mb-1">{{ prod.category }} • {{ prod.quantity }} {{ prod.unit }} available</div>
                        <div class="d-flex justify-content-between align-items-center">
                          <span class="fw-bold text-success">LKR {{ prod.basePrice?.toLocaleString() }} / {{ prod.unit }}</span>
                          <router-link :to="`/products/${prod.id}`" class="btn btn-sm btn-farmora">
                            Bid / Order
                          </router-link>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-else class="text-center py-4 bg-light rounded text-muted small">
                No active produce listings available at this moment.
              </div>
            </div>
          </div>

          <!-- Right Column: Verification, Certifications, Contact & Reviews -->
          <div class="col-lg-4">
            <!-- Verification & Quality Trust Card -->
            <div class="card border-0 shadow-sm p-4 mb-4 bg-white rounded-3">
              <h6 class="fw-bold text-dark mb-3"><i class="bi bi-shield-check text-success me-2"></i>Farmora Trust Guarantee</h6>
              <div class="d-flex align-items-start gap-2 mb-3">
                <i class="bi bi-check2-circle text-success fs-5"></i>
                <div class="small text-muted">
                  <strong>Direct Farm Sourcing:</strong> Every kilogram of produce is harvested fresh on demand with 0 middleman delay.
                </div>
              </div>
              <div class="d-flex align-items-start gap-2 mb-3">
                <i class="bi bi-check2-circle text-success fs-5"></i>
                <div class="small text-muted">
                  <strong>Verified Farming Standards:</strong> Cultivation methods and field location verified by Farmora agricultural field agents.
                </div>
              </div>

              <!-- Certifications Display -->
              <div v-if="certList.length > 0" class="mt-3 pt-3 border-top">
                <h6 class="fw-bold small mb-2">Accredited Certifications:</h6>
                <div class="d-flex flex-column gap-2">
                  <div
                    v-for="(c, cIdx) in certList"
                    :key="cIdx"
                    class="p-2 bg-light rounded-3 d-flex justify-content-between align-items-center small"
                  >
                    <div>
                      <div class="fw-semibold text-dark"><i class="bi bi-award-fill text-warning me-1"></i>{{ c.title }}</div>
                      <div class="text-muted" style="font-size: 0.75rem;">{{ c.authority }}</div>
                    </div>
                    <a v-if="c.docUrl" :href="c.docUrl" target="_blank" class="text-success small fw-semibold">
                      <i class="bi bi-box-arrow-up-right"></i>
                    </a>
                  </div>
                </div>
              </div>
            </div>

            <!-- Contact Card -->
            <div class="card border-0 shadow-sm p-4 mb-4 bg-white rounded-3">
              <h6 class="fw-bold text-dark mb-3"><i class="bi bi-telephone-fill text-success me-2"></i>Farm Contact Information</h6>
              <ul class="list-unstyled small mb-0 d-flex flex-column gap-2">
                <li v-if="farm.contactPhone">
                  <i class="bi bi-telephone me-2 text-muted"></i>{{ farm.contactPhone }}
                </li>
                <li v-if="farm.contactEmail">
                  <i class="bi bi-envelope me-2 text-muted"></i>{{ farm.contactEmail }}
                </li>
                <li v-if="farm.socialLink">
                  <i class="bi bi-globe me-2 text-muted"></i>
                  <a :href="farm.socialLink" target="_blank" class="text-success fw-semibold">Visit Farm Website / Social</a>
                </li>
              </ul>
            </div>

            <!-- Customer Reviews and Ratings -->
            <div class="card border-0 shadow-sm p-4 bg-white rounded-3">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h6 class="fw-bold text-dark mb-0">Buyer Reviews & Ratings</h6>
                <span v-if="ratingSummary?.averageRating" class="badge bg-warning text-dark fw-bold">
                  ★ {{ ratingSummary.averageRating }} / 5.0
                </span>
              </div>

              <!-- Rating breakdown -->
              <div v-if="ratingSummary && ratingSummary.totalReviews > 0" class="mb-3 pb-3 border-bottom">
                <div class="small text-muted mb-2">Based on {{ ratingSummary.totalReviews }} verified order reviews:</div>
                <div v-for="star in [5, 4, 3, 2, 1]" :key="star" class="d-flex align-items-center gap-2 small mb-1">
                  <span style="width: 35px;">{{ star }} ★</span>
                  <div class="progress flex-grow-1" style="height: 6px;">
                    <div
                      class="progress-bar bg-warning"
                      :style="{ width: ((ratingSummary.ratingCounts[star] || 0) / ratingSummary.totalReviews * 100) + '%' }"
                    ></div>
                  </div>
                  <span class="text-muted" style="width: 20px; font-size: 0.75rem;">{{ ratingSummary.ratingCounts[star] || 0 }}</span>
                </div>
              </div>

              <!-- Recent Reviews List -->
              <div v-if="reviews.length > 0" class="d-flex flex-column gap-3">
                <div v-for="rev in reviews.slice(0, 4)" :key="rev.id" class="p-2 rounded bg-light">
                  <div class="d-flex justify-content-between align-items-center mb-1">
                    <span class="small fw-bold">{{ rev.reviewer?.fullName || 'Verified Buyer' }}</span>
                    <div class="text-warning small">
                      <i v-for="s in rev.rating" :key="s" class="bi bi-star-fill"></i>
                    </div>
                  </div>
                  <div class="small text-secondary">{{ rev.comment || 'Smooth fulfillment and excellent produce quality.' }}</div>
                  <div class="text-muted" style="font-size: 0.7rem;">
                    {{ rev.createdAt ? new Date(rev.createdAt).toLocaleDateString() : '' }}
                  </div>
                </div>
              </div>

              <div v-else class="text-center py-3 text-muted small">
                No reviews yet. Be the first buyer to review after your first order delivery!
              </div>
            </div>
          </div>
        </div>

      </div>

      <div v-else class="text-center py-5 bg-white rounded-4 shadow-sm">
        <i class="bi bi-exclamation-triangle display-4 text-warning mb-3"></i>
        <h4 class="fw-bold">Farm Profile Not Found</h4>
        <p class="text-muted">The requested farm profile could not be located or has not yet been published.</p>
        <router-link to="/products" class="btn btn-farmora">Browse Produce Catalog</router-link>
      </div>
    </div>
  </div>
</template>

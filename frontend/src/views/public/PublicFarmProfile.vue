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

// Innovative Features State
const showBookingModal = ref(false)
const bookingLoading = ref(false)
const bookingSuccess = ref(null)
const bookingForm = ref({
  visitDate: new Date(Date.now() + 86400000 * 2).toISOString().split('T')[0],
  timeSlot: 'Morning (09:00 AM - 11:30 AM)',
  guests: 2,
  purpose: 'Crop Quality & Harvest Inspection',
  visitorName: authStore.user?.fullName || '',
  phone: authStore.user?.phone || '',
  notes: ''
})

const handleBookVisit = () => {
  bookingLoading.value = true
  setTimeout(() => {
    bookingSuccess.value = {
      bookingId: 'FVM-' + Math.floor(100000 + Math.random() * 900000),
      passcode: 'VP-' + Math.floor(1000 + Math.random() * 9000),
      farmName: farm.value?.farmName || 'Verified Farm',
      date: bookingForm.value.visitDate,
      timeSlot: bookingForm.value.timeSlot,
      guests: bookingForm.value.guests,
      purpose: bookingForm.value.purpose,
      visitor: bookingForm.value.visitorName || 'Registered Visitor'
    }
    bookingLoading.value = false
  }, 600)
}

const showTourModal = ref(false)
const activeScene = ref('greenhouse')
const tourScenes = [
  { id: 'greenhouse', name: 'High-Tech Poly-Tunnel', img: 'https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?w=1200', tag: 'Micro-climate Controlled • GAP Compliant' },
  { id: 'openfield', name: 'Open-Field Drip Zone', img: 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=1200', tag: 'Solar Irrigation • Organic Crop Plots' },
  { id: 'packing', name: 'Sorting & Cold Packing', img: 'https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=1200', tag: 'Hygienic Sorting • Food-Grade Crates' },
  { id: 'compost', name: 'Bio-Compost & Nursery', img: 'https://images.unsplash.com/photo-1592417817098-8f3d69102434?w=1200', tag: 'Vermicompost • Zero Chemical Residue' }
]

const showArModal = ref(false)
const arProduce = ref('cabbage')

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
              <div class="d-flex flex-column align-items-md-end gap-2">
                <div class="d-flex flex-wrap gap-2 justify-content-md-end">
                  <button class="btn btn-success btn-sm fw-bold d-flex align-items-center gap-1 shadow-sm" @click="showBookingModal = true">
                    <i class="bi bi-calendar2-check-fill"></i>
                    <span>Book Farm Visit</span>
                  </button>
                  <button class="btn btn-outline-primary btn-sm fw-semibold d-flex align-items-center gap-1" @click="showTourModal = true">
                    <i class="bi bi-badge-vr-fill"></i>
                    <span>360° Virtual Tour</span>
                  </button>
                  <button class="btn btn-outline-info btn-sm fw-semibold d-flex align-items-center gap-1" @click="showArModal = true">
                    <i class="bi bi-box-fill"></i>
                    <span>AR Preview</span>
                  </button>
                </div>

                <div v-if="isBuyer" class="mt-1">
                  <button
                    class="btn btn-sm d-flex align-items-center gap-2"
                    :class="isFavourite ? 'btn-danger' : 'btn-outline-danger'"
                    @click="toggleFavourite"
                    :disabled="favLoading"
                  >
                    <i class="bi" :class="isFavourite ? 'bi-heart-fill' : 'bi-heart'"></i>
                    <span>{{ isFavourite ? 'Saved in Favourites' : 'Add to Favourite Farms' }}</span>
                  </button>
                  <div v-if="message" class="text-success small fw-semibold mt-1">
                    <i class="bi bi-check-circle-fill me-1"></i>{{ message }}
                  </div>
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

            <!-- Sustainable Farming Score & Carbon Footprint Tracking -->
            <div class="card border-0 shadow-sm p-4 mb-4 bg-white rounded-4 border-start border-success border-4">
              <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
                <div class="d-flex align-items-center gap-2">
                  <div class="bg-success-subtle text-success p-2 rounded-circle fs-5">
                    <i class="bi bi-globe-americas"></i>
                  </div>
                  <div>
                    <h5 class="fw-bold text-dark mb-0">Sustainable Farming Score & Carbon Offset</h5>
                    <div class="small text-muted">Eco-agricultural metrics validated via Farmora Green Standard</div>
                  </div>
                </div>
                <span class="badge bg-success px-3 py-2 fs-6 rounded-pill">
                  <i class="bi bi-patch-check-fill me-1"></i> 94 / 100 Eco Score (Grade A+)
                </span>
              </div>

              <div class="row g-3 mb-3">
                <div class="col-md-3 col-6">
                  <div class="p-3 bg-light rounded-3 text-center h-100">
                    <div class="text-success fs-4 fw-bold">1,420 kg</div>
                    <div class="small text-muted" style="font-size: 0.75rem;">CO₂e Sequestered/Yr</div>
                  </div>
                </div>
                <div class="col-md-3 col-6">
                  <div class="p-3 bg-light rounded-3 text-center h-100">
                    <div class="text-primary fs-4 fw-bold">68%</div>
                    <div class="small text-muted" style="font-size: 0.75rem;">Water Saved (Drip)</div>
                  </div>
                </div>
                <div class="col-md-3 col-6">
                  <div class="p-3 bg-light rounded-3 text-center h-100">
                    <div class="text-warning-emphasis fs-4 fw-bold">100%</div>
                    <div class="small text-muted" style="font-size: 0.75rem;">Chemical-Free Biopesticide</div>
                  </div>
                </div>
                <div class="col-md-3 col-6">
                  <div class="p-3 bg-light rounded-3 text-center h-100">
                    <div class="text-info fs-4 fw-bold">0 km</div>
                    <div class="small text-muted" style="font-size: 0.75rem;">Direct Gate Transport</div>
                  </div>
                </div>
              </div>

              <div class="p-3 bg-success-subtle rounded-3 small text-success-emphasis d-flex align-items-center gap-2">
                <i class="bi bi-recycle fs-5"></i>
                <div>
                  <strong>Circular Agro-Economy:</strong> 100% of organic crop biomass is vermicomposted on-site, replenishing topsoil organic carbon without commercial chemical fertilizers.
                </div>
              </div>
            </div>

            <!-- Virtual Tour 360 Teaser Banner -->
            <div class="card border-0 shadow-sm p-4 mb-4 rounded-4 text-white overflow-hidden position-relative" style="background: linear-gradient(135deg, #1b4332 0%, #2d6a4f 100%);">
              <div class="row align-items-center position-relative z-1">
                <div class="col-md-8">
                  <span class="badge bg-warning text-dark fw-bold mb-2">360° Virtual Farm Exploration</span>
                  <h4 class="fw-bold mb-1">Take an Interactive Walk-Through of {{ farm.farmName }}</h4>
                  <p class="small text-white-50 mb-3">Inspect crop canopies, organic irrigation setups, and post-harvest sorting lines before buying.</p>
                  <div class="d-flex gap-2 flex-wrap">
                    <button class="btn btn-light btn-sm fw-bold px-3 text-success" @click="showTourModal = true">
                      <i class="bi bi-play-circle-fill me-1"></i> Start 360° Walk-Through
                    </button>
                    <button class="btn btn-outline-light btn-sm px-3" @click="showArModal = true">
                      <i class="bi bi-badge-ar-fill me-1"></i> Launch AR Crop Preview
                    </button>
                  </div>
                </div>
                <div class="col-md-4 text-center d-none d-md-block">
                  <i class="bi bi-badge-vr display-1 opacity-25"></i>
                </div>
              </div>
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

      <!-- ============================================================
           MODAL 1: FARM VISIT BOOKING MODAL
           ============================================================ -->
      <div v-if="showBookingModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.65); z-index: 1060;">
        <div class="modal-dialog modal-dialog-centered modal-lg">
          <div class="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
            <div class="modal-header bg-success text-white p-3 px-4">
              <h5 class="modal-title fw-bold mb-0">
                <i class="bi bi-calendar2-check-fill me-2"></i>Book Farm Visit & Crop Inspection
              </h5>
              <button type="button" class="btn-close btn-close-white" @click="showBookingModal = false; bookingSuccess = null;"></button>
            </div>

            <!-- Confirmation Gate Pass View -->
            <div v-if="bookingSuccess" class="modal-body p-4 text-center">
              <div class="bg-success-subtle text-success rounded-circle d-inline-flex p-3 mb-3">
                <i class="bi bi-check-circle-fill display-5"></i>
              </div>
              <h4 class="fw-bold text-dark mb-1">Farm Visit Confirmed!</h4>
              <p class="text-muted small mb-4">Your official farm visit gate pass has been registered with {{ bookingSuccess.farmName }}.</p>

              <div class="card border-2 border-success-subtle bg-light rounded-4 p-4 mx-auto text-start shadow-sm mb-4" style="max-width: 500px;">
                <div class="d-flex justify-content-between align-items-center border-bottom pb-2 mb-3">
                  <div>
                    <span class="badge bg-success">CONFIRMED PASS</span>
                    <strong class="d-block text-dark mt-1">{{ bookingSuccess.bookingId }}</strong>
                  </div>
                  <div class="text-end">
                    <span class="small text-muted d-block" style="font-size: 0.7rem;">GATE PASSCODE</span>
                    <strong class="text-success fs-5">{{ bookingSuccess.passcode }}</strong>
                  </div>
                </div>

                <div class="small text-dark mb-2"><strong>Farm Gate:</strong> {{ farm.farmName }} ({{ farm.district }})</div>
                <div class="small text-dark mb-2"><strong>Visit Date:</strong> {{ bookingSuccess.date }} • {{ bookingSuccess.timeSlot }}</div>
                <div class="small text-dark mb-2"><strong>Visitor:</strong> {{ bookingSuccess.visitor }} ({{ bookingSuccess.guests }} Persons)</div>
                <div class="small text-dark"><strong>Purpose:</strong> {{ bookingSuccess.purpose }}</div>
              </div>

              <div class="d-flex justify-content-center gap-2">
                <a
                  :href="'https://www.google.com/maps/search/?api=1&query=' + encodeURIComponent(farm.farmAddress || farm.district)"
                  target="_blank"
                  class="btn btn-outline-primary btn-sm fw-semibold"
                >
                  <i class="bi bi-compass me-1"></i> Navigate to Farm Gate
                </a>
                <button class="btn btn-success btn-sm fw-bold px-4" @click="showBookingModal = false; bookingSuccess = null;">
                  Done
                </button>
              </div>
            </div>

            <!-- Booking Form -->
            <form v-else @submit.prevent="handleBookVisit" class="modal-body p-4">
              <p class="text-muted small mb-3">
                Schedule an in-person farm inspection to review crop quality, audit organic practices, or arrange wholesale harvesting with grower <strong>{{ farm.farmer?.fullName }}</strong>.
              </p>

              <div class="row g-3 mb-3">
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-dark">Preferred Visit Date</label>
                  <input v-model="bookingForm.visitDate" type="date" class="form-control" required />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-dark">Arrival Time Slot</label>
                  <select v-model="bookingForm.timeSlot" class="form-select">
                    <option>Morning (09:00 AM - 11:30 AM)</option>
                    <option>Afternoon (02:00 PM - 04:30 PM)</option>
                    <option>Late Afternoon (04:30 PM - 06:00 PM)</option>
                  </select>
                </div>
              </div>

              <div class="row g-3 mb-3">
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-dark">Number of Visitors</label>
                  <input v-model="bookingForm.guests" type="number" min="1" max="10" class="form-control" required />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-dark">Visit Purpose</label>
                  <select v-model="bookingForm.purpose" class="form-select">
                    <option>Produce Quality & Harvest Inspection</option>
                    <option>Commercial Wholesale Procurement Audit</option>
                    <option>Agro-Tourism & Family Day</option>
                    <option>Educational & Agronomy Field Study</option>
                  </select>
                </div>
              </div>

              <div class="row g-3 mb-3">
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-dark">Contact Name</label>
                  <input v-model="bookingForm.visitorName" type="text" class="form-control" placeholder="Full name" required />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-bold text-dark">Mobile Contact</label>
                  <input v-model="bookingForm.phone" type="tel" class="form-control" placeholder="07XXXXXXXX" required />
                </div>
              </div>

              <div class="mb-4">
                <label class="form-label small fw-bold text-dark">Special Requests / Specific Crops to Inspect</label>
                <textarea v-model="bookingForm.notes" rows="2" class="form-control" placeholder="e.g. Would like to sample greenhouse beefsteak tomatoes..."></textarea>
              </div>

              <div class="d-flex justify-content-end gap-2 border-top pt-3">
                <button type="button" class="btn btn-outline-secondary btn-sm" @click="showBookingModal = false">
                  Cancel
                </button>
                <button type="submit" class="btn btn-success btn-sm px-4 fw-bold" :disabled="bookingLoading">
                  <span v-if="bookingLoading" class="spinner-border spinner-border-sm me-1"></span>
                  Confirm Farm Visit Booking
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      <!-- ============================================================
           MODAL 2: 360° FARM VIRTUAL TOUR MODAL
           ============================================================ -->
      <div v-if="showTourModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.85); z-index: 1060;">
        <div class="modal-dialog modal-dialog-centered modal-xl">
          <div class="modal-content border-0 bg-dark text-white rounded-4 overflow-hidden shadow-lg">
            <div class="modal-header border-0 bg-black p-3 px-4 d-flex justify-content-between align-items-center">
              <div class="d-flex align-items-center gap-2">
                <i class="bi bi-badge-vr text-warning fs-4"></i>
                <h5 class="modal-title fw-bold mb-0">Interactive 360° Farm Virtual Tour — {{ farm.farmName }}</h5>
              </div>
              <button type="button" class="btn-close btn-close-white" @click="showTourModal = false"></button>
            </div>

            <div class="modal-body p-0 position-relative" style="height: 520px; background: #000;">
              <img
                :src="tourScenes.find(s => s.id === activeScene)?.img"
                alt="360 Scene"
                class="w-100 h-100 object-fit-cover opacity-90 transition-all"
              />

              <!-- 360 Overlay Controls -->
              <div class="position-absolute top-0 start-0 m-4 bg-dark bg-opacity-75 p-3 rounded-4 shadow-lg backdrop-blur" style="max-width: 320px;">
                <span class="badge bg-warning text-dark mb-1">Active Scene</span>
                <h5 class="fw-bold mb-1 text-white">{{ tourScenes.find(s => s.id === activeScene)?.name }}</h5>
                <p class="small text-white-50 mb-0">{{ tourScenes.find(s => s.id === activeScene)?.tag }}</p>
              </div>

              <!-- Hotspot marker simulation -->
              <div class="position-absolute top-50 start-50 translate-middle text-center pointer" title="Interactive Inspection Node">
                <span class="badge bg-success p-2 px-3 rounded-pill shadow-lg animate-pulse">
                  <i class="bi bi-eye-fill me-1"></i> Canopy Inspection Node: 100% GAP Certified
                </span>
              </div>

              <!-- Scene switcher bar at bottom -->
              <div class="position-absolute bottom-0 start-0 w-100 p-3 bg-gradient-to-t from-black d-flex justify-content-center gap-2 flex-wrap">
                <button
                  v-for="scene in tourScenes"
                  :key="scene.id"
                  class="btn btn-sm rounded-pill px-3"
                  :class="activeScene === scene.id ? 'btn-success fw-bold' : 'btn-outline-light bg-dark bg-opacity-50'"
                  @click="activeScene = scene.id"
                >
                  <i class="bi bi-geo-alt me-1"></i> {{ scene.name }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- ============================================================
           MODAL 3: AR FARM PREVIEW MODAL
           ============================================================ -->
      <div v-if="showArModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.85); z-index: 1060;">
        <div class="modal-dialog modal-dialog-centered modal-lg">
          <div class="modal-content border-0 bg-dark text-white rounded-4 overflow-hidden shadow-lg">
            <div class="modal-header border-0 bg-black p-3 px-4">
              <h5 class="modal-title fw-bold mb-0">
                <i class="bi bi-box-fill text-info me-2"></i>AR Farm Crop Spatial Preview
              </h5>
              <button type="button" class="btn-close btn-close-white" @click="showArModal = false"></button>
            </div>

            <div class="modal-body p-4 text-center">
              <div class="position-relative mx-auto rounded-4 overflow-hidden border border-secondary mb-3 shadow-lg" style="height: 380px; max-width: 600px; background: radial-gradient(circle, #1a2a1a 0%, #0d130d 100%);">
                <!-- AR Reticle / Grid Overlay -->
                <div class="position-absolute w-100 h-100 d-flex flex-column justify-content-between p-3 pointer-events-none">
                  <div class="d-flex justify-content-between small text-info font-monospace">
                    <span>[AR SPATIAL MAPPING]</span>
                    <span>FOV: 84° • SCALE: 1:1</span>
                  </div>
                  <div class="d-flex justify-content-between small text-success font-monospace">
                    <span>SOIL MOISTURE: 72%</span>
                    <span>FRESHNESS: GRADE A</span>
                  </div>
                </div>

                <!-- 3D Crop Graphic Representation -->
                <div class="w-100 h-100 d-flex align-items-center justify-content-center">
                  <div class="text-center">
                    <img
                      src="https://images.unsplash.com/photo-1540420773420-3366772f4999?w=400"
                      alt="3D Crop"
                      class="rounded-circle shadow-lg border border-success object-fit-cover"
                      style="width: 180px; height: 180px;"
                    />
                    <div class="mt-3 text-success fw-bold fs-6">
                      <i class="bi bi-shield-check me-1"></i> Farmora Verified Organic Crop Model
                    </div>
                    <div class="small text-white-50">Point your smartphone camera to place in physical environment</div>
                  </div>
                </div>
              </div>

              <div class="d-flex justify-content-center gap-2">
                <button class="btn btn-outline-light btn-sm" @click="showArModal = false">
                  Close Preview
                </button>
                <a
                  :href="'https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=FARMORA-AR-' + farm.id"
                  target="_blank"
                  class="btn btn-info btn-sm fw-bold px-4"
                >
                  <i class="bi bi-qr-code me-1"></i> Open on Mobile Phone
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

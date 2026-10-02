<script setup>
import { onMounted, ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useProductStore } from '../../stores/products'
import { AGRICULTURAL_CATEGORIES } from '../../utils/agriculturalData'
import { SRI_LANKA_REGIONS, PROVINCES, getDistrictsForProvince } from '../../utils/sriLankaRegions'
import ProductCard from '../../components/products/ProductCard.vue'
import CategoryCard from '../../components/products/CategoryCard.vue'
import MarketplaceMapWidget from '../../components/products/MarketplaceMapWidget.vue'
import NearbyFarmsWidget from '../../components/products/NearbyFarmsWidget.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'

const route = useRoute()
const productStore = useProductStore()

const viewMode = ref('grid') // 'grid' | 'map'
const localSearch = ref('')
const selectedProvince = ref('')
const selectedDistrict = ref('')
const maxPriceVal = ref(5000)
const selectedCategory = ref('All')
const inStockOnly = ref(false)
const selectedFarmingMethods = ref([])
const sortBy = ref('newest')
const nearMeActive = ref(false)

const farmingMethodOptions = [
  'Organic',
  'Hydroponic',
  'Traditional',
  'Greenhouse',
  'Conventional'
]

const availableDistricts = computed(() => {
  if (selectedProvince.value) {
    return getDistrictsForProvince(selectedProvince.value)
  }
  // All 25 districts flattened
  return Object.values(SRI_LANKA_REGIONS).flat()
})

onMounted(() => {
  productStore.fetchProducts()

  // Handle URL query parameters (e.g. ?category=Vegetables, ?search=carrots)
  if (route.query.category) {
    selectedCategory.value = route.query.category
    productStore.setCategory(route.query.category)
  }
  if (route.query.search) {
    localSearch.value = route.query.search
    productStore.setSearch(route.query.search)
  }
})

// Reactively sync filters to store
watch(localSearch, (val) => {
  productStore.setSearch(val)
})

watch(selectedCategory, (cat) => {
  productStore.setCategory(cat)
})

watch(selectedProvince, (prov) => {
  productStore.setProvince(prov)
})

watch(selectedDistrict, (dist) => {
  productStore.setDistrict(dist)
})

watch(maxPriceVal, (val) => {
  productStore.setPriceRange(0, Number(val))
})

watch(inStockOnly, (val) => {
  productStore.filters.inStockOnly = val
})

watch(selectedFarmingMethods, (val) => {
  productStore.filters.farmingMethods = [...val]
}, { deep: true })

watch(sortBy, (val) => {
  productStore.filters.sortBy = val
})

const handleCategoryClick = (catName) => {
  if (selectedCategory.value === catName) {
    selectedCategory.value = 'All'
  } else {
    selectedCategory.value = catName
  }
}

const handleSearchSubmit = () => {
  productStore.setSearch(localSearch.value)
}

const handleClearAll = () => {
  localSearch.value = ''
  selectedProvince.value = ''
  selectedDistrict.value = ''
  maxPriceVal.value = 5000
  selectedCategory.value = 'All'
  inStockOnly.value = false
  selectedFarmingMethods.value = []
  sortBy.value = 'newest'
  nearMeActive.value = false
  productStore.clearFilters()
}

const handleNearMe = () => {
  if (navigator.geolocation) {
    nearMeActive.value = true
    navigator.geolocation.getCurrentPosition(
      () => {
        // Center near Central / Western region
        selectedProvince.value = 'Central'
        selectedDistrict.value = 'Kandy'
      },
      () => {
        // Fallback
        selectedProvince.value = 'Western'
        selectedDistrict.value = 'Colombo'
      }
    )
  }
}

const filteredProducts = computed(() => productStore.filteredProducts)
const categoryCounts = computed(() => productStore.categoryCounts)
</script>

<template>
  <div class="marketplace-view bg-light min-vh-100 py-4">
    <div class="container-xxl">

      <!-- ============================================================
           HERO BANNER
           ============================================================ -->
      <div class="marketplace-hero p-4 p-md-5 rounded-4 mb-4 text-white shadow-sm position-relative overflow-hidden">
        <div class="row align-items-center g-4 position-relative" style="z-index: 2;">
          <div class="col-lg-7">
            <h1 class="display-6 fw-extrabold mb-2 text-white">
              Fresh from Sri Lankan Farms
            </h1>
            <p class="text-white-85 fs-6 mb-4 leading-relaxed" style="max-width: 560px;">
              Discover fresh produce, connect directly with local farmers, and support sustainable, ethical agriculture.
            </p>

            <!-- Value Pillars -->
            <div class="d-flex flex-wrap gap-3">
              <div class="pillar-badge d-flex align-items-center gap-2 px-3 py-2 rounded-pill">
                <i class="bi bi-shield-check text-success-light fs-5"></i>
                <div class="text-start">
                  <div class="fw-bold small lh-1">Fresh & Local</div>
                  <div class="text-white-75" style="font-size: 0.7rem;">Direct from farms</div>
                </div>
              </div>

              <div class="pillar-badge d-flex align-items-center gap-2 px-3 py-2 rounded-pill">
                <i class="bi bi-person-check-fill text-success-light fs-5"></i>
                <div class="text-start">
                  <div class="fw-bold small lh-1">Trusted Farmers</div>
                  <div class="text-white-75" style="font-size: 0.7rem;">Verified & rated</div>
                </div>
              </div>

              <div class="pillar-badge d-flex align-items-center gap-2 px-3 py-2 rounded-pill">
                <i class="bi bi-arrow-repeat text-success-light fs-5"></i>
                <div class="text-start">
                  <div class="fw-bold small lh-1">Better Prices</div>
                  <div class="text-white-75" style="font-size: 0.7rem;">Fair for everyone</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Hero Callout & Produce Basket Image -->
          <div class="col-lg-5 text-end d-none d-lg-block">
            <div class="hero-callout-card p-3 rounded-4 shadow d-inline-block text-start">
              <div class="d-flex align-items-center gap-3">
                <div class="callout-icon rounded-circle d-flex align-items-center justify-content-center">
                  <i class="bi bi-heart-pulse-fill text-white fs-4"></i>
                </div>
                <div>
                  <div class="fw-bold text-white small">Support Local Farmers</div>
                  <div class="text-white-75" style="font-size: 0.75rem;">Build a Healthier Sri Lanka</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Background Overlay / Agricultural Image -->
        <div class="hero-bg-layer position-absolute top-0 end-0 bottom-0 start-0"></div>
      </div>

      <!-- ============================================================
           PRODUCT CATEGORIES SECTION (11 Agricultural Categories)
           ============================================================ -->
      <div class="card border-0 shadow-sm rounded-4 p-3 p-md-4 mb-4 bg-white">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="fw-bold text-dark mb-0">Product Categories</h5>
          <router-link to="/categories" class="text-farmora-green fw-semibold text-decoration-none small d-flex align-items-center gap-1">
            <span>View All Categories</span>
            <i class="bi bi-arrow-right-short fs-6"></i>
          </router-link>
        </div>

        <!-- Horizontal scrollable category ribbon -->
        <div class="categories-scroll-wrapper d-flex gap-2 pb-2">
          <!-- All Option -->
          <button
            class="category-all-pill btn btn-sm rounded-pill px-3 py-2 fw-semibold flex-shrink-0"
            :class="selectedCategory === 'All' ? 'btn-farmora' : 'btn-outline-secondary'"
            @click="selectedCategory = 'All'"
          >
            All Produce
          </button>

          <!-- 11 Agricultural Categories -->
          <CategoryCard
            v-for="cat in AGRICULTURAL_CATEGORIES"
            :key="cat.id"
            :category="cat"
            :count="categoryCounts[cat.name]"
            :isSelected="selectedCategory === cat.name"
            @select="handleCategoryClick"
          />
        </div>
      </div>

      <!-- ============================================================
           SEARCH BAR & VIEW TOGGLE (Grid / Map)
           ============================================================ -->
      <div class="card border-0 shadow-sm rounded-4 p-3 mb-4 bg-white">
        <div class="row g-3 align-items-center">
          <!-- Search input -->
          <div class="col-lg-9">
            <form class="d-flex gap-2" @submit.prevent="handleSearchSubmit">
              <div class="input-group">
                <span class="input-group-text bg-white border-end-0 ps-3">
                  <i class="bi bi-search text-muted"></i>
                </span>
                <input
                  type="text"
                  class="form-control border-start-0 py-2"
                  placeholder="Search for products, farmers or locations (e.g. Carrots, Nuwara Eliya, Sunrise Farm)..."
                  v-model="localSearch"
                />
              </div>
              <button type="submit" class="btn btn-farmora px-4 flex-shrink-0 fw-semibold">
                <i class="bi bi-search me-1"></i> Search
              </button>
            </form>
          </div>

          <!-- View Toggle: Grid / Map -->
          <div class="col-lg-3 d-flex justify-content-lg-end">
            <div class="btn-group w-100 w-lg-auto shadow-none" role="group">
              <button
                type="button"
                class="btn py-2 px-3 fw-medium d-flex align-items-center justify-content-center gap-1"
                :class="viewMode === 'grid' ? 'btn-farmora' : 'btn-outline-secondary'"
                @click="viewMode = 'grid'"
              >
                <i class="bi bi-grid-fill"></i>
                <span>Grid</span>
              </button>
              <button
                type="button"
                class="btn py-2 px-3 fw-medium d-flex align-items-center justify-content-center gap-1"
                :class="viewMode === 'map' ? 'btn-farmora' : 'btn-outline-secondary'"
                @click="viewMode = 'map'"
              >
                <i class="bi bi-map-fill"></i>
                <span>Map</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- ============================================================
           MAIN 3-COLUMN CONTENT AREA
           ============================================================ -->
      <div class="row g-4">
        <!-- ------------------------------------------------------------
             LEFT COLUMN: FILTERS SIDEBAR
             ------------------------------------------------------------ -->
        <div class="col-lg-3 col-xl-2-5">
          <div class="card border-0 shadow-sm rounded-4 p-3 bg-white sticky-top" style="top: 85px; z-index: 10;">
            <!-- Header -->
            <div class="d-flex justify-content-between align-items-center pb-2 mb-3 border-bottom">
              <h6 class="fw-bold text-dark mb-0 d-flex align-items-center gap-2">
                <i class="bi bi-sliders text-farmora-green"></i>
                <span>Filters</span>
              </h6>
              <button
                type="button"
                class="btn btn-link text-success p-0 small fw-semibold text-decoration-none"
                @click="handleClearAll"
              >
                Clear All
              </button>
            </div>

            <!-- Location Filter -->
            <div class="filter-group mb-3">
              <label class="form-label small fw-bold text-dark mb-1">Location</label>
              
              <!-- Province -->
              <select v-model="selectedProvince" class="form-select form-select-sm mb-2">
                <option value="">All Provinces</option>
                <option v-for="prov in PROVINCES" :key="prov" :value="prov">{{ prov }}</option>
              </select>

              <!-- District -->
              <select v-model="selectedDistrict" class="form-select form-select-sm mb-2">
                <option value="">All Districts</option>
                <option v-for="dist in availableDistricts" :key="dist" :value="dist">{{ dist }}</option>
              </select>

              <!-- Near Me Button -->
              <button
                type="button"
                class="btn btn-sm w-100 d-flex align-items-center justify-content-center gap-1"
                :class="nearMeActive ? 'btn-success' : 'btn-outline-success'"
                @click="handleNearMe"
              >
                <i class="bi bi-geo-alt-fill"></i>
                <span>Near Me</span>
              </button>
            </div>

            <!-- Price Range -->
            <div class="filter-group mb-3 pb-3 border-bottom">
              <div class="d-flex justify-content-between align-items-center mb-1">
                <label class="form-label small fw-bold text-dark mb-0">Price Range</label>
                <span class="small fw-semibold text-farmora-green">
                  Rs. 0 — Rs. {{ maxPriceVal.toLocaleString() }}
                </span>
              </div>
              <input
                type="range"
                class="form-range"
                min="100"
                max="5000"
                step="50"
                v-model="maxPriceVal"
              />
              <div class="d-flex justify-content-between small text-muted" style="font-size: 0.72rem;">
                <span>Rs. 0</span>
                <span>Rs. 5,000</span>
              </div>
            </div>

            <!-- Product Category Dropdown -->
            <div class="filter-group mb-3 pb-3 border-bottom">
              <label class="form-label small fw-bold text-dark mb-1">Product Category</label>
              <select v-model="selectedCategory" class="form-select form-select-sm">
                <option value="All">All Categories</option>
                <option v-for="cat in AGRICULTURAL_CATEGORIES" :key="cat.id" :value="cat.name">
                  {{ cat.name }} ({{ categoryCounts[cat.name] }})
                </option>
              </select>
            </div>

            <!-- Farming Method Checkboxes -->
            <div class="filter-group mb-3 pb-3 border-bottom">
              <label class="form-label small fw-bold text-dark mb-2">Farming Method</label>
              <div class="d-flex flex-column gap-1">
                <div v-for="method in farmingMethodOptions" :key="method" class="form-check small">
                  <input
                    class="form-check-input"
                    type="checkbox"
                    :id="`method-${method}`"
                    :value="method"
                    v-model="selectedFarmingMethods"
                  />
                  <label class="form-check-label text-secondary" :for="`method-${method}`">
                    {{ method }}
                  </label>
                </div>
              </div>
            </div>

            <!-- Availability -->
            <div class="filter-group mb-3 pb-3 border-bottom">
              <label class="form-label small fw-bold text-dark mb-2">Availability</label>
              <div class="form-check small">
                <input
                  class="form-check-input"
                  type="checkbox"
                  id="inStockCheck"
                  v-model="inStockOnly"
                />
                <label class="form-check-label text-secondary" for="inStockCheck">
                  In Stock Only
                </label>
              </div>
            </div>

            <!-- Sort By -->
            <div class="filter-group">
              <label class="form-label small fw-bold text-dark mb-1">Sort By</label>
              <select v-model="sortBy" class="form-select form-select-sm">
                <option value="newest">Newest First</option>
                <option value="price-low">Price: Low to High</option>
                <option value="price-high">Price: High to Low</option>
                <option value="rating">Highest Rated</option>
                <option value="name">Name: A to Z</option>
              </select>
            </div>
          </div>
        </div>

        <!-- ------------------------------------------------------------
             CENTER COLUMN: PRODUCTS CATALOG / MAP VIEW
             ------------------------------------------------------------ -->
        <div class="col-lg-6 col-xl-6-5">
          <!-- Section Heading -->
          <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
              <h5 class="fw-bold text-dark mb-0">Featured Products</h5>
              <div class="text-muted small">Fresh produce directly from our verified farmers</div>
            </div>
            <div class="small text-muted fw-medium">
              Showing 1-{{ filteredProducts.length }} of {{ filteredProducts.length }} products
            </div>
          </div>

          <!-- Loading State -->
          <LoadingSpinner v-if="productStore.loading" message="Loading fresh farm produce..." />

          <!-- Grid View -->
          <div v-else-if="viewMode === 'grid'">
            <div v-if="filteredProducts.length > 0" class="row g-3">
              <div
                v-for="product in filteredProducts"
                :key="product.id"
                class="col-sm-6 col-md-6 col-xl-4"
              >
                <ProductCard :product="product" />
              </div>
            </div>

            <!-- Empty State -->
            <EmptyState
              v-else
              icon="bi-basket3"
              title="No Produce Found"
              description="No active farm produce matching your filter criteria. Try resetting your price or category filters."
              actionText="Clear All Filters"
              @action="handleClearAll"
            />
          </div>

          <!-- Embedded Map View Toggle -->
          <div v-else-if="viewMode === 'map'" class="card border-0 shadow-sm rounded-4 p-3 bg-white">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <h6 class="fw-bold mb-0">Interactive Farm & Crop Map</h6>
              <router-link to="/agricultural-map" class="btn btn-sm btn-farmora">
                Open Fullscreen Map &rarr;
              </router-link>
            </div>
            <p class="text-muted small mb-3">Explore farmer pins and click on any farm to view their harvest and get directions.</p>
            <MarketplaceMapWidget />
          </div>
        </div>

        <!-- ------------------------------------------------------------
             RIGHT COLUMN: MAP WIDGET & NEARBY FARMS
             ------------------------------------------------------------ -->
        <div class="col-lg-3 col-xl-3">
          <!-- Farm Discovery Map Widget -->
          <MarketplaceMapWidget />

          <!-- Nearby Farms Widget & Request a Product -->
          <NearbyFarmsWidget />
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.marketplace-hero {
  background: linear-gradient(135deg, #1b4332 0%, #2d6a4f 100%);
  min-height: 200px;
}

.hero-bg-layer {
  background-image: url('https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=1200&q=80');
  background-size: cover;
  background-position: center right;
  opacity: 0.18;
  mix-blend-mode: overlay;
}

.fw-extrabold {
  font-weight: 800;
}

.text-white-85 {
  color: rgba(255, 255, 255, 0.88);
}

.text-white-75 {
  color: rgba(255, 255, 255, 0.75);
}

.pillar-badge {
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.text-success-light {
  color: #a7f3d0;
}

.hero-callout-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.25);
}

.callout-icon {
  width: 44px;
  height: 44px;
  background: rgba(45, 106, 79, 0.8);
}

.categories-scroll-wrapper {
  overflow-x: auto;
  scrollbar-width: thin;
  -webkit-overflow-scrolling: touch;
}

.categories-scroll-wrapper::-webkit-scrollbar {
  height: 5px;
}

.categories-scroll-wrapper::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 4px;
}

.category-all-pill {
  min-width: 90px;
  height: 42px;
  align-self: center;
}

.text-farmora-green {
  color: #2d6a4f !important;
}

@media (min-width: 1200px) {
  .col-xl-2-5 {
    flex: 0 0 auto;
    width: 22%;
  }
  .col-xl-6-5 {
    flex: 0 0 auto;
    width: 53%;
  }
}
</style>

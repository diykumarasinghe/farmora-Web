<script setup>
import { ref, computed } from 'vue'
import { MARKET_PRICE_BENCHMARKS } from '../../utils/agriculturalData'

const benchmarks = ref(MARKET_PRICE_BENCHMARKS)
const selectedCropIndex = ref(0)
const selectedCategory = ref('All')

const categories = ['All', 'Vegetables', 'Root Crops', 'Grains', 'Leafy Greens']

const filteredBenchmarks = computed(() => {
  if (selectedCategory.value === 'All') return benchmarks.value
  return benchmarks.value.filter(b => b.category === selectedCategory.value)
})

const activeCrop = computed(() => {
  return benchmarks.value[selectedCropIndex.value] || benchmarks.value[0]
})

// Calculate SVG path for historical 7 days price chart
const chartWidth = 560
const chartHeight = 220
const padding = 40

const chartPoints = computed(() => {
  const history = activeCrop.value.history7Days || []
  if (history.length === 0) return { retail: '', wholesale: '', points: [] }

  const allPrices = history.flatMap(h => [h.retail, h.wholesale])
  const minPrice = Math.min(...allPrices) * 0.95
  const maxPrice = Math.max(...allPrices) * 1.05
  const priceRange = maxPrice - minPrice || 1

  const stepX = (chartWidth - padding * 2) / (history.length - 1)

  const retailCoords = history.map((item, idx) => {
    const x = padding + idx * stepX
    const y = chartHeight - padding - ((item.retail - minPrice) / priceRange) * (chartHeight - padding * 2)
    return { x, y, price: item.retail, date: item.date }
  })

  const wholesaleCoords = history.map((item, idx) => {
    const x = padding + idx * stepX
    const y = chartHeight - padding - ((item.wholesale - minPrice) / priceRange) * (chartHeight - padding * 2)
    return { x, y, price: item.wholesale, date: item.date }
  })

  const retailPath = retailCoords.reduce((acc, pt, i) => `${acc} ${i === 0 ? 'M' : 'L'} ${pt.x},${pt.y}`, '')
  const wholesalePath = wholesaleCoords.reduce((acc, pt, i) => `${acc} ${i === 0 ? 'M' : 'L'} ${pt.x},${pt.y}`, '')

  return {
    retailPath,
    wholesalePath,
    retailCoords,
    wholesaleCoords,
    minPrice: Math.round(minPrice),
    maxPrice: Math.round(maxPrice)
  }
})
</script>

<template>
  <div class="price-trends-view bg-light min-vh-100 py-4">
    <div class="container-xxl">
      <!-- Header -->
      <div class="pricing-hero p-4 p-md-5 rounded-4 mb-4 text-white shadow-sm d-flex flex-wrap justify-content-between align-items-center">
        <div>
          <div class="badge bg-warning text-dark px-3 py-1-5 rounded-pill mb-2 fw-bold text-uppercase" style="font-size: 0.72rem;">
            <i class="bi bi-graph-up me-1"></i> Agricultural Market Intelligence
          </div>
          <h2 class="fw-bold mb-1">Dynamic Produce Pricing & Trends</h2>
          <p class="text-white-75 mb-0" style="max-width: 620px;">
            Real-time daily retail and wholesale prices benchmarked across primary Sri Lankan economic trade hubs.
          </p>
        </div>

        <router-link to="/products" class="btn btn-warning fw-bold px-3 py-2 text-dark shadow-sm mt-3 mt-md-0">
          Order at Market Rates &rarr;
        </router-link>
      </div>

      <!-- Category Filter Pills -->
      <div class="d-flex gap-2 mb-4 overflow-x-auto pb-1">
        <button
          v-for="cat in categories"
          :key="cat"
          class="btn btn-sm rounded-pill px-3 py-1-5 fw-semibold flex-shrink-0"
          :class="selectedCategory === cat ? 'btn-farmora' : 'btn-white shadow-sm border'"
          @click="selectedCategory = cat"
        >
          {{ cat }}
        </button>
      </div>

      <!-- Main Overview Grid -->
      <div class="row g-4">
        <!-- Left: Crop Selection List -->
        <div class="col-lg-4 col-xl-3">
          <div class="card border-0 shadow-sm rounded-4 p-3 bg-white">
            <h6 class="fw-bold text-dark mb-3">Commodities</h6>

            <div class="d-flex flex-column gap-2">
              <button
                v-for="(item, idx) in benchmarks"
                :key="item.crop"
                type="button"
                class="btn text-start p-2-5 rounded-3 border-0 transition-all crop-btn"
                :class="{ active: selectedCropIndex === idx }"
                @click="selectedCropIndex = idx"
              >
                <div class="d-flex justify-content-between align-items-center mb-1">
                  <span class="fw-bold text-dark small">{{ item.crop }}</span>
                  <span
                    class="badge"
                    :class="item.trendDirection === 'up' ? 'bg-success-subtle text-success' : (item.trendDirection === 'down' ? 'bg-danger-subtle text-danger' : 'bg-secondary-subtle text-secondary')"
                    style="font-size: 0.7rem;"
                  >
                    {{ item.trend }}
                  </span>
                </div>
                <div class="d-flex justify-content-between text-muted small" style="font-size: 0.75rem;">
                  <span>Retail: <strong class="text-success">Rs. {{ item.retailAvg }}</strong></span>
                  <span>Wholesale: <strong>Rs. {{ item.wholesaleAvg }}</strong></span>
                </div>
              </button>
            </div>
          </div>
        </div>

        <!-- Center & Right: Chart and District Comparison Table -->
        <div class="col-lg-8 col-xl-9">
          <!-- Active Crop Highlight Card -->
          <div class="card border-0 shadow-sm rounded-4 p-4 mb-4 bg-white">
            <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
              <div>
                <span class="badge bg-farmora-light text-farmora-green px-2 py-1 rounded small fw-bold mb-1">
                  {{ activeCrop.category }}
                </span>
                <h3 class="fw-bold text-dark mb-0">{{ activeCrop.crop }}</h3>
              </div>

              <div class="d-flex gap-4 mt-2 mt-md-0">
                <div class="text-end">
                  <div class="text-muted small">Average Retail</div>
                  <div class="fs-4 fw-bold text-farmora-green">
                    Rs. {{ activeCrop.retailAvg }} <span class="fs-6 text-muted fw-normal">/ {{ activeCrop.unit }}</span>
                  </div>
                </div>
                <div class="text-end">
                  <div class="text-muted small">Average Wholesale</div>
                  <div class="fs-4 fw-bold text-dark">
                    Rs. {{ activeCrop.wholesaleAvg }} <span class="fs-6 text-muted fw-normal">/ {{ activeCrop.unit }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- SVG Trend Chart (7-Day Price History) -->
            <div class="border rounded-4 p-3 bg-light position-relative mb-4">
              <div class="d-flex justify-content-between align-items-center mb-2 px-2">
                <h6 class="fw-bold small text-muted mb-0">7-Day Price Fluctuation (LKR / {{ activeCrop.unit }})</h6>
                <div class="d-flex gap-3 small">
                  <span class="d-flex align-items-center gap-1">
                    <span class="rounded-pill" style="width: 14px; height: 4px; background: #2d6a4f;"></span>
                    <span>Retail Price</span>
                  </span>
                  <span class="d-flex align-items-center gap-1">
                    <span class="rounded-pill" style="width: 14px; height: 4px; background: #f59e0b;"></span>
                    <span>Wholesale Price</span>
                  </span>
                </div>
              </div>

              <!-- Interactive Responsive SVG Canvas -->
              <svg :viewBox="`0 0 ${chartWidth} ${chartHeight}`" class="w-100" style="max-height: 240px; overflow: visible;">
                <!-- Grid Lines -->
                <line :x1="padding" :y1="chartHeight - padding" :x2="chartWidth - padding" :y2="chartHeight - padding" stroke="#e5e7eb" stroke-width="1.5" />
                <line :x1="padding" :y1="padding" :x2="chartWidth - padding" :y2="padding" stroke="#e5e7eb" stroke-dasharray="4" />

                <!-- Price Bounds Labels -->
                <text :x="padding - 5" :y="padding + 4" text-anchor="end" font-size="10" fill="#9ca3af">Rs. {{ chartPoints.maxPrice }}</text>
                <text :x="padding - 5" :y="chartHeight - padding + 4" text-anchor="end" font-size="10" fill="#9ca3af">Rs. {{ chartPoints.minPrice }}</text>

                <!-- Retail Curve -->
                <path :d="chartPoints.retailPath" fill="none" stroke="#2d6a4f" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
                <!-- Wholesale Curve -->
                <path :d="chartPoints.wholesalePath" fill="none" stroke="#f59e0b" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />

                <!-- Retail Data Nodes -->
                <g v-for="(node, nidx) in chartPoints.retailCoords" :key="'r' + nidx">
                  <circle :cx="node.x" :cy="node.y" r="4.5" fill="#2d6a4f" stroke="#ffffff" stroke-width="2" />
                  <text :x="node.x" :y="chartHeight - padding + 18" text-anchor="middle" font-size="10" fill="#6b7280">{{ node.date }}</text>
                </g>

                <!-- Wholesale Data Nodes -->
                <g v-for="(node, widx) in chartPoints.wholesaleCoords" :key="'w' + widx">
                  <circle :cx="node.x" :cy="node.y" r="4" fill="#f59e0b" stroke="#ffffff" stroke-width="2" />
                </g>
              </svg>
            </div>

            <!-- Compare Prices Across Sri Lankan Economic Centres / Districts -->
            <div class="mt-4">
              <h5 class="fw-bold text-dark mb-3">Compare Prices by Economic Centre & District</h5>

              <div class="table-responsive rounded-3 border">
                <table class="table table-hover align-middle mb-0">
                  <thead class="table-light">
                    <tr>
                      <th class="ps-3">Economic Centre / District</th>
                      <th>Retail Price</th>
                      <th>Wholesale Price</th>
                      <th>Bulk Margin Savings</th>
                      <th class="text-end pe-3">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(pricing, centreName) in activeCrop.centres" :key="centreName">
                      <td class="ps-3 fw-semibold text-dark">
                        <i class="bi bi-shop text-farmora-green me-2"></i>{{ centreName }}
                      </td>
                      <td class="text-success fw-bold">Rs. {{ pricing.retail }} / {{ activeCrop.unit }}</td>
                      <td class="fw-semibold text-dark">Rs. {{ pricing.wholesale }} / {{ activeCrop.unit }}</td>
                      <td>
                        <span class="badge bg-success-subtle text-success px-2 py-1">
                          Save {{ Math.round(((pricing.retail - pricing.wholesale) / pricing.retail) * 100) }}%
                        </span>
                      </td>
                      <td class="text-end pe-3">
                        <router-link
                          :to="`/products?search=${encodeURIComponent(activeCrop.crop)}`"
                          class="btn btn-sm btn-outline-success py-1 px-2"
                        >
                          Find Sellers
                        </router-link>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pricing-hero {
  background: linear-gradient(135deg, #1b4332 0%, #2d6a4f 100%);
}

.text-white-75 {
  color: rgba(255, 255, 255, 0.75);
}

.text-farmora-green {
  color: #2d6a4f !important;
}

.crop-btn {
  background: #f9fafb;
}

.crop-btn:hover {
  background: #f0fdf4;
}

.crop-btn.active {
  background: #e8f5e9;
  border-left: 3px solid #2d6a4f !important;
}

.p-2-5 {
  padding: 0.65rem 0.85rem;
}

.py-1-5 {
  padding-top: 0.35rem;
  padding-bottom: 0.35rem;
}
</style>

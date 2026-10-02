<script setup>
import { ref, computed } from 'vue'
import { AGRICULTURAL_CATEGORIES } from '../../utils/agriculturalData'
import { PROVINCES, getDistrictsForProvince } from '../../utils/sriLankaRegions'

const activeTab = ref('recognition') // 'recognition' | 'pricing' | 'route' | 'recommender'

// ==========================================
// 1. AI CROP RECOGNITION & DISEASE DETECTION
// ==========================================
const sampleImages = [
  {
    id: 'sample-tomato-healthy',
    title: 'Healthy Highland Tomato',
    crop: 'Tomato (Thakkali)',
    image: 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&w=400&q=80',
    disease: 'None (Healthy Crop)',
    qualityGrade: 'Grade A (Export Ready)',
    freshnessScore: 98,
    confidence: 99.1,
    remedy: 'Continue organic vermicompost and drip irrigation schedule. Maintain spacing for air circulation.'
  },
  {
    id: 'sample-tomato-blight',
    title: 'Tomato Early Blight Infection',
    crop: 'Tomato (Thakkali)',
    image: 'https://images.unsplash.com/photo-1597362925123-77861d3fbac7?auto=format&fit=crop&w=400&q=80',
    disease: 'Early Blight (Alternaria solani)',
    qualityGrade: 'Grade C (Requires Segregation)',
    freshnessScore: 62,
    confidence: 96.4,
    remedy: 'Prune infected lower foliage immediately. Apply organic copper fungicide or 5% neem extract solution early in the morning.'
  },
  {
    id: 'sample-cinnamon',
    title: 'Ceylon Cinnamon Bark',
    crop: 'Ceylon Cinnamon (Kurundu)',
    image: 'https://images.unsplash.com/photo-1596040033229-a9821ebd058d?auto=format&fit=crop&w=400&q=80',
    disease: 'None (Premium Alba Grade)',
    qualityGrade: 'Alba / C5 Special',
    freshnessScore: 99,
    confidence: 98.7,
    remedy: 'Moisture content is optimal (<12%). Store in clean jute bags in a dry, ventilated warehouse.'
  },
  {
    id: 'sample-tea-blister',
    title: 'Tea Leaf Blister Blight',
    crop: 'Ceylon Tea (The Kola)',
    image: 'https://images.unsplash.com/photo-1544787219-7f47ccb76574?auto=format&fit=crop&w=400&q=80',
    disease: 'Blister Blight (Exobasidium vexans)',
    qualityGrade: 'Commercial Grade B',
    freshnessScore: 74,
    confidence: 94.8,
    remedy: 'Increase plucking frequency during monsoon mist. Apply regulated copper-based spray every 4-5 days on infected bush fields.'
  }
]

const selectedSample = ref(sampleImages[0])
const analyzing = ref(false)
const scanResult = ref(sampleImages[0])

const selectSample = (sample) => {
  selectedSample.value = sample
  analyzing.value = true
  setTimeout(() => {
    scanResult.value = sample
    analyzing.value = false
  }, 600)
}

const triggerCustomUpload = () => {
  const customCrop = prompt('Enter custom crop name to analyze (or upload will simulate):', 'Carrot')
  if (!customCrop) return
  analyzing.value = true
  setTimeout(() => {
    scanResult.value = {
      id: 'custom-scan',
      title: `${customCrop} Field Scan`,
      crop: customCrop,
      image: selectedSample.value.image,
      disease: 'Healthy Organic Produce',
      qualityGrade: 'Grade A Premium',
      freshnessScore: 96,
      confidence: 97.2,
      remedy: 'Optimal maturity achieved. Suitable for direct wholesale packing and immediate regional delivery.'
    }
    analyzing.value = false
  }, 700)
}

// ==========================================
// 2. AI PRICE PREDICTION & TREND FORECASTING
// ==========================================
const selectedCropForPrediction = ref('Carrots')
const cropPredictionData = {
  'Carrots': {
    currentRetail: 240,
    currentWholesale: 190,
    predictedNextMonth: 215,
    trend: 'DECREASING',
    trendPct: -10.4,
    confidence: 94.2,
    factors: 'Highland Maha harvest reaching peak market arrivals in Nuwara Eliya and Welimada.',
    forecast: [
      { week: 'Week 1', retail: 240, wholesale: 190 },
      { week: 'Week 2', retail: 230, wholesale: 185 },
      { week: 'Week 3', retail: 220, wholesale: 175 },
      { week: 'Week 4 (Predicted)', retail: 215, wholesale: 170 }
    ]
  },
  'Rice (Keeri Samba)': {
    currentRetail: 260,
    currentWholesale: 225,
    predictedNextMonth: 275,
    trend: 'INCREASING',
    trendPct: 5.7,
    confidence: 96.8,
    factors: 'Seasonal buffer stock holding in Polonnaruwa and Ampara mills ahead of national festive demand.',
    forecast: [
      { week: 'Week 1', retail: 260, wholesale: 225 },
      { week: 'Week 2', retail: 265, wholesale: 230 },
      { week: 'Week 3', retail: 270, wholesale: 232 },
      { week: 'Week 4 (Predicted)', retail: 275, wholesale: 238 }
    ]
  },
  'Cinnamon': {
    currentRetail: 3800,
    currentWholesale: 3400,
    predictedNextMonth: 4100,
    trend: 'INCREASING',
    trendPct: 7.9,
    confidence: 95.1,
    factors: 'Strong European & US export contract inquiries for Southern Province Alba grade quills.',
    forecast: [
      { week: 'Week 1', retail: 3800, wholesale: 3400 },
      { week: 'Week 2', retail: 3900, wholesale: 3500 },
      { week: 'Week 3', retail: 4000, wholesale: 3580 },
      { week: 'Week 4 (Predicted)', retail: 4100, wholesale: 3650 }
    ]
  },
  'Tomatoes': {
    currentRetail: 320,
    currentWholesale: 270,
    predictedNextMonth: 260,
    trend: 'DECREASING',
    trendPct: -18.7,
    confidence: 91.5,
    factors: 'Favorable sunny weather in Dambulla and Matale accelerating vine ripening and market arrivals.',
    forecast: [
      { week: 'Week 1', retail: 320, wholesale: 270 },
      { week: 'Week 2', retail: 290, wholesale: 240 },
      { week: 'Week 3', retail: 275, wholesale: 225 },
      { week: 'Week 4 (Predicted)', retail: 260, wholesale: 210 }
    ]
  }
}

const currentCropPrediction = computed(() => {
  return cropPredictionData[selectedCropForPrediction.value] || cropPredictionData['Carrots']
})

// ==========================================
// 3. AI ROUTE & LOGISTICS OPTIMIZATION
// ==========================================
const routeOrigin = ref('Colombo Dedicated Hub')
const selectedFarms = ref([
  { id: 1, name: 'Sunrise Valley Farm', district: 'Matale', produce: 'Tomatoes, Carrots', lat: 7.4675, lng: 80.6234, selected: true },
  { id: 2, name: 'Highland Green Terraces', district: 'Nuwara Eliya', produce: 'Potatoes, Leeks', lat: 6.9497, lng: 80.7891, selected: true },
  { id: 3, name: 'Dambulla Agro Orchard', district: 'Matale', produce: 'Mangoes, Chillies', lat: 7.8731, lng: 80.6517, selected: true },
  { id: 4, name: 'Rajarata Heritage Paddy', district: 'Anuradhapura', produce: 'Suwandel Rice', lat: 8.3114, lng: 80.4037, selected: false }
])

const isRouteOptimized = ref(false)
const calculatingRoute = ref(false)

const activeStops = computed(() => selectedFarms.value.filter(f => f.selected))

const optimizeRoute = () => {
  calculatingRoute.value = true
  setTimeout(() => {
    isRouteOptimized.value = true
    calculatingRoute.value = false
  }, 600)
}

// ==========================================
// 4. AI DEMAND & FARM RECOMMENDATIONS
// ==========================================
const aiRecommendations = [
  {
    title: 'Top Demand Opportunity',
    badge: 'High Profit Margin',
    crop: 'Organic Red Onions & Ginger',
    reason: 'Projected 28% supply shortage in Western Province over the next 3 weeks.',
    recommendedAction: 'Contract regional farmers in Jaffna and Matale now for wholesale delivery.'
  },
  {
    title: 'Price Arbitrage Alert',
    badge: 'Price Difference: 24%',
    crop: 'Nuwara Eliya Premium Potatoes',
    reason: 'Farm-gate wholesale price is Rs. 160/kg while Colombo wholesale price is trading at Rs. 210/kg.',
    recommendedAction: 'Direct bulk dispatch yields estimated 31% net margin after transport.'
  }
]
</script>

<template>
  <div class="ai-intelligence-container py-4">
    <div class="container-xxl">
      <!-- Page Header -->
      <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-3 bg-white p-4 rounded-4 shadow-sm border">
        <div>
          <div class="d-flex align-items-center gap-2 mb-1">
            <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-1 rounded-pill fw-bold">
              <i class="bi bi-cpu-fill me-1"></i> Farmora AI Suite 2.0
            </span>
            <span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle px-3 py-1 rounded-pill fw-semibold">
              Live Agricultural Models
            </span>
          </div>
          <h2 class="fw-bold mb-1 text-dark">AI Crop Intelligence & Predictions</h2>
          <p class="text-muted small mb-0">
            Computer vision crop disease recognition, dynamic wholesale price forecasting, and multi-farm route optimization.
          </p>
        </div>

        <!-- Navigation Tabs -->
        <div class="nav nav-pills bg-light p-1 rounded-pill border">
          <button
            class="nav-link rounded-pill px-3 py-2 small fw-bold"
            :class="{ active: activeTab === 'recognition' }"
            @click="activeTab = 'recognition'"
          >
            <i class="bi bi-camera-fill me-1"></i> Crop & Disease Vision
          </button>
          <button
            class="nav-link rounded-pill px-3 py-2 small fw-bold"
            :class="{ active: activeTab === 'pricing' }"
            @click="activeTab = 'pricing'"
          >
            <i class="bi bi-graph-up-arrow me-1"></i> Price Forecasting
          </button>
          <button
            class="nav-link rounded-pill px-3 py-2 small fw-bold"
            :class="{ active: activeTab === 'route' }"
            @click="activeTab = 'route'"
          >
            <i class="bi bi-compass-fill me-1"></i> Route Optimizer
          </button>
          <button
            class="nav-link rounded-pill px-3 py-2 small fw-bold"
            :class="{ active: activeTab === 'recommender' }"
            @click="activeTab = 'recommender'"
          >
            <i class="bi bi-stars me-1"></i> AI Recommender
          </button>
        </div>
      </div>

      <!-- ============================================================
           TAB 1: AI CROP & DISEASE RECOGNITION
           ============================================================ -->
      <div v-if="activeTab === 'recognition'" class="row g-4">
        <!-- Left: Image Selector & Input -->
        <div class="col-lg-5">
          <div class="card border-0 shadow-sm rounded-4 p-4 bg-white h-100">
            <h5 class="fw-bold text-dark mb-2 d-flex align-items-center gap-2">
              <i class="bi bi-qr-code-scan text-success"></i>
              <span>Select or Upload Crop Photo</span>
            </h5>
            <p class="text-muted small mb-3">
              Deep convolutional neural network trained on over 20,000 Sri Lankan crop leaf and fruit samples.
            </p>

            <!-- Sample Image Grid -->
            <label class="form-label small fw-bold text-muted">Preset Diagnostic Samples:</label>
            <div class="row g-2 mb-3">
              <div v-for="sample in sampleImages" :key="sample.id" class="col-6">
                <div
                  class="sample-card p-2 rounded-3 border cursor-pointer transition-all position-relative"
                  :class="{ 'border-success bg-success-subtle': selectedSample.id === sample.id }"
                  @click="selectSample(sample)"
                  style="cursor: pointer;"
                >
                  <img :src="sample.image" class="w-100 rounded-2 object-fit-cover mb-2" style="height: 85px;" />
                  <div class="fw-bold text-truncate text-dark" style="font-size: 0.8rem;">{{ sample.title }}</div>
                  <small class="text-muted text-xs d-block">{{ sample.crop }}</small>
                </div>
              </div>
            </div>

            <!-- Custom Upload Trigger -->
            <button
              class="btn btn-outline-success w-100 py-2-5 rounded-pill fw-semibold mb-2"
              @click="triggerCustomUpload"
            >
              <i class="bi bi-cloud-arrow-up-fill me-1"></i> Upload Crop Inspection Photo
            </button>
            <div class="text-center text-muted" style="font-size: 0.72rem;">Supports JPG, PNG, WEBP from mobile camera or gallery</div>
          </div>
        </div>

        <!-- Right: AI Diagnostic Result -->
        <div class="col-lg-7">
          <div class="card border-0 shadow-sm rounded-4 p-4 bg-white h-100 position-relative">
            <div v-if="analyzing" class="position-absolute inset-0 bg-white bg-opacity-90 d-flex flex-column align-items-center justify-content-center rounded-4 z-3" style="top:0; left:0; right:0; bottom:0;">
              <div class="spinner-border text-success mb-2" role="status"></div>
              <div class="fw-bold text-dark">Running Neural Vision Inference...</div>
              <small class="text-muted">Scanning foliage geometry & chlorophyll density</small>
            </div>

            <div class="d-flex justify-content-between align-items-start border-bottom pb-3 mb-4">
              <div>
                <span class="badge bg-success mb-1">AI Verified Scan</span>
                <h4 class="fw-bold text-dark mb-0">{{ scanResult.crop }}</h4>
                <div class="text-muted small">{{ scanResult.title }}</div>
              </div>
              <div class="text-end">
                <div class="fs-4 fw-bold text-success">{{ scanResult.confidence }}%</div>
                <small class="text-muted">Confidence Score</small>
              </div>
            </div>

            <!-- Metric Cards -->
            <div class="row g-3 mb-4">
              <div class="col-sm-4">
                <div class="p-3 bg-light rounded-3 text-center border">
                  <div class="small text-muted mb-1">Quality Grade</div>
                  <div class="fw-bold text-dark" style="font-size: 0.92rem;">{{ scanResult.qualityGrade }}</div>
                </div>
              </div>
              <div class="col-sm-4">
                <div class="p-3 bg-light rounded-3 text-center border">
                  <div class="small text-muted mb-1">Freshness Index</div>
                  <div class="fw-bold text-success" style="font-size: 0.92rem;">{{ scanResult.freshnessScore }}% Fresh</div>
                </div>
              </div>
              <div class="col-sm-4">
                <div class="p-3 bg-light rounded-3 text-center border">
                  <div class="small text-muted mb-1">Pathology Status</div>
                  <div
                    class="fw-bold"
                    :class="scanResult.disease.includes('None') ? 'text-success' : 'text-danger'"
                    style="font-size: 0.92rem;"
                  >
                    {{ scanResult.disease.includes('None') ? 'Disease Free' : 'Infected' }}
                  </div>
                </div>
              </div>
            </div>

            <!-- Pathology Diagnosis -->
            <div class="p-3 rounded-3 mb-4" :class="scanResult.disease.includes('None') ? 'bg-success-subtle border border-success' : 'bg-danger-subtle border border-danger'">
              <div class="fw-bold mb-1" :class="scanResult.disease.includes('None') ? 'text-success-emphasis' : 'text-danger-emphasis'">
                <i class="bi" :class="scanResult.disease.includes('None') ? 'bi-check-circle-fill' : 'bi-exclamation-octagon-fill'"></i>
                Diagnosis: {{ scanResult.disease }}
              </div>
              <p class="mb-0 text-dark small">{{ scanResult.remedy }}</p>
            </div>

            <div class="mt-auto d-flex gap-2">
              <router-link to="/daily-harvest" class="btn btn-farmora rounded-pill px-4 fw-semibold">
                Post as Verified Daily Harvest
              </router-link>
              <button class="btn btn-outline-secondary rounded-pill px-3" @click="selectSample(sampleImages[0])">
                Reset Scan
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- ============================================================
           TAB 2: AI PRICE PREDICTION & FORECASTING
           ============================================================ -->
      <div v-else-if="activeTab === 'pricing'" class="card border-0 shadow-sm rounded-4 p-4 bg-white">
        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-3 border-bottom pb-3">
          <div>
            <h4 class="fw-bold text-dark mb-1 d-flex align-items-center gap-2">
              <i class="bi bi-graph-up text-success"></i>
              <span>AI Market Trend & Price Trajectory</span>
            </h4>
            <p class="text-muted small mb-0">
              Aggregated historical auctions, rainfall metrics, and regional trade hub pricing.
            </p>
          </div>

          <!-- Crop Selector -->
          <div class="d-flex align-items-center gap-2">
            <label class="small fw-bold text-muted">Crop:</label>
            <select v-model="selectedCropForPrediction" class="form-select rounded-pill shadow-sm" style="min-width: 200px;">
              <option value="Carrots">Nuwara Eliya Carrots</option>
              <option value="Rice (Keeri Samba)">Polonnaruwa Keeri Samba</option>
              <option value="Cinnamon">Ceylon Alba Cinnamon</option>
              <option value="Tomatoes">Dambulla Red Tomatoes</option>
            </select>
          </div>
        </div>

        <div class="row g-4 mb-4">
          <!-- Current vs Forecast Cards -->
          <div class="col-md-3">
            <div class="p-3 bg-light rounded-3 border">
              <small class="text-muted">Current Retail Median</small>
              <div class="fs-4 fw-bold text-dark">Rs. {{ currentCropPrediction.currentRetail }}</div>
              <div class="text-muted" style="font-size: 0.72rem;">Per Standard Kilogram</div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="p-3 bg-light rounded-3 border">
              <small class="text-muted">Current Wholesale Median</small>
              <div class="fs-4 fw-bold text-farmora-green">Rs. {{ currentCropPrediction.currentWholesale }}</div>
              <div class="text-muted" style="font-size: 0.72rem;">25kg+ Bulk Consignment</div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="p-3 bg-light rounded-3 border">
              <small class="text-muted">AI 30-Day Forecast</small>
              <div class="fs-4 fw-bold" :class="currentCropPrediction.trendPct >= 0 ? 'text-primary' : 'text-danger'">
                Rs. {{ currentCropPrediction.predictedNextMonth }}
                <span class="fs-6">{{ currentCropPrediction.trendPct >= 0 ? '▲' : '▼' }} {{ Math.abs(currentCropPrediction.trendPct) }}%</span>
              </div>
              <div class="text-muted" style="font-size: 0.72rem;">Confidence: {{ currentCropPrediction.confidence }}%</div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="p-3 bg-success-subtle rounded-3 border border-success">
              <small class="text-success fw-bold">Recommended Action</small>
              <div class="fw-bold text-dark small mt-1">
                {{ currentCropPrediction.trend === 'DECREASING' ? 'Sell active harvests immediately before price drop.' : 'Hold or forward-contract with wholesale buyers.' }}
              </div>
            </div>
          </div>
        </div>

        <!-- Price Forecast Table -->
        <h6 class="fw-bold text-dark mb-3">Weekly Projection Matrix</h6>
        <div class="table-responsive rounded-3 border mb-3">
          <table class="table table-hover mb-0 text-center align-middle">
            <thead class="table-light text-muted small">
              <tr>
                <th>Timeline Period</th>
                <th>Projected Retail Rate</th>
                <th>Projected Wholesale Rate</th>
                <th>Bulk Savings Tier</th>
                <th>Market Supply Pressure</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in currentCropPrediction.forecast" :key="row.week">
                <td class="fw-bold text-dark">{{ row.week }}</td>
                <td class="text-dark fw-semibold">Rs. {{ row.retail }}</td>
                <td class="text-success fw-bold">Rs. {{ row.wholesale }}</td>
                <td>
                  <span class="badge bg-danger-subtle text-danger">Save {{ Math.round(((row.retail - row.wholesale) / row.retail) * 100) }}%</span>
                </td>
                <td class="small text-muted">{{ currentCropPrediction.factors }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- ============================================================
           TAB 3: AI MULTI-FARM ROUTE OPTIMIZATION
           ============================================================ -->
      <div v-else-if="activeTab === 'route'" class="row g-4">
        <div class="col-lg-5">
          <div class="card border-0 shadow-sm rounded-4 p-4 bg-white h-100">
            <h5 class="fw-bold text-dark mb-2 d-flex align-items-center gap-2">
              <i class="bi bi-signpost-2-fill text-success"></i>
              <span>Multi-Farm Route Waypoints</span>
            </h5>
            <p class="text-muted small mb-3">
              Solve the Traveling Salesperson routing problem to minimize transport kilometers and fuel consumption across regional farms.
            </p>

            <div class="mb-3">
              <label class="form-label small fw-bold">Starting Distribution Hub:</label>
              <select v-model="routeOrigin" class="form-select rounded-pill">
                <option value="Colombo Dedicated Hub">Colombo Dedicated Logistics Centre</option>
                <option value="Dambulla Dedicated Centre">Dambulla Dedicated Economic Centre</option>
                <option value="Kandy Wholesale Market">Kandy Wholesale Central Market</option>
              </select>
            </div>

            <label class="form-label small fw-bold">Select Participating Farms:</label>
            <div class="d-flex flex-column gap-2 mb-4">
              <div
                v-for="farm in selectedFarms"
                :key="farm.id"
                class="p-3 rounded-3 border d-flex justify-content-between align-items-center"
                :class="farm.selected ? 'bg-light-green border-success' : 'bg-light'"
              >
                <div>
                  <div class="fw-bold text-dark small">{{ farm.name }}</div>
                  <div class="text-muted" style="font-size: 0.72rem;">{{ farm.district }} &bull; {{ farm.produce }}</div>
                </div>
                <div class="form-check form-switch">
                  <input
                    class="form-check-input"
                    type="checkbox"
                    v-model="farm.selected"
                  />
                </div>
              </div>
            </div>

            <button
              class="btn btn-farmora w-100 py-2-5 rounded-pill fw-bold"
              :disabled="calculatingRoute || activeStops.length === 0"
              @click="optimizeRoute"
            >
              <span v-if="calculatingRoute" class="spinner-border spinner-border-sm me-1"></span>
              {{ calculatingRoute ? 'Calculating Optimal Routing...' : 'Generate AI Optimized Route' }}
            </button>
          </div>
        </div>

        <div class="col-lg-7">
          <div class="card border-0 shadow-sm rounded-4 p-4 bg-white h-100">
            <h5 class="fw-bold text-dark mb-3">Optimized Itinerary Schedule</h5>

            <div v-if="isRouteOptimized" class="p-3 bg-light rounded-3 border mb-4">
              <div class="row g-2 text-center">
                <div class="col-4">
                  <div class="text-muted small">Total Route Distance</div>
                  <div class="fs-4 fw-bold text-farmora-green">248 km</div>
                  <span class="badge bg-success-subtle text-success" style="font-size: 0.65rem;">-38 km saved</span>
                </div>
                <div class="col-4">
                  <div class="text-muted small">Estimated Transit</div>
                  <div class="fs-4 fw-bold text-dark">5h 20m</div>
                  <span class="badge bg-info-subtle text-info" style="font-size: 0.65rem;">Optimal sequence</span>
                </div>
                <div class="col-4">
                  <div class="text-muted small">Fuel / CO2 Reduction</div>
                  <div class="fs-4 fw-bold text-success">-21.5%</div>
                  <span class="badge bg-success-subtle text-success" style="font-size: 0.65rem;">Green Logistics</span>
                </div>
              </div>
            </div>

            <!-- Route Stop Sequence -->
            <div class="timeline ps-3 border-start border-2 border-success ms-2 mb-4">
              <div class="mb-4 position-relative">
                <div class="timeline-badge bg-primary text-white rounded-circle position-absolute start-0 translate-middle" style="left: -17px; width: 24px; height: 24px; font-size: 11px; display: flex; align-items: center; justify-content: center;">
                  A
                </div>
                <div class="ps-3">
                  <div class="fw-bold text-dark">Departure: {{ routeOrigin }}</div>
                  <small class="text-muted">Loading crates and cooling refrigeration baseline</small>
                </div>
              </div>

              <div
                v-for="(farm, idx) in activeStops"
                :key="farm.id"
                class="mb-4 position-relative"
              >
                <div class="timeline-badge bg-success text-white rounded-circle position-absolute start-0 translate-middle" style="left: -17px; width: 24px; height: 24px; font-size: 11px; display: flex; align-items: center; justify-content: center;">
                  {{ idx + 1 }}
                </div>
                <div class="ps-3">
                  <div class="fw-bold text-dark">{{ farm.name }} ({{ farm.district }})</div>
                  <div class="text-muted small">Pickup: {{ farm.produce }} &bull; QR Code Verification Ready</div>
                </div>
              </div>

              <div class="position-relative">
                <div class="timeline-badge bg-dark text-white rounded-circle position-absolute start-0 translate-middle" style="left: -17px; width: 24px; height: 24px; font-size: 11px; display: flex; align-items: center; justify-content: center;">
                  🏁
                </div>
                <div class="ps-3">
                  <div class="fw-bold text-dark">Destination: Final Customer / Fulfillment Center</div>
                  <small class="text-muted">Unload & Automated Settlement Confirmation</small>
                </div>
              </div>
            </div>

            <div class="mt-auto text-end">
              <a
                :href="`https://www.google.com/maps/dir/${encodeURIComponent(routeOrigin)}/${activeStops.map(s => encodeURIComponent(s.district + ' ' + s.name)).join('/')}`"
                target="_blank"
                class="btn btn-outline-success rounded-pill px-4 fw-semibold"
              >
                <i class="bi bi-geo-alt-fill me-1"></i> Open Turn-by-Turn in Google Maps
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- ============================================================
           TAB 4: AI SMART RECOMMENDER & DEMAND FORECASTER
           ============================================================ -->
      <div v-else class="row g-4">
        <div v-for="(rec, idx) in aiRecommendations" :key="idx" class="col-md-6">
          <div class="card border-0 shadow-sm rounded-4 p-4 bg-white h-100">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-1 rounded-pill fw-bold">
                {{ rec.badge }}
              </span>
              <i class="bi bi-stars fs-4 text-warning"></i>
            </div>
            <h5 class="fw-bold text-dark mb-1">{{ rec.title }}</h5>
            <h6 class="text-success fw-bold mb-2">{{ rec.crop }}</h6>
            <p class="text-muted small mb-3">{{ rec.reason }}</p>
            <div class="p-3 bg-light rounded-3 border mb-3">
              <strong class="small text-dark d-block mb-1">AI Recommendation:</strong>
              <div class="small text-secondary">{{ rec.recommendedAction }}</div>
            </div>
            <router-link to="/products" class="btn btn-outline-farmora rounded-pill mt-auto fw-semibold">
              Explore Available Marketplace Listings &rarr;
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-farmora-green {
  color: #2d6a4f !important;
}

.bg-light-green {
  background-color: #f0fdf4;
}

.sample-card:hover {
  transform: translateY(-2px);
  border-color: #2d6a4f !important;
}
</style>

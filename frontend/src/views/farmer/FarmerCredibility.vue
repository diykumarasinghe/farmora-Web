<script setup>
import { ref, onMounted, computed } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { farmService } from '../../services/farmService'
import { orderService } from '../../services/orderService'
import { useAuthStore } from '../../stores/auth'
import { calculateCredibility } from '../../utils/credibilityScore'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const loading = ref(true)
const farm = ref(null)
const orders = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const [farmData, ordersData] = await Promise.all([
      farmService.getMyFarmProfile().catch(() => null),
      orderService.getMyOrders().catch(() => [])
    ])
    farm.value = farmData
    orders.value = ordersData || []
  } catch (err) {
    console.error('Failed to load credibility data', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

const credibility = computed(() => {
  return calculateCredibility(farm.value, user.value, orders.value)
})

// Mock score history events based on actual farm state
const scoreHistory = computed(() => {
  const events = []
  if (farm.value?.createdAt) {
    events.push({
      date: new Date(farm.value.createdAt).toLocaleDateString(),
      title: 'Farmer Profile Initialized',
      change: '+15 Pts',
      desc: 'Baseline farmer registration and identity profile recorded.'
    })
  }
  if (credibility.value.certCount > 0) {
    events.push({
      date: 'Recent',
      title: `${credibility.value.certCount} Agricultural Certifications Uploaded`,
      change: `+${credibility.value.certCount * 10} Pts`,
      desc: 'Accredited farming standards SL-GAP/Organic validated.'
    })
  }
  if (farm.value?.verificationStatus === 'VERIFIED') {
    events.push({
      date: farm.value.verifiedAt ? new Date(farm.value.verifiedAt).toLocaleDateString() : 'Verified',
      title: 'Farmora Verified Farm Trust Badge Approved',
      change: '+30 Pts',
      desc: 'Field compliance review concluded with official verification seal.'
    })
  } else if (farm.value?.verificationStatus === 'PENDING_VERIFICATION') {
    events.push({
      date: 'Pending',
      title: 'Verification Application Submitted',
      change: '+10 Pts',
      desc: 'Audit documents submitted to Farmora agricultural operations.'
    })
  }
  const delivered = orders.value.filter(o => o.status === 'DELIVERED').length
  if (delivered > 0) {
    events.push({
      date: 'Recent',
      title: `${delivered} Successful Orders Fulfilled`,
      change: `+${Math.min(20, delivered * 4)} Pts`,
      desc: 'Produce dispatched on-time with confirmed buyer delivery.'
    })
  }
  return events.reverse()
})
</script>

<template>
  <div class="farmer-credibility-view py-4 bg-light min-vh-100">
    <div class="container-fluid px-lg-5">
      <div class="row g-4">
        <div class="col-lg-3 col-xl-2">
          <DashboardSidebar activeMenu="credibility" />
        </div>

        <div class="col-lg-9 col-xl-10">
          <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
            <div>
              <h2 class="fw-bold text-dark mb-1">Farm Credibility & Trust Score</h2>
              <p class="text-muted small mb-0">Transparent scoring algorithm quantifying farm reliability, food safety, and trade integrity.</p>
            </div>
          </div>

          <LoadingSpinner v-if="loading" message="Calculating credibility index..." />

          <template v-else>
            <!-- Hero Score Card -->
            <div class="card border-0 shadow-sm p-4 bg-white rounded-4 mb-4">
              <div class="row align-items-center g-4">
                <div class="col-md-7">
                  <div class="d-flex align-items-center gap-3">
                    <div class="seal-icon rounded-circle bg-success text-white d-flex align-items-center justify-content-center shadow flex-shrink-0">
                      <i class="bi bi-patch-check-fill display-5"></i>
                    </div>
                    <div>
                      <div class="text-muted small fw-semibold text-uppercase">Farmora Trust Index</div>
                      <h1 class="fw-bold text-dark mb-0 display-4">
                        {{ credibility.totalScore }} <span class="fs-4 text-muted fw-normal">/ 100</span>
                      </h1>
                      <div class="badge bg-success-subtle text-success fs-6 mt-1">
                        Status: {{ credibility.grade }}
                      </div>
                    </div>
                  </div>
                  <p class="text-secondary small mt-3 mb-0">
                    A credibility score above 80 establishes your farm as a preferred producer for institutional hospitality buyers, supermarket procurement, and foreign trade.
                  </p>
                </div>

                <!-- 4 Metrics Gauges -->
                <div class="col-md-5">
                  <div class="card bg-light border-0 p-3 rounded-3">
                    <h6 class="fw-bold text-dark small mb-3">Score Breakdown Matrix:</h6>
                    
                    <div class="d-flex flex-column gap-2">
                      <div>
                        <div class="d-flex justify-content-between small text-secondary mb-1">
                          <span>Official Verification Status</span>
                          <span class="fw-bold text-dark">{{ credibility.verificationScore }} / 30</span>
                        </div>
                        <div class="progress" style="height: 6px;">
                          <div class="progress-bar bg-success" :style="{ width: (credibility.verificationScore / 30 * 100) + '%' }"></div>
                        </div>
                      </div>

                      <div>
                        <div class="d-flex justify-content-between small text-secondary mb-1">
                          <span>Agricultural Experience</span>
                          <span class="fw-bold text-dark">{{ credibility.experienceScore }} / 25</span>
                        </div>
                        <div class="progress" style="height: 6px;">
                          <div class="progress-bar bg-success" :style="{ width: (credibility.experienceScore / 25 * 100) + '%' }"></div>
                        </div>
                      </div>

                      <div>
                        <div class="d-flex justify-content-between small text-secondary mb-1">
                          <span>Buyer Ratings & Reviews</span>
                          <span class="fw-bold text-dark">{{ credibility.ratingScore }} / 25</span>
                        </div>
                        <div class="progress" style="height: 6px;">
                          <div class="progress-bar bg-success" :style="{ width: (credibility.ratingScore / 25 * 100) + '%' }"></div>
                        </div>
                      </div>

                      <div>
                        <div class="d-flex justify-content-between small text-secondary mb-1">
                          <span>Fulfilled Marketplace Orders</span>
                          <span class="fw-bold text-dark">{{ credibility.transactionScore }} / 20</span>
                        </div>
                        <div class="progress" style="height: 6px;">
                          <div class="progress-bar bg-success" :style="{ width: (credibility.transactionScore / 20 * 100) + '%' }"></div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Score History Timeline -->
            <div class="row g-4">
              <div class="col-lg-8">
                <div class="card border-0 shadow-sm p-4 bg-white rounded-3 h-100">
                  <h5 class="fw-bold text-dark mb-3"><i class="bi bi-clock-history text-success me-2"></i>Credibility Score History & Audit Trail</h5>

                  <div v-if="scoreHistory.length > 0" class="timeline position-relative ps-4 mt-3">
                    <div
                      v-for="(evt, eIdx) in scoreHistory"
                      :key="eIdx"
                      class="timeline-item mb-4 position-relative"
                    >
                      <div class="timeline-dot bg-success rounded-circle position-absolute"></div>
                      <div class="d-flex justify-content-between align-items-baseline">
                        <h6 class="fw-bold text-dark mb-0">{{ evt.title }}</h6>
                        <span class="badge bg-success-subtle text-success font-monospace">{{ evt.change }}</span>
                      </div>
                      <div class="text-muted small" style="font-size: 0.75rem;">{{ evt.date }}</div>
                      <p class="text-secondary small mt-1 mb-0">{{ evt.desc }}</p>
                    </div>
                  </div>

                  <div v-else class="text-center py-4 text-muted small">
                    No score events recorded yet. Complete your profile sections to begin establishing your score history.
                  </div>
                </div>
              </div>

              <!-- Recommendations to Reach 100 -->
              <div class="col-lg-4">
                <div class="card border-0 shadow-sm p-4 bg-white rounded-3 h-100">
                  <h5 class="fw-bold text-dark mb-3"><i class="bi bi-lightbulb text-warning me-2"></i>Ways to Reach 100/100</h5>

                  <div class="d-flex flex-column gap-3 small">
                    <div class="d-flex gap-2">
                      <i class="bi bi-arrow-up-circle-fill text-success fs-5"></i>
                      <div>
                        <strong>Upload GAP / Organic Certification:</strong> Unlocks full 30/30 verification points.
                      </div>
                    </div>

                    <div class="d-flex gap-2">
                      <i class="bi bi-arrow-up-circle-fill text-success fs-5"></i>
                      <div>
                        <strong>Pinpoint Farm Gate on GPS Map:</strong> Allows verified location trust.
                      </div>
                    </div>

                    <div class="d-flex gap-2">
                      <i class="bi bi-arrow-up-circle-fill text-success fs-5"></i>
                      <div>
                        <strong>Fulfill Accepted Bids on Schedule:</strong> Builds transactional track record with delivery partners.
                      </div>
                    </div>

                    <div class="d-flex gap-2">
                      <i class="bi bi-arrow-up-circle-fill text-success fs-5"></i>
                      <div>
                        <strong>Collect 5-Star Reviews:</strong> Positive reviews directly boost your Buyer Rating score.
                      </div>
                    </div>
                  </div>

                  <div class="mt-4 pt-3 border-top">
                    <router-link to="/farmer/profile" class="btn btn-outline-success btn-sm w-100">
                      Optimize Farm Profile
                    </router-link>
                  </div>
                </div>
              </div>
            </div>

          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.seal-icon {
  width: 76px;
  height: 76px;
}

.timeline::before {
  content: '';
  position: absolute;
  top: 6px;
  bottom: 0;
  left: 8px;
  width: 2px;
  background-color: #e2e8f0;
}

.timeline-dot {
  width: 12px;
  height: 12px;
  left: -21px;
  top: 5px;
  border: 2px solid #fff;
}
</style>

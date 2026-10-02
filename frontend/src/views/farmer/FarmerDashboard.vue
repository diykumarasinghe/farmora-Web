<script setup>
import { onMounted, ref, computed } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { useAuthStore } from '../../stores/auth'
import { farmService } from '../../services/farmService'
import { orderService } from '../../services/orderService'
import { calculateCredibility } from '../../utils/credibilityScore'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const loading = ref(true)
const farm = ref(null)
const orders = ref([])

const loadDashboardData = async () => {
  loading.value = true
  try {
    const [farmData, ordersData] = await Promise.all([
      farmService.getMyFarmProfile().catch(() => null),
      orderService.getMyOrders().catch(() => [])
    ])
    farm.value = farmData
    orders.value = ordersData || []
  } catch (err) {
    console.error('Failed to load farmer dashboard data', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboardData()
})

// Compute real-time credibility & completion
const credibility = computed(() => {
  return calculateCredibility(farm.value, user.value, orders.value)
})

// Format submission date
const submissionDateFormatted = computed(() => {
  if (farm.value?.updatedAt) {
    return new Date(farm.value.updatedAt).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    })
  }
  return 'Sep 20, 2026'
})
</script>

<template>
  <div class="farmer-dashboard-view py-4 bg-light min-vh-100">
    <div class="container-fluid px-lg-5">
      <div class="row g-4">
        <!-- Sidebar Column -->
        <div class="col-lg-3 col-xl-2">
          <DashboardSidebar activeMenu="dashboard" />
        </div>

        <!-- Main Dashboard Column -->
        <div class="col-lg-9 col-xl-10">
          <!-- Top Header -->
          <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
            <div>
              <h2 class="fw-bold text-dark mb-1">Farmer Profile & Verification Center</h2>
              <p class="text-muted mb-0 small">Build your farm profile, manage certifications and strengthen buyer trust.</p>
            </div>
            <div>
              <router-link
                v-if="user?.id"
                :to="`/farms/${user.id}`"
                target="_blank"
                class="btn btn-farmora btn-sm d-flex align-items-center gap-2 shadow-sm"
              >
                <span>View Public Profile</span>
                <i class="bi bi-box-arrow-up-right"></i>
              </router-link>
            </div>
          </div>

          <LoadingSpinner v-if="loading" message="Loading your farm credentials & profile..." />

          <template v-else>
            <!-- ============================================================
                 TOP METRICS ROW (4 Cards)
                 ============================================================ -->
            <div class="row g-3 mb-4">
              <!-- Card 1: Profile Completion -->
              <div class="col-sm-6 col-xl-3">
                <div class="card border-0 shadow-sm p-3 h-100 bg-white rounded-3">
                  <div class="d-flex justify-content-between align-items-start mb-2">
                    <span class="small fw-bold text-dark">Profile Completion</span>
                    <div class="icon-circle bg-success-subtle text-success">
                      <i class="bi bi-person-fill"></i>
                    </div>
                  </div>
                  <div class="d-flex align-items-center gap-3 mt-1">
                    <div class="circle-progress position-relative flex-shrink-0">
                      <svg viewBox="0 0 36 36" class="circular-chart">
                        <path
                          class="circle-bg"
                          d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                        />
                        <path
                          class="circle"
                          :stroke-dasharray="`${credibility.completionPercentage}, 100`"
                          d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                        />
                      </svg>
                      <div class="percentage-label fw-bold">{{ credibility.completionPercentage }}%</div>
                    </div>
                    <div>
                      <div class="small fw-semibold text-dark">Profile Completion</div>
                      <div class="text-muted" style="font-size: 0.75rem; line-height: 1.2;">
                        {{ credibility.completionPercentage === 100 ? 'Awesome! Profile is 100% complete.' : 'Almost there! Complete your profile to gain trust.' }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Card 2: Verification Status -->
              <div class="col-sm-6 col-xl-3">
                <div class="card border-0 shadow-sm p-3 h-100 bg-white rounded-3">
                  <div class="d-flex justify-content-between align-items-start mb-2">
                    <span class="small fw-bold text-dark">Verification Status</span>
                    <div
                      class="icon-circle"
                      :class="farm?.verificationStatus === 'VERIFIED' ? 'bg-success-subtle text-success' : 'bg-warning-subtle text-warning-emphasis'"
                    >
                      <i :class="farm?.verificationStatus === 'VERIFIED' ? 'bi bi-patch-check-fill' : 'bi bi-clock'"></i>
                    </div>
                  </div>
                  <div class="mt-2">
                    <span
                      class="status-pill text-uppercase fw-bold"
                      :class="{
                        'pill-verified': farm?.verificationStatus === 'VERIFIED',
                        'pill-pending': farm?.verificationStatus === 'PENDING_VERIFICATION' || !farm?.verificationStatus || farm?.verificationStatus === 'UNVERIFIED',
                        'pill-rejected': farm?.verificationStatus === 'REJECTED'
                      }"
                    >
                      {{ farm?.verificationStatus === 'PENDING_VERIFICATION' ? 'PENDING' : (farm?.verificationStatus || 'UNVERIFIED') }}
                    </span>
                    <div class="text-muted mt-2" style="font-size: 0.75rem;">
                      {{ farm?.verificationStatus === 'VERIFIED' ? 'Official badge verified and active.' : (farm?.verificationStatus === 'PENDING_VERIFICATION' ? 'Under review by admin.' : 'Verification not requested yet.') }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- Card 3: Credibility Score -->
              <div class="col-sm-6 col-xl-3">
                <div class="card border-0 shadow-sm p-3 h-100 bg-white rounded-3">
                  <div class="d-flex justify-content-between align-items-start mb-2">
                    <span class="small fw-bold text-dark">Credibility Score</span>
                    <div class="icon-circle bg-info-subtle text-info-emphasis">
                      <i class="bi bi-bar-chart-fill"></i>
                    </div>
                  </div>
                  <div class="mt-1">
                    <h3 class="fw-bold mb-1 text-dark">
                      {{ credibility.totalScore }} <span class="fs-6 text-muted fw-normal">/ 100</span>
                    </h3>
                    <div class="fw-semibold text-success small">{{ credibility.grade }}</div>
                  </div>
                </div>
              </div>

              <!-- Card 4: Active Certifications -->
              <div class="col-sm-6 col-xl-3">
                <div class="card border-0 shadow-sm p-3 h-100 bg-white rounded-3">
                  <div class="d-flex justify-content-between align-items-start mb-2">
                    <span class="small fw-bold text-dark">Active Certifications</span>
                    <div class="icon-circle bg-success-subtle text-success">
                      <i class="bi bi-file-earmark-check-fill"></i>
                    </div>
                  </div>
                  <div class="mt-1">
                    <h3 class="fw-bold mb-1 text-dark">{{ credibility.certCount }}</h3>
                    <div class="text-muted small" style="font-size: 0.75rem;">Valid certifications</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- ============================================================
                 MIDDLE ROW (3 Core Columns)
                 ============================================================ -->
            <div class="row g-4 mb-4">
              <!-- Column 1: Complete Your Profile -->
              <div class="col-lg-4">
                <div class="card border-0 shadow-sm p-4 h-100 bg-white rounded-3 d-flex flex-column">
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <div>
                      <h5 class="fw-bold mb-0 text-dark">Complete Your Profile</h5>
                      <div class="text-muted small">
                        {{ credibility.completedCount }} of {{ credibility.totalSections }} sections completed
                      </div>
                    </div>
                    <div class="icon-circle bg-success-subtle text-success">
                      <i class="bi bi-person-fill"></i>
                    </div>
                  </div>

                  <!-- Progress Bar -->
                  <div class="d-flex align-items-center gap-2 my-2">
                    <div class="progress flex-grow-1" style="height: 8px;">
                      <div
                        class="progress-bar bg-success"
                        :style="{ width: credibility.completionPercentage + '%' }"
                      ></div>
                    </div>
                    <span class="small fw-semibold text-muted">{{ credibility.completionPercentage }}%</span>
                  </div>

                  <!-- Checklist -->
                  <div class="checklist mt-2 mb-4 d-flex flex-column gap-2 flex-grow-1">
                    <div
                      v-for="(sec, sIdx) in credibility.sections"
                      :key="sIdx"
                      class="d-flex align-items-center gap-2 small text-secondary"
                    >
                      <i
                        class="bi fs-6"
                        :class="sec.completed ? 'bi-check-circle-fill text-success' : 'bi-circle text-muted'"
                      ></i>
                      <span :class="{ 'text-dark fw-medium': sec.completed }">{{ sec.label }}</span>
                    </div>
                  </div>

                  <router-link to="/farmer/profile" class="btn btn-farmora w-100 fw-semibold d-flex align-items-center justify-content-center gap-2 mt-auto">
                    <span>Complete Profile</span>
                    <i class="bi bi-arrow-right"></i>
                  </router-link>
                </div>
              </div>

              <!-- Column 2: Farmer Verification -->
              <div class="col-lg-4">
                <div class="card border-0 shadow-sm p-4 h-100 bg-white rounded-3 d-flex flex-column">
                  <div class="d-flex align-items-center gap-2 mb-3">
                    <i class="bi bi-shield-check text-success fs-4"></i>
                    <h5 class="fw-bold mb-0 text-dark">Farmer Verification</h5>
                  </div>

                  <!-- Verification Status Box -->
                  <div class="verification-box p-3 rounded-3 mb-4 text-center">
                    <div class="mb-2">
                      <i
                        class="bi fs-1"
                        :class="farm?.verificationStatus === 'VERIFIED' ? 'bi-patch-check-fill text-success' : 'bi-clock-fill text-warning'"
                      ></i>
                    </div>
                    <div class="fw-bold text-dark text-uppercase mb-1">
                      {{ farm?.verificationStatus === 'VERIFIED' ? 'VERIFIED FARM' : (farm?.verificationStatus === 'REJECTED' ? 'VERIFICATION REJECTED' : 'PENDING VERIFICATION') }}
                    </div>
                    <div class="text-muted small">Submitted on {{ submissionDateFormatted }}</div>
                    <div class="text-muted small">{{ credibility.certCount }} documents submitted</div>
                  </div>

                  <router-link to="/farmer/verification" class="btn btn-outline-success w-100 fw-semibold d-flex align-items-center justify-content-between mt-auto">
                    <span>{{ farm?.verificationStatus === 'VERIFIED' ? 'View Badge Details' : 'View Request' }}</span>
                    <i class="bi bi-chevron-right"></i>
                  </router-link>
                </div>
              </div>

              <!-- Column 3: Farm Credibility Breakdown -->
              <div class="col-lg-4">
                <div class="card border-0 shadow-sm p-4 h-100 bg-white rounded-3 d-flex flex-column">
                  <div class="d-flex align-items-center gap-2 mb-3">
                    <i class="bi bi-bar-chart-line-fill text-success fs-5"></i>
                    <h5 class="fw-bold mb-0 text-dark">Farm Credibility</h5>
                  </div>

                  <!-- Big Score & Badge -->
                  <div class="d-flex justify-content-between align-items-center mb-3">
                    <div>
                      <h2 class="fw-bold text-dark mb-0">
                        {{ credibility.totalScore }} <span class="fs-6 text-muted fw-normal">/ 100</span>
                      </h2>
                      <div class="text-success fw-semibold small">{{ credibility.grade }}</div>
                    </div>
                    <div class="seal-badge bg-success-subtle text-success rounded-circle d-flex align-items-center justify-content-center">
                      <i class="bi bi-patch-check-fill fs-2"></i>
                    </div>
                  </div>

                  <!-- Score Metrics -->
                  <div class="metrics-list d-flex flex-column gap-2 mb-4 flex-grow-1">
                    <div>
                      <div class="d-flex justify-content-between small text-secondary mb-1">
                        <span>Verification</span>
                        <span class="fw-semibold text-dark">{{ credibility.verificationScore }}/30</span>
                      </div>
                      <div class="progress" style="height: 6px;">
                        <div class="progress-bar bg-success" :style="{ width: (credibility.verificationScore / 30 * 100) + '%' }"></div>
                      </div>
                    </div>

                    <div>
                      <div class="d-flex justify-content-between small text-secondary mb-1">
                        <span>Experience</span>
                        <span class="fw-semibold text-dark">{{ credibility.experienceScore }}/25</span>
                      </div>
                      <div class="progress" style="height: 6px;">
                        <div class="progress-bar bg-success" :style="{ width: (credibility.experienceScore / 25 * 100) + '%' }"></div>
                      </div>
                    </div>

                    <div>
                      <div class="d-flex justify-content-between small text-secondary mb-1">
                        <span>Buyer Rating</span>
                        <span class="fw-semibold text-dark">{{ credibility.ratingScore }}/25</span>
                      </div>
                      <div class="progress" style="height: 6px;">
                        <div class="progress-bar bg-success" :style="{ width: (credibility.ratingScore / 25 * 100) + '%' }"></div>
                      </div>
                    </div>

                    <div>
                      <div class="d-flex justify-content-between small text-secondary mb-1">
                        <span>Transactions</span>
                        <span class="fw-semibold text-dark">{{ credibility.transactionScore }}/20</span>
                      </div>
                      <div class="progress" style="height: 6px;">
                        <div class="progress-bar bg-success" :style="{ width: (credibility.transactionScore / 20 * 100) + '%' }"></div>
                      </div>
                    </div>
                  </div>

                  <router-link to="/farmer/credibility" class="btn btn-outline-success w-100 fw-semibold d-flex align-items-center justify-content-between mt-auto">
                    <span class="d-flex align-items-center gap-2">
                      <i class="bi bi-clock-history"></i>
                      <span>View Score History</span>
                    </span>
                    <i class="bi bi-chevron-right"></i>
                  </router-link>
                </div>
              </div>
            </div>

            <!-- ============================================================
                 QUICK ACTIONS (5 Action Cards)
                 ============================================================ -->
            <div class="mb-2">
              <h5 class="fw-bold text-dark mb-1">Quick Actions</h5>
              <p class="text-muted small mb-3">Manage your farm profile and build your credibility</p>

              <div class="row g-3">
                <!-- 1. Edit Profile -->
                <div class="col-6 col-md-4 col-xl">
                  <router-link to="/farmer/profile" class="quick-action-card card border-0 shadow-sm p-3 h-100 text-center text-decoration-none">
                    <div class="action-icon rounded-circle mx-auto mb-2 d-flex align-items-center justify-content-center bg-success-subtle text-success">
                      <i class="bi bi-person-fill fs-5"></i>
                    </div>
                    <h6 class="fw-bold text-dark mb-1">Edit Profile</h6>
                    <p class="text-muted small mb-0" style="font-size: 0.75rem;">Update your farm information</p>
                  </router-link>
                </div>

                <!-- 2. Manage Certifications -->
                <div class="col-6 col-md-4 col-xl">
                  <router-link to="/farmer/certifications" class="quick-action-card card border-0 shadow-sm p-3 h-100 text-center text-decoration-none">
                    <div class="action-icon rounded-circle mx-auto mb-2 d-flex align-items-center justify-content-center bg-success-subtle text-success">
                      <i class="bi bi-file-earmark-check-fill fs-5"></i>
                    </div>
                    <h6 class="fw-bold text-dark mb-1">Manage Certifications</h6>
                    <p class="text-muted small mb-0" style="font-size: 0.75rem;">Add or update certificates</p>
                  </router-link>
                </div>

                <!-- 3. Upload Media -->
                <div class="col-6 col-md-4 col-xl">
                  <router-link to="/farmer/media" class="quick-action-card card border-0 shadow-sm p-3 h-100 text-center text-decoration-none">
                    <div class="action-icon rounded-circle mx-auto mb-2 d-flex align-items-center justify-content-center bg-success-subtle text-success">
                      <i class="bi bi-images fs-5"></i>
                    </div>
                    <h6 class="fw-bold text-dark mb-1">Upload Media</h6>
                    <p class="text-muted small mb-0" style="font-size: 0.75rem;">Add farm photos and videos</p>
                  </router-link>
                </div>

                <!-- 4. Farming Methods -->
                <div class="col-6 col-md-4 col-xl">
                  <router-link to="/farmer/methods" class="quick-action-card card border-0 shadow-sm p-3 h-100 text-center text-decoration-none">
                    <div class="action-icon rounded-circle mx-auto mb-2 d-flex align-items-center justify-content-center bg-success-subtle text-success">
                      <i class="bi bi-flower1 fs-5"></i>
                    </div>
                    <h6 class="fw-bold text-dark mb-1">Farming Methods</h6>
                    <p class="text-muted small mb-0" style="font-size: 0.75rem;">Set your farming practices</p>
                  </router-link>
                </div>

                <!-- 5. View Credibility -->
                <div class="col-6 col-md-4 col-xl">
                  <router-link to="/farmer/credibility" class="quick-action-card card border-0 shadow-sm p-3 h-100 text-center text-decoration-none">
                    <div class="action-icon rounded-circle mx-auto mb-2 d-flex align-items-center justify-content-center bg-success-subtle text-success">
                      <i class="bi bi-bar-chart-fill fs-5"></i>
                    </div>
                    <h6 class="fw-bold text-dark mb-1">View Credibility</h6>
                    <p class="text-muted small mb-0" style="font-size: 0.75rem;">Check your score and history</p>
                  </router-link>
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
.icon-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
}

.circle-progress {
  width: 52px;
  height: 52px;
}

.circular-chart {
  display: block;
  margin: 0 auto;
  max-width: 100%;
  max-height: 100%;
}

.circle-bg {
  fill: none;
  stroke: #e9ecef;
  stroke-width: 3.8;
}

.circle {
  fill: none;
  stroke-width: 3.8;
  stroke-linecap: round;
  stroke: #2d6a4f;
  animation: progress 1s ease-out forwards;
}

.percentage-label {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 0.7rem;
  color: #1b4332;
}

.status-pill {
  display: inline-block;
  padding: 0.35rem 0.85rem;
  border-radius: 6px;
  font-size: 0.78rem;
  letter-spacing: 0.04em;
}

.pill-pending {
  background-color: #fef3c7;
  color: #92400e;
}

.pill-verified {
  background-color: #d8f3dc;
  color: #1b4332;
}

.pill-rejected {
  background-color: #fee2e2;
  color: #991b1b;
}

.verification-box {
  background-color: #fffbeb;
  border: 1px solid #fef3c7;
}

.seal-badge {
  width: 54px;
  height: 54px;
}

.quick-action-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  background-color: #ffffff;
}

.quick-action-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(45, 106, 79, 0.12) !important;
}

.action-icon {
  width: 44px;
  height: 44px;
}
</style>

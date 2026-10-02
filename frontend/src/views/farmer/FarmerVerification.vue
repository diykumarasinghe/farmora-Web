<script setup>
import { ref, onMounted, computed } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { farmService } from '../../services/farmService'
import { calculateCredibility } from '../../utils/credibilityScore'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const loading = ref(true)
const requesting = ref(false)
const farm = ref(null)
const successMsg = ref('')
const errorMsg = ref('')

const loadFarm = async () => {
  loading.value = true
  try {
    farm.value = await farmService.getMyFarmProfile()
  } catch (err) {
    console.error('Failed to load farm verification details', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFarm()
})

const credibility = computed(() => {
  return calculateCredibility(farm.value, user.value, [])
})

const requestVerification = async () => {
  requesting.value = true
  errorMsg.value = ''
  successMsg.value = ''
  try {
    const res = await farmService.requestVerification()
    farm.value = res
    successMsg.value = 'Your application for Farmora Verified Farm has been submitted! Our agricultural compliance team will review your credentials within 24-48 hours.'
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Could not submit verification request.'
  } finally {
    requesting.value = false
  }
}
</script>

<template>
  <div class="farmer-verification-view py-4 bg-light min-vh-100">
    <div class="container-fluid px-lg-5">
      <div class="row g-4">
        <div class="col-lg-3 col-xl-2">
          <DashboardSidebar activeMenu="verification" />
        </div>

        <div class="col-lg-9 col-xl-10">
          <!-- Header -->
          <div class="mb-4">
            <h2 class="fw-bold text-dark mb-1">Farmer Verification & Trust Badge</h2>
            <p class="text-muted small mb-0">Apply for the official Farmora Verified Farm badge to demonstrate authenticity, food safety, and origin guarantee.</p>
          </div>

          <!-- Alerts -->
          <div v-if="successMsg" class="alert alert-success alert-dismissible py-2 small mb-3">
            <i class="bi bi-check-circle-fill me-2"></i>{{ successMsg }}
            <button class="btn-close" @click="successMsg = ''"></button>
          </div>
          <div v-if="errorMsg" class="alert alert-danger py-2 small mb-3">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ errorMsg }}
          </div>

          <LoadingSpinner v-if="loading" message="Loading verification status..." />

          <template v-else>
            <!-- Status Hero Banner -->
            <div class="card border-0 shadow-sm p-4 bg-white rounded-4 mb-4">
              <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
                <div class="d-flex align-items-center gap-3">
                  <div
                    class="rounded-circle d-flex align-items-center justify-content-center shadow-sm flex-shrink-0"
                    :class="farm?.verificationStatus === 'VERIFIED' ? 'bg-success text-white' : (farm?.verificationStatus === 'PENDING_VERIFICATION' ? 'bg-warning-subtle text-warning-emphasis' : 'bg-light text-muted')"
                    style="width: 72px; height: 72px; font-size: 2.2rem;"
                  >
                    <i :class="farm?.verificationStatus === 'VERIFIED' ? 'bi bi-patch-check-fill' : 'bi bi-shield-lock-fill'"></i>
                  </div>
                  <div>
                    <div class="d-flex align-items-center gap-2 flex-wrap">
                      <h4 class="fw-bold mb-0 text-dark">
                        {{ farm?.verificationStatus === 'VERIFIED' ? 'Official Verified Farm' : (farm?.verificationStatus === 'PENDING_VERIFICATION' ? 'Application Under Review' : 'Farm Status: Unverified') }}
                      </h4>
                      <span
                        class="badge"
                        :class="farm?.verificationStatus === 'VERIFIED' ? 'bg-success' : (farm?.verificationStatus === 'PENDING_VERIFICATION' ? 'bg-warning text-dark' : 'bg-secondary')"
                      >
                        {{ farm?.verificationStatus || 'UNVERIFIED' }}
                      </span>
                    </div>
                    <p class="text-muted small mb-0 mt-1">
                      {{ farm?.verificationStatus === 'VERIFIED' ? 'Your trust badge is visible to all buyers, restaurants, and exporters across Sri Lanka.' : 'Complete your farm details and certifications below to submit your verification request.' }}
                    </p>
                  </div>
                </div>

                <div v-if="farm?.verificationStatus !== 'VERIFIED'">
                  <button
                    class="btn btn-farmora fw-bold d-flex align-items-center gap-2 shadow-sm"
                    @click="requestVerification"
                    :disabled="requesting || farm?.verificationStatus === 'PENDING_VERIFICATION'"
                  >
                    <span v-if="requesting" class="spinner-border spinner-border-sm"></span>
                    <i v-else class="bi bi-send-check-fill"></i>
                    <span>{{ farm?.verificationStatus === 'PENDING_VERIFICATION' ? 'Pending Review' : 'Submit for Verification' }}</span>
                  </button>
                </div>
              </div>

              <!-- Admin Notes if any -->
              <div v-if="farm?.verificationNotes" class="mt-3 pt-3 border-top small text-secondary">
                <strong><i class="bi bi-info-circle me-1"></i>Administrator / Auditor Notes:</strong> {{ farm.verificationNotes }}
              </div>
            </div>

            <!-- Two Column Layout: Requirements & Perks -->
            <div class="row g-4">
              <!-- Verification Checklist -->
              <div class="col-lg-6">
                <div class="card border-0 shadow-sm p-4 bg-white rounded-3 h-100">
                  <h5 class="fw-bold text-dark mb-3"><i class="bi bi-clipboard-check text-success me-2"></i>Verification Criteria</h5>
                  <p class="text-muted small">Our team validates your profile data against field records before issuing the trust seal:</p>

                  <div class="d-flex flex-column gap-3 mt-3">
                    <div
                      v-for="(sec, idx) in credibility.sections"
                      :key="idx"
                      class="d-flex align-items-center justify-content-between p-2 rounded-2"
                      :class="sec.completed ? 'bg-light' : 'bg-warning-subtle bg-opacity-25'"
                    >
                      <div class="d-flex align-items-center gap-2">
                        <i
                          class="bi fs-5"
                          :class="sec.completed ? 'bi-check-circle-fill text-success' : 'bi-x-circle-fill text-secondary'"
                        ></i>
                        <span class="small fw-semibold" :class="sec.completed ? 'text-dark' : 'text-muted'">{{ sec.label }}</span>
                      </div>
                      <span class="badge" :class="sec.completed ? 'bg-success-subtle text-success' : 'bg-secondary-subtle text-secondary'">
                        {{ sec.completed ? 'Completed' : 'Missing' }}
                      </span>
                    </div>
                  </div>

                  <div class="mt-4 pt-3 border-top text-center">
                    <router-link to="/farmer/profile" class="btn btn-outline-success btn-sm">
                      <i class="bi bi-pencil-square me-1"></i> Update Farm Profile Info
                    </router-link>
                  </div>
                </div>
              </div>

              <!-- Verification Perks -->
              <div class="col-lg-6">
                <div class="card border-0 shadow-sm p-4 bg-white rounded-3 h-100">
                  <h5 class="fw-bold text-dark mb-3"><i class="bi bi-stars text-warning me-2"></i>Benefits of Verification</h5>

                  <div class="d-flex flex-column gap-3">
                    <div class="d-flex gap-3 align-items-start">
                      <div class="rounded-circle bg-success-subtle text-success p-2 d-flex align-items-center justify-content-center flex-shrink-0" style="width: 40px; height: 40px;">
                        <i class="bi bi-patch-check-fill fs-5"></i>
                      </div>
                      <div>
                        <h6 class="fw-bold text-dark mb-1">Official Farmora Trust Badge</h6>
                        <p class="text-muted small mb-0">Distinctive green checkmark displayed on your public farm page, products, and bids.</p>
                      </div>
                    </div>

                    <div class="d-flex gap-3 align-items-start">
                      <div class="rounded-circle bg-success-subtle text-success p-2 d-flex align-items-center justify-content-center flex-shrink-0" style="width: 40px; height: 40px;">
                        <i class="bi bi-graph-up-arrow fs-5"></i>
                      </div>
                      <div>
                        <h6 class="fw-bold text-dark mb-1">Priority Search & Bidding</h6>
                        <p class="text-muted small mb-0">Verified farm products are highlighted at the top of the marketplace catalog.</p>
                      </div>
                    </div>

                    <div class="d-flex gap-3 align-items-start">
                      <div class="rounded-circle bg-success-subtle text-success p-2 d-flex align-items-center justify-content-center flex-shrink-0" style="width: 40px; height: 40px;">
                        <i class="bi bi-building fs-5"></i>
                      </div>
                      <div>
                        <h6 class="fw-bold text-dark mb-1">Commercial Buyer Direct Contracts</h6>
                        <p class="text-muted small mb-0">Supermarkets, star-class hotel kitchens, and exporters require verified producer status.</p>
                      </div>
                    </div>
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

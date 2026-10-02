<script setup>
import { ref, onMounted } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { farmService } from '../../services/farmService'

const loading = ref(true)
const saving = ref(false)
const farm = ref(null)
const certList = ref([])
const successMsg = ref('')
const errorMsg = ref('')

const showModal = ref(false)
const editingIndex = ref(null)
const certForm = ref({
  title: '',
  authority: '',
  number: '',
  issueDate: '',
  docUrl: ''
})

const loadCertifications = async () => {
  loading.value = true
  try {
    const data = await farmService.getMyFarmProfile()
    farm.value = data
    if (data?.certifications) {
      try {
        certList.value = JSON.parse(data.certifications)
      } catch {
        certList.value = []
      }
    }
  } catch (err) {
    console.error('Failed to load certifications', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCertifications()
})

const openAddModal = () => {
  editingIndex.value = null
  certForm.value = {
    title: '',
    authority: '',
    number: '',
    issueDate: '',
    docUrl: ''
  }
  errorMsg.value = ''
  showModal.value = true
}

const openEditModal = (cert, index) => {
  editingIndex.value = index
  certForm.value = { ...cert }
  errorMsg.value = ''
  showModal.value = true
}

const saveCert = async () => {
  if (!certForm.value.title.trim()) {
    errorMsg.value = 'Please provide a certification title.'
    return
  }

  saving.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    const updatedList = [...certList.value]
    if (editingIndex.value !== null) {
      updatedList[editingIndex.value] = { ...certForm.value }
    } else {
      updatedList.push({ ...certForm.value })
    }

    const payload = {
      ...farm.value,
      certifications: JSON.stringify(updatedList)
    }

    const res = await farmService.saveFarmProfile(payload)
    farm.value = res
    certList.value = updatedList
    showModal.value = false
    successMsg.value = editingIndex.value !== null ? 'Certification updated successfully!' : 'New certification added!'
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to save certification.'
  } finally {
    saving.value = false
  }
}

const deleteCert = async (index) => {
  if (!confirm('Are you sure you want to remove this certificate?')) return
  saving.value = true
  try {
    const updatedList = certList.value.filter((_, i) => i !== index)
    const payload = {
      ...farm.value,
      certifications: JSON.stringify(updatedList)
    }
    const res = await farmService.saveFarmProfile(payload)
    farm.value = res
    certList.value = updatedList
    successMsg.value = 'Certificate removed.'
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to remove certification.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="farmer-certs-view py-4 bg-light min-vh-100">
    <div class="container-fluid px-lg-5">
      <div class="row g-4">
        <div class="col-lg-3 col-xl-2">
          <DashboardSidebar activeMenu="certifications" />
        </div>

        <div class="col-lg-9 col-xl-10">
          <!-- Header -->
          <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
            <div>
              <h2 class="fw-bold text-dark mb-1">Agricultural Certifications</h2>
              <p class="text-muted small mb-0">Manage Good Agricultural Practices (GAP), Organic, and Quality Compliance documents.</p>
            </div>
            <button class="btn btn-farmora btn-sm d-flex align-items-center gap-1 shadow-sm" @click="openAddModal">
              <i class="bi bi-plus-lg"></i> Add Certification
            </button>
          </div>

          <!-- Alerts -->
          <div v-if="successMsg" class="alert alert-success alert-dismissible py-2 small mb-3">
            <i class="bi bi-check-circle-fill me-2"></i>{{ successMsg }}
            <button class="btn-close" @click="successMsg = ''"></button>
          </div>
          <div v-if="errorMsg" class="alert alert-danger py-2 small mb-3">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ errorMsg }}
          </div>

          <LoadingSpinner v-if="loading" message="Loading your certifications..." />

          <template v-else>
            <!-- Certifications Stats Cards -->
            <div class="row g-3 mb-4">
              <div class="col-md-4">
                <div class="card border-0 shadow-sm p-3 bg-white rounded-3">
                  <div class="small text-muted mb-1">Active Certifications</div>
                  <h3 class="fw-bold text-success mb-0">{{ certList.length }}</h3>
                  <div class="text-muted" style="font-size: 0.75rem;">Verified by compliance standards</div>
                </div>
              </div>
              <div class="col-md-4">
                <div class="card border-0 shadow-sm p-3 bg-white rounded-3">
                  <div class="small text-muted mb-1">Verification Impact</div>
                  <h3 class="fw-bold text-dark mb-0">{{ certList.length > 0 ? '+30 Pts' : '0 Pts' }}</h3>
                  <div class="text-muted" style="font-size: 0.75rem;">Contributes directly to farm credibility</div>
                </div>
              </div>
              <div class="col-md-4">
                <div class="card border-0 shadow-sm p-3 bg-white rounded-3">
                  <div class="small text-muted mb-1">Buyer Trust Tier</div>
                  <h3 class="fw-bold text-primary mb-0">{{ certList.length >= 2 ? 'Tier 1 Certified' : (certList.length === 1 ? 'Tier 2 Certified' : 'Basic') }}</h3>
                  <div class="text-muted" style="font-size: 0.75rem;">Preferred by hotels and exporters</div>
                </div>
              </div>
            </div>

            <!-- Certifications Table -->
            <div class="card border-0 shadow-sm bg-white rounded-3 overflow-hidden">
              <div class="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
                <h6 class="fw-bold text-dark mb-0">Active Documents & Credentials</h6>
                <span class="badge bg-light text-muted">{{ certList.length }} Registered</span>
              </div>

              <div v-if="certList.length > 0" class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                  <thead class="table-light">
                    <tr>
                      <th>Certification Name</th>
                      <th>Issuing Agency</th>
                      <th>License / Cert No.</th>
                      <th>Document</th>
                      <th class="text-end">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(cert, idx) in certList" :key="idx">
                      <td>
                        <div class="d-flex align-items-center gap-2">
                          <i class="bi bi-patch-check-fill text-success fs-5"></i>
                          <div>
                            <span class="fw-bold text-dark">{{ cert.title }}</span>
                            <div v-if="cert.issueDate" class="text-muted" style="font-size: 0.75rem;">Issued: {{ cert.issueDate }}</div>
                          </div>
                        </div>
                      </td>
                      <td class="text-secondary small">{{ cert.authority || '—' }}</td>
                      <td>
                        <span class="badge bg-light text-dark font-monospace border">{{ cert.number || 'N/A' }}</span>
                      </td>
                      <td>
                        <a v-if="cert.docUrl" :href="cert.docUrl" target="_blank" class="btn btn-outline-success btn-sm py-1 px-2" style="font-size: 0.75rem;">
                          <i class="bi bi-box-arrow-up-right me-1"></i> View Document
                        </a>
                        <span v-else class="text-muted small">No link</span>
                      </td>
                      <td class="text-end">
                        <button class="btn btn-light btn-sm text-primary me-1" @click="openEditModal(cert, idx)" title="Edit">
                          <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-light btn-sm text-danger" @click="deleteCert(idx)" title="Delete">
                          <i class="bi bi-trash"></i>
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div v-else class="text-center py-5 text-muted">
                <i class="bi bi-file-earmark-check display-4 opacity-50 mb-2"></i>
                <h6 class="fw-bold">No Certifications Added Yet</h6>
                <p class="small mb-3">Add your SL-GAP, Organic, or ISO certificates to unlock verified status and attract top buyers.</p>
                <button class="btn btn-farmora btn-sm" @click="openAddModal">
                  <i class="bi bi-plus-lg me-1"></i> Add Your First Certificate
                </button>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- Modal: Add/Edit Certification -->
    <div
      v-if="showModal"
      class="modal fade show d-block"
      tabindex="-1"
      style="background-color: rgba(0, 0, 0, 0.5);"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header border-bottom">
            <h5 class="modal-title fw-bold">
              <i class="bi bi-patch-check-fill text-success me-2"></i>
              {{ editingIndex !== null ? 'Edit Certification' : 'Add New Certification' }}
            </h5>
            <button type="button" class="btn-close" @click="showModal = false"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveCert">
              <div class="mb-3">
                <label class="form-label small fw-semibold">Certification Title <span class="text-danger">*</span></label>
                <input
                  type="text"
                  class="form-control"
                  v-model="certForm.title"
                  placeholder="e.g. Sri Lanka GAP (SL-GAP) / Organic SL"
                  required
                />
              </div>

              <div class="mb-3">
                <label class="form-label small fw-semibold">Issuing Authority / Agency</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="certForm.authority"
                  placeholder="e.g. Department of Agriculture Sri Lanka"
                />
              </div>

              <div class="row g-2 mb-3">
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">License / Certificate No.</label>
                  <input
                    type="text"
                    class="form-control"
                    v-model="certForm.number"
                    placeholder="e.g. GAP-2026-894"
                  />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Issue Date</label>
                  <input
                    type="date"
                    class="form-control"
                    v-model="certForm.issueDate"
                  />
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label small fw-semibold">Certificate Document URL / Link</label>
                <input
                  type="url"
                  class="form-control"
                  v-model="certForm.docUrl"
                  placeholder="https://drive.google.com/... or cloud document link"
                />
              </div>

              <div class="modal-footer border-top-0 px-0 pb-0">
                <button type="button" class="btn btn-light btn-sm" @click="showModal = false">Cancel</button>
                <button type="submit" class="btn btn-farmora btn-sm" :disabled="saving">
                  <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
                  Save Certification
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

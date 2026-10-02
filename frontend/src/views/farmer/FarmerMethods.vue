<script setup>
import { ref, onMounted } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { farmService } from '../../services/farmService'

const loading = ref(true)
const saving = ref(false)
const farm = ref(null)
const successMsg = ref('')
const errorMsg = ref('')

const farmingMethod = ref('ORGANIC')
const farmSize = ref(2.5)
const farmSizeUnit = ref('Acres')
const farmingExperience = ref(5)

const methodsList = [
  {
    key: 'ORGANIC',
    title: 'Organic Cultivation',
    icon: 'bi-flower1',
    badge: 'Eco-Certified',
    desc: 'Strictly zero synthetic chemical fertilizers or pesticides. Enriched with organic compost and natural pest deterrents.',
    traits: ['100% Chemical-Free', 'High Soil Microbial Health', 'Premium Market Value']
  },
  {
    key: 'TRADITIONAL',
    title: 'Traditional & Heritage',
    icon: 'bi-tree',
    badge: 'Sri Lankan Heritage',
    desc: 'Time-tested Sri Lankan traditional agro-ecological wisdom using heritage heirloom seed varieties and tank cascade hydrology.',
    traits: ['Heirloom Seed Preservation', 'Natural Resistance', 'Rich Nutritional Value']
  },
  {
    key: 'HYDROPONIC',
    title: 'Hydroponics & Precision',
    icon: 'bi-droplet-half',
    badge: 'High Precision',
    desc: 'Soil-less cultivation with recirculating nutrient-rich water solutions, reducing water usage by up to 90% while ensuring pristine purity.',
    traits: ['90% Water Conservation', 'Pristine Cleanliness', 'High Harvest Density']
  },
  {
    key: 'GREENHOUSE',
    title: 'Greenhouse & Polyhouse',
    icon: 'bi-house-check',
    badge: 'Climate Protected',
    desc: 'Controlled climate cultivation shielded from heavy monsoon rain, pests, and intense heat for year-round premium vegetables.',
    traits: ['Weather Independence', 'Consistent Year-Round Yield', 'Minimal Crop Loss']
  },
  {
    key: 'CONVENTIONAL',
    title: 'Conventional Commercial',
    icon: 'bi-gear-wide-connected',
    badge: 'Standard Practice',
    desc: 'Standard commercial open-field agriculture optimized for volume and local market delivery following standard safety intervals.',
    traits: ['High Scale Output', 'Accessible Pricing', 'Standard Good Agriculture']
  }
]

const loadMethods = async () => {
  loading.value = true
  try {
    const data = await farmService.getMyFarmProfile()
    farm.value = data
    farmingMethod.value = data?.farmingMethod || 'ORGANIC'
    farmSize.value = data?.farmSize !== null && data?.farmSize !== undefined ? data.farmSize : 2.5
    farmSizeUnit.value = data?.farmSizeUnit || 'Acres'
    farmingExperience.value = data?.farmingExperience || 5
  } catch (err) {
    console.error('Failed to load farming methods', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMethods()
})

const saveMethods = async () => {
  saving.value = true
  errorMsg.value = ''
  successMsg.value = ''
  try {
    const payload = {
      ...farm.value,
      farmingMethod: farmingMethod.value,
      farmSize: farmSize.value,
      farmSizeUnit: farmSizeUnit.value,
      farmingExperience: farmingExperience.value
    }
    const res = await farmService.saveFarmProfile(payload)
    farm.value = res
    successMsg.value = 'Farming methods and operations updated successfully!'
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to save farming methods.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="farmer-methods-view py-4 bg-light min-vh-100">
    <div class="container-fluid px-lg-5">
      <div class="row g-4">
        <div class="col-lg-3 col-xl-2">
          <DashboardSidebar activeMenu="methods" />
        </div>

        <div class="col-lg-9 col-xl-10">
          <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
            <div>
              <h2 class="fw-bold text-dark mb-1">Farming Methods & Operational Scale</h2>
              <p class="text-muted small mb-0">Specify your cultivation techniques to attract buyers looking for organic, hydroponic, or greenhouse crops.</p>
            </div>
            <button class="btn btn-farmora btn-sm d-flex align-items-center gap-1 shadow-sm" @click="saveMethods" :disabled="saving">
              <span v-if="saving" class="spinner-border spinner-border-sm"></span>
              <i v-else class="bi bi-check2-circle"></i>
              <span>Save Farming Methods</span>
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

          <LoadingSpinner v-if="loading" message="Loading farming methods..." />

          <template v-else>
            <!-- Scale & Experience Configuration Card -->
            <div class="card border-0 shadow-sm p-4 bg-white rounded-3 mb-4">
              <h5 class="fw-bold text-dark mb-3"><i class="bi bi-rulers text-success me-2"></i>Operational Scale & Track Record</h5>
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Cultivated Farm Area</label>
                  <div class="input-group">
                    <input
                      type="number"
                      step="0.1"
                      min="0.1"
                      class="form-control"
                      v-model.number="farmSize"
                      placeholder="e.g. 3.5"
                    />
                    <select class="form-select" style="max-width: 120px;" v-model="farmSizeUnit">
                      <option value="Acres">Acres</option>
                      <option value="Hectares">Hectares</option>
                      <option value="Perches">Perches</option>
                    </select>
                  </div>
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Farming Experience (Years in Agriculture)</label>
                  <input
                    type="number"
                    min="0"
                    max="80"
                    class="form-control"
                    v-model.number="farmingExperience"
                    placeholder="e.g. 8"
                  />
                  <div class="form-text" style="font-size: 0.75rem;">Contributes up to 25 points towards your Farm Credibility score.</div>
                </div>
              </div>
            </div>

            <!-- Methods Selection Cards -->
            <div class="mb-3">
              <h5 class="fw-bold text-dark mb-3"><i class="bi bi-flower1 text-success me-2"></i>Select Primary Cultivation Methodology</h5>
              <div class="row g-3">
                <div v-for="m in methodsList" :key="m.key" class="col-md-6 col-xl-4">
                  <div
                    class="card h-100 p-3 rounded-3 cursor-pointer transition border"
                    :class="farmingMethod === m.key ? 'border-success shadow bg-success-subtle bg-opacity-10' : 'border-light-subtle shadow-sm bg-white'"
                    @click="farmingMethod = m.key"
                  >
                    <div class="d-flex justify-content-between align-items-start mb-2">
                      <div class="d-flex align-items-center gap-2">
                        <div class="rounded-circle p-2 bg-success text-white d-flex align-items-center justify-content-center" style="width: 38px; height: 38px;">
                          <i :class="m.icon" class="fs-5"></i>
                        </div>
                        <h6 class="fw-bold text-dark mb-0">{{ m.title }}</h6>
                      </div>
                      <input
                        type="radio"
                        class="form-check-input"
                        name="methodRadio"
                        :value="m.key"
                        v-model="farmingMethod"
                      />
                    </div>

                    <p class="text-secondary small mb-3 flex-grow-1" style="font-size: 0.8rem;">
                      {{ m.desc }}
                    </p>

                    <div class="border-top pt-2">
                      <div v-for="(trait, tIdx) in m.traits" :key="tIdx" class="small text-muted d-flex align-items-center gap-1 mb-1" style="font-size: 0.75rem;">
                        <i class="bi bi-check2 text-success"></i>
                        <span>{{ trait }}</span>
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

<style scoped>
.cursor-pointer {
  cursor: pointer;
}
.transition {
  transition: all 0.2s ease-in-out;
}
.transition:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(45, 106, 79, 0.15) !important;
}
</style>

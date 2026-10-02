<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { SRI_LANKA_REGIONS } from '../../utils/sriLankaRegions'

const router = useRouter()
const authStore = useAuthStore()

const allDistricts = Object.values(SRI_LANKA_REGIONS).flat()

const form = ref({
  fullName: '',
  phone: '',
  email: '',
  password: '',
  farmName: '',
  district: 'Colombo',
  role: 'FARMER',
  buyerType: 'INDIVIDUAL',
  businessName: '',
  businessRegNumber: ''
})

const error = ref('')
const loading = ref(false)

const handleRegister = async () => {
  error.value = ''
  loading.value = true
  try {
    const payload = {
      fullName: form.value.fullName.trim(),
      phone: form.value.phone.trim(),
      password: form.value.password,
      role: form.value.role,
      district: form.value.district,
      address: form.value.district
    }

    if (form.value.email && form.value.email.trim()) {
      payload.email = form.value.email.trim().toLowerCase()
    }

    if (form.value.role === 'FARMER') {
      payload.farmName = form.value.farmName ? form.value.farmName.trim() : (form.value.fullName + "'s Farm")
    } else if (form.value.role === 'BUYER') {
      payload.buyerType = form.value.buyerType
      if (form.value.buyerType !== 'INDIVIDUAL') {
        payload.businessName = form.value.businessName
        payload.businessRegNumber = form.value.businessRegNumber
      }
    }

    const res = await authStore.register(payload)
    const role = res.user.role
    switch (role) {
      case 'FARMER':
        router.push('/farmer/dashboard')
        break
      case 'BUYER':
        // Let buyers start browsing products immediately
        router.push('/products')
        break
      case 'DELIVERY_PARTNER':
        router.push('/delivery/dashboard')
        break
      default:
        router.push('/')
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Registration failed. Please check your inputs.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container py-5">
    <div class="row justify-content-center">
      <div class="col-md-8 col-lg-7">
        <div class="card shadow border-0 p-4 p-md-5 rounded-4">
          <div class="text-center mb-4">
            <div class="d-inline-flex p-3 rounded-circle bg-farmora-light text-farmora-green mb-2">
              <i class="bi bi-person-plus-fill fs-2"></i>
            </div>
            <h3 class="fw-bold mb-1">Join the Farmora Network</h3>
            <p class="text-muted small">
              {{ form.role === 'FARMER' ? 'Farmer Quick Sign-Up (Create account in < 2 minutes)' :
                 form.role === 'BUYER' ? 'Buyer Quick Sign-Up (Register in 1 minute & start browsing immediately)' :
                 'Logistics Partner Sign-Up' }}
            </p>
          </div>

          <div v-if="error" class="alert alert-danger py-2 small rounded-3" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ error }}
          </div>

          <form @submit.prevent="handleRegister">
            <!-- Role Selection Tabs -->
            <div class="mb-4">
              <label class="form-label small fw-semibold text-center d-block mb-2">Choose Your Account Role:</label>
              <div class="row g-2 text-center">
                <div class="col-4">
                  <input type="radio" class="btn-check" name="role" id="roleFarmer" value="FARMER" v-model="form.role">
                  <label class="btn btn-outline-success w-100 py-2 d-flex flex-column align-items-center rounded-3" for="roleFarmer">
                    <i class="bi bi-tree fs-5 mb-1"></i>
                    <span class="small fw-bold">Farmer</span>
                    <span class="badge bg-success-subtle text-success mt-1" style="font-size: 0.65rem;">6 Fields</span>
                  </label>
                </div>
                <div class="col-4">
                  <input type="radio" class="btn-check" name="role" id="roleBuyer" value="BUYER" v-model="form.role">
                  <label class="btn btn-outline-success w-100 py-2 d-flex flex-column align-items-center rounded-3" for="roleBuyer">
                    <i class="bi bi-cart3 fs-5 mb-1"></i>
                    <span class="small fw-bold">Buyer</span>
                    <span class="badge bg-primary-subtle text-primary mt-1" style="font-size: 0.65rem;">5 Fields</span>
                  </label>
                </div>
                <div class="col-4">
                  <input type="radio" class="btn-check" name="role" id="roleDelivery" value="DELIVERY_PARTNER" v-model="form.role">
                  <label class="btn btn-outline-success w-100 py-2 d-flex flex-column align-items-center rounded-3" for="roleDelivery">
                    <i class="bi bi-truck fs-5 mb-1"></i>
                    <span class="small fw-bold">Partner</span>
                    <span class="badge bg-secondary-subtle text-secondary mt-1" style="font-size: 0.65rem;">Logistics</span>
                  </label>
                </div>
              </div>
            </div>

            <!-- ============================================================
                 FARMER QUICK SIGN-UP (ONLY 6 ESSENTIAL FIELDS)
                 ============================================================ -->
            <template v-if="form.role === 'FARMER'">
              <div class="p-3 bg-light rounded-3 border mb-4">
                <div class="d-flex align-items-center gap-2 mb-3">
                  <span class="badge bg-success">Quick Sign-Up</span>
                  <span class="small text-muted fw-semibold">Only 6 essential fields to get started</span>
                </div>

                <!-- 1. Full Name -->
                <div class="mb-3">
                  <label class="form-label small fw-bold">1. Full Name <span class="text-danger">*</span></label>
                  <input
                    type="text"
                    v-model="form.fullName"
                    class="form-control"
                    placeholder="e.g. Sunil Perera"
                    required
                  />
                </div>

                <!-- 2. Mobile Number -->
                <div class="mb-3">
                  <label class="form-label small fw-bold">2. Mobile Number <span class="text-danger">*</span></label>
                  <div class="input-group">
                    <span class="input-group-text bg-white"><i class="bi bi-telephone text-muted"></i></span>
                    <input
                      type="tel"
                      v-model="form.phone"
                      class="form-control"
                      placeholder="e.g. 077 123 4567"
                      required
                    />
                  </div>
                  <div class="text-muted" style="font-size: 0.72rem;">You can use your mobile number to sign in directly.</div>
                </div>

                <!-- 3. Email (Optional) -->
                <div class="mb-3">
                  <label class="form-label small fw-bold">
                    3. Email <span class="badge bg-secondary-subtle text-secondary fw-normal">Optional</span>
                  </label>
                  <div class="input-group">
                    <span class="input-group-text bg-white"><i class="bi bi-envelope text-muted"></i></span>
                    <input
                      type="email"
                      v-model="form.email"
                      class="form-control"
                      placeholder="e.g. sunil@example.com (optional)"
                    />
                  </div>
                </div>

                <!-- 4. Password -->
                <div class="mb-3">
                  <label class="form-label small fw-bold">4. Password <span class="text-danger">*</span></label>
                  <div class="input-group">
                    <span class="input-group-text bg-white"><i class="bi bi-lock text-muted"></i></span>
                    <input
                      type="password"
                      v-model="form.password"
                      class="form-control"
                      placeholder="Min. 6 characters"
                      minlength="6"
                      required
                    />
                  </div>
                </div>

                <!-- 5. Farm Name -->
                <div class="mb-3">
                  <label class="form-label small fw-bold">5. Farm Name <span class="text-danger">*</span></label>
                  <div class="input-group">
                    <span class="input-group-text bg-white"><i class="bi bi-house-door text-muted"></i></span>
                    <input
                      type="text"
                      v-model="form.farmName"
                      class="form-control"
                      placeholder="e.g. Green Valley Farm"
                      required
                    />
                  </div>
                </div>

                <!-- 6. District -->
                <div class="mb-2">
                  <label class="form-label small fw-bold">6. District <span class="text-danger">*</span></label>
                  <select v-model="form.district" class="form-select" required>
                    <option v-for="d in allDistricts" :key="d" :value="d">{{ d }}</option>
                  </select>
                </div>
              </div>
            </template>

            <!-- ============================================================
                 BUYER QUICK SIGN-UP (ONLY 5 ESSENTIAL FIELDS)
                 ============================================================ -->
            <template v-else-if="form.role === 'BUYER'">
              <!-- Buyer Category Chips -->
              <div class="card bg-light border-0 p-3 mb-3 rounded-3">
                <label class="form-label small fw-bold text-dark mb-2">
                  <i class="bi bi-building me-1 text-success"></i>Buyer Account Category
                </label>
                <div class="row g-2">
                  <div class="col-6 col-md-3">
                    <input type="radio" class="btn-check" name="buyerType" id="btIndividual" value="INDIVIDUAL" v-model="form.buyerType">
                    <label class="btn btn-outline-dark btn-sm w-100 py-2 d-flex flex-column align-items-center text-center" for="btIndividual">
                      <i class="bi bi-person fs-6 mb-1"></i>
                      <span style="font-size: 0.75rem;" class="fw-semibold">Individual</span>
                    </label>
                  </div>
                  <div class="col-6 col-md-3">
                    <input type="radio" class="btn-check" name="buyerType" id="btRestaurant" value="RESTAURANT" v-model="form.buyerType">
                    <label class="btn btn-outline-dark btn-sm w-100 py-2 d-flex flex-column align-items-center text-center" for="btRestaurant">
                      <i class="bi bi-cup-hot fs-6 mb-1"></i>
                      <span style="font-size: 0.75rem;" class="fw-semibold">Restaurant</span>
                    </label>
                  </div>
                  <div class="col-6 col-md-3">
                    <input type="radio" class="btn-check" name="buyerType" id="btSupermarket" value="SUPERMARKET" v-model="form.buyerType">
                    <label class="btn btn-outline-dark btn-sm w-100 py-2 d-flex flex-column align-items-center text-center" for="btSupermarket">
                      <i class="bi bi-shop fs-6 mb-1"></i>
                      <span style="font-size: 0.75rem;" class="fw-semibold">Supermarket</span>
                    </label>
                  </div>
                  <div class="col-6 col-md-3">
                    <input type="radio" class="btn-check" name="buyerType" id="btExporter" value="EXPORTER" v-model="form.buyerType">
                    <label class="btn btn-outline-dark btn-sm w-100 py-2 d-flex flex-column align-items-center text-center" for="btExporter">
                      <i class="bi bi-globe fs-6 mb-1"></i>
                      <span style="font-size: 0.75rem;" class="fw-semibold">Exporter</span>
                    </label>
                  </div>
                </div>

                <div v-if="form.buyerType !== 'INDIVIDUAL'" class="mt-3 pt-3 border-top">
                  <div class="row g-2">
                    <div class="col-md-7">
                      <label class="form-label small fw-semibold">Business / Company Name <span class="text-danger">*</span></label>
                      <input
                        type="text"
                        v-model="form.businessName"
                        class="form-control form-control-sm"
                        placeholder="e.g. Ceylon Gourmet Kitchens"
                        :required="form.buyerType !== 'INDIVIDUAL'"
                      />
                    </div>
                    <div class="col-md-5">
                      <label class="form-label small fw-semibold">BR / Tax No.</label>
                      <input
                        type="text"
                        v-model="form.businessRegNumber"
                        class="form-control form-control-sm"
                        placeholder="e.g. PV-89211"
                      />
                    </div>
                  </div>
                </div>
              </div>

              <!-- 5 Essential Buyer Fields -->
              <div class="p-3 bg-light rounded-3 border mb-4">
                <div class="d-flex align-items-center gap-2 mb-3">
                  <span class="badge bg-primary">Quick Sign-Up</span>
                  <span class="small text-muted fw-semibold">Only 5 essential fields to start purchasing</span>
                </div>

                <!-- 1. Full Name -->
                <div class="mb-3">
                  <label class="form-label small fw-bold">
                    1. {{ form.buyerType !== 'INDIVIDUAL' ? 'Primary Contact Person' : 'Full Name' }} <span class="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    v-model="form.fullName"
                    class="form-control"
                    placeholder="e.g. Anura Wickramasinghe"
                    required
                  />
                </div>

                <!-- 2. Mobile Number -->
                <div class="mb-3">
                  <label class="form-label small fw-bold">2. Mobile Number <span class="text-danger">*</span></label>
                  <div class="input-group">
                    <span class="input-group-text bg-white"><i class="bi bi-telephone text-muted"></i></span>
                    <input
                      type="tel"
                      v-model="form.phone"
                      class="form-control"
                      placeholder="e.g. 071 987 6543"
                      required
                    />
                  </div>
                </div>

                <!-- 3. Email (Optional) -->
                <div class="mb-3">
                  <label class="form-label small fw-bold">
                    3. Email <span class="badge bg-secondary-subtle text-secondary fw-normal">Optional</span>
                  </label>
                  <div class="input-group">
                    <span class="input-group-text bg-white"><i class="bi bi-envelope text-muted"></i></span>
                    <input
                      type="email"
                      v-model="form.email"
                      class="form-control"
                      placeholder="e.g. anura@example.com (optional)"
                    />
                  </div>
                </div>

                <!-- 4. Password -->
                <div class="mb-3">
                  <label class="form-label small fw-bold">4. Password <span class="text-danger">*</span></label>
                  <div class="input-group">
                    <span class="input-group-text bg-white"><i class="bi bi-lock text-muted"></i></span>
                    <input
                      type="password"
                      v-model="form.password"
                      class="form-control"
                      placeholder="Min. 6 characters"
                      minlength="6"
                      required
                    />
                  </div>
                </div>

                <!-- 5. District -->
                <div class="mb-2">
                  <label class="form-label small fw-bold">5. Delivery District <span class="text-danger">*</span></label>
                  <select v-model="form.district" class="form-select" required>
                    <option v-for="d in allDistricts" :key="d" :value="d">{{ d }}</option>
                  </select>
                </div>
              </div>
            </template>

            <!-- ============================================================
                 DELIVERY PARTNER SIGN-UP
                 ============================================================ -->
            <template v-else>
              <div class="p-3 bg-light rounded-3 border mb-4">
                <div class="mb-3">
                  <label class="form-label small fw-bold">Full Name <span class="text-danger">*</span></label>
                  <input type="text" v-model="form.fullName" class="form-control" required />
                </div>
                <div class="mb-3">
                  <label class="form-label small fw-bold">Mobile Number <span class="text-danger">*</span></label>
                  <input type="tel" v-model="form.phone" class="form-control" required />
                </div>
                <div class="mb-3">
                  <label class="form-label small fw-bold">Email (Optional)</label>
                  <input type="email" v-model="form.email" class="form-control" />
                </div>
                <div class="mb-3">
                  <label class="form-label small fw-bold">Password <span class="text-danger">*</span></label>
                  <input type="password" v-model="form.password" class="form-control" minlength="6" required />
                </div>
                <div class="mb-2">
                  <label class="form-label small fw-bold">Operating District <span class="text-danger">*</span></label>
                  <select v-model="form.district" class="form-select" required>
                    <option v-for="d in allDistricts" :key="d" :value="d">{{ d }}</option>
                  </select>
                </div>
              </div>
            </template>

            <button
              type="submit"
              class="btn btn-farmora w-100 py-2-5 fw-bold fs-6 rounded-pill shadow-sm"
              :disabled="loading"
            >
              <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
              {{ form.role === 'FARMER' ? 'Create Farmer Account (< 2 Mins)' :
                 form.role === 'BUYER' ? 'Create Buyer Account & Start Browsing' :
                 'Register Delivery Partner' }}
            </button>
          </form>

          <div class="text-center mt-4 pt-3 border-top small text-muted">
            Already have an account?
            <router-link to="/login" class="text-success fw-bold text-decoration-none ms-1">
              Sign In
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

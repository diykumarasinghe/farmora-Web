<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  fullName: '',
  email: '',
  phone: '',
  password: '',
  role: 'FARMER',
  address: '',
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
    const payload = { ...form.value }
    if (payload.role !== 'BUYER') {
      delete payload.buyerType
      delete payload.businessName
      delete payload.businessRegNumber
    }
    const res = await authStore.register(payload)
    const role = res.user.role
    switch (role) {
      case 'FARMER':
        router.push('/farmer/dashboard')
        break
      case 'BUYER':
        router.push('/buyer/dashboard')
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
        <div class="card shadow border-0 p-4 p-md-5">
          <div class="text-center mb-4">
            <div class="d-inline-flex p-3 rounded-circle bg-farmora-light text-farmora-green mb-2">
              <i class="bi bi-person-plus-fill fs-2"></i>
            </div>
            <h3 class="fw-bold">Join the Farmora Network</h3>
            <p class="text-muted small">Select your role and start trading or delivering</p>
          </div>

          <div v-if="error" class="alert alert-danger py-2 small" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ error }}
          </div>

          <form @submit.prevent="handleRegister">
            <!-- Role Selection Tabs -->
            <div class="mb-4">
              <label class="form-label small fw-semibold text-center d-block mb-2">I am registering as a:</label>
              <div class="row g-2 text-center">
                <div class="col-4">
                  <input type="radio" class="btn-check" name="role" id="roleFarmer" value="FARMER" v-model="form.role">
                  <label class="btn btn-outline-success w-100 py-2 d-flex flex-column align-items-center" for="roleFarmer">
                    <i class="bi bi-tree fs-5 mb-1"></i>
                    <span class="small fw-bold">Farmer</span>
                  </label>
                </div>
                <div class="col-4">
                  <input type="radio" class="btn-check" name="role" id="roleBuyer" value="BUYER" v-model="form.role">
                  <label class="btn btn-outline-success w-100 py-2 d-flex flex-column align-items-center" for="roleBuyer">
                    <i class="bi bi-cart3 fs-5 mb-1"></i>
                    <span class="small fw-bold">Buyer</span>
                  </label>
                </div>
                <div class="col-4">
                  <input type="radio" class="btn-check" name="role" id="roleDelivery" value="DELIVERY_PARTNER" v-model="form.role">
                  <label class="btn btn-outline-success w-100 py-2 d-flex flex-column align-items-center" for="roleDelivery">
                    <i class="bi bi-truck fs-5 mb-1"></i>
                    <span class="small fw-bold">Partner</span>
                  </label>
                </div>
              </div>
            </div>

            <!-- Buyer Account Type Selector (Only when Role is BUYER) -->
            <div v-if="form.role === 'BUYER'" class="card bg-light border-0 p-3 mb-4">
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

              <!-- Commercial Business Info (For Restaurant, Supermarket, Exporter) -->
              <div v-if="form.buyerType !== 'INDIVIDUAL'" class="mt-3 pt-3 border-top">
                <div class="row g-2">
                  <div class="col-md-7">
                    <label class="form-label small fw-semibold">Business / Organization Name <span class="text-danger">*</span></label>
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

            <div class="mb-3">
              <label class="form-label small fw-semibold">
                {{ form.role === 'BUYER' && form.buyerType !== 'INDIVIDUAL' ? 'Primary Contact Person' : 'Full Name' }}
              </label>
              <input
                type="text"
                v-model="form.fullName"
                class="form-control"
                placeholder="e.g. Sunil Perera"
                required
              />
            </div>

            <div class="row g-3 mb-3">
              <div class="col-md-6">
                <label class="form-label small fw-semibold">Email</label>
                <input
                  type="email"
                  v-model="form.email"
                  class="form-control"
                  placeholder="sunil@example.com"
                  required
                />
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">Phone</label>
                <input
                  type="text"
                  v-model="form.phone"
                  class="form-control"
                  placeholder="0771234567"
                />
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label small fw-semibold">
                {{ form.role === 'FARMER' ? 'Farm Address / Region' : 'Delivery / Business Address' }}
              </label>
              <input
                type="text"
                v-model="form.address"
                class="form-control"
                placeholder="e.g. Dambulla Road, Kurunegala"
              />
            </div>

            <div class="mb-4">
              <label class="form-label small fw-semibold">Password (min. 6 characters)</label>
              <input
                type="password"
                v-model="form.password"
                class="form-control"
                placeholder="••••••••"
                minlength="6"
                required
              />
            </div>

            <button
              type="submit"
              class="btn btn-farmora w-100 py-2 fw-bold"
              :disabled="loading"
            >
              <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
              Create Account
            </button>
          </form>

          <div class="text-center mt-4 pt-3 border-top small text-muted">
            Already have an account?
            <router-link to="/login" class="text-success fw-bold text-decoration-none">
              Sign In
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

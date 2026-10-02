<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { productService } from '../../services/productService'
import { AGRICULTURAL_CATEGORIES, SAMPLE_PRODUCTS } from '../../utils/agriculturalData'
import { SRI_LANKA_REGIONS } from '../../utils/sriLankaRegions'

const route = useRoute()
const router = useRouter()

const categories = AGRICULTURAL_CATEGORIES.map(c => c.name)
const allDistricts = Object.values(SRI_LANKA_REGIONS).flat()
const units = ['kg', 'ton', 'bag', 'crate', 'bunch', 'piece', 'g']
const statuses = ['AVAILABLE', 'SOLD_OUT', 'ARCHIVED']

const form = ref({
  name: '',
  description: '',
  category: 'Vegetables',
  price: '',
  wholesalePrice: '',
  quantity: '',
  unit: 'kg',
  location: '',
  harvestDate: '',
  imageUrl: '',
  videoUrl: '',
  status: 'AVAILABLE'
})

const loading = ref(true)
const saving = ref(false)
const archiving = ref(false)
const error = ref('')

onMounted(async () => {
  const id = route.params.id
  try {
    const data = await productService.getProductById(id)
    form.value = {
      name: data.name,
      description: data.description || '',
      category: data.category,
      price: data.price,
      wholesalePrice: data.wholesalePrice || '',
      quantity: data.quantity,
      unit: data.unit,
      location: data.location || '',
      harvestDate: data.harvestDate || '',
      imageUrl: data.imageUrl || '',
      videoUrl: data.videoUrl || '',
      status: data.status || 'AVAILABLE'
    }
  } catch (err) {
    const sample = SAMPLE_PRODUCTS.find(p => p.id === Number(id))
    if (sample) {
      form.value = {
        name: sample.name,
        description: sample.description || '',
        category: sample.category,
        price: sample.price,
        wholesalePrice: sample.wholesalePrice || '',
        quantity: sample.quantity,
        unit: sample.unit,
        location: sample.location || '',
        harvestDate: sample.harvestDate || '',
        imageUrl: sample.imageUrl || '',
        videoUrl: '',
        status: sample.status || 'AVAILABLE'
      }
    } else {
      error.value = 'Failed to load produce listing for editing.'
    }
  } finally {
    loading.value = false
  }
})

const handleSubmit = async () => {
  error.value = ''
  saving.value = true
  try {
    const id = route.params.id
    const payload = {
      name: form.value.name,
      description: form.value.description,
      category: form.value.category,
      price: parseFloat(form.value.price),
      quantity: parseFloat(form.value.quantity),
      unit: form.value.unit,
      location: form.value.location,
      harvestDate: form.value.harvestDate,
      imageUrl: form.value.imageUrl
    }
    await productService.updateProduct(id, payload)
    
    // Update status if modified
    if (form.value.status) {
      try {
        await productService.updateProductStatus(id, form.value.status)
      } catch (e) {
        console.warn('Status update fallback', e)
      }
    }

    router.push('/farmer/my-products')
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to update produce listing.'
  } finally {
    saving.value = false
  }
}

const handleArchive = async () => {
  if (confirm('Are you sure you want to archive this harvest listing?')) {
    archiving.value = true
    try {
      const id = route.params.id
      await productService.archiveProduct(id)
      router.push('/farmer/my-products')
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to archive produce')
    } finally {
      archiving.value = false
    }
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="my-products" />
      </div>

      <div class="col-lg-9">
        <div class="card border-0 shadow-sm p-4 rounded-4 bg-white">
          <div class="border-bottom pb-3 mb-4 d-flex justify-content-between align-items-center">
            <div>
              <h4 class="fw-bold mb-1 d-flex align-items-center gap-2">
                <i class="bi bi-pencil-square text-farmora-green"></i>
                <span>Edit Produce Listing</span>
              </h4>
              <p class="text-muted small mb-0">Update pricing, inventory, availability status, or archive older crops.</p>
            </div>

            <button
              type="button"
              class="btn btn-outline-danger btn-sm d-flex align-items-center gap-1"
              @click="handleArchive"
              :disabled="archiving"
            >
              <i class="bi bi-archive-fill"></i>
              <span>Archive Produce</span>
            </button>
          </div>

          <LoadingSpinner v-if="loading" message="Loading produce information..." />

          <template v-else>
            <div v-if="error" class="alert alert-danger py-2 small rounded-3" role="alert">
              {{ error }}
            </div>

            <form @submit.prevent="handleSubmit">
              <div class="row g-3 mb-3">
                <div class="col-md-8">
                  <label class="form-label small fw-bold">Produce Name</label>
                  <input
                    type="text"
                    v-model="form.name"
                    class="form-control"
                    required
                  />
                </div>
                <div class="col-md-4">
                  <label class="form-label small fw-bold">Agricultural Category</label>
                  <select v-model="form.category" class="form-select" required>
                    <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
                  </select>
                </div>
              </div>

              <div class="row g-3 mb-3">
                <div class="col-md-3">
                  <label class="form-label small fw-bold">Retail Price (Rs.)</label>
                  <div class="input-group">
                    <span class="input-group-text bg-light text-muted">Rs.</span>
                    <input
                      type="number"
                      step="0.01"
                      min="0.01"
                      v-model="form.price"
                      class="form-control"
                      required
                    />
                  </div>
                </div>

                <div class="col-md-3">
                  <label class="form-label small fw-bold">Wholesale Price (Rs.)</label>
                  <div class="input-group">
                    <span class="input-group-text bg-light text-muted">Rs.</span>
                    <input
                      type="number"
                      step="0.01"
                      min="0.01"
                      v-model="form.wholesalePrice"
                      class="form-control"
                    />
                  </div>
                </div>

                <div class="col-md-3">
                  <label class="form-label small fw-bold">Quantity</label>
                  <input
                    type="number"
                    step="0.1"
                    min="0"
                    v-model="form.quantity"
                    class="form-control"
                    required
                  />
                </div>

                <div class="col-md-3">
                  <label class="form-label small fw-bold">Unit</label>
                  <select v-model="form.unit" class="form-select" required>
                    <option v-for="u in units" :key="u" :value="u">{{ u }}</option>
                  </select>
                </div>
              </div>

              <div class="row g-3 mb-3">
                <div class="col-md-4">
                  <label class="form-label small fw-bold">Location / District</label>
                  <select v-model="form.location" class="form-select">
                    <option v-for="d in allDistricts" :key="d" :value="d">{{ d }}</option>
                  </select>
                </div>

                <div class="col-md-4">
                  <label class="form-label small fw-bold">Listing Status</label>
                  <select v-model="form.status" class="form-select">
                    <option v-for="s in statuses" :key="s" :value="s">{{ s }}</option>
                  </select>
                </div>

                <div class="col-md-4">
                  <label class="form-label small fw-bold">Harvest Date</label>
                  <input
                    type="date"
                    v-model="form.harvestDate"
                    class="form-control"
                  />
                </div>
              </div>

              <div class="row g-3 mb-3">
                <div class="col-md-6">
                  <label class="form-label small fw-bold">Produce Photo URL</label>
                  <input
                    type="url"
                    v-model="form.imageUrl"
                    class="form-control"
                  />
                </div>

                <div class="col-md-6">
                  <label class="form-label small fw-bold">Harvest Video URL</label>
                  <input
                    type="url"
                    v-model="form.videoUrl"
                    class="form-control"
                  />
                </div>
              </div>

              <div class="mb-4">
                <label class="form-label small fw-bold">Description & Quality Notes</label>
                <textarea
                  v-model="form.description"
                  class="form-control"
                  rows="4"
                ></textarea>
              </div>

              <div class="d-flex justify-content-end gap-2 border-top pt-3">
                <router-link to="/farmer/my-products" class="btn btn-outline-secondary px-3">
                  Cancel
                </router-link>
                <button
                  type="submit"
                  class="btn btn-farmora px-4 fw-bold"
                  :disabled="saving"
                >
                  <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
                  Save Changes
                </button>
              </div>
            </form>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-farmora-green {
  color: #2d6a4f !important;
}
</style>

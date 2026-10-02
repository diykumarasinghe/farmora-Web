<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import { productService } from '../../services/productService'
import { AGRICULTURAL_CATEGORIES } from '../../utils/agriculturalData'
import { SRI_LANKA_REGIONS } from '../../utils/sriLankaRegions'

const router = useRouter()

const categories = AGRICULTURAL_CATEGORIES.map(c => c.name)
const allDistricts = Object.values(SRI_LANKA_REGIONS).flat()
const units = ['kg', 'ton', 'bag', 'crate', 'bunch', 'piece', 'g']
const farmingMethods = ['Organic', 'Hydroponic', 'Traditional', 'Greenhouse', 'Conventional']

const form = ref({
  name: '',
  description: '',
  category: 'Vegetables',
  price: '',
  wholesalePrice: '',
  quantity: '',
  unit: 'kg',
  location: 'Nuwara Eliya',
  district: 'Nuwara Eliya',
  farmingMethod: 'Organic',
  harvestDate: new Date().toISOString().split('T')[0],
  imageUrl: '',
  videoUrl: '',
  qualityGrade: 'Grade A',
  packagingInfo: 'Eco Crates',
  minOrderQuantity: 1,
  bulkOrderAvailable: true,
  isFreshToday: true
})

const loading = ref(false)
const error = ref('')

const handleSubmit = async () => {
  error.value = ''
  loading.value = true
  try {
    const payload = {
      name: form.value.name,
      description: form.value.description,
      category: form.value.category,
      price: parseFloat(form.value.price),
      wholesalePrice: form.value.wholesalePrice ? parseFloat(form.value.wholesalePrice) : null,
      quantity: parseFloat(form.value.quantity),
      unit: form.value.unit,
      location: form.value.location || form.value.district,
      harvestDate: form.value.harvestDate,
      imageUrl: form.value.imageUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=600&q=80',
      videoUrl: form.value.videoUrl || null,
      farmingMethod: form.value.farmingMethod,
      qualityGrade: form.value.qualityGrade,
      packagingInfo: form.value.packagingInfo,
      minOrderQuantity: form.value.minOrderQuantity ? parseFloat(form.value.minOrderQuantity) : 1,
      bulkOrderAvailable: form.value.bulkOrderAvailable
    }

    await productService.createProduct(payload)
    router.push('/farmer/my-products')
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to publish produce listing. Please verify inputs.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <!-- Left Dashboard Sidebar -->
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="add-product" />
      </div>

      <!-- Right Main Content Form -->
      <div class="col-lg-9">
        <div class="card border-0 shadow-sm p-4 rounded-4 bg-white">
          <div class="border-bottom pb-3 mb-4 d-flex justify-content-between align-items-center">
            <div>
              <h4 class="fw-bold mb-1 d-flex align-items-center gap-2">
                <i class="bi bi-plus-circle-fill text-farmora-green"></i>
                <span>List New Harvest Produce</span>
              </h4>
              <p class="text-muted small mb-0">Publish your crops for buyers to discover, bid, and purchase across Sri Lanka.</p>
            </div>
            <router-link to="/farmer/my-products" class="btn btn-sm btn-outline-secondary">
              View Inventory
            </router-link>
          </div>

          <!-- Error Alert -->
          <div v-if="error" class="alert alert-danger py-2 small rounded-3" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ error }}
          </div>

          <form @submit.prevent="handleSubmit">
            <!-- Row 1: Name & Category -->
            <div class="row g-3 mb-3">
              <div class="col-md-8">
                <label class="form-label small fw-bold">Produce Name <span class="text-danger">*</span></label>
                <input
                  type="text"
                  v-model="form.name"
                  class="form-control"
                  placeholder="e.g. Organic Red Carrots, Nuwara Eliya Potatoes..."
                  required
                />
              </div>

              <div class="col-md-4">
                <label class="form-label small fw-bold">Agricultural Category <span class="text-danger">*</span></label>
                <select v-model="form.category" class="form-select" required>
                  <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
                </select>
              </div>
            </div>

            <!-- Row 2: Pricing & Quantity -->
            <div class="row g-3 mb-3">
              <div class="col-md-4">
                <label class="form-label small fw-bold">Retail Price (Rs. per unit) <span class="text-danger">*</span></label>
                <div class="input-group">
                  <span class="input-group-text bg-light text-muted">Rs.</span>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    v-model="form.price"
                    class="form-control"
                    placeholder="250.00"
                    required
                  />
                </div>
              </div>

              <div class="col-md-4">
                <label class="form-label small fw-bold">Wholesale Price (Optional)</label>
                <div class="input-group">
                  <span class="input-group-text bg-light text-muted">Rs.</span>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    v-model="form.wholesalePrice"
                    class="form-control"
                    placeholder="210.00"
                  />
                </div>
                <div class="form-text small" style="font-size: 0.72rem;">Discounted rate for bulk commercial orders (25kg+)</div>
              </div>

              <div class="col-md-2 col-sm-6">
                <label class="form-label small fw-bold">Quantity <span class="text-danger">*</span></label>
                <input
                  type="number"
                  step="0.1"
                  min="0.1"
                  v-model="form.quantity"
                  class="form-control"
                  placeholder="100"
                  required
                />
              </div>

              <div class="col-md-2 col-sm-6">
                <label class="form-label small fw-bold">Unit <span class="text-danger">*</span></label>
                <select v-model="form.unit" class="form-select" required>
                  <option v-for="u in units" :key="u" :value="u">{{ u }}</option>
                </select>
              </div>
            </div>

            <!-- Row 3: Location, Farming Method, Harvest Date -->
            <div class="row g-3 mb-3">
              <div class="col-md-4">
                <label class="form-label small fw-bold">Farm District / Location</label>
                <select v-model="form.district" class="form-select">
                  <option v-for="d in allDistricts" :key="d" :value="d">{{ d }}</option>
                </select>
              </div>

              <div class="col-md-4">
                <label class="form-label small fw-bold">Farming Method</label>
                <select v-model="form.farmingMethod" class="form-select">
                  <option v-for="m in farmingMethods" :key="m" :value="m">{{ m }}</option>
                </select>
              </div>

              <div class="col-md-4">
                <label class="form-label small fw-bold">Harvest Date</label>
                <input
                  type="date"
                  v-model="form.harvestDate"
                  class="form-control"
                />
                <div class="form-text text-success small fw-medium" style="font-size: 0.72rem;">
                  <i class="bi bi-patch-check-fill me-1"></i> Today's harvest displays the "Fresh Today" badge!
                </div>
              </div>
            </div>

            <!-- Row 4: Image URL & Video URL -->
            <div class="row g-3 mb-3">
              <div class="col-md-6">
                <label class="form-label small fw-bold">Produce Photo URL</label>
                <input
                  type="url"
                  v-model="form.imageUrl"
                  class="form-control"
                  placeholder="https://images.unsplash.com/... or cloud image URL"
                />
                <div class="form-text small" style="font-size: 0.72rem;">Leave blank to use a high-resolution verified stock image.</div>
              </div>

              <div class="col-md-6">
                <label class="form-label small fw-bold">Harvest Video URL (YouTube / MP4)</label>
                <input
                  type="url"
                  v-model="form.videoUrl"
                  class="form-control"
                  placeholder="https://www.youtube.com/embed/... (Optional)"
                />
                <div class="form-text small" style="font-size: 0.72rem;">Optional video demonstration of the produce harvest.</div>
              </div>
            </div>

            <!-- Row 5: Quality Grade, Packaging & Wholesale Ordering -->
            <div class="row g-3 mb-3">
              <div class="col-md-4">
                <label class="form-label small fw-bold">Quality Grade</label>
                <select v-model="form.qualityGrade" class="form-select">
                  <option value="Grade A">Grade A (Export Quality)</option>
                  <option value="Grade B">Grade B (Standard Market)</option>
                  <option value="Premium Organic">Premium Organic</option>
                  <option value="Commercial Processing">Commercial Processing</option>
                </select>
              </div>

              <div class="col-md-4">
                <label class="form-label small fw-bold">Packaging Information</label>
                <input
                  type="text"
                  v-model="form.packagingInfo"
                  class="form-control"
                  placeholder="e.g. Eco Wooden Crates, Jute Sacks..."
                />
              </div>

              <div class="col-md-2 col-sm-6">
                <label class="form-label small fw-bold">MOQ (Min Order)</label>
                <input
                  type="number"
                  min="1"
                  v-model="form.minOrderQuantity"
                  class="form-control"
                  placeholder="1"
                />
              </div>

              <div class="col-md-2 col-sm-6 d-flex align-items-center pt-3">
                <div class="form-check form-switch">
                  <input
                    class="form-check-input"
                    type="checkbox"
                    id="bulkOrderToggle"
                    v-model="form.bulkOrderAvailable"
                  />
                  <label class="form-check-label small fw-bold" for="bulkOrderToggle">Bulk Orders</label>
                </div>
              </div>
            </div>

            <!-- Row 6: Quality Notes & Description -->
            <div class="mb-4">
              <label class="form-label small fw-bold">Description & Quality Specifications</label>
              <textarea
                v-model="form.description"
                class="form-control"
                rows="4"
                placeholder="Mention cultivar details, fertilizer-free or organic certification, grade size, freshness guarantee..."
              ></textarea>
            </div>

            <!-- Form Actions -->
            <div class="d-flex justify-content-end gap-2 border-top pt-3">
              <router-link to="/farmer/my-products" class="btn btn-outline-secondary px-3">
                Cancel
              </router-link>
              <button
                type="submit"
                class="btn btn-farmora px-4 fw-bold"
                :disabled="loading"
              >
                <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
                Publish Produce to Marketplace
              </button>
            </div>
          </form>
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

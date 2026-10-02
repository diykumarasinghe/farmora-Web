<script setup>
import { ref, computed } from 'vue'
import { bulkOrderService } from '../../services/bulkOrderService'
import { useAuthStore } from '../../stores/auth'

const props = defineProps({
  show: Boolean,
  product: {
    type: Object,
    default: null
  },
  existingBulkOrder: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'bulkOrderCreated', 'bulkOrderUpdated'])
const authStore = useAuthStore()

const quantity = ref(props.existingBulkOrder?.quantity || 100)
const targetPrice = ref(props.existingBulkOrder?.targetPricePerUnit || props.product?.wholesalePrice || props.product?.price || 150)
const packagingType = ref(props.existingBulkOrder?.packagingType || 'Standard Wooden Crates')
const deliveryFrequency = ref(props.existingBulkOrder?.deliveryFrequency || 'One-time Wholesale Batch')
const requiredDate = ref(props.existingBulkOrder?.requiredDate || '')
const destinationAddress = ref(props.existingBulkOrder?.destinationAddress || authStore.user?.address || '')
const specialRequirements = ref(props.existingBulkOrder?.specialRequirements || '')

const submitting = ref(false)
const errorMsg = ref('')
const successOrder = ref(null)

const packagingOptions = [
  'Standard Wooden Crates',
  'Breathable Jute Bags',
  'Cold-Chain Temperature Controlled Boxes',
  'Industrial Heavy-Duty Pallets'
]

const frequencyOptions = [
  'One-time Wholesale Batch',
  'Weekly Scheduled Delivery (Contract)',
  'Bi-weekly Batch Supply',
  'Monthly Recurring Commercial Order'
]

const totalEstimated = computed(() => {
  const q = parseFloat(quantity.value) || 0
  const p = parseFloat(targetPrice.value) || 0
  return (q * p).toFixed(2)
})

const volumeDiscountPct = computed(() => {
  if (quantity.value >= 500) return 20
  if (quantity.value >= 250) return 15
  if (quantity.value >= 100) return 10
  return 5
})

const handleSubmit = async () => {
  if (!authStore.isAuthenticated) {
    alert('Please sign in as a Buyer to place bulk wholesale orders.')
    return
  }

  submitting.value = true
  errorMsg.value = ''

  try {
    if (props.existingBulkOrder) {
      // Modify quantity
      const res = await bulkOrderService.modifyQuantity(props.existingBulkOrder.id, {
        quantity: quantity.value,
        targetPricePerUnit: targetPrice.value,
        specialRequirements: specialRequirements.value
      })
      emit('bulkOrderUpdated', res)
      emit('close')
    } else {
      // Create new bulk order
      const res = await bulkOrderService.createBulkOrder({
        productId: props.product.id,
        quantity: quantity.value,
        targetPricePerUnit: targetPrice.value,
        packagingType: packagingType.value,
        deliveryFrequency: deliveryFrequency.value,
        requiredDate: requiredDate.value,
        destinationAddress: destinationAddress.value,
        specialRequirements: specialRequirements.value,
        fulfillmentType: 'DELIVERY'
      })
      successOrder.value = res
      emit('bulkOrderCreated', res)
    }
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to submit bulk order request.'
  } finally {
    submitting.value = false
  }
}

const closeModal = () => {
  successOrder.value = null
  errorMsg.value = ''
  emit('close')
}
</script>

<template>
  <div v-if="show" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.6); z-index: 1060;">
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
        <!-- Header -->
        <div class="modal-header bg-farmora-green text-white p-3 px-4">
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-box-seam-fill fs-5"></i>
            <h5 class="modal-title fw-bold mb-0">
              {{ existingBulkOrder ? 'Modify Bulk Order Quantity' : 'Wholesale Bulk Ordering Contract' }}
            </h5>
          </div>
          <button type="button" class="btn-close btn-close-white" @click="closeModal"></button>
        </div>

        <!-- Success view -->
        <div v-if="successOrder" class="modal-body p-4 text-center">
          <div class="mb-3 text-success">
            <i class="bi bi-check-circle-fill display-3"></i>
          </div>
          <h4 class="fw-bold text-dark mb-1">Bulk Order Request Dispatched!</h4>
          <p class="small text-muted mb-3">
            Contract request <strong>#{{ successOrder.bulkOrderNumber }}</strong> has been sent to farmer
            <strong>{{ product?.farmer?.fullName }}</strong> for commercial review and dispatch scheduling.
          </p>

          <div class="card bg-light border-0 p-3 rounded-3 mb-4 mx-auto text-start small" style="max-width: 440px;">
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Total Volume:</span>
              <strong class="text-dark">{{ successOrder.quantity }} {{ product?.unit || 'kg' }}</strong>
            </div>
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Estimated Contract Value:</span>
              <strong class="text-success fs-6">LKR {{ Number(successOrder.totalEstimatedAmount).toLocaleString() }}</strong>
            </div>
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Packaging:</span>
              <span class="text-dark">{{ successOrder.packagingType }}</span>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted">Frequency:</span>
              <span class="text-dark">{{ successOrder.deliveryFrequency }}</span>
            </div>
          </div>

          <button class="btn btn-farmora px-4 fw-bold" @click="closeModal">Done</button>
        </div>

        <!-- Form view -->
        <form v-else @submit.prevent="handleSubmit">
          <div class="modal-body p-4">
            <!-- Produce banner -->
            <div v-if="product" class="p-3 bg-light rounded-3 mb-3 d-flex align-items-center justify-content-between">
              <div>
                <h6 class="fw-bold text-dark mb-1">{{ product.name }}</h6>
                <div class="small text-muted">
                  Farmer: {{ product.farmer?.fullName }} &bull; Origin: {{ product.location || 'Local Farm' }}
                </div>
              </div>
              <div class="text-end">
                <span class="badge bg-danger text-white px-2 py-1 mb-1 d-inline-block">Tier Discount: {{ volumeDiscountPct }}% Off</span>
                <div class="small text-muted">Standard rate: LKR {{ product.price }}/{{ product.unit || 'kg' }}</div>
              </div>
            </div>

            <div v-if="errorMsg" class="alert alert-danger py-2 small mb-3">
              <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ errorMsg }}
            </div>

            <div class="row g-3">
              <!-- Quantity -->
              <div class="col-md-6">
                <label class="form-label small fw-bold text-dark">Wholesale Quantity (Min. 50 {{ product?.unit || 'kg' }})</label>
                <div class="input-group">
                  <input
                    type="number"
                    step="10"
                    min="1"
                    class="form-control"
                    v-model="quantity"
                    required
                  />
                  <span class="input-group-text bg-light text-muted">{{ product?.unit || 'kg' }}</span>
                </div>
                <!-- Quick volume chips -->
                <div class="d-flex gap-1 mt-1">
                  <button type="button" class="btn btn-xs btn-outline-secondary py-0 px-2" @click="quantity = 50">50kg</button>
                  <button type="button" class="btn btn-xs btn-outline-secondary py-0 px-2" @click="quantity = 100">100kg</button>
                  <button type="button" class="btn btn-xs btn-outline-secondary py-0 px-2" @click="quantity = 250">250kg</button>
                  <button type="button" class="btn btn-xs btn-outline-secondary py-0 px-2" @click="quantity = 500">500kg</button>
                  <button type="button" class="btn btn-xs btn-outline-secondary py-0 px-2" @click="quantity = 1000">1 Ton</button>
                </div>
              </div>

              <!-- Target Unit Price -->
              <div class="col-md-6">
                <label class="form-label small fw-bold text-dark">Target Wholesale Unit Price (LKR)</label>
                <div class="input-group">
                  <span class="input-group-text bg-light fw-bold text-muted">LKR</span>
                  <input
                    type="number"
                    step="1"
                    min="1"
                    class="form-control"
                    v-model="targetPrice"
                    required
                  />
                  <span class="input-group-text bg-light text-muted">/ {{ product?.unit || 'kg' }}</span>
                </div>
              </div>

              <!-- Packaging Type -->
              <div class="col-md-6" v-if="!existingBulkOrder">
                <label class="form-label small fw-bold text-dark">Packaging Preference</label>
                <select class="form-select form-select-sm" v-model="packagingType">
                  <option v-for="opt in packagingOptions" :key="opt" :value="opt">{{ opt }}</option>
                </select>
              </div>

              <!-- Delivery Frequency -->
              <div class="col-md-6" v-if="!existingBulkOrder">
                <label class="form-label small fw-bold text-dark">Supply Schedule / Frequency</label>
                <select class="form-select form-select-sm" v-model="deliveryFrequency">
                  <option v-for="freq in frequencyOptions" :key="freq" :value="freq">{{ freq }}</option>
                </select>
              </div>

              <!-- Required Date & Destination -->
              <div class="col-md-6" v-if="!existingBulkOrder">
                <label class="form-label small fw-bold text-dark">Target Delivery Date</label>
                <input type="date" class="form-control form-control-sm" v-model="requiredDate" />
              </div>

              <div class="col-md-6" v-if="!existingBulkOrder">
                <label class="form-label small fw-bold text-dark">Commercial Destination / Warehouse</label>
                <input type="text" class="form-control form-control-sm" v-model="destinationAddress" placeholder="Warehouse address, Manning Market, etc." />
              </div>

              <!-- Special Requirements -->
              <div class="col-12">
                <label class="form-label small fw-bold text-dark">Quality Standards / Handling Specs</label>
                <textarea
                  class="form-control"
                  rows="2"
                  placeholder="e.g. Export-grade ripeness, uniform sizing, refrigeration required during transit..."
                  v-model="specialRequirements"
                ></textarea>
              </div>

              <!-- Total Contract Summary Box -->
              <div class="col-12">
                <div class="p-3 bg-light rounded-3 d-flex justify-content-between align-items-center">
                  <div>
                    <div class="small text-muted">Total Estimated Contract Valuation:</div>
                    <div class="fs-4 fw-bold text-farmora-green">
                      LKR {{ Number(totalEstimated).toLocaleString() }}
                    </div>
                  </div>
                  <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-2 rounded-pill fw-semibold">
                    Wholesale B2B Guaranteed
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer bg-light px-4 py-3">
            <button type="button" class="btn btn-outline-secondary btn-sm" @click="closeModal">Cancel</button>
            <button
              type="submit"
              class="btn btn-farmora btn-sm px-4 fw-bold"
              :disabled="submitting"
            >
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              {{ existingBulkOrder ? 'Save Modified Quantity' : 'Submit Wholesale Bulk Order' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bg-farmora-green {
  background-color: #2d6a4f !important;
}
.text-farmora-green {
  color: #2d6a4f !important;
}
</style>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { orderService } from '../../services/orderService'
import { useAuthStore } from '../../stores/auth'

const props = defineProps({
  show: Boolean,
  product: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'orderCreated'])
const router = useRouter()
const authStore = useAuthStore()

const quantity = ref(1)
const fulfillmentType = ref('DELIVERY') // DELIVERY or PICKUP
const deliveryAddress = ref(authStore.user?.address || '')
const paymentMethod = ref('CARD') // CARD, BANK_TRANSFER, CASH_ON_DELIVERY, WALLET
const notes = ref('')

const submitting = ref(false)
const errorMessage = ref('')
const orderSuccess = ref(null)

const unitPrice = computed(() => {
  if (!props.product) return 0
  if (props.product.wholesalePrice && quantity.value >= 25) {
    return Number(props.product.wholesalePrice)
  }
  return Number(props.product.price)
})

const deliveryFee = computed(() => {
  return fulfillmentType.value === 'DELIVERY' ? 350 : 0
})

const subtotal = computed(() => {
  return unitPrice.value * (Number(quantity.value) || 0)
})

const grandTotal = computed(() => {
  return subtotal.value + deliveryFee.value
})

const handleBuyNow = async () => {
  if (!authStore.isAuthenticated) {
    router.push({ path: '/login' })
    return
  }
  if (authStore.role !== 'BUYER') {
    errorMessage.value = 'Only registered BUYERS can purchase produce.'
    return
  }
  if (quantity.value > props.product.quantity) {
    errorMessage.value = `Requested quantity exceeds available stock (${props.product.quantity} ${props.product.unit}).`
    return
  }

  errorMessage.value = ''
  submitting.value = true
  try {
    const res = await orderService.createDirectOrder({
      productId: props.product.id,
      quantity: quantity.value,
      unitPrice: unitPrice.value,
      fulfillmentType: fulfillmentType.value,
      deliveryAddress: fulfillmentType.value === 'DELIVERY' ? deliveryAddress.value : null,
      pickupAddress: fulfillmentType.value === 'PICKUP' ? (props.product.location || 'Farm Location') : null,
      paymentMethod: paymentMethod.value,
      notes: notes.value
    })
    orderSuccess.value = res
    emit('orderCreated', res)
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Failed to place order. Please try again.'
  } finally {
    submitting.value = false
  }
}

const closeModal = () => {
  orderSuccess.value = null
  errorMessage.value = ''
  emit('close')
}

const goToOrders = () => {
  closeModal()
  router.push('/buyer/orders')
}
</script>

<template>
  <div v-if="show" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.6); z-index: 1060;">
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
        <!-- Header -->
        <div class="modal-header bg-farmora-green text-white p-3 px-4">
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-bag-check-fill fs-5"></i>
            <h5 class="modal-title fw-bold mb-0">Direct Farm Purchase & Checkout</h5>
          </div>
          <button type="button" class="btn-close btn-close-white" @click="closeModal"></button>
        </div>

        <!-- Success Body -->
        <div v-if="orderSuccess" class="modal-body p-4 text-center">
          <div class="mb-3 text-success">
            <i class="bi bi-check-circle-fill display-3"></i>
          </div>
          <h4 class="fw-bold text-dark mb-1">Order Placed Successfully!</h4>
          <p class="text-muted small mb-3">
            Your purchase order <strong>#{{ orderSuccess.orderNumber || orderSuccess.id }}</strong> has been confirmed and dispatched to farmer <strong>{{ product.farmer?.fullName }}</strong>.
          </p>

          <div class="card bg-light border-0 p-3 rounded-3 mb-4 mx-auto" style="max-width: 480px;">
            <div class="d-flex justify-content-between small text-muted mb-1">
              <span>Fulfillment Method:</span>
              <strong class="text-dark">{{ orderSuccess.fulfillmentType }}</strong>
            </div>
            <div class="d-flex justify-content-between small text-muted mb-1">
              <span>Payment Status:</span>
              <span class="badge bg-success-subtle text-success border border-success-subtle">{{ orderSuccess.paymentStatus }}</span>
            </div>
            <div class="d-flex justify-content-between small text-muted mb-1">
              <span>Total Paid / Due:</span>
              <strong class="text-farmora-green fs-6">LKR {{ Number(orderSuccess.totalAmount).toLocaleString() }}</strong>
            </div>
          </div>

          <div class="d-flex justify-content-center gap-3">
            <button class="btn btn-outline-secondary px-4 fw-semibold" @click="closeModal">Close</button>
            <button class="btn btn-farmora px-4 fw-bold" @click="goToOrders">
              <i class="bi bi-box-seam me-1"></i> Track My Purchases
            </button>
          </div>
        </div>

        <!-- Form Body -->
        <form v-else @submit.prevent="handleBuyNow">
          <div class="modal-body p-4">
            <!-- Produce Summary Banner -->
            <div class="p-3 bg-light rounded-3 mb-4 d-flex align-items-center gap-3">
              <img
                :src="product.imageUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=200&q=80'"
                :alt="product.name"
                class="rounded-3 object-fit-cover shadow-sm"
                style="width: 72px; height: 72px;"
              />
              <div class="flex-grow-1">
                <div class="d-flex justify-content-between align-items-start">
                  <div>
                    <h6 class="fw-bold mb-1 text-dark">{{ product.name }}</h6>
                    <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-0-5 rounded-pill small">
                      {{ product.category }}
                    </span>
                    <span class="small text-muted ms-2"><i class="bi bi-geo-alt-fill text-danger me-0-5"></i> {{ product.location || 'Local Farm' }}</span>
                  </div>
                  <div class="text-end">
                    <div class="fw-bold text-farmora-green fs-5">LKR {{ unitPrice.toLocaleString() }}</div>
                    <div class="small text-muted">/ {{ product.unit || 'kg' }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="errorMessage" class="alert alert-danger py-2 small mb-3">
              <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ errorMessage }}
            </div>

            <div class="row g-3">
              <!-- Quantity -->
              <div class="col-md-6">
                <label class="form-label small fw-bold text-dark">Order Quantity ({{ product.unit || 'kg' }})</label>
                <div class="input-group">
                  <input
                    type="number"
                    step="1"
                    min="1"
                    :max="product.quantity"
                    class="form-control"
                    v-model="quantity"
                    required
                  />
                  <span class="input-group-text bg-light text-muted">{{ product.unit || 'kg' }}</span>
                </div>
                <div class="form-text small">Available stock: {{ product.quantity }} {{ product.unit }}</div>
              </div>

              <!-- Fulfillment Type -->
              <div class="col-md-6">
                <label class="form-label small fw-bold text-dark">Fulfillment Preference</label>
                <div class="d-flex gap-2">
                  <button
                    type="button"
                    class="btn btn-sm flex-fill p-2 text-start border rounded-3"
                    :class="fulfillmentType === 'DELIVERY' ? 'border-success bg-success-subtle text-dark fw-bold' : 'bg-white text-muted'"
                    @click="fulfillmentType = 'DELIVERY'"
                  >
                    <i class="bi bi-truck text-success me-1"></i> Delivery (+LKR 350)
                  </button>
                  <button
                    type="button"
                    class="btn btn-sm flex-fill p-2 text-start border rounded-3"
                    :class="fulfillmentType === 'PICKUP' ? 'border-success bg-success-subtle text-dark fw-bold' : 'bg-white text-muted'"
                    @click="fulfillmentType = 'PICKUP'"
                  >
                    <i class="bi bi-shop text-primary me-1"></i> Farm Pickup (Free)
                  </button>
                </div>
              </div>

              <!-- Address or Pickup Location -->
              <div class="col-12" v-if="fulfillmentType === 'DELIVERY'">
                <label class="form-label small fw-bold text-dark">Delivery Address</label>
                <input
                  type="text"
                  class="form-control form-control-sm"
                  placeholder="Street address, city, district"
                  v-model="deliveryAddress"
                  required
                />
              </div>
              <div class="col-12" v-else>
                <div class="p-2-5 bg-light-green border border-success-subtle rounded-3 small">
                  <strong><i class="bi bi-geo-alt-fill text-danger me-1"></i> Farm Pickup Location:</strong>
                  {{ product.location || 'Direct Farm Gate Pickup Point' }}
                  <div class="text-muted mt-1" style="font-size: 0.8rem;">
                    * You can choose your preferred pickup date & time slot right after confirming your order.
                  </div>
                </div>
              </div>

              <!-- Payment Method Selection -->
              <div class="col-12">
                <label class="form-label small fw-bold text-dark">Payment Method</label>
                <div class="row g-2">
                  <div class="col-sm-4">
                    <div
                      class="p-2 border rounded-3 cursor-pointer text-center"
                      :class="paymentMethod === 'CARD' ? 'border-success bg-success-subtle fw-bold text-dark' : 'bg-light text-muted'"
                      @click="paymentMethod = 'CARD'"
                    >
                      <i class="bi bi-credit-card-2-front text-primary me-1"></i> Card (Online)
                    </div>
                  </div>
                  <div class="col-sm-4">
                    <div
                      class="p-2 border rounded-3 cursor-pointer text-center"
                      :class="paymentMethod === 'BANK_TRANSFER' ? 'border-success bg-success-subtle fw-bold text-dark' : 'bg-light text-muted'"
                      @click="paymentMethod = 'BANK_TRANSFER'"
                    >
                      <i class="bi bi-bank text-warning-emphasis me-1"></i> Bank Transfer
                    </div>
                  </div>
                  <div class="col-sm-4">
                    <div
                      class="p-2 border rounded-3 cursor-pointer text-center"
                      :class="paymentMethod === 'CASH_ON_DELIVERY' ? 'border-success bg-success-subtle fw-bold text-dark' : 'bg-light text-muted'"
                      @click="paymentMethod = 'CASH_ON_DELIVERY'"
                    >
                      <i class="bi bi-cash-stack text-success me-1"></i> Cash on Delivery
                    </div>
                  </div>
                </div>
              </div>

              <!-- Special Instructions -->
              <div class="col-12">
                <label class="form-label small fw-bold text-dark">Order Instructions (Optional)</label>
                <input
                  type="text"
                  class="form-control form-control-sm"
                  placeholder="e.g. Ring the bell at gate, call prior to dispatch..."
                  v-model="notes"
                />
              </div>

              <!-- Price Breakdown -->
              <div class="col-12">
                <div class="p-3 bg-light rounded-3">
                  <div class="d-flex justify-content-between small text-muted mb-1">
                    <span>Subtotal ({{ quantity }} x LKR {{ unitPrice.toLocaleString() }}):</span>
                    <span>LKR {{ subtotal.toLocaleString() }}</span>
                  </div>
                  <div class="d-flex justify-content-between small text-muted mb-1">
                    <span>Fulfillment Fee:</span>
                    <span>{{ deliveryFee > 0 ? 'LKR ' + deliveryFee : 'FREE' }}</span>
                  </div>
                  <hr class="my-2">
                  <div class="d-flex justify-content-between fw-bold text-dark fs-5">
                    <span>Grand Total:</span>
                    <span class="text-farmora-green">LKR {{ grandTotal.toLocaleString() }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Footer Actions -->
          <div class="modal-footer bg-light px-4 py-3">
            <button type="button" class="btn btn-outline-secondary btn-sm px-3" @click="closeModal">Cancel</button>
            <button
              type="submit"
              class="btn btn-farmora btn-sm px-4 fw-bold"
              :disabled="submitting || product.status !== 'AVAILABLE'"
            >
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              <i class="bi bi-shield-lock-fill me-1"></i> Confirm & Place Order
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
.bg-light-green {
  background-color: #f0fdf4;
}
.cursor-pointer {
  cursor: pointer;
}
</style>

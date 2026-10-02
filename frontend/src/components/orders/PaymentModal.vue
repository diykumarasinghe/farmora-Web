<script setup>
import { ref, computed } from 'vue'
import { paymentService } from '../../services/paymentService'

const props = defineProps({
  show: Boolean,
  order: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'paymentSuccess'])

const paymentMethod = ref('CARD') // CARD, BANK_TRANSFER, WALLET, CASH_ON_DELIVERY
const cardNumber = ref('4242 4242 4242 4242')
const cardExpiry = ref('12/28')
const cardCvv = ref('123')
const cardHolder = ref('Nimal Silva')
const bankRef = ref('')
const notes = ref('')

const submitting = ref(false)
const errorMsg = ref('')
const paymentResult = ref(null)

const payableAmount = computed(() => {
  if (!props.order) return 0
  const deliveryFee = props.order.fulfillmentType === 'DELIVERY' ? 350 : 0
  return (Number(props.order.totalAmount) || 0) + deliveryFee
})

const handlePay = async () => {
  submitting.value = true
  errorMsg.value = ''
  try {
    const res = await paymentService.processPayment({
      orderId: props.order.id,
      amount: payableAmount.value,
      paymentMethod: paymentMethod.value,
      cardNumber: cardNumber.value,
      cardExpiry: cardExpiry.value,
      cardCvv: cardCvv.value,
      cardHolderName: cardHolder.value,
      bankReference: bankRef.value,
      notes: notes.value
    })
    paymentResult.value = res
    emit('paymentSuccess', res)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Payment processing failed. Please check your credentials.'
  } finally {
    submitting.value = false
  }
}

const closeModal = () => {
  paymentResult.value = null
  errorMsg.value = ''
  emit('close')
}
</script>

<template>
  <div v-if="show && order" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.6); z-index: 1060;">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
        <!-- Header -->
        <div class="modal-header bg-farmora-green text-white p-3 px-4">
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-shield-check fs-5"></i>
            <h5 class="modal-title fw-bold mb-0">Farmora Secure Payment Gateway</h5>
          </div>
          <button type="button" class="btn-close btn-close-white" @click="closeModal"></button>
        </div>

        <!-- Success view -->
        <div v-if="paymentResult" class="modal-body p-4 text-center">
          <div class="mb-3 text-success">
            <i class="bi bi-check-circle-fill display-3"></i>
          </div>
          <h4 class="fw-bold text-dark mb-1">Payment Successful!</h4>
          <p class="small text-muted mb-3">
            Transaction <strong>#{{ paymentResult.transactionReference }}</strong> has been verified and settled.
          </p>

          <div class="p-3 bg-light rounded-3 text-start small mb-4 mx-auto" style="max-width: 400px;">
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Order ID:</span>
              <strong class="text-dark">#{{ order.orderNumber || order.id }}</strong>
            </div>
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Amount Paid:</span>
              <strong class="text-success fs-6">LKR {{ Number(paymentResult.amount).toLocaleString() }}</strong>
            </div>
            <div class="d-flex justify-content-between mb-1">
              <span class="text-muted">Method:</span>
              <span class="text-dark fw-semibold">{{ paymentResult.paymentMethod }}</span>
            </div>
            <div class="d-flex justify-content-between">
              <span class="text-muted">Settlement Time:</span>
              <span class="text-dark">{{ new Date(paymentResult.paidAt).toLocaleTimeString() }}</span>
            </div>
          </div>

          <button class="btn btn-farmora px-4 fw-bold" @click="closeModal">Done</button>
        </div>

        <!-- Form view -->
        <form v-else @submit.prevent="handlePay">
          <div class="modal-body p-4">
            <!-- Order summary banner -->
            <div class="p-3 bg-light rounded-3 mb-3 d-flex justify-content-between align-items-center">
              <div>
                <div class="small text-muted">Order #{{ order.orderNumber || order.id }} &bull; {{ order.product?.name }}</div>
                <div class="fw-bold text-dark fs-5">LKR {{ payableAmount.toLocaleString() }}</div>
              </div>
              <span class="badge bg-success-subtle text-success border border-success-subtle px-3 py-1 rounded-pill">
                256-Bit SSL Encrypted
              </span>
            </div>

            <div v-if="errorMsg" class="alert alert-danger py-2 small mb-3">
              <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ errorMsg }}
            </div>

            <!-- Method Selection -->
            <div class="mb-3">
              <label class="form-label small fw-bold text-dark">Select Payment Method</label>
              <div class="row g-2">
                <div class="col-4">
                  <div
                    class="p-2 border rounded-3 text-center cursor-pointer"
                    :class="paymentMethod === 'CARD' ? 'border-success bg-success-subtle fw-bold text-dark' : 'bg-white text-muted'"
                    @click="paymentMethod = 'CARD'"
                  >
                    <i class="bi bi-credit-card-2-front text-primary d-block fs-5 mb-1"></i>
                    <span class="small">Credit/Debit</span>
                  </div>
                </div>
                <div class="col-4">
                  <div
                    class="p-2 border rounded-3 text-center cursor-pointer"
                    :class="paymentMethod === 'BANK_TRANSFER' ? 'border-success bg-success-subtle fw-bold text-dark' : 'bg-white text-muted'"
                    @click="paymentMethod = 'BANK_TRANSFER'"
                  >
                    <i class="bi bi-bank text-warning-emphasis d-block fs-5 mb-1"></i>
                    <span class="small">Bank Slip</span>
                  </div>
                </div>
                <div class="col-4">
                  <div
                    class="p-2 border rounded-3 text-center cursor-pointer"
                    :class="paymentMethod === 'WALLET' ? 'border-success bg-success-subtle fw-bold text-dark' : 'bg-white text-muted'"
                    @click="paymentMethod = 'WALLET'"
                  >
                    <i class="bi bi-wallet2 text-success d-block fs-5 mb-1"></i>
                    <span class="small">Farmora Pay</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Card Form -->
            <div v-if="paymentMethod === 'CARD'" class="p-3 border rounded-3 bg-light">
              <div class="mb-2">
                <label class="form-label small fw-bold text-dark">Cardholder Name</label>
                <input type="text" class="form-control form-control-sm" v-model="cardHolder" required />
              </div>
              <div class="mb-2">
                <label class="form-label small fw-bold text-dark">Card Number</label>
                <div class="input-group input-group-sm">
                  <span class="input-group-text bg-white"><i class="bi bi-credit-card"></i></span>
                  <input type="text" class="form-control" v-model="cardNumber" placeholder="0000 0000 0000 0000" required />
                </div>
              </div>
              <div class="row g-2">
                <div class="col-6">
                  <label class="form-label small fw-bold text-dark">Expiry (MM/YY)</label>
                  <input type="text" class="form-control form-control-sm" v-model="cardExpiry" placeholder="MM/YY" required />
                </div>
                <div class="col-6">
                  <label class="form-label small fw-bold text-dark">CVV / CVC</label>
                  <input type="password" maxlength="4" class="form-control form-control-sm" v-model="cardCvv" placeholder="123" required />
                </div>
              </div>
            </div>

            <!-- Bank Transfer Form -->
            <div v-else-if="paymentMethod === 'BANK_TRANSFER'" class="p-3 border rounded-3 bg-light">
              <div class="small text-muted mb-2">
                Transfer to <strong>Farmora Escrow Account</strong>:
                <br>Commercial Bank of Ceylon &bull; Acc: 1000849201 &bull; Pettah Branch
              </div>
              <label class="form-label small fw-bold text-dark">Bank Reference / Slip Number</label>
              <input
                type="text"
                class="form-control form-control-sm"
                placeholder="e.g. CBC-REF-984029"
                v-model="bankRef"
                required
              />
            </div>

            <!-- Wallet -->
            <div v-else class="p-3 border rounded-3 bg-light text-center">
              <i class="bi bi-wallet-fill text-success fs-3 mb-2 d-block"></i>
              <div class="fw-bold text-dark mb-1">Agro-Wallet Balance Available</div>
              <div class="text-muted small">Instant 1-click deduction from your pre-loaded buyer balance.</div>
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
              <i class="bi bi-lock-fill me-1"></i> Pay LKR {{ payableAmount.toLocaleString() }}
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
.cursor-pointer {
  cursor: pointer;
}
</style>

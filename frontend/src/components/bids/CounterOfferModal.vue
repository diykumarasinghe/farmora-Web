<script setup>
import { ref, computed } from 'vue'
import { bidService } from '../../services/bidService'

const props = defineProps({
  show: Boolean,
  bid: {
    type: Object,
    default: null
  },
  isFarmer: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'counterSent'])

const counterAmount = ref('')
const counterNote = ref('')
const submitting = ref(false)
const errorMsg = ref('')

// Initialize default values when bid changes
const initForm = () => {
  if (props.bid) {
    counterAmount.value = props.bid.counterAmount || props.bid.amount
    counterNote.value = ''
    errorMsg.value = ''
  }
}

const totalOffer = computed(() => {
  const amt = parseFloat(counterAmount.value) || 0
  const qty = parseFloat(props.bid?.quantity) || 0
  return (amt * qty).toFixed(2)
})

const handleSubmit = async () => {
  if (!counterAmount.value || counterAmount.value <= 0) {
    errorMsg.value = 'Please specify a valid counter-offer price.'
    return
  }

  submitting.value = true
  errorMsg.value = ''
  try {
    const updated = await bidService.sendCounterOffer(props.bid.id, {
      counterAmount: counterAmount.value,
      counterNote: counterNote.value
    })
    emit('counterSent', updated)
    emit('close')
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to submit counter offer.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div v-if="show && bid" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.6); z-index: 1060;">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
        <div class="modal-header bg-farmora-green text-white p-3 px-4">
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-arrow-left-right fs-5"></i>
            <h5 class="modal-title fw-bold mb-0">Negotiate & Counter-Offer</h5>
          </div>
          <button type="button" class="btn-close btn-close-white" @click="$emit('close')"></button>
        </div>

        <form @submit.prevent="handleSubmit">
          <div class="modal-body p-4">
            <!-- Negotiation Context -->
            <div class="p-3 bg-light rounded-3 mb-3">
              <div class="fw-bold text-dark">{{ bid.product?.name }}</div>
              <div class="small text-muted mb-1">
                Requested Quantity: <strong>{{ bid.quantity }} {{ bid.product?.unit || 'kg' }}</strong>
              </div>
              <div class="small text-muted">
                Initial Offer: <strong>LKR {{ Number(bid.amount).toFixed(2) }}</strong> / {{ bid.product?.unit || 'kg' }}
              </div>
              <div v-if="bid.counterAmount" class="small text-warning-emphasis fw-bold mt-1">
                Last Counter by {{ bid.counterBy }}: LKR {{ Number(bid.counterAmount).toFixed(2) }} / {{ bid.product?.unit || 'kg' }}
              </div>
            </div>

            <div v-if="errorMsg" class="alert alert-danger py-2 small mb-3">
              <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ errorMsg }}
            </div>

            <!-- Price Input -->
            <div class="mb-3">
              <label class="form-label small fw-bold text-dark">Proposed Counter Price per {{ bid.product?.unit || 'kg' }} (LKR)</label>
              <div class="input-group">
                <span class="input-group-text bg-light text-muted fw-bold">LKR</span>
                <input
                  type="number"
                  step="1"
                  min="1"
                  class="form-control"
                  v-model="counterAmount"
                  placeholder="Enter your proposed price..."
                  required
                />
              </div>
              <div class="form-text small">
                Total revised valuation: <strong>LKR {{ Number(totalOffer).toLocaleString() }}</strong>
              </div>
            </div>

            <!-- Note Input -->
            <div class="mb-3">
              <label class="form-label small fw-bold text-dark">Negotiation Note / Reason</label>
              <textarea
                class="form-control"
                rows="3"
                placeholder="e.g. Can meet in the middle at this rate with prompt farm pickup..."
                v-model="counterNote"
              ></textarea>
            </div>
          </div>

          <div class="modal-footer bg-light px-4 py-3">
            <button type="button" class="btn btn-outline-secondary btn-sm" @click="$emit('close')">Cancel</button>
            <button
              type="submit"
              class="btn btn-farmora btn-sm px-4 fw-bold"
              :disabled="submitting"
            >
              <span v-if="submitting" class="spinner-border spinner-border-sm me-1"></span>
              <i class="bi bi-send-fill me-1"></i> Send Counter-Offer
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
</style>

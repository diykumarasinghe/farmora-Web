<script setup>
import { ref, watch } from 'vue'
import { invoiceService } from '../../services/invoiceService'

const props = defineProps({
  show: Boolean,
  order: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'invoiceUpdated'])

const invoice = ref(null)
const loading = ref(true)
const isEditing = ref(false)
const submitting = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

// Editable fields
const editForm = ref({
  billingName: '',
  billingAddress: '',
  taxId: '',
  businessNotes: ''
})

const loadInvoice = async () => {
  if (!props.order) return
  loading.value = true
  errorMsg.value = ''
  try {
    invoice.value = await invoiceService.getInvoiceByOrderId(props.order.id)
    editForm.value = {
      billingName: invoice.value.billingName || '',
      billingAddress: invoice.value.billingAddress || '',
      taxId: invoice.value.taxId || '',
      businessNotes: invoice.value.businessNotes || ''
    }
  } catch (err) {
    errorMsg.value = 'Failed to load invoice and receipt details.'
  } finally {
    loading.value = false
  }
}

watch(() => props.show, (newVal) => {
  if (newVal) {
    isEditing.value = false
    loadInvoice()
  }
})

const handleSaveDetails = async () => {
  submitting.value = true
  errorMsg.value = ''
  successMsg.value = ''
  try {
    const updated = await invoiceService.updateInvoice(invoice.value.id, editForm.value)
    invoice.value = updated
    isEditing.value = false
    successMsg.value = 'Invoice billing details updated!'
    emit('invoiceUpdated', updated)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to update invoice.'
  } finally {
    submitting.value = false
  }
}

const handleToggleArchive = async () => {
  try {
    const updated = await invoiceService.toggleArchive(invoice.value.id)
    invoice.value = updated
    successMsg.value = updated.isArchived ? 'Receipt archived for tax records.' : 'Receipt unarchived.'
    emit('invoiceUpdated', updated)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to update archive status.'
  }
}

const printReceipt = () => {
  window.print()
}
</script>

<template>
  <div v-if="show && order" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.6); z-index: 1060;">
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content border-0 shadow-lg rounded-4 overflow-hidden">
        <!-- Modal Top Bar (Not printed) -->
        <div class="modal-header bg-light border-bottom p-3 px-4 d-print-none">
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-receipt-cutoff fs-5 text-success"></i>
            <h5 class="modal-title fw-bold mb-0 text-dark">Digital Invoice & Official Receipt</h5>
          </div>
          <button type="button" class="btn-close" @click="$emit('close')"></button>
        </div>

        <div class="modal-body p-4 p-md-5 bg-white print-area">
          <div v-if="successMsg" class="alert alert-success py-2 small mb-3 alert-dismissible d-print-none" role="alert">
            <i class="bi bi-check-circle-fill me-1"></i> {{ successMsg }}
            <button type="button" class="btn-close" @click="successMsg = ''"></button>
          </div>
          <div v-if="errorMsg" class="alert alert-danger py-2 small mb-3 d-print-none">
            <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ errorMsg }}
          </div>

          <div v-if="loading" class="text-center py-5">
            <span class="spinner-border text-success"></span>
            <div class="small text-muted mt-2">Generating verified digital invoice...</div>
          </div>

          <div v-else-if="invoice">
            <!-- Invoice Header -->
            <div class="d-flex justify-content-between align-items-start border-bottom pb-4 mb-4">
              <div>
                <div class="d-flex align-items-center gap-2 mb-2">
                  <div class="rounded-circle bg-success text-white p-2 d-flex align-items-center justify-content-center" style="width: 38px; height: 38px;">
                    <i class="bi bi-flower2 fs-5"></i>
                  </div>
                  <span class="fw-bold fs-4 text-farmora-green tracking-wide">FARMORA</span>
                </div>
                <div class="small text-muted">Agricultural Wholesale & Digital Marketplace</div>
                <div class="small text-muted">Colombo 03, Western Province, Sri Lanka</div>
                <div class="small text-muted">VAT Registration: LK-83920194</div>
              </div>

              <div class="text-end">
                <span
                  class="badge px-3 py-1-5 rounded-pill mb-2 fw-bold"
                  :class="invoice.paymentStatus === 'PAID' ? 'bg-success' : 'bg-warning text-dark'"
                >
                  {{ invoice.paymentStatus }}
                </span>
                <span v-if="invoice.isArchived" class="badge bg-secondary text-white ms-1">
                  <i class="bi bi-archive me-1"></i> ARCHIVED
                </span>
                <h5 class="fw-bold text-dark mb-0">{{ invoice.invoiceNumber }}</h5>
                <div class="small text-muted">Date: {{ new Date(invoice.issueDate).toLocaleDateString() }}</div>
                <div class="small text-muted">Linked Order: #{{ order.orderNumber || order.id }}</div>
              </div>
            </div>

            <!-- Seller and Buyer Info Grid -->
            <div class="row g-4 mb-4 pb-2 border-bottom">
              <div class="col-6">
                <h6 class="small fw-bold text-uppercase text-muted mb-2">Producer / Farmer:</h6>
                <div class="fw-bold text-dark fs-6">{{ invoice.farmer?.fullName }}</div>
                <div class="small text-muted">{{ order.pickupAddress || invoice.farmer?.address || 'Local Farm' }}</div>
                <div class="small text-muted">{{ invoice.farmer?.phone || invoice.farmer?.email }}</div>
              </div>

              <div class="col-6 text-end">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <span></span>
                  <div class="d-flex align-items-center gap-1">
                    <h6 class="small fw-bold text-uppercase text-muted mb-0">Billed Recipient:</h6>
                    <button
                      v-if="!isEditing"
                      class="btn btn-sm btn-link text-decoration-none p-0 text-success ms-1 d-print-none"
                      @click="isEditing = true"
                      title="Edit billing info"
                    >
                      <i class="bi bi-pencil-square"></i> Edit
                    </button>
                  </div>
                </div>

                <!-- Read-only Billing Info -->
                <div v-if="!isEditing">
                  <div class="fw-bold text-dark fs-6">{{ invoice.billingName }}</div>
                  <div class="small text-muted">{{ invoice.billingAddress }}</div>
                  <div v-if="invoice.taxId" class="small text-muted">Tax ID / TIN: {{ invoice.taxId }}</div>
                  <div v-if="invoice.businessNotes" class="small text-muted fst-italic">"{{ invoice.businessNotes }}"</div>
                </div>

                <!-- Editable Billing Form -->
                <div v-else class="text-start p-3 bg-light rounded-3 border d-print-none">
                  <div class="mb-2">
                    <label class="form-label small fw-bold text-dark">Business / Buyer Name</label>
                    <input type="text" class="form-control form-control-sm" v-model="editForm.billingName" />
                  </div>
                  <div class="mb-2">
                    <label class="form-label small fw-bold text-dark">Billing Address</label>
                    <input type="text" class="form-control form-control-sm" v-model="editForm.billingAddress" />
                  </div>
                  <div class="mb-2">
                    <label class="form-label small fw-bold text-dark">TIN / Tax ID</label>
                    <input type="text" class="form-control form-control-sm" v-model="editForm.taxId" />
                  </div>
                  <div class="mb-2">
                    <label class="form-label small fw-bold text-dark">PO / Business Notes</label>
                    <input type="text" class="form-control form-control-sm" v-model="editForm.businessNotes" />
                  </div>
                  <div class="d-flex justify-content-end gap-2 mt-2">
                    <button class="btn btn-sm btn-light" @click="isEditing = false">Cancel</button>
                    <button class="btn btn-sm btn-success fw-bold" @click="handleSaveDetails" :disabled="submitting">
                      Save
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Itemized Table -->
            <div class="table-responsive mb-4">
              <table class="table table-borderless align-middle mb-0">
                <thead class="bg-light text-muted small text-uppercase">
                  <tr>
                    <th scope="col" class="py-2">Item Description</th>
                    <th scope="col" class="text-center py-2">Quantity</th>
                    <th scope="col" class="text-end py-2">Unit Price</th>
                    <th scope="col" class="text-end py-2">Amount</th>
                  </tr>
                </thead>
                <tbody>
                  <tr class="border-bottom">
                    <td class="py-3">
                      <div class="fw-bold text-dark">{{ order.product?.name }}</div>
                      <div class="small text-muted">Category: {{ order.product?.category }} &bull; Fresh Farm Produce</div>
                    </td>
                    <td class="text-center py-3 fw-semibold">{{ order.quantity }} {{ order.product?.unit || 'kg' }}</td>
                    <td class="text-end py-3">LKR {{ Number(order.unitPrice).toLocaleString() }}</td>
                    <td class="text-end py-3 fw-bold text-dark">LKR {{ Number(order.totalAmount).toLocaleString() }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Totals & Taxes Summary -->
            <div class="row justify-content-end mb-4">
              <div class="col-md-5">
                <div class="p-3 bg-light rounded-3">
                  <div class="d-flex justify-content-between small text-muted mb-2">
                    <span>Subtotal:</span>
                    <span>LKR {{ Number(invoice.subtotal).toLocaleString() }}</span>
                  </div>
                  <div class="d-flex justify-content-between small text-muted mb-2">
                    <span>Fulfillment / Delivery:</span>
                    <span>{{ Number(invoice.deliveryFee) > 0 ? 'LKR ' + Number(invoice.deliveryFee).toLocaleString() : 'FREE (Pickup)' }}</span>
                  </div>
                  <div class="d-flex justify-content-between small text-muted mb-2">
                    <span>Value Added Tax (0% Agri):</span>
                    <span>LKR 0.00</span>
                  </div>
                  <hr class="my-2">
                  <div class="d-flex justify-content-between fw-bold text-dark fs-5">
                    <span>Total Amount:</span>
                    <span class="text-farmora-green">LKR {{ Number(invoice.totalAmount).toLocaleString() }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Official Seal & Disclaimer -->
            <div class="p-3 border rounded-3 bg-light d-flex justify-content-between align-items-center">
              <div>
                <div class="small fw-bold text-dark">Authorized Agricultural Marketplace Receipt</div>
                <div class="small text-muted">Digitally generated & authenticated via Farmora smart contract ledger.</div>
              </div>
              <div class="text-success text-center">
                <i class="bi bi-patch-check-fill fs-3 d-block mb-1"></i>
                <span class="small fw-bold text-uppercase" style="font-size: 0.7rem;">VERIFIED</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer / Print / Archive Actions -->
        <div class="modal-footer bg-light px-4 py-3 d-flex justify-content-between align-items-center d-print-none">
          <div>
            <button
              v-if="invoice"
              class="btn btn-outline-secondary btn-sm"
              @click="handleToggleArchive"
            >
              <i :class="invoice.isArchived ? 'bi bi-arrow-up-circle me-1' : 'bi bi-archive me-1'"></i>
              {{ invoice.isArchived ? 'Unarchive Receipt' : 'Archive for Bookkeeping' }}
            </button>
          </div>

          <div class="d-flex gap-2">
            <button class="btn btn-outline-secondary btn-sm" @click="$emit('close')">Close</button>
            <button class="btn btn-primary btn-sm fw-semibold" @click="printReceipt">
              <i class="bi bi-printer me-1"></i> Print / Save PDF
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-farmora-green {
  color: #2d6a4f !important;
}
.tracking-wide {
  letter-spacing: 0.1em;
}
.py-1-5 {
  padding-top: 0.35rem;
  padding-bottom: 0.35rem;
}
@media print {
  .modal {
    position: static;
    background: none !important;
  }
  .modal-dialog {
    max-width: 100% !important;
    margin: 0 !important;
  }
}
</style>

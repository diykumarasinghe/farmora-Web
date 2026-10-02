<script setup>
import { onMounted, ref, computed } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import OrderCard from '../../components/orders/OrderCard.vue'
import OrderDetailsModal from '../../components/orders/OrderDetailsModal.vue'
import SchedulePickupModal from '../../components/orders/SchedulePickupModal.vue'
import DeliveryTrackerModal from '../../components/deliveries/DeliveryTrackerModal.vue'
import PaymentModal from '../../components/orders/PaymentModal.vue'
import ReceiptModal from '../../components/orders/ReceiptModal.vue'
import BulkOrderModal from '../../components/orders/BulkOrderModal.vue'

import { orderService } from '../../services/orderService'
import { reviewService } from '../../services/reviewService'
import { pickupService } from '../../services/pickupService'
import { paymentService } from '../../services/paymentService'
import { invoiceService } from '../../services/invoiceService'
import { bulkOrderService } from '../../services/bulkOrderService'

// Active tab: 'orders', 'pickups', 'payments', 'bulk'
const activeTab = ref('orders')

const orders = ref([])
const pickups = ref([])
const payments = ref([])
const invoices = ref([])
const bulkOrders = ref([])

const loading = ref(true)
const selectedStatus = ref('ALL')
const searchQuery = ref('')
const archiveFilter = ref(false)

// Modals State
const showDetailsModal = ref(false)
const showPickupModal = ref(false)
const showTrackerModal = ref(false)
const showPaymentModal = ref(false)
const showReceiptModal = ref(false)
const showBulkModal = ref(false)
const showReviewModal = ref(false)

const selectedOrder = ref(null)
const selectedBulkOrder = ref(null)

// Review form state
const reviewRating = ref(5)
const reviewComment = ref('')
const submittingReview = ref(false)
const reviewSuccess = ref('')
const reviewError = ref('')

// Load all buyer transactions data
const loadAllData = async () => {
  loading.value = true
  try {
    const [ordersRes, pickupsRes, paymentsRes, invoicesRes, bulkRes] = await Promise.allSettled([
      orderService.getMyOrders(),
      pickupService.getMyPickups(),
      paymentService.getMyPayments(),
      invoiceService.getMyInvoices(),
      bulkOrderService.getMyBulkOrders()
    ])

    if (ordersRes.status === 'fulfilled') orders.value = ordersRes.value || []
    if (pickupsRes.status === 'fulfilled') pickups.value = pickupsRes.value || []
    if (paymentsRes.status === 'fulfilled') payments.value = paymentsRes.value || []
    if (invoicesRes.status === 'fulfilled') invoices.value = invoicesRes.value || []
    if (bulkRes.status === 'fulfilled') bulkOrders.value = bulkRes.value || []
  } catch (err) {
    console.error('Failed to load transaction data', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAllData()
})

// Metrics calculations
const totalSpent = computed(() => {
  return orders.value
    .filter(o => o.status !== 'CANCELLED')
    .reduce((sum, o) => sum + (Number(o.totalAmount) || 0), 0)
})

const deliveredOrdersCount = computed(() => {
  return orders.value.filter(o => o.status === 'DELIVERED').length
})

const activePickupsCount = computed(() => {
  return pickups.value.filter(p => p.status !== 'COMPLETED' && p.status !== 'CANCELLED').length
})

const filteredOrders = computed(() => {
  return orders.value.filter(o => {
    const matchesStatus = selectedStatus.value === 'ALL' || o.status === selectedStatus.value
    const query = searchQuery.value.toLowerCase().trim()
    const matchesSearch = !query ||
      o.id.toString().includes(query) ||
      (o.orderNumber && o.orderNumber.toLowerCase().includes(query)) ||
      o.product?.name?.toLowerCase().includes(query) ||
      o.farmer?.fullName?.toLowerCase().includes(query)
    return matchesStatus && matchesSearch
  })
})

const filteredInvoices = computed(() => {
  return invoices.value.filter(inv => {
    return archiveFilter.value ? inv.isArchived : !inv.isArchived
  })
})

// Modal Openers
const openDetails = (order) => {
  selectedOrder.value = order
  showDetailsModal.value = true
}

const openDeliveryTracker = (order) => {
  selectedOrder.value = order
  showTrackerModal.value = true
}

const openPickupModal = (order) => {
  selectedOrder.value = order
  showPickupModal.value = true
}

const openPaymentModal = (order) => {
  selectedOrder.value = order
  showPaymentModal.value = true
}

const openReceiptModal = (order) => {
  selectedOrder.value = order
  showReceiptModal.value = true
}

const openEditBulkModal = (bulkOrder) => {
  selectedBulkOrder.value = bulkOrder
  showBulkModal.value = true
}

const openReviewModal = (order) => {
  selectedOrder.value = order
  reviewRating.value = 5
  reviewComment.value = ''
  reviewSuccess.value = ''
  reviewError.value = ''
  showReviewModal.value = true
}

// Order cancellation from card
const handleCancelOrder = async (order) => {
  const reason = prompt('Please enter cancellation reason for Order #' + (order.orderNumber || order.id) + ':')
  if (reason === null) return
  try {
    await orderService.cancelOrder(order.id, reason)
    loadAllData()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to cancel order.')
  }
}

// Bulk Order cancellation
const handleCancelBulkOrder = async (bulkOrder) => {
  if (!confirm('Are you sure you want to cancel wholesale bulk order #' + bulkOrder.bulkOrderNumber + '?')) return
  try {
    await bulkOrderService.cancelBulkOrder(bulkOrder.id, 'Cancelled by buyer')
    loadAllData()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to cancel bulk order.')
  }
}

// Submit Review
const submitReview = async () => {
  submittingReview.value = true
  reviewError.value = ''
  try {
    await reviewService.createReview({
      orderId: selectedOrder.value.id,
      rating: reviewRating.value,
      comment: reviewComment.value
    })
    reviewSuccess.value = 'Thank you! Your feedback has been recorded.'
    setTimeout(() => {
      showReviewModal.value = false
    }, 1500)
  } catch (err) {
    reviewError.value = err.response?.data?.message || 'Failed to submit review.'
  } finally {
    submittingReview.value = false
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="orders" />
      </div>

      <div class="col-lg-9">
        <!-- Page Header -->
        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
          <div>
            <h4 class="fw-bold mb-1">
              <i class="bi bi-box-seam text-success me-2"></i>Transactions & Logistics Hub
            </h4>
            <p class="text-muted small mb-0">Manage orders, live delivery tracking, farm gate pickups, invoices & wholesale contracts.</p>
          </div>
          <router-link to="/products" class="btn btn-farmora btn-sm fw-bold">
            <i class="bi bi-search me-1"></i> Buy Fresh Crops
          </router-link>
        </div>

        <!-- Metrics KPI Cards -->
        <div class="row g-3 mb-4">
          <div class="col-sm-6 col-md-3">
            <div class="card border-0 shadow-sm p-3 bg-white rounded-3 h-100">
              <div class="small text-muted mb-1">Total Purchases</div>
              <h4 class="fw-bold text-dark mb-0">{{ orders.length }}</h4>
              <span class="text-muted" style="font-size: 0.75rem;">Placed on platform</span>
            </div>
          </div>
          <div class="col-sm-6 col-md-3">
            <div class="card border-0 shadow-sm p-3 bg-white rounded-3 h-100">
              <div class="small text-muted mb-1">Completed Deliveries</div>
              <h4 class="fw-bold text-success mb-0">{{ deliveredOrdersCount }}</h4>
              <span class="text-muted" style="font-size: 0.75rem;">Received & reviewed</span>
            </div>
          </div>
          <div class="col-sm-6 col-md-3">
            <div class="card border-0 shadow-sm p-3 bg-white rounded-3 h-100">
              <div class="small text-muted mb-1">Active Farm Pickups</div>
              <h4 class="fw-bold text-primary mb-0">{{ activePickupsCount }}</h4>
              <span class="text-muted" style="font-size: 0.75rem;">Scheduled gate passes</span>
            </div>
          </div>
          <div class="col-sm-6 col-md-3">
            <div class="card border-0 shadow-sm p-3 bg-white rounded-3 h-100">
              <div class="small text-muted mb-1">Total Spent</div>
              <h4 class="fw-bold text-farmora-green mb-0">LKR {{ totalSpent.toLocaleString() }}</h4>
              <span class="text-muted" style="font-size: 0.75rem;">Settled payments</span>
            </div>
          </div>
        </div>

        <!-- Navigation Tabs for the 7 Group Functionalities -->
        <ul class="nav nav-pills custom-nav-pills mb-4 p-1 bg-light rounded-3 shadow-sm border">
          <li class="nav-item">
            <button
              class="nav-link py-2 px-3 fw-bold small rounded-3"
              :class="{ active: activeTab === 'orders' }"
              @click="activeTab = 'orders'"
            >
              <i class="bi bi-bag-check me-1"></i> All Orders ({{ orders.length }})
            </button>
          </li>
          <li class="nav-item">
            <button
              class="nav-link py-2 px-3 fw-bold small rounded-3"
              :class="{ active: activeTab === 'pickups' }"
              @click="activeTab = 'pickups'"
            >
              <i class="bi bi-shop me-1"></i> Farm Pickups ({{ pickups.length }})
            </button>
          </li>
          <li class="nav-item">
            <button
              class="nav-link py-2 px-3 fw-bold small rounded-3"
              :class="{ active: activeTab === 'payments' }"
              @click="activeTab = 'payments'"
            >
              <i class="bi bi-receipt me-1"></i> Payments & Invoices ({{ invoices.length }})
            </button>
          </li>
          <li class="nav-item">
            <button
              class="nav-link py-2 px-3 fw-bold small rounded-3"
              :class="{ active: activeTab === 'bulk' }"
              @click="activeTab = 'bulk'"
            >
              <i class="bi bi-box-seam me-1"></i> Bulk Wholesale ({{ bulkOrders.length }})
            </button>
          </li>
        </ul>

        <LoadingSpinner v-if="loading" message="Loading your transaction and logistics portal..." />

        <div v-else>
          <!-- TAB 1: ALL ORDERS -->
          <div v-if="activeTab === 'orders'">
            <!-- Filters & Search Toolbar -->
            <div class="card border-0 shadow-sm p-3 mb-4 bg-white rounded-3">
              <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
                <div class="d-flex gap-1 flex-wrap">
                  <button
                    class="btn btn-sm"
                    :class="selectedStatus === 'ALL' ? 'btn-success' : 'btn-outline-secondary'"
                    @click="selectedStatus = 'ALL'"
                  >
                    All
                  </button>
                  <button
                    class="btn btn-sm"
                    :class="selectedStatus === 'CONFIRMED' ? 'btn-success' : 'btn-outline-secondary'"
                    @click="selectedStatus = 'CONFIRMED'"
                  >
                    Confirmed
                  </button>
                  <button
                    class="btn btn-sm"
                    :class="selectedStatus === 'PROCESSING' ? 'btn-success' : 'btn-outline-secondary'"
                    @click="selectedStatus = 'PROCESSING'"
                  >
                    Processing
                  </button>
                  <button
                    class="btn btn-sm"
                    :class="selectedStatus === 'READY_FOR_PICKUP' ? 'btn-success' : 'btn-outline-secondary'"
                    @click="selectedStatus = 'READY_FOR_PICKUP'"
                  >
                    Ready / Pickup
                  </button>
                  <button
                    class="btn btn-sm"
                    :class="selectedStatus === 'OUT_FOR_DELIVERY' ? 'btn-success' : 'btn-outline-secondary'"
                    @click="selectedStatus = 'OUT_FOR_DELIVERY'"
                  >
                    Out for Delivery
                  </button>
                  <button
                    class="btn btn-sm"
                    :class="selectedStatus === 'DELIVERED' ? 'btn-success' : 'btn-outline-secondary'"
                    @click="selectedStatus = 'DELIVERED'"
                  >
                    Delivered
                  </button>
                  <button
                    class="btn btn-sm"
                    :class="selectedStatus === 'CANCELLED' ? 'btn-success' : 'btn-outline-secondary'"
                    @click="selectedStatus = 'CANCELLED'"
                  >
                    Cancelled
                  </button>
                </div>

                <div class="input-group input-group-sm" style="max-width: 260px;">
                  <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
                  <input
                    type="text"
                    class="form-control"
                    placeholder="Search order or produce..."
                    v-model="searchQuery"
                  />
                </div>
              </div>
            </div>

            <div v-if="filteredOrders.length > 0">
              <OrderCard
                v-for="order in filteredOrders"
                :key="order.id"
                :order="order"
                role="BUYER"
                @viewDetails="openDetails"
                @trackDelivery="openDeliveryTracker"
                @schedulePickup="openPickupModal"
                @payNow="openPaymentModal"
                @viewReceipt="openReceiptModal"
                @cancelOrder="handleCancelOrder"
                @leaveReview="openReviewModal"
              />
            </div>

            <EmptyState
              v-else
              icon="bi-bag-x"
              title="No Orders Match Your Filter"
              description="Explore the marketplace to purchase high quality crops directly from Sri Lankan growers."
              actionText="Browse Farm Marketplace"
              actionRoute="/products"
            />
          </div>

          <!-- TAB 2: FARM PICKUPS -->
          <div v-else-if="activeTab === 'pickups'">
            <div class="card border-0 shadow-sm p-4 bg-white rounded-3 mb-4">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                  <h5 class="fw-bold mb-1 text-dark"><i class="bi bi-shop text-primary me-2"></i>Farm Pickup Gate Passes</h5>
                  <p class="small text-muted mb-0">Present your 6-digit gate passcode upon arriving at the farm depot.</p>
                </div>
              </div>

              <div v-if="pickups.length > 0" class="row g-3">
                <div v-for="p in pickups" :key="p.id" class="col-md-6">
                  <div class="card border rounded-3 p-3 shadow-sm h-100 bg-light">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                      <div>
                        <span
                          class="badge px-3 py-1 rounded-pill me-2"
                          :class="p.status === 'COMPLETED' ? 'bg-success' : (p.status === 'CANCELLED' ? 'bg-danger' : 'bg-warning text-dark')"
                        >
                          {{ p.status }}
                        </span>
                        <strong class="text-dark">Order #{{ p.order?.orderNumber || p.order?.id }}</strong>
                      </div>
                      <div class="d-flex align-items-center gap-2">
                        <div class="text-center bg-white border rounded px-2 py-0-5 shadow-sm">
                          <span class="small text-muted d-block" style="font-size: 0.65rem;">PASSCODE</span>
                          <strong class="text-success fs-6">{{ p.passcode }}</strong>
                        </div>
                        <img
                          :src="'https://api.qrserver.com/v1/create-qr-code/?size=90x90&data=FARMORA-PICKUP-' + p.id + '-' + p.passcode"
                          alt="QR Code"
                          class="rounded border bg-white p-1 shadow-sm"
                          style="width: 44px; height: 44px; object-fit: contain;"
                          @error="(e) => e.target.style.display = 'none'"
                        />
                      </div>
                    </div>

                    <h6 class="fw-bold text-dark mb-1">{{ p.order?.product?.name }}</h6>
                    <div class="small text-muted mb-2">
                      <i class="bi bi-calendar-event me-1"></i> Date: <strong>{{ p.pickupDate }}</strong> &bull; {{ p.timeSlot }}
                    </div>
                    <div class="small text-muted mb-2">
                      <i class="bi bi-geo-alt-fill text-danger me-1"></i> Farm Gate: {{ p.pickupLocation }}
                    </div>
                    <div class="small text-muted mb-3">
                      <i class="bi bi-person text-success me-1"></i> Farmer: {{ p.farmer?.fullName }} ({{ p.farmer?.phone }})
                    </div>

                    <div class="d-flex justify-content-between align-items-center border-top pt-2 mt-auto">
                      <a
                        :href="'https://www.google.com/maps/search/?api=1&query=' + encodeURIComponent(p.pickupLocation)"
                        target="_blank"
                        rel="noopener noreferrer"
                        class="btn btn-sm btn-link text-decoration-none text-primary p-0"
                      >
                        <i class="bi bi-compass"></i> GPS Directions
                      </a>
                      <div class="d-flex gap-2">
                        <button class="btn btn-sm btn-outline-secondary" @click="openReceiptModal(p.order)">
                          Receipt
                        </button>
                        <button class="btn btn-sm btn-outline-success fw-semibold" @click="openPickupModal(p.order)">
                          Gate Pass & QR
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <EmptyState
                v-else
                icon="bi-shop"
                title="No Farm Pickups Scheduled"
                description="When placing an order, select Farm Pickup to schedule self-collection at the grower's gate."
              />
            </div>
          </div>

          <!-- TAB 3: PAYMENTS & INVOICES -->
          <div v-else-if="activeTab === 'payments'">
            <div class="card border-0 shadow-sm p-4 bg-white rounded-3 mb-4">
              <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <div>
                  <h5 class="fw-bold mb-1 text-dark"><i class="bi bi-receipt-cutoff text-success me-2"></i>Official Invoices & Tax Receipts</h5>
                  <p class="small text-muted mb-0">Verified VAT/GST digital invoices for agricultural accounting.</p>
                </div>
                <div class="form-check form-switch">
                  <input class="form-check-input" type="checkbox" id="archiveSwitch" v-model="archiveFilter" />
                  <label class="form-check-label small fw-semibold text-muted" for="archiveSwitch">
                    Show Archived ({{ invoices.filter(i => i.isArchived).length }})
                  </label>
                </div>
              </div>

              <div v-if="filteredInvoices.length > 0" class="table-responsive">
                <table class="table table-hover align-middle">
                  <thead class="bg-light small text-uppercase text-muted">
                    <tr>
                      <th>Invoice #</th>
                      <th>Linked Order</th>
                      <th>Farmer / Seller</th>
                      <th>Date</th>
                      <th>Amount</th>
                      <th>Status</th>
                      <th class="text-end">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="inv in filteredInvoices" :key="inv.id">
                      <td class="fw-bold text-dark">{{ inv.invoiceNumber }}</td>
                      <td>Order #{{ inv.order?.orderNumber || inv.order?.id }}</td>
                      <td>{{ inv.farmer?.fullName }}</td>
                      <td class="small text-muted">{{ new Date(inv.issueDate).toLocaleDateString() }}</td>
                      <td class="fw-bold text-farmora-green">LKR {{ Number(inv.totalAmount).toLocaleString() }}</td>
                      <td>
                        <span class="badge px-3 py-1 rounded-pill" :class="inv.paymentStatus === 'PAID' ? 'bg-success' : 'bg-warning text-dark'">
                          {{ inv.paymentStatus }}
                        </span>
                      </td>
                      <td class="text-end">
                        <button class="btn btn-sm btn-outline-success py-1 px-3" @click="openReceiptModal(inv.order)">
                          <i class="bi bi-eye me-1"></i> View Receipt
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <EmptyState
                v-else
                icon="bi-receipt"
                title="No Invoices in This View"
                description="All your purchases automatically receive certified digital tax invoices."
              />
            </div>
          </div>

          <!-- TAB 4: BULK WHOLESALE CONTRACTS -->
          <div v-else-if="activeTab === 'bulk'">
            <div class="card border-0 shadow-sm p-4 bg-white rounded-3 mb-4">
              <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
                <div>
                  <h5 class="fw-bold mb-1 text-dark"><i class="bi bi-box-seam text-danger me-2"></i>B2B Wholesale Bulk Contracts</h5>
                  <p class="small text-muted mb-0">Commercial volume procurement with tier discounts and custom packaging.</p>
                </div>
                <router-link to="/products" class="btn btn-sm btn-outline-success fw-bold">
                  <i class="bi bi-plus-circle me-1"></i> New Bulk Request
                </router-link>
              </div>

              <div v-if="bulkOrders.length > 0" class="row g-3">
                <div v-for="b in bulkOrders" :key="b.id" class="col-md-6">
                  <div class="card border rounded-3 p-3 shadow-sm h-100 bg-light">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                      <span class="badge px-3 py-1 rounded-pill bg-primary">
                        {{ b.status }}
                      </span>
                      <strong class="text-dark">#{{ b.bulkOrderNumber }}</strong>
                    </div>

                    <h5 class="fw-bold text-dark mb-1">{{ b.product?.name }}</h5>
                    <div class="small text-muted mb-2">
                      Volume: <strong>{{ b.quantity }} {{ b.product?.unit || 'kg' }}</strong> &bull;
                      Rate: <strong>LKR {{ b.targetPricePerUnit }}/{{ b.product?.unit }}</strong>
                    </div>
                    <div class="small text-muted mb-2">
                      Valuation: <strong class="text-farmora-green fs-6">LKR {{ Number(b.totalEstimatedAmount).toLocaleString() }}</strong>
                    </div>
                    <div class="small text-muted mb-1">
                      Packaging: {{ b.packagingType }} &bull; {{ b.deliveryFrequency }}
                    </div>
                    <div class="small text-muted mb-3">
                      Farmer: {{ b.farmer?.fullName }} ({{ b.farmer?.phone }})
                    </div>
                    <div v-if="b.farmerNotes" class="alert alert-info py-1 px-2 small mb-3">
                      Farmer Note: "{{ b.farmerNotes }}"
                    </div>

                    <div class="d-flex justify-content-end gap-2 border-top pt-2 mt-auto">
                      <button
                        v-if="b.status === 'PENDING_QUOTE'"
                        class="btn btn-sm btn-outline-secondary"
                        @click="handleCancelBulkOrder(b)"
                      >
                        Cancel Contract
                      </button>
                      <button
                        v-if="b.status === 'PENDING_QUOTE'"
                        class="btn btn-sm btn-outline-success fw-semibold"
                        @click="openEditBulkModal(b)"
                      >
                        <i class="bi bi-pencil me-1"></i> Modify Quantity
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <EmptyState
                v-else
                icon="bi-box-seam"
                title="No Bulk Wholesale Orders"
                description="Institutional buyers, supermarkets, and restaurants can request high volume orders from any product page."
                actionText="Explore Marketplace"
                actionRoute="/products"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <OrderDetailsModal
      :show="showDetailsModal"
      :order="selectedOrder"
      role="BUYER"
      @close="showDetailsModal = false"
      @orderUpdated="loadAllData"
      @trackDelivery="openDeliveryTracker"
      @schedulePickup="openPickupModal"
      @viewReceipt="openReceiptModal"
      @payNow="openPaymentModal"
    />

    <DeliveryTrackerModal
      :show="showTrackerModal"
      :order="selectedOrder"
      @close="showTrackerModal = false"
      @deliveryUpdated="loadAllData"
    />

    <SchedulePickupModal
      :show="showPickupModal"
      :order="selectedOrder"
      :isFarmer="false"
      @close="showPickupModal = false"
      @pickupScheduled="loadAllData"
    />

    <PaymentModal
      :show="showPaymentModal"
      :order="selectedOrder"
      @close="showPaymentModal = false"
      @paymentSuccess="loadAllData"
    />

    <ReceiptModal
      :show="showReceiptModal"
      :order="selectedOrder"
      @close="showReceiptModal = false"
      @invoiceUpdated="loadAllData"
    />

    <BulkOrderModal
      :show="showBulkModal"
      :existingBulkOrder="selectedBulkOrder"
      @close="showBulkModal = false"
      @bulkOrderUpdated="loadAllData"
    />

    <!-- Review Modal -->
    <div
      v-if="showReviewModal"
      class="modal fade show d-block"
      tabindex="-1"
      style="background-color: rgba(0, 0, 0, 0.5); z-index: 1060;"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header border-bottom">
            <h5 class="modal-title fw-bold"><i class="bi bi-star-fill text-warning me-2"></i>Review & Rate Farmer</h5>
            <button type="button" class="btn-close" @click="showReviewModal = false"></button>
          </div>
          <div class="modal-body">
            <div v-if="reviewSuccess" class="alert alert-success small">{{ reviewSuccess }}</div>
            <div v-if="reviewError" class="alert alert-danger small">{{ reviewError }}</div>

            <p class="small text-muted mb-3">
              Share feedback on produce freshness, packaging, and fulfillment with farmer
              <strong>{{ selectedOrder?.farmer?.fullName }}</strong> for <strong>{{ selectedOrder?.product?.name }}</strong>.
            </p>

            <div class="mb-3">
              <label class="form-label small fw-semibold">Quality & Fulfillment Rating</label>
              <div class="d-flex gap-2">
                <button
                  v-for="star in 5"
                  :key="star"
                  type="button"
                  class="btn btn-sm"
                  :class="star <= reviewRating ? 'btn-warning text-white' : 'btn-outline-secondary'"
                  @click="reviewRating = star"
                >
                  <i class="bi bi-star-fill"></i> {{ star }}
                </button>
              </div>
            </div>

            <div class="mb-3">
              <label class="form-label small fw-semibold">Review Comments</label>
              <textarea
                class="form-control"
                rows="3"
                v-model="reviewComment"
                placeholder="How was the produce quality, timely dispatch, and taste?..."
              ></textarea>
            </div>
          </div>
          <div class="modal-footer border-top-0">
            <button type="button" class="btn btn-light btn-sm" @click="showReviewModal = false">Cancel</button>
            <button
              type="button"
              class="btn btn-farmora btn-sm"
              @click="submitReview"
              :disabled="submittingReview"
            >
              <span v-if="submittingReview" class="spinner-border spinner-border-sm me-1"></span>
              Submit Review
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.custom-nav-pills .nav-link {
  color: #495057;
  border-radius: 0.5rem;
}
.custom-nav-pills .nav-link.active {
  background-color: #2d6a4f !important;
  color: #fff !important;
}
.text-farmora-green {
  color: #2d6a4f !important;
}
.btn-farmora {
  background-color: #2d6a4f;
  color: #fff;
}
.btn-farmora:hover {
  background-color: #1b4332;
  color: #fff;
}
</style>

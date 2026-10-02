<script setup>
import { onMounted, ref, computed } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import OrderCard from '../../components/orders/OrderCard.vue'
import OrderDetailsModal from '../../components/orders/OrderDetailsModal.vue'
import SchedulePickupModal from '../../components/orders/SchedulePickupModal.vue'
import ReceiptModal from '../../components/orders/ReceiptModal.vue'

import { orderService } from '../../services/orderService'
import { pickupService } from '../../services/pickupService'
import { invoiceService } from '../../services/invoiceService'
import { bulkOrderService } from '../../services/bulkOrderService'

const activeTab = ref('orders')

const orders = ref([])
const pickups = ref([])
const invoices = ref([])
const bulkOrders = ref([])

const loading = ref(true)
const selectedOrder = ref(null)

const showDetailsModal = ref(false)
const showPickupModal = ref(false)
const showReceiptModal = ref(false)

const loadAllData = async () => {
  loading.value = true
  try {
    const [ordersRes, pickupsRes, invoicesRes, bulkRes] = await Promise.allSettled([
      orderService.getMyOrders(),
      pickupService.getMyPickups(),
      invoiceService.getMyInvoices(),
      bulkOrderService.getMyBulkOrders()
    ])

    if (ordersRes.status === 'fulfilled') orders.value = ordersRes.value || []
    if (pickupsRes.status === 'fulfilled') pickups.value = pickupsRes.value || []
    if (invoicesRes.status === 'fulfilled') invoices.value = invoicesRes.value || []
    if (bulkRes.status === 'fulfilled') bulkOrders.value = bulkRes.value || []
  } catch (err) {
    console.error('Failed to load farmer data', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAllData()
})

const handleUpdateStatus = async (orderId, status) => {
  try {
    await orderService.updateOrderStatus(orderId, status)
    loadAllData()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to update order status.')
  }
}

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

// Bulk order actions
const handleApproveBulk = async (bulkOrder) => {
  const notes = prompt('Enter confirmation note for buyer (e.g. Harvest packaged on Thursday, ready for truck loading):')
  try {
    await bulkOrderService.updateStatus(bulkOrder.id, 'APPROVED', notes || 'Approved by producer')
    loadAllData()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to approve bulk contract.')
  }
}

const handleRejectBulk = async (bulkOrder) => {
  const reason = prompt('Enter reason for declining bulk order (e.g. Insufficient yield, price too low):')
  if (reason === null) return
  try {
    await bulkOrderService.updateStatus(bulkOrder.id, 'REJECTED', reason)
    loadAllData()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to reject bulk order.')
  }
}

const openDetails = (order) => {
  selectedOrder.value = order
  showDetailsModal.value = true
}

const openPickup = (order) => {
  selectedOrder.value = order
  showPickupModal.value = true
}

const openReceipt = (order) => {
  selectedOrder.value = order
  showReceiptModal.value = true
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="orders" />
      </div>

      <div class="col-lg-9">
        <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
          <div>
            <h4 class="fw-bold mb-1">
              <i class="bi bi-truck text-success me-2"></i>Farmer Logistics & Shipments
            </h4>
            <p class="text-muted small mb-0">Track customer orders, prepare farm gate pickups, and manage commercial bulk contracts.</p>
          </div>
        </div>

        <!-- Tabs -->
        <ul class="nav nav-pills custom-nav-pills mb-4 p-1 bg-light rounded-3 shadow-sm border">
          <li class="nav-item">
            <button
              class="nav-link py-2 px-3 fw-bold small rounded-3"
              :class="{ active: activeTab === 'orders' }"
              @click="activeTab = 'orders'"
            >
              <i class="bi bi-bag-check me-1"></i> Received Orders ({{ orders.length }})
            </button>
          </li>
          <li class="nav-item">
            <button
              class="nav-link py-2 px-3 fw-bold small rounded-3"
              :class="{ active: activeTab === 'pickups' }"
              @click="activeTab = 'pickups'"
            >
              <i class="bi bi-shop me-1"></i> Farm Gate Pickups ({{ pickups.length }})
            </button>
          </li>
          <li class="nav-item">
            <button
              class="nav-link py-2 px-3 fw-bold small rounded-3"
              :class="{ active: activeTab === 'bulk' }"
              @click="activeTab = 'bulk'"
            >
              <i class="bi bi-box-seam me-1"></i> Bulk Enquiries ({{ bulkOrders.length }})
            </button>
          </li>
          <li class="nav-item">
            <button
              class="nav-link py-2 px-3 fw-bold small rounded-3"
              :class="{ active: activeTab === 'invoices' }"
              @click="activeTab = 'invoices'"
            >
              <i class="bi bi-receipt me-1"></i> Financial Invoices ({{ invoices.length }})
            </button>
          </li>
        </ul>

        <LoadingSpinner v-if="loading" message="Loading your orders and logistics hub..." />

        <div v-else>
          <!-- TAB 1: ORDERS -->
          <div v-if="activeTab === 'orders'">
            <div v-if="orders.length > 0">
              <OrderCard
                v-for="order in orders"
                :key="order.id"
                :order="order"
                role="FARMER"
                @updateStatus="handleUpdateStatus"
                @viewDetails="openDetails"
                @schedulePickup="openPickup"
                @viewReceipt="openReceipt"
                @cancelOrder="handleCancelOrder"
              />
            </div>
            <EmptyState
              v-else
              icon="bi-receipt"
              title="No Orders Received Yet"
              description="When buyers accept direct purchases or you confirm bids, orders will be tracked here for preparation and delivery."
            />
          </div>

          <!-- TAB 2: FARM PICKUPS -->
          <div v-else-if="activeTab === 'pickups'">
            <div class="card border-0 shadow-sm p-4 bg-white rounded-3 mb-4">
              <h5 class="fw-bold mb-1 text-dark"><i class="bi bi-shop text-primary me-2"></i>Farm Depot Pickup Handover</h5>
              <p class="small text-muted mb-4">Verify buyer gate passcodes to complete harvest collection.</p>

              <div v-if="pickups.length > 0" class="row g-3">
                <div v-for="p in pickups" :key="p.id" class="col-md-6">
                  <div class="card border rounded-3 p-3 shadow-sm h-100 bg-light">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                      <span
                        class="badge px-3 py-1 rounded-pill"
                        :class="p.status === 'COMPLETED' ? 'bg-success' : 'bg-warning text-dark'"
                      >
                        {{ p.status }}
                      </span>
                      <span class="small text-muted">Passcode: <strong>{{ p.passcode }}</strong></span>
                    </div>

                    <h6 class="fw-bold text-dark mb-1">{{ p.order?.product?.name }}</h6>
                    <div class="small text-muted mb-1">
                      Buyer: <strong>{{ p.buyer?.fullName }}</strong> ({{ p.contactPhone || p.buyer?.phone }})
                    </div>
                    <div class="small text-muted mb-1">
                      Scheduled: <strong>{{ p.pickupDate }}</strong> &bull; {{ p.timeSlot }}
                    </div>
                    <div class="small text-muted mb-3">
                      Transport: {{ p.vehicleNumber || 'Standard Transport' }}
                    </div>

                    <div class="d-flex justify-content-end gap-2 border-top pt-2 mt-auto">
                      <button class="btn btn-sm btn-outline-success fw-semibold" @click="openPickup(p.order)">
                        <i class="bi bi-shield-check me-1"></i> Verify & Handover
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <EmptyState
                v-else
                icon="bi-shop"
                title="No Farm Pickups Scheduled"
                description="When buyers schedule farm gate collection, appointments will appear here."
              />
            </div>
          </div>

          <!-- TAB 3: BULK ORDERS -->
          <div v-else-if="activeTab === 'bulk'">
            <div class="card border-0 shadow-sm p-4 bg-white rounded-3 mb-4">
              <h5 class="fw-bold mb-1 text-dark"><i class="bi bi-box-seam text-danger me-2"></i>Incoming Commercial Bulk Contracts</h5>
              <p class="small text-muted mb-4">Review wholesale price quotes and contract supply requests from commercial buyers.</p>

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
                    <div class="small text-muted mb-1">
                      Buyer: <strong>{{ b.buyer?.fullName }}</strong> ({{ b.buyer?.email }})
                    </div>
                    <div class="small text-muted mb-1">
                      Requested Volume: <strong>{{ b.quantity }} {{ b.product?.unit || 'kg' }}</strong> @ LKR {{ b.targetPricePerUnit }}
                    </div>
                    <div class="small text-muted mb-1">
                      Total Contract: <strong class="text-farmora-green">LKR {{ Number(b.totalEstimatedAmount).toLocaleString() }}</strong>
                    </div>
                    <div class="small text-muted mb-3">
                      Packaging: {{ b.packagingType }} &bull; Frequency: {{ b.deliveryFrequency }}
                    </div>

                    <div v-if="b.status === 'PENDING_QUOTE'" class="d-flex justify-content-end gap-2 border-top pt-2 mt-auto">
                      <button class="btn btn-sm btn-outline-danger" @click="handleRejectBulk(b)">
                        Decline
                      </button>
                      <button class="btn btn-sm btn-farmora fw-semibold" @click="handleApproveBulk(b)">
                        <i class="bi bi-check2-circle me-1"></i> Accept Contract
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <EmptyState
                v-else
                icon="bi-box-seam"
                title="No Bulk Inquiries"
                description="Commercial buyers will contact you for wholesale bulk orders when viewing your produce."
              />
            </div>
          </div>

          <!-- TAB 4: FINANCIAL INVOICES -->
          <div v-else-if="activeTab === 'invoices'">
            <div class="card border-0 shadow-sm p-4 bg-white rounded-3 mb-4">
              <h5 class="fw-bold mb-1 text-dark"><i class="bi bi-receipt-cutoff text-success me-2"></i>Fulfilled Orders Invoices & Settlements</h5>
              <p class="small text-muted mb-4">Digital tax invoices generated for your crop sales.</p>

              <div v-if="invoices.length > 0" class="table-responsive">
                <table class="table table-hover align-middle">
                  <thead class="bg-light small text-uppercase text-muted">
                    <tr>
                      <th>Invoice #</th>
                      <th>Order</th>
                      <th>Buyer</th>
                      <th>Date</th>
                      <th>Total (LKR)</th>
                      <th>Status</th>
                      <th class="text-end">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="inv in invoices" :key="inv.id">
                      <td class="fw-bold text-dark">{{ inv.invoiceNumber }}</td>
                      <td>#{{ inv.order?.orderNumber || inv.order?.id }}</td>
                      <td>{{ inv.buyer?.fullName }}</td>
                      <td class="small text-muted">{{ new Date(inv.issueDate).toLocaleDateString() }}</td>
                      <td class="fw-bold text-farmora-green">LKR {{ Number(inv.totalAmount).toLocaleString() }}</td>
                      <td>
                        <span class="badge px-3 py-1 rounded-pill" :class="inv.paymentStatus === 'PAID' ? 'bg-success' : 'bg-warning text-dark'">
                          {{ inv.paymentStatus }}
                        </span>
                      </td>
                      <td class="text-end">
                        <button class="btn btn-sm btn-outline-success py-1 px-3" @click="openReceipt(inv.order)">
                          View
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <EmptyState
                v-else
                icon="bi-receipt"
                title="No Invoices Recorded"
                description="As your produce orders are processed, invoices will appear here."
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
      role="FARMER"
      @close="showDetailsModal = false"
      @orderUpdated="loadAllData"
      @schedulePickup="openPickup"
      @viewReceipt="openReceipt"
    />

    <SchedulePickupModal
      :show="showPickupModal"
      :order="selectedOrder"
      :isFarmer="true"
      @close="showPickupModal = false"
      @pickupScheduled="loadAllData"
    />

    <ReceiptModal
      :show="showReceiptModal"
      :order="selectedOrder"
      @close="showReceiptModal = false"
      @invoiceUpdated="loadAllData"
    />
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

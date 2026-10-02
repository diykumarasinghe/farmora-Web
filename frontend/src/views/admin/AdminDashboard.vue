<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import { adminService } from '../../services/adminService'

const stats = ref(null)
const loading = ref(true)
const activeTab = ref('overview') // 'overview' | 'verifications' | 'users' | 'transactions' | 'complaints'

const verifications = ref([])
const users = ref([])
const transactions = ref([])
const complaints = ref([])

const loadAdminData = async () => {
  loading.value = true
  try {
    const [statsData, verifData, usersData, transData, compData] = await Promise.all([
      adminService.getStats().catch(() => null),
      adminService.getVerifications().catch(() => []),
      adminService.getUsers().catch(() => []),
      adminService.getTransactions().catch(() => []),
      adminService.getComplaints().catch(() => [])
    ])
    stats.value = statsData
    verifications.value = verifData || []
    users.value = usersData || []
    transactions.value = transData || []
    complaints.value = compData || []
  } catch (err) {
    console.error('Failed to load admin management data', err)
  } finally {
    loading.value = false
  }
}

// Verification Actions
const approveVerification = async (farmId) => {
  const notes = prompt('Enter approval verification remarks:', 'All organic accreditation and land deeds verified.')
  if (notes === null) return
  try {
    await adminService.approveFarmerVerification(farmId, notes)
    const f = verifications.value.find(item => item.id === farmId)
    if (f) f.verificationStatus = 'VERIFIED'
  } catch (err) {
    console.error('Failed to approve farmer verification', err)
  }
}

const rejectVerification = async (farmId) => {
  const notes = prompt('Enter reason for rejecting verification:', 'Incomplete agrarian center registration or blurry certificates.')
  if (!notes) return
  try {
    await adminService.rejectFarmerVerification(farmId, notes)
    const f = verifications.value.find(item => item.id === farmId)
    if (f) f.verificationStatus = 'REJECTED'
  } catch (err) {
    console.error('Failed to reject farmer verification', err)
  }
}

// User Actions
const toggleUser = async (userId) => {
  try {
    const updated = await adminService.toggleUserStatus(userId)
    const u = users.value.find(item => item.id === userId)
    if (u) u.active = updated.active
  } catch (err) {
    console.error('Failed to toggle user status', err)
  }
}

// Complaint Actions
const updateComplaint = async (complaintId, status) => {
  const notes = prompt(`Enter resolution notes for setting status to ${status}:`, 'Case examined with farmer and order refund reviewed.')
  if (notes === null) return
  try {
    await adminService.updateComplaintStatus(complaintId, status, notes)
    const c = complaints.value.find(item => item.id === complaintId)
    if (c) {
      c.status = status
      c.adminNotes = notes
    }
  } catch (err) {
    console.error('Failed to update complaint', err)
  }
}

onMounted(() => {
  loadAdminData()
})
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="dashboard" />
      </div>

      <div class="col-lg-9">
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
          <div>
            <h3 class="fw-bold mb-1">Administrative Control & Verification Center</h3>
            <p class="text-muted small mb-0">Platform oversight, farmer credibility approval, transaction monitoring, and dispute resolution.</p>
          </div>
        </div>

        <!-- Management Navigation Tabs -->
        <div class="nav nav-pills bg-white p-1 rounded-pill border shadow-sm mb-4 d-inline-flex flex-wrap">
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-bold"
            :class="{ active: activeTab === 'overview' }"
            @click="activeTab = 'overview'"
          >
            <i class="bi bi-speedometer2 me-1"></i> Overview
          </button>
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-bold"
            :class="{ active: activeTab === 'verifications' }"
            @click="activeTab = 'verifications'"
          >
            <i class="bi bi-patch-check-fill text-success me-1"></i> Farmer Verifications
            <span v-if="verifications.filter(v => v.verificationStatus === 'PENDING').length > 0" class="badge bg-danger rounded-pill ms-1">
              {{ verifications.filter(v => v.verificationStatus === 'PENDING').length }}
            </span>
          </button>
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-bold"
            :class="{ active: activeTab === 'users' }"
            @click="activeTab = 'users'"
          >
            <i class="bi bi-people-fill text-primary me-1"></i> User Accounts
          </button>
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-bold"
            :class="{ active: activeTab === 'transactions' }"
            @click="activeTab = 'transactions'"
          >
            <i class="bi bi-credit-card-fill text-info me-1"></i> Transactions Monitor
          </button>
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-bold"
            :class="{ active: activeTab === 'complaints' }"
            @click="activeTab = 'complaints'"
          >
            <i class="bi bi-exclamation-octagon-fill text-danger me-1"></i> Dispute & Complaints
            <span v-if="complaints.filter(c => c.status === 'PENDING').length > 0" class="badge bg-warning text-dark rounded-pill ms-1">
              {{ complaints.filter(c => c.status === 'PENDING').length }}
            </span>
          </button>
        </div>

        <LoadingSpinner v-if="loading" message="Loading administrative data..." />

        <template v-else>
          <!-- ============================================================
               TAB 1: OVERVIEW METRICS
               ============================================================ -->
          <div v-if="activeTab === 'overview' && stats">
            <div class="row g-3 mb-4">
              <div class="col-sm-6 col-md-4">
                <div class="card border-0 shadow-sm p-3 rounded-4 bg-white">
                  <div class="d-flex align-items-center justify-content-between">
                    <div>
                      <div class="text-muted small fw-semibold">Registered Users</div>
                      <div class="fs-3 fw-bold text-dark">{{ stats.totalUsers }}</div>
                    </div>
                    <div class="rounded-circle bg-primary bg-opacity-10 text-primary p-3">
                      <i class="bi bi-people-fill fs-3"></i>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-sm-6 col-md-4">
                <div class="card border-0 shadow-sm p-3 rounded-4 bg-white">
                  <div class="d-flex align-items-center justify-content-between">
                    <div>
                      <div class="text-muted small fw-semibold">Listed Crops</div>
                      <div class="fs-3 fw-bold text-success">{{ stats.totalProducts }}</div>
                    </div>
                    <div class="rounded-circle bg-success bg-opacity-10 text-success p-3">
                      <i class="bi bi-boxes fs-3"></i>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-sm-6 col-md-4">
                <div class="card border-0 shadow-sm p-3 rounded-4 bg-white">
                  <div class="d-flex align-items-center justify-content-between">
                    <div>
                      <div class="text-muted small fw-semibold">Total Offers (Bids)</div>
                      <div class="fs-3 fw-bold text-warning">{{ stats.totalBids }}</div>
                    </div>
                    <div class="rounded-circle bg-warning bg-opacity-10 text-warning p-3">
                      <i class="bi bi-tags-fill fs-3"></i>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-sm-6 col-md-6">
                <div class="card border-0 shadow-sm p-3 rounded-4 bg-white">
                  <div class="d-flex align-items-center justify-content-between">
                    <div>
                      <div class="text-muted small fw-semibold">Fulfilled Orders</div>
                      <div class="fs-3 fw-bold text-info">{{ stats.totalOrders }}</div>
                    </div>
                    <div class="rounded-circle bg-info bg-opacity-10 text-info p-3">
                      <i class="bi bi-cart-check-fill fs-3"></i>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-sm-12 col-md-6">
                <div class="card border-0 shadow-sm p-3 rounded-4 bg-white">
                  <div class="d-flex align-items-center justify-content-between">
                    <div>
                      <div class="text-muted small fw-semibold">Total Platform Volume</div>
                      <div class="fs-3 fw-bold text-success">
                        Rs. {{ Number(stats.totalRevenue || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}
                      </div>
                    </div>
                    <div class="rounded-circle bg-success bg-opacity-10 text-success p-3">
                      <i class="bi bi-currency-dollar fs-3"></i>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- ============================================================
               TAB 2: FARMER VERIFICATIONS (APPROVE / REJECT)
               ============================================================ -->
          <div v-else-if="activeTab === 'verifications'">
            <div class="card border-0 shadow-sm rounded-4 p-4 bg-white mb-4">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                  <h5 class="fw-bold mb-1"><i class="bi bi-patch-check text-success me-2"></i>Farmer Credibility Verifications</h5>
                  <p class="text-muted small mb-0">Approve or reject farmer credential verification requests</p>
                </div>
              </div>

              <div v-if="verifications.length > 0" class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                  <thead class="table-light small text-muted">
                    <tr>
                      <th>Farm Name / Owner</th>
                      <th>Location</th>
                      <th>Method</th>
                      <th>Status</th>
                      <th class="text-end">Verification Actions</th>
                    </tr>
                  </thead>
                  <tbody class="small">
                    <tr v-for="farm in verifications" :key="farm.id">
                      <td>
                        <div class="fw-bold text-dark">{{ farm.farmName || 'Farm #' + farm.id }}</div>
                        <small class="text-muted">{{ farm.farmer?.fullName }} ({{ farm.farmer?.phone }})</small>
                      </td>
                      <td>{{ farm.district }}, {{ farm.province }}</td>
                      <td><span class="badge bg-light text-secondary border rounded-pill">{{ farm.farmingMethod || 'CONVENTIONAL' }}</span></td>
                      <td>
                        <span
                          class="badge rounded-pill"
                          :class="farm.verificationStatus === 'VERIFIED' ? 'bg-success' : (farm.verificationStatus === 'PENDING' ? 'bg-warning text-dark' : 'bg-danger')"
                        >
                          {{ farm.verificationStatus }}
                        </span>
                      </td>
                      <td class="text-end">
                        <div class="d-flex justify-content-end gap-1">
                          <button
                            v-if="farm.verificationStatus !== 'VERIFIED'"
                            class="btn btn-sm btn-success rounded-pill px-3"
                            @click="approveVerification(farm.id)"
                          >
                            <i class="bi bi-check-lg me-1"></i> Approve
                          </button>
                          <button
                            v-if="farm.verificationStatus !== 'REJECTED'"
                            class="btn btn-sm btn-outline-danger rounded-pill px-3"
                            @click="rejectVerification(farm.id)"
                          >
                            <i class="bi bi-x-lg me-1"></i> Reject
                          </button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <EmptyState
                v-else
                icon="bi-shield-check"
                title="No Farm Verifications"
                description="There are currently no farm profiles in the database."
              />
            </div>
          </div>

          <!-- ============================================================
               TAB 3: USER ACCOUNTS MANAGEMENT
               ============================================================ -->
          <div v-else-if="activeTab === 'users'">
            <div class="card border-0 shadow-sm rounded-4 p-4 bg-white mb-4">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-bold mb-0"><i class="bi bi-people-fill text-primary me-2"></i>Platform User Directory</h5>
                <span class="badge bg-primary rounded-pill px-3 py-2">{{ users.length }} Total Accounts</span>
              </div>

              <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                  <thead class="table-light small text-muted">
                    <tr>
                      <th>User</th>
                      <th>Email & Phone</th>
                      <th>Role</th>
                      <th>Status</th>
                      <th class="text-end">Toggle Active</th>
                    </tr>
                  </thead>
                  <tbody class="small">
                    <tr v-for="u in users" :key="u.id">
                      <td>
                        <div class="fw-bold text-dark">{{ u.fullName }}</div>
                        <small class="text-muted">ID: #{{ u.id }}</small>
                      </td>
                      <td>
                        <div>{{ u.email }}</div>
                        <small class="text-muted">{{ u.phone }}</small>
                      </td>
                      <td>
                        <span
                          class="badge rounded-pill"
                          :class="u.role === 'ADMIN' ? 'bg-danger' : (u.role === 'FARMER' ? 'bg-success' : (u.role === 'BUYER' ? 'bg-primary' : 'bg-secondary'))"
                        >
                          {{ u.role }}
                        </span>
                      </td>
                      <td>
                        <span class="badge rounded-pill" :class="u.active ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger'">
                          {{ u.active ? 'ACTIVE' : 'DISABLED' }}
                        </span>
                      </td>
                      <td class="text-end">
                        <button
                          class="btn btn-sm rounded-pill px-3"
                          :class="u.active ? 'btn-outline-danger' : 'btn-outline-success'"
                          @click="toggleUser(u.id)"
                        >
                          {{ u.active ? 'Deactivate' : 'Activate' }}
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <!-- ============================================================
               TAB 4: TRANSACTIONS & REVENUE MONITORING
               ============================================================ -->
          <div v-else-if="activeTab === 'transactions'">
            <div class="card border-0 shadow-sm rounded-4 p-4 bg-white mb-4">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-bold mb-0"><i class="bi bi-credit-card-2-front-fill text-info me-2"></i>Financial & Payment Transactions</h5>
                <span class="badge bg-info text-white rounded-pill px-3 py-2">{{ transactions.length }} Payments</span>
              </div>

              <div v-if="transactions.length > 0" class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                  <thead class="table-light small text-muted">
                    <tr>
                      <th>Transaction Ref</th>
                      <th>Order ID</th>
                      <th>Payer (Buyer)</th>
                      <th>Amount</th>
                      <th>Method</th>
                      <th>Status</th>
                      <th>Date</th>
                    </tr>
                  </thead>
                  <tbody class="small">
                    <tr v-for="t in transactions" :key="t.id">
                      <td><code>{{ t.transactionReference }}</code></td>
                      <td><strong>#{{ t.orderId }}</strong></td>
                      <td>{{ t.buyerName || 'Buyer' }}</td>
                      <td class="fw-bold text-success">Rs. {{ Number(t.amount).toLocaleString() }}</td>
                      <td><span class="badge bg-light text-secondary border rounded-pill">{{ t.method }}</span></td>
                      <td>
                        <span class="badge rounded-pill" :class="t.status === 'COMPLETED' ? 'bg-success' : (t.status === 'REFUNDED' ? 'bg-info' : 'bg-warning text-dark')">
                          {{ t.status }}
                        </span>
                      </td>
                      <td class="text-muted" style="font-size: 0.72rem;">{{ new Date(t.createdAt).toLocaleDateString() }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <EmptyState
                v-else
                icon="bi-cash-coin"
                title="No Transactions Logged"
                description="Completed order payment slips and card transactions will stream here in real time."
              />
            </div>
          </div>

          <!-- ============================================================
               TAB 5: COMPLAINTS & DISPUTE RESOLUTION
               ============================================================ -->
          <div v-else-if="activeTab === 'complaints'">
            <div class="card border-0 shadow-sm rounded-4 p-4 bg-white mb-4">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-bold mb-0"><i class="bi bi-shield-exclamation text-danger me-2"></i>Grievance & Dispute Center</h5>
                <span class="badge bg-danger rounded-pill px-3 py-2">{{ complaints.length }} Complaints</span>
              </div>

              <div v-if="complaints.length > 0" class="d-flex flex-column gap-3">
                <div
                  v-for="c in complaints"
                  :key="c.id"
                  class="border rounded-4 p-3 bg-light bg-opacity-50"
                >
                  <div class="d-flex justify-content-between align-items-start mb-2">
                    <div>
                      <span class="badge bg-danger bg-opacity-10 text-danger rounded-pill px-2 py-1 small me-2">Ticket #{{ c.id }}</span>
                      <strong class="text-dark">{{ c.subject }}</strong>
                    </div>
                    <span
                      class="badge rounded-pill"
                      :class="c.status === 'RESOLVED' ? 'bg-success' : (c.status === 'INVESTIGATING' ? 'bg-warning text-dark' : 'bg-danger')"
                    >
                      {{ c.status }}
                    </span>
                  </div>

                  <p class="small text-secondary mb-2">{{ c.description }}</p>

                  <div class="small text-muted mb-2 d-flex flex-wrap gap-3">
                    <span>Complainant: <strong>{{ c.complainant?.fullName }}</strong> ({{ c.complainant?.role }})</span>
                    <span v-if="c.targetUser">Target: <strong>{{ c.targetUser.fullName }}</strong></span>
                    <span v-if="c.orderId">Order: <strong>#{{ c.orderId }}</strong></span>
                    <span>Date: {{ new Date(c.createdAt).toLocaleDateString() }}</span>
                  </div>

                  <div v-if="c.adminNotes" class="alert alert-info py-2 small mb-2">
                    <i class="bi bi-info-circle-fill me-1"></i> Admin Resolution: {{ c.adminNotes }}
                  </div>

                  <div class="d-flex justify-content-end gap-2 pt-2 border-top">
                    <button
                      v-if="c.status !== 'INVESTIGATING'"
                      class="btn btn-sm btn-outline-warning rounded-pill px-3"
                      @click="updateComplaint(c.id, 'INVESTIGATING')"
                    >
                      Investigate
                    </button>
                    <button
                      v-if="c.status !== 'RESOLVED'"
                      class="btn btn-sm btn-success rounded-pill px-3"
                      @click="updateComplaint(c.id, 'RESOLVED')"
                    >
                      Mark Resolved
                    </button>
                    <button
                      v-if="c.status !== 'DISMISSED'"
                      class="btn btn-sm btn-outline-secondary rounded-pill px-3"
                      @click="updateComplaint(c.id, 'DISMISSED')"
                    >
                      Dismiss
                    </button>
                  </div>
                </div>
              </div>
              <EmptyState
                v-else
                icon="bi-shield-check"
                title="Zero Complaints"
                description="No active disputes, grievances, or damaged shipment reports have been submitted."
              />
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

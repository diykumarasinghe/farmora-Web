<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { adminService } from '../../services/adminService'

const stats = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    stats.value = await adminService.getStats()
  } catch (err) {
    console.error('Failed to load admin stats', err)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="dashboard" />
      </div>

      <div class="col-lg-9">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h3 class="fw-bold mb-1">Administrative Control Center</h3>
            <p class="text-muted small mb-0">Monitor agricultural ecosystem metrics, platform users, and global orders.</p>
          </div>
        </div>

        <LoadingSpinner v-if="loading" message="Gathering system analytics..." />

        <template v-else-if="stats">
          <!-- Metrics Grid -->
          <div class="row g-3 mb-4">
            <div class="col-sm-6 col-md-4">
              <div class="card border-0 shadow-sm p-3">
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
              <div class="card border-0 shadow-sm p-3">
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
              <div class="card border-0 shadow-sm p-3">
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
              <div class="card border-0 shadow-sm p-3">
                <div class="d-flex align-items-center justify-content-between">
                  <div>
                    <div class="text-muted small fw-semibold">Fulfilled / Active Orders</div>
                    <div class="fs-3 fw-bold text-info">{{ stats.totalOrders }}</div>
                  </div>
                  <div class="rounded-circle bg-info bg-opacity-10 text-info p-3">
                    <i class="bi bi-cart-check-fill fs-3"></i>
                  </div>
                </div>
              </div>
            </div>

            <div class="col-sm-12 col-md-6">
              <div class="card border-0 shadow-sm p-3">
                <div class="d-flex align-items-center justify-content-between">
                  <div>
                    <div class="text-muted small fw-semibold">Gross Trading Volume</div>
                    <div class="fs-3 fw-bold text-success">${{ Number(stats.totalRevenue).toFixed(2) }}</div>
                  </div>
                  <div class="rounded-circle bg-success bg-opacity-10 text-success p-3">
                    <i class="bi bi-currency-dollar fs-3"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Fast Navigation Modules -->
          <div class="row g-3">
            <div class="col-md-4">
              <router-link to="/admin/users" class="card border-0 shadow-sm p-4 text-decoration-none text-dark h-100">
                <i class="bi bi-person-gear fs-2 text-primary mb-2"></i>
                <h5 class="fw-bold">User Directory</h5>
                <p class="text-muted small mb-0">View all farmers, buyers, delivery drivers, and toggle account activation.</p>
              </router-link>
            </div>
            <div class="col-md-4">
              <router-link to="/admin/products" class="card border-0 shadow-sm p-4 text-decoration-none text-dark h-100">
                <i class="bi bi-box-seam fs-2 text-success mb-2"></i>
                <h5 class="fw-bold">Crop Listings</h5>
                <p class="text-muted small mb-0">Audit marketplace produce items and manage listing statuses.</p>
              </router-link>
            </div>
            <div class="col-md-4">
              <router-link to="/admin/orders" class="card border-0 shadow-sm p-4 text-decoration-none text-dark h-100">
                <i class="bi bi-receipt fs-2 text-info mb-2"></i>
                <h5 class="fw-bold">Market Transactions</h5>
                <p class="text-muted small mb-0">Inspect real-time orders, pricing settlements, and delivery addresses.</p>
              </router-link>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

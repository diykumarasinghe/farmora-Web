<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { bidService } from '../../services/bidService'
import { orderService } from '../../services/orderService'

const loading = ref(true)
const myBids = ref([])
const myOrders = ref([])

onMounted(async () => {
  try {
    const [bids, orders] = await Promise.all([
      bidService.getMyBids(),
      orderService.getMyOrders()
    ])
    myBids.value = bids
    myOrders.value = orders
  } catch (err) {
    console.error('Failed to load buyer data', err)
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
            <h3 class="fw-bold mb-1">Buyer Dashboard</h3>
            <p class="text-muted small mb-0">Track your bids, purchased shipments, and discover fresh farm produce.</p>
          </div>
          <router-link to="/products" class="btn btn-farmora btn-sm">
            <i class="bi bi-search me-1"></i> Explore Market
          </router-link>
        </div>

        <LoadingSpinner v-if="loading" message="Loading buyer information..." />

        <template v-else>
          <!-- Metric Highlights -->
          <div class="row g-3 mb-4">
            <div class="col-sm-4">
              <div class="card border-0 shadow-sm p-3">
                <div class="d-flex align-items-center justify-content-between">
                  <div>
                    <div class="text-muted small fw-semibold">Active Bids</div>
                    <div class="fs-4 fw-bold text-warning">{{ myBids.filter(b => b.status === 'PENDING').length }}</div>
                  </div>
                  <div class="rounded-circle bg-warning bg-opacity-10 text-warning p-3">
                    <i class="bi bi-clock-history fs-4"></i>
                  </div>
                </div>
              </div>
            </div>

            <div class="col-sm-4">
              <div class="card border-0 shadow-sm p-3">
                <div class="d-flex align-items-center justify-content-between">
                  <div>
                    <div class="text-muted small fw-semibold">Accepted Bids</div>
                    <div class="fs-4 fw-bold text-success">{{ myBids.filter(b => b.status === 'ACCEPTED').length }}</div>
                  </div>
                  <div class="rounded-circle bg-success bg-opacity-10 text-success p-3">
                    <i class="bi bi-check2-circle fs-4"></i>
                  </div>
                </div>
              </div>
            </div>

            <div class="col-sm-4">
              <div class="card border-0 shadow-sm p-3">
                <div class="d-flex align-items-center justify-content-between">
                  <div>
                    <div class="text-muted small fw-semibold">Total Orders</div>
                    <div class="fs-4 fw-bold text-primary">{{ myOrders.length }}</div>
                  </div>
                  <div class="rounded-circle bg-primary bg-opacity-10 text-primary p-3">
                    <i class="bi bi-bag-check-fill fs-4"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Recent Purchases -->
          <div class="card border-0 shadow-sm mb-4">
            <div class="card-header bg-white border-0 pt-3 d-flex justify-content-between align-items-center">
              <h5 class="fw-bold mb-0">Recent Purchases</h5>
              <router-link to="/buyer/orders" class="small text-decoration-none text-success fw-bold">
                View All <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
            <div class="card-body p-0">
              <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                  <thead>
                    <tr>
                      <th>Order ID</th>
                      <th>Produce</th>
                      <th>Farmer</th>
                      <th>Total Amount</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="order in myOrders.slice(0, 5)" :key="order.id">
                      <td class="fw-bold">#{{ order.id }}</td>
                      <td>{{ order.product?.name }}</td>
                      <td>{{ order.farmer?.fullName }}</td>
                      <td class="text-success fw-bold">${{ Number(order.totalAmount).toFixed(2) }}</td>
                      <td>
                        <span class="badge bg-light text-dark border">{{ order.status }}</span>
                      </td>
                    </tr>
                    <tr v-if="myOrders.length === 0">
                      <td colspan="5" class="text-center py-4 text-muted">No orders placed yet.</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

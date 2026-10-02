<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { deliveryService } from '../../services/deliveryService'

const loading = ref(true)
const availableDeliveries = ref([])
const myDeliveries = ref([])

onMounted(async () => {
  try {
    const [avail, my] = await Promise.all([
      deliveryService.getAvailableDeliveries(),
      deliveryService.getMyDeliveries()
    ])
    availableDeliveries.value = avail
    myDeliveries.value = my
  } catch (err) {
    console.error('Failed to load logistics data', err)
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
            <h3 class="fw-bold mb-1">Delivery Logistics Hub</h3>
            <p class="text-muted small mb-0">Discover ready-for-pickup agricultural orders and manage your assigned delivery runs.</p>
          </div>
          <router-link to="/delivery/available" class="btn btn-farmora btn-sm">
            <i class="bi bi-geo-alt me-1"></i> Available Orders
          </router-link>
        </div>

        <LoadingSpinner v-if="loading" message="Loading logistics overview..." />

        <template v-else>
          <!-- Metric Highlights -->
          <div class="row g-3 mb-4">
            <div class="col-sm-4">
              <div class="card border-0 shadow-sm p-3">
                <div class="d-flex align-items-center justify-content-between">
                  <div>
                    <div class="text-muted small fw-semibold">Available Runs</div>
                    <div class="fs-4 fw-bold text-success">{{ availableDeliveries.length }}</div>
                  </div>
                  <div class="rounded-circle bg-success bg-opacity-10 text-success p-3">
                    <i class="bi bi-pin-map-fill fs-4"></i>
                  </div>
                </div>
              </div>
            </div>

            <div class="col-sm-4">
              <div class="card border-0 shadow-sm p-3">
                <div class="d-flex align-items-center justify-content-between">
                  <div>
                    <div class="text-muted small fw-semibold">Active In-Transit</div>
                    <div class="fs-4 fw-bold text-warning">
                      {{ myDeliveries.filter(d => d.status === 'ASSIGNED' || d.status === 'PICKED_UP' || d.status === 'IN_TRANSIT').length }}
                    </div>
                  </div>
                  <div class="rounded-circle bg-warning bg-opacity-10 text-warning p-3">
                    <i class="bi bi-truck fs-4"></i>
                  </div>
                </div>
              </div>
            </div>

            <div class="col-sm-4">
              <div class="card border-0 shadow-sm p-3">
                <div class="d-flex align-items-center justify-content-between">
                  <div>
                    <div class="text-muted small fw-semibold">Completed Runs</div>
                    <div class="fs-4 fw-bold text-primary">
                      {{ myDeliveries.filter(d => d.status === 'DELIVERED').length }}
                    </div>
                  </div>
                  <div class="rounded-circle bg-primary bg-opacity-10 text-primary p-3">
                    <i class="bi bi-check2-circle fs-4"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Active Runs Table -->
          <div class="card border-0 shadow-sm">
            <div class="card-header bg-white border-0 pt-3 d-flex justify-content-between align-items-center">
              <h5 class="fw-bold mb-0">My Active Delivery Runs</h5>
              <router-link to="/delivery/my-deliveries" class="small text-decoration-none text-success fw-bold">
                Manage All <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
            <div class="card-body p-0">
              <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                  <thead>
                    <tr>
                      <th>Delivery ID</th>
                      <th>Produce</th>
                      <th>Pickup From</th>
                      <th>Drop-Off</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="del in myDeliveries.slice(0, 5)" :key="del.id">
                      <td class="fw-bold">#{{ del.id }}</td>
                      <td>{{ del.order?.product?.name }} ({{ del.order?.quantity }} {{ del.order?.product?.unit }})</td>
                      <td class="small">{{ del.pickupAddress }}</td>
                      <td class="small">{{ del.deliveryAddress }}</td>
                      <td>
                        <span class="badge bg-light text-dark border">{{ del.status }}</span>
                      </td>
                    </tr>
                    <tr v-if="myDeliveries.length === 0">
                      <td colspan="5" class="text-center py-4 text-muted">No deliveries accepted yet.</td>
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

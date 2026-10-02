<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { adminService } from '../../services/adminService'

const orders = ref([])
const loading = ref(true)

const loadOrders = async () => {
  loading.value = true
  try {
    orders.value = await adminService.getOrders()
  } catch (err) {
    console.error('Failed to load global orders', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="orders" />
      </div>

      <div class="col-lg-9">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="fw-bold mb-1">Global Marketplace Orders</h4>
            <p class="text-muted small mb-0">Audit orders across all buyers, farmers, and delivery channels.</p>
          </div>
        </div>

        <LoadingSpinner v-if="loading" message="Loading global transactions..." />

        <div v-else class="card border-0 shadow-sm">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead>
                <tr>
                  <th>Order #</th>
                  <th>Produce</th>
                  <th>Buyer</th>
                  <th>Farmer</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="o in orders" :key="o.id">
                  <td class="fw-bold">#{{ o.id }}</td>
                  <td>{{ o.product?.name }}</td>
                  <td>{{ o.buyer?.fullName }}</td>
                  <td>{{ o.farmer?.fullName }}</td>
                  <td class="text-success fw-bold">${{ Number(o.totalAmount).toFixed(2) }}</td>
                  <td>
                    <span class="badge bg-light text-dark border">{{ o.status }}</span>
                  </td>
                  <td class="small text-muted">{{ new Date(o.createdAt).toLocaleDateString() }}</td>
                </tr>
                <tr v-if="orders.length === 0">
                  <td colspan="7" class="text-center py-4 text-muted">No orders recorded in system.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

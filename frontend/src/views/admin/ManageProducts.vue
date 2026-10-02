<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { adminService } from '../../services/adminService'

const products = ref([])
const loading = ref(true)

const loadProducts = async () => {
  loading.value = true
  try {
    products.value = await adminService.getProducts()
  } catch (err) {
    console.error('Failed to load products', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProducts()
})

const handleStatusChange = async (id, status) => {
  try {
    const updated = await adminService.updateProductStatus(id, status)
    const idx = products.value.findIndex(p => p.id === id)
    if (idx !== -1) {
      products.value[idx] = updated
    }
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to update product status.')
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="products" />
      </div>

      <div class="col-lg-9">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="fw-bold mb-1">Global Crop Directory</h4>
            <p class="text-muted small mb-0">Oversee all marketplace listings across all registered farmers.</p>
          </div>
        </div>

        <LoadingSpinner v-if="loading" message="Loading crops..." />

        <div v-else class="card border-0 shadow-sm">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead>
                <tr>
                  <th>Produce</th>
                  <th>Farmer</th>
                  <th>Category</th>
                  <th>Price</th>
                  <th>Remaining</th>
                  <th>Status</th>
                  <th class="text-end">Moderation</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in products" :key="p.id">
                  <td>
                    <div class="fw-bold">{{ p.name }}</div>
                    <div class="small text-muted">{{ p.location || 'Local' }}</div>
                  </td>
                  <td>{{ p.farmer?.fullName }}</td>
                  <td><span class="badge bg-light text-dark border">{{ p.category }}</span></td>
                  <td class="text-success fw-bold">${{ Number(p.price).toFixed(2) }}</td>
                  <td>{{ p.quantity }} {{ p.unit }}</td>
                  <td>
                    <span class="badge" :class="p.status === 'AVAILABLE' ? 'bg-success' : 'bg-secondary'">
                      {{ p.status }}
                    </span>
                  </td>
                  <td class="text-end">
                    <button
                      v-if="p.status === 'AVAILABLE'"
                      class="btn btn-sm btn-outline-danger"
                      @click="handleStatusChange(p.id, 'INACTIVE')"
                    >
                      Deactivate
                    </button>
                    <button
                      v-else
                      class="btn btn-sm btn-outline-success"
                      @click="handleStatusChange(p.id, 'AVAILABLE')"
                    >
                      Activate
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

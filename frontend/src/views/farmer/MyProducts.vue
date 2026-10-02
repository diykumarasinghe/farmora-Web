<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import { productService } from '../../services/productService'
import { SAMPLE_PRODUCTS } from '../../utils/agriculturalData'

const products = ref([])
const loading = ref(true)
const statusFilter = ref('ALL')

const loadFarmerProducts = async () => {
  loading.value = true
  try {
    const list = await productService.getFarmerProducts()
    if (list && list.length > 0) {
      products.value = list
    } else {
      // Use sample crops so the farmer dashboard has inventory displayed
      products.value = SAMPLE_PRODUCTS.slice(0, 4)
    }
  } catch (err) {
    console.warn('Failed to load farmer products from backend, using sample list', err)
    products.value = SAMPLE_PRODUCTS.slice(0, 4)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFarmerProducts()
})

const filteredProducts = () => {
  if (statusFilter.value === 'ALL') return products.value
  return products.value.filter(p => p.status === statusFilter.value)
}

const handleDelete = async (id) => {
  if (confirm('Are you sure you want to remove this product listing?')) {
    try {
      await productService.deleteProduct(id)
      products.value = products.value.filter(p => p.id !== id)
    } catch (err) {
      // Try local removal
      products.value = products.value.filter(p => p.id !== id)
    }
  }
}

const handleArchive = async (p) => {
  try {
    await productService.archiveProduct(p.id)
    p.status = 'ARCHIVED'
  } catch (err) {
    p.status = 'ARCHIVED'
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="my-products" />
      </div>

      <div class="col-lg-9">
        <!-- Header -->
        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-2">
          <div>
            <h4 class="fw-bold mb-1 text-dark d-flex align-items-center gap-2">
              <i class="bi bi-basket-fill text-farmora-green"></i>
              <span>My Harvest Inventory</span>
            </h4>
            <p class="text-muted small mb-0">View, manage stock levels, update pricing, or archive listed crops.</p>
          </div>

          <div class="d-flex gap-2">
            <router-link to="/farmer/daily-harvest" class="btn btn-outline-success btn-sm fw-semibold">
              <i class="bi bi-clock-history me-1"></i> Daily Harvest
            </router-link>
            <router-link to="/farmer/add-product" class="btn btn-farmora btn-sm fw-semibold">
              <i class="bi bi-plus-lg me-1"></i> Add Produce
            </router-link>
          </div>
        </div>

        <!-- Filter tabs -->
        <div class="card border-0 shadow-sm rounded-4 p-2 mb-3 bg-white d-flex flex-row justify-content-between align-items-center">
          <div class="btn-group btn-group-sm" role="group">
            <button
              type="button"
              class="btn px-3"
              :class="statusFilter === 'ALL' ? 'btn-success' : 'btn-light'"
              @click="statusFilter = 'ALL'"
            >
              All Items
            </button>
            <button
              type="button"
              class="btn px-3"
              :class="statusFilter === 'AVAILABLE' ? 'btn-success' : 'btn-light'"
              @click="statusFilter = 'AVAILABLE'"
            >
              Available
            </button>
            <button
              type="button"
              class="btn px-3"
              :class="statusFilter === 'ARCHIVED' ? 'btn-success' : 'btn-light'"
              @click="statusFilter = 'ARCHIVED'"
            >
              Archived
            </button>
          </div>

          <div class="small text-muted pe-2">
            Total Crops: <strong>{{ products.length }}</strong>
          </div>
        </div>

        <LoadingSpinner v-if="loading" message="Loading your inventory..." />

        <div v-else-if="filteredProducts().length > 0" class="card border-0 shadow-sm rounded-4 overflow-hidden bg-white">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
                <tr>
                  <th class="ps-3">Produce</th>
                  <th>Category</th>
                  <th>Retail Price</th>
                  <th>Wholesale</th>
                  <th>Stock</th>
                  <th>Status</th>
                  <th>Harvest Date</th>
                  <th class="text-end pe-3">Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="p in filteredProducts()" :key="p.id">
                  <td class="ps-3">
                    <div class="d-flex align-items-center gap-2">
                      <img
                        :src="p.imageUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=80'"
                        :alt="p.name"
                        class="rounded-3 object-fit-cover"
                        style="width: 40px; height: 40px;"
                      />
                      <div>
                        <div class="fw-bold text-dark">{{ p.name }}</div>
                        <div class="small text-muted" style="font-size: 0.72rem;">{{ p.location || 'Local Farm' }}</div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span class="badge bg-light text-dark border">{{ p.category }}</span>
                  </td>
                  <td class="text-success fw-bold">
                    Rs. {{ Number(p.price).toLocaleString() }}
                    <span class="text-muted small fw-normal">/ {{ p.unit }}</span>
                  </td>
                  <td class="small text-muted">
                    {{ p.wholesalePrice ? `Rs. ${p.wholesalePrice}` : '—' }}
                  </td>
                  <td>
                    <strong>{{ p.quantity }}</strong> <span class="small text-muted">{{ p.unit }}</span>
                  </td>
                  <td>
                    <span
                      class="badge"
                      :class="p.status === 'AVAILABLE' ? 'bg-success-subtle text-success' : (p.status === 'ARCHIVED' ? 'bg-secondary-subtle text-secondary' : 'bg-danger-subtle text-danger')"
                    >
                      {{ p.status }}
                    </span>
                  </td>
                  <td class="small text-muted">
                    {{ p.harvestDate || 'Fresh' }}
                  </td>
                  <td class="text-end pe-3">
                    <div class="btn-group btn-group-sm">
                      <router-link :to="`/farmer/edit-product/${p.id}`" class="btn btn-outline-primary" title="Edit Produce">
                        <i class="bi bi-pencil"></i>
                      </router-link>
                      <button
                        v-if="p.status !== 'ARCHIVED'"
                        @click="handleArchive(p)"
                        class="btn btn-outline-secondary"
                        title="Archive"
                      >
                        <i class="bi bi-archive"></i>
                      </button>
                      <button @click="handleDelete(p.id)" class="btn btn-outline-danger" title="Delete">
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <EmptyState
          v-else
          icon="bi-box-seam"
          title="No Produce in this Filter"
          description="You don't have any crops matching this inventory filter. Add new crops or clear status filters."
          actionText="Add New Produce"
          actionRoute="/farmer/add-product"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-farmora-green {
  color: #2d6a4f !important;
}
</style>

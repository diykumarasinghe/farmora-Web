<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { adminService } from '../../services/adminService'

const users = ref([])
const loading = ref(true)

const loadUsers = async () => {
  loading.value = true
  try {
    users.value = await adminService.getUsers()
  } catch (err) {
    console.error('Failed to load users', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUsers()
})

const toggleUser = async (id) => {
  try {
    const updated = await adminService.toggleUserStatus(id)
    const idx = users.value.findIndex(u => u.id === id)
    if (idx !== -1) {
      users.value[idx] = updated
    }
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to update user status.')
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="users" />
      </div>

      <div class="col-lg-9">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="fw-bold mb-1">User Management</h4>
            <p class="text-muted small mb-0">Control access for farmers, buyers, delivery partners, and administrators.</p>
          </div>
        </div>

        <LoadingSpinner v-if="loading" message="Loading platform users..." />

        <div v-else class="card border-0 shadow-sm">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Role</th>
                  <th>Phone</th>
                  <th>Location</th>
                  <th>Status</th>
                  <th class="text-end">Action</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="u in users" :key="u.id">
                  <td>
                    <div class="fw-bold">{{ u.fullName }}</div>
                    <div class="small text-muted">{{ u.email }}</div>
                  </td>
                  <td>
                    <span class="badge" :class="u.role === 'ADMIN' ? 'bg-danger' : (u.role === 'FARMER' ? 'bg-success' : 'bg-primary')">
                      {{ u.role }}
                    </span>
                    <span v-if="u.buyerType" class="badge bg-secondary-subtle text-dark ms-1" style="font-size: 0.7rem;">
                      {{ u.buyerType }}
                    </span>
                    <div v-if="u.businessName" class="small text-muted" style="font-size: 0.75rem;">
                      {{ u.businessName }}
                    </div>
                  </td>
                  <td class="small">{{ u.phone || 'N/A' }}</td>
                  <td class="small text-muted">{{ u.address || 'N/A' }}</td>
                  <td>
                    <span class="badge" :class="u.active ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger'">
                      {{ u.active ? 'Active' : 'Disabled' }}
                    </span>
                    <div v-if="u.role === 'FARMER'" class="mt-1">
                      <router-link :to="`/farms/${u.id}`" target="_blank" class="small text-success text-decoration-none">
                        <i class="bi bi-box-arrow-up-right me-1"></i>Farm Profile
                      </router-link>
                    </div>
                  </td>
                  <td class="text-end">
                    <button
                      v-if="u.role !== 'ADMIN'"
                      class="btn btn-sm"
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
    </div>
  </div>
</template>

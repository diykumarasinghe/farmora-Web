<script setup>
import { computed } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const user = computed(() => authStore.user)
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="profile" />
      </div>

      <div class="col-lg-9">
        <div class="card border-0 shadow-sm p-4">
          <h4 class="fw-bold mb-4">Delivery Partner Account</h4>

          <div class="d-flex align-items-center gap-4 mb-4 pb-4 border-bottom">
            <div class="rounded-circle bg-warning text-dark d-flex align-items-center justify-content-center fs-2" style="width: 80px; height: 80px;">
              <i class="bi bi-truck"></i>
            </div>
            <div>
              <h5 class="fw-bold mb-1">{{ user?.fullName }}</h5>
              <div class="badge bg-warning text-dark mb-1">{{ user?.role }}</div>
              <div class="text-muted small">Registered on Farmora: {{ user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'Active' }}</div>
            </div>
          </div>

          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label small text-muted">Email Address</label>
              <div class="p-2 bg-light rounded text-dark">{{ user?.email }}</div>
            </div>
            <div class="col-md-6">
              <label class="form-label small text-muted">Driver Contact Phone</label>
              <div class="p-2 bg-light rounded text-dark">{{ user?.phone || 'Not provided' }}</div>
            </div>
            <div class="col-12">
              <label class="form-label small text-muted">Service Base Hub Address</label>
              <div class="p-2 bg-light rounded text-dark">{{ user?.address || 'Standard Hub' }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

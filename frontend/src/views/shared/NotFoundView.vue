<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { computed } from 'vue'

const router = useRouter()
const authStore = useAuthStore()

const dashboardRoute = computed(() => {
  switch (authStore.role) {
    case 'FARMER': return '/farmer/dashboard'
    case 'BUYER': return '/buyer/dashboard'
    case 'DELIVERY_PARTNER': return '/delivery/dashboard'
    case 'ADMIN': return '/admin/dashboard'
    default: return '/'
  }
})
</script>

<template>
  <div class="min-vh-100 d-flex align-items-center justify-content-center bg-light py-5">
    <div class="text-center px-4" style="max-width: 500px;">
      <!-- Animated 404 illustration -->
      <div class="mb-4">
        <div class="position-relative d-inline-block">
          <div class="rounded-circle bg-farmora-light d-inline-flex p-5 mb-3" style="width: 160px; height: 160px; align-items: center; justify-content: center;">
            <i class="bi bi-compass text-farmora-green" style="font-size: 5rem;"></i>
          </div>
        </div>
      </div>

      <h1 class="display-2 fw-black text-farmora-green mb-0" style="line-height: 1;">404</h1>
      <h3 class="fw-bold mb-3 text-dark">Page Not Found</h3>
      <p class="text-muted mb-4 lead">
        Looks like this harvest route doesn't exist. The page you're looking for may have been moved, deleted, or you may have typed the address incorrectly.
      </p>

      <div class="d-flex flex-wrap gap-3 justify-content-center">
        <router-link to="/" class="btn btn-farmora btn-lg px-4 rounded-pill">
          <i class="bi bi-house me-2"></i>Return to Farmora Home
        </router-link>
        <router-link
          v-if="authStore.isAuthenticated"
          :to="dashboardRoute"
          class="btn btn-farmora-outline btn-lg px-4 rounded-pill"
        >
          <i class="bi bi-speedometer2 me-2"></i>Go to My Dashboard
        </router-link>
        <router-link
          v-else
          to="/products"
          class="btn btn-farmora-outline btn-lg px-4 rounded-pill"
        >
          <i class="bi bi-search me-2"></i>Browse Produce
        </router-link>
      </div>

      <div class="mt-5 pt-4 border-top">
        <p class="small text-muted">
          If you believe this is an error, please
          <router-link to="/contact" class="text-success fw-semibold text-decoration-none">contact our support team</router-link>.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const handleLogin = async () => {
  error.value = ''
  loading.value = true
  try {
    const res = await authStore.login({
      email: email.value,
      password: password.value
    })

    const role = res.user.role
    const redirect = route.query.redirect
    if (redirect) {
      router.push(redirect)
    } else {
      switch (role) {
        case 'FARMER':
          router.push('/farmer/dashboard')
          break
        case 'BUYER':
          router.push('/buyer/dashboard')
          break
        case 'DELIVERY_PARTNER':
          router.push('/delivery/dashboard')
          break
        case 'ADMIN':
          router.push('/admin/dashboard')
          break
        default:
          router.push('/')
      }
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Login failed. Please check your credentials.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="container py-5">
    <div class="row justify-content-center">
      <div class="col-md-6 col-lg-5">
        <div class="card shadow border-0 p-4 p-md-5">
          <div class="text-center mb-4">
            <div class="d-inline-flex p-3 rounded-circle bg-farmora-light text-farmora-green mb-2">
              <i class="bi bi-flower1 fs-2"></i>
            </div>
            <h3 class="fw-bold">Welcome Back</h3>
            <p class="text-muted small">Sign in to your Farmora portal</p>
          </div>

          <div v-if="error" class="alert alert-danger py-2 small" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-1"></i> {{ error }}
          </div>

          <form @submit.prevent="handleLogin">
            <div class="mb-3">
              <label for="email" class="form-label small fw-semibold">Email Address</label>
              <div class="input-group">
                <span class="input-group-text bg-light"><i class="bi bi-envelope"></i></span>
                <input
                  type="email"
                  id="email"
                  v-model="email"
                  class="form-control"
                  placeholder="e.g. farmer@example.com"
                  required
                />
              </div>
            </div>

            <div class="mb-4">
              <label for="password" class="form-label small fw-semibold">Password</label>
              <div class="input-group">
                <span class="input-group-text bg-light"><i class="bi bi-lock"></i></span>
                <input
                  type="password"
                  id="password"
                  v-model="password"
                  class="form-control"
                  placeholder="••••••••"
                  required
                />
              </div>
            </div>

            <button
              type="submit"
              class="btn btn-farmora w-100 py-2 fw-bold"
              :disabled="loading"
            >
              <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
              Sign In
            </button>
          </form>

          <div class="text-center mt-4 pt-3 border-top small text-muted">
            Don't have an account yet?
            <router-link to="/register" class="text-success fw-bold text-decoration-none">
              Register here
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

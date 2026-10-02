<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, RouterView } from 'vue-router'
import AppNavbar from './components/common/AppNavbar.vue'
import AppFooter from './components/common/AppFooter.vue'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()
const route = useRoute()

// Apply light background on protected dashboard pages for better readability
const mainClass = computed(() => {
  const path = route.path
  const isDashboard = path.includes('/farmer/') ||
    path.includes('/buyer/') ||
    path.includes('/delivery/') ||
    path.includes('/admin/') ||
    path.includes('/notifications') ||
    path.includes('/messages') ||
    path.includes('/map')
  return isDashboard ? 'main-content flex-grow-1 bg-soft-page' : 'main-content flex-grow-1'
})

onMounted(async () => {
  if (authStore.token && !authStore.user) {
    await authStore.restoreSession()
  }
})
</script>

<template>
  <div class="app-layout d-flex flex-column min-vh-100">
    <AppNavbar />
    <main :class="mainClass">
      <RouterView />
    </main>
    <AppFooter />
  </div>
</template>

<style>
.app-layout { min-height: 100vh; }
.bg-soft-page { background-color: #f8f9f4; }
</style>

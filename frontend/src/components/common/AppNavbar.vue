<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useNotificationStore } from '../../stores/notifications'

const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const user = computed(() => authStore.user)
const role = computed(() => authStore.role)
const unreadCount = computed(() => notificationStore.unreadCount)

const searchQuery = ref('')

onMounted(() => {
  if (authStore.isAuthenticated) {
    notificationStore.fetchUnreadCount()
  }
})

const dashboardRoute = computed(() => {
  if (!user.value) return '/'
  switch (user.value.role) {
    case 'FARMER': return '/farmer/dashboard'
    case 'BUYER': return '/buyer/dashboard'
    case 'DELIVERY_PARTNER': return '/delivery/dashboard'
    case 'ADMIN': return '/admin/dashboard'
    default: return '/'
  }
})

const profileRoute = computed(() => {
  if (!user.value) return '/'
  switch (user.value.role) {
    case 'FARMER': return '/farmer/profile'
    case 'BUYER': return '/buyer/profile'
    case 'DELIVERY_PARTNER': return '/delivery/profile'
    default: return '/'
  }
})

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ path: '/products', query: { search: searchQuery.value.trim() } })
    searchQuery.value = ''
  }
}

const handleLogout = () => {
  authStore.logout()
  router.push('/')
}
</script>

<template>
  <nav class="farmora-navbar navbar navbar-expand-lg sticky-top">
    <div class="container">
      <!-- Brand Logo -->
      <router-link to="/" class="navbar-brand d-flex align-items-center gap-2 text-decoration-none">
        <div class="brand-icon">
          <i class="bi bi-flower2"></i>
        </div>
        <div class="brand-text">
          <div class="brand-name">FARMORA</div>
          <div class="brand-tagline">Grow Together</div>
        </div>
      </router-link>

      <!-- Mobile Toggler -->
      <button
        class="navbar-toggler border-0 shadow-none"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#farmoraNavbar"
        aria-controls="farmoraNavbar"
        aria-expanded="false"
        aria-label="Toggle navigation"
      >
        <i class="bi bi-list fs-3 text-dark"></i>
      </button>

      <div class="collapse navbar-collapse" id="farmoraNavbar">
        <!-- Nav Links -->
        <ul class="navbar-nav ms-lg-4 me-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <router-link to="/" class="nav-link fw-medium px-3" active-class="nav-active">Home</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/products" class="nav-link fw-medium px-3" active-class="nav-active">Marketplace</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/agricultural-map" class="nav-link fw-medium px-3" active-class="nav-active">Agri Map</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/agri-info" class="nav-link fw-medium px-3" active-class="nav-active">Agri Hub</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/price-trends" class="nav-link fw-medium px-3" active-class="nav-active">Price Trends</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/ai-tools" class="nav-link fw-medium px-3 d-flex align-items-center gap-1" active-class="nav-active">
              <i class="bi bi-stars text-warning"></i>
              <span>AI Hub</span>
            </router-link>
          </li>
          <li class="nav-item">
            <router-link to="/about" class="nav-link fw-medium px-3" active-class="nav-active">About</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/contact" class="nav-link fw-medium px-3" active-class="nav-active">Contact</router-link>
          </li>
        </ul>

        <!-- Search Bar -->
        <form class="navbar-search me-3 d-none d-lg-flex" @submit.prevent="handleSearch">
          <div class="input-group">
            <input
              type="search"
              class="form-control search-input"
              placeholder="Search for fresh produce..."
              v-model="searchQuery"
            />
            <button class="btn search-btn" type="submit">
              <i class="bi bi-search"></i>
            </button>
          </div>
        </form>

        <!-- Auth Buttons / User Menu -->
        <div class="d-flex align-items-center gap-2">
          <template v-if="!isAuthenticated">
            <router-link to="/login" class="btn btn-sign-in px-3">Sign In</router-link>
            <router-link to="/register" class="btn btn-register px-3 fw-semibold">Register</router-link>
          </template>

          <template v-else>
            <!-- Chat & Video Communication -->
            <router-link to="/messages" class="position-relative nav-icon-btn" title="Messages & Live Calls">
              <i class="bi bi-chat-dots fs-5"></i>
            </router-link>

            <!-- Notification Bell -->
            <router-link to="/notifications" class="position-relative nav-icon-btn">
              <i class="bi bi-bell fs-5"></i>
              <span
                v-if="unreadCount > 0"
                class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
                style="font-size: 0.6rem;"
              >{{ unreadCount }}</span>
            </router-link>

            <!-- User Dropdown -->
            <div class="dropdown">
              <button
                class="btn user-dropdown-btn dropdown-toggle d-flex align-items-center gap-2"
                type="button"
                id="userDropdown"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <div class="user-avatar">
                  <i class="bi bi-person-fill"></i>
                </div>
                <span class="user-name d-none d-xl-block">{{ user?.fullName?.split(' ')[0] || 'Account' }}</span>
              </button>
              <ul class="dropdown-menu dropdown-menu-end shadow border-0 mt-2" aria-labelledby="userDropdown">
                <li class="px-3 py-2 border-bottom">
                  <div class="small text-muted">Signed in as</div>
                  <div class="fw-bold text-truncate small" style="max-width: 180px;">{{ user?.email }}</div>
                  <span class="badge bg-farmora-green mt-1">{{ role }}</span>
                </li>
                <li>
                  <router-link :to="dashboardRoute" class="dropdown-item py-2">
                    <i class="bi bi-speedometer2 me-2 text-success"></i>Dashboard
                  </router-link>
                </li>
                <li>
                  <router-link to="/messages" class="dropdown-item py-2">
                    <i class="bi bi-chat-dots me-2 text-info"></i>Messages & Calls
                  </router-link>
                </li>
                <li>
                  <router-link to="/agri-info" class="dropdown-item py-2">
                    <i class="bi bi-journal-text me-2 text-success"></i>Agri Hub & Calendar
                  </router-link>
                </li>
                <li>
                  <router-link :to="profileRoute" class="dropdown-item py-2">
                    <i class="bi bi-person-circle me-2 text-primary"></i>My Profile
                  </router-link>
                </li>
                <li>
                  <router-link to="/notifications" class="dropdown-item py-2">
                    <i class="bi bi-bell me-2 text-warning"></i>Notifications
                    <span v-if="unreadCount > 0" class="badge bg-danger ms-1">{{ unreadCount }}</span>
                  </router-link>
                </li>
                <li><hr class="dropdown-divider"></li>
                <li>
                  <button @click="handleLogout" class="dropdown-item py-2 text-danger">
                    <i class="bi bi-box-arrow-right me-2"></i>Log Out
                  </button>
                </li>
              </ul>
            </div>
          </template>
        </div>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.farmora-navbar {
  background: #ffffff;
  border-bottom: 1px solid rgba(0,0,0,0.08);
  padding: 0.75rem 0;
  z-index: 1030;
}

.brand-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #2d6a4f, #40916c);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 1.4rem;
}

.brand-name {
  font-size: 1.1rem;
  font-weight: 800;
  color: #1b4332;
  letter-spacing: 0.05em;
  line-height: 1.1;
}

.brand-tagline {
  font-size: 0.65rem;
  color: #40916c;
  font-weight: 500;
  letter-spacing: 0.03em;
  line-height: 1;
}

.navbar-nav .nav-link {
  color: #374151 !important;
  font-size: 0.92rem;
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.navbar-nav .nav-link:hover,
.navbar-nav .nav-link.nav-active {
  color: #2d6a4f !important;
  border-bottom-color: #2d6a4f;
}

.navbar-search .search-input {
  border: 1.5px solid #e5e7eb;
  border-right: none;
  border-radius: 8px 0 0 8px;
  font-size: 0.85rem;
  width: 220px;
  background: #f9fafb;
  padding: 0.45rem 0.75rem;
}

.navbar-search .search-input:focus {
  box-shadow: none;
  border-color: #2d6a4f;
  background: #fff;
}

.navbar-search .search-btn {
  border: 1.5px solid #e5e7eb;
  border-left: none;
  border-radius: 0 8px 8px 0;
  background: #f9fafb;
  color: #6b7280;
  padding: 0.45rem 0.75rem;
  transition: all 0.2s;
}

.navbar-search .search-btn:hover {
  background: #2d6a4f;
  border-color: #2d6a4f;
  color: #fff;
}

.btn-sign-in {
  border: 1.5px solid #2d6a4f;
  color: #2d6a4f;
  background: transparent;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-sign-in:hover {
  background: #2d6a4f;
  color: #fff;
}

.btn-register {
  background: #2d6a4f;
  color: #fff;
  border: 2px solid #2d6a4f;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.btn-register:hover {
  background: #1b4332;
  border-color: #1b4332;
  color: #fff;
}

.nav-icon-btn {
  color: #374151;
  padding: 0.4rem 0.5rem;
  border-radius: 8px;
  text-decoration: none;
  transition: background 0.2s;
}

.nav-icon-btn:hover {
  background: #f3f4f6;
  color: #2d6a4f;
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #2d6a4f, #40916c);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 0.9rem;
}

.user-dropdown-btn {
  background: #f9fafb;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  color: #374151;
  padding: 0.35rem 0.75rem;
  font-size: 0.875rem;
}

.user-dropdown-btn:hover {
  background: #f3f4f6;
  color: #2d6a4f;
}

.user-name { font-weight: 600; color: #374151; }

.bg-farmora-green { background-color: #2d6a4f !important; }
</style>

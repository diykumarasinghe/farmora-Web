<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../../stores/auth'

defineProps({
  activeMenu: {
    type: String,
    default: ''
  }
})

const authStore = useAuthStore()
const user = computed(() => authStore.user)
const role = computed(() => authStore.role)
</script>

<template>
  <div class="sidebar shadow-sm mb-4">
    <!-- User Profile Header -->
    <div class="d-flex align-items-center gap-3 p-2 border-bottom mb-3">
      <div
        class="rounded-circle text-white d-flex align-items-center justify-content-center overflow-hidden shadow-sm flex-shrink-0"
        :class="role === 'FARMER' ? 'bg-success' : 'bg-primary'"
        style="width: 48px; height: 48px;"
      >
        <img
          v-if="user?.profileImageUrl"
          :src="user.profileImageUrl"
          alt="Avatar"
          class="w-100 h-100 object-fit-cover"
        />
        <i v-else class="bi bi-person-fill fs-4"></i>
      </div>
      <div class="overflow-hidden">
        <h6 class="mb-0 fw-bold text-truncate text-dark">{{ user?.fullName || 'User' }}</h6>
        <span
          class="badge text-uppercase"
          :class="role === 'FARMER' ? 'bg-success' : (role === 'ADMIN' ? 'bg-danger' : 'bg-primary')"
          style="font-size: 0.68rem; letter-spacing: 0.05em;"
        >
          {{ role }}
        </span>
      </div>
    </div>

    <!-- ============================================================
         FARMER LINKS (Profile & Verification System)
         ============================================================ -->
    <template v-if="role === 'FARMER'">
      <!-- DASHBOARD -->
      <div class="sidebar-section-title">DASHBOARD</div>
      <router-link to="/farmer/dashboard" class="nav-link" :class="{ active: activeMenu === 'dashboard' }">
        <i class="bi bi-house-door-fill"></i>
        <span>Overview</span>
      </router-link>

      <!-- PROFILE & TRUST -->
      <div class="sidebar-section-title mt-3">PROFILE & TRUST</div>
      <router-link to="/farmer/profile" class="nav-link" :class="{ active: activeMenu === 'profile' }">
        <i class="bi bi-person"></i>
        <span>Farmer Profile</span>
      </router-link>
      <router-link to="/farmer/certifications" class="nav-link" :class="{ active: activeMenu === 'certifications' }">
        <i class="bi bi-patch-check"></i>
        <span>Certifications</span>
      </router-link>
      <router-link to="/farmer/verification" class="nav-link" :class="{ active: activeMenu === 'verification' }">
        <i class="bi bi-shield-check"></i>
        <span>Verification</span>
      </router-link>
      <router-link to="/farmer/media" class="nav-link" :class="{ active: activeMenu === 'media' }">
        <i class="bi bi-images"></i>
        <span>Farm Media</span>
      </router-link>
      <router-link to="/farmer/methods" class="nav-link" :class="{ active: activeMenu === 'methods' }">
        <i class="bi bi-flower1"></i>
        <span>Farming Methods</span>
      </router-link>
      <router-link to="/farmer/credibility" class="nav-link" :class="{ active: activeMenu === 'credibility' }">
        <i class="bi bi-bar-chart-line"></i>
        <span>Credibility Score</span>
      </router-link>

      <!-- MARKETPLACE -->
      <div class="sidebar-section-title mt-3">MARKETPLACE</div>
      <router-link to="/products" class="nav-link" :class="{ active: activeMenu === 'marketplace' }">
        <i class="bi bi-shop"></i>
        <span>Browse Marketplace</span>
      </router-link>
      <router-link to="/farmer/add-product" class="nav-link" :class="{ active: activeMenu === 'add-product' }">
        <i class="bi bi-plus-circle"></i>
        <span>Add Produce</span>
      </router-link>
      <router-link to="/farmer/my-products" class="nav-link" :class="{ active: activeMenu === 'my-products' }">
        <i class="bi bi-basket"></i>
        <span>My Inventory</span>
      </router-link>
      <router-link to="/farmer/daily-harvest" class="nav-link" :class="{ active: activeMenu === 'daily-harvest' }">
        <i class="bi bi-clock-history"></i>
        <span>Daily Harvest</span>
      </router-link>
      <router-link to="/farmer/bids" class="nav-link" :class="{ active: activeMenu === 'bids' }">
        <i class="bi bi-tag"></i>
        <span>Received Bids</span>
      </router-link>
      <router-link to="/farmer/orders" class="nav-link" :class="{ active: activeMenu === 'orders' }">
        <i class="bi bi-truck"></i>
        <span>Orders & Shipments</span>
      </router-link>

      <!-- ENGAGEMENT & COMMS -->
      <div class="sidebar-section-title mt-3">ENGAGEMENT & AI</div>
      <router-link to="/messages" class="nav-link" :class="{ active: activeMenu === 'messages' }">
        <i class="bi bi-chat-dots"></i>
        <span>Chat & Video</span>
      </router-link>
      <router-link to="/ai-tools" class="nav-link" :class="{ active: activeMenu === 'ai-tools' }">
        <i class="bi bi-cpu text-warning"></i>
        <span>AI Intelligence</span>
      </router-link>
      <router-link to="/agri-info" class="nav-link" :class="{ active: activeMenu === 'agri-info' }">
        <i class="bi bi-journal-text"></i>
        <span>Agri Info & Calendar</span>
      </router-link>

      <!-- Support Box -->
      <router-link to="/contact" class="support-card text-decoration-none mt-4 d-flex align-items-center gap-2 p-3 rounded-3">
        <div class="support-icon rounded-circle d-flex align-items-center justify-content-center">
          <i class="bi bi-headset fs-5 text-white"></i>
        </div>
        <div>
          <div class="fw-bold text-white small">Need Help?</div>
          <div class="text-white-50" style="font-size: 0.75rem;">Contact Support</div>
        </div>
      </router-link>
    </template>

    <!-- ============================================================
         BUYER LINKS
         ============================================================ -->
    <template v-else-if="role === 'BUYER'">
      <div class="sidebar-section-title">BUYER PORTAL</div>
      <router-link to="/buyer/dashboard" class="nav-link" :class="{ active: activeMenu === 'dashboard' }">
        <i class="bi bi-speedometer2"></i> Dashboard
      </router-link>
      <router-link to="/products" class="nav-link" :class="{ active: activeMenu === 'explore' }">
        <i class="bi bi-search"></i> Explore Market
      </router-link>
      <router-link to="/buyer/my-bids" class="nav-link" :class="{ active: activeMenu === 'bids' }">
        <i class="bi bi-tag"></i> Bids & Negotiations
      </router-link>
      <router-link to="/buyer/orders" class="nav-link" :class="{ active: activeMenu === 'orders' }">
        <i class="bi bi-bag-check"></i> Orders & Logistics
      </router-link>
      <router-link to="/buyer/favourites" class="nav-link" :class="{ active: activeMenu === 'favourites' }">
        <i class="bi bi-heart"></i> Favourite Farms
      </router-link>
      <router-link to="/messages" class="nav-link" :class="{ active: activeMenu === 'messages' }">
        <i class="bi bi-chat-dots"></i> Chat & Video
      </router-link>
      <router-link to="/ai-tools" class="nav-link" :class="{ active: activeMenu === 'ai-tools' }">
        <i class="bi bi-cpu text-warning"></i> AI Intelligence
      </router-link>
      <router-link to="/agri-info" class="nav-link" :class="{ active: activeMenu === 'agri-info' }">
        <i class="bi bi-journal-text"></i> Agri Info & Calendar
      </router-link>
      <router-link to="/buyer/profile" class="nav-link" :class="{ active: activeMenu === 'profile' }">
        <i class="bi bi-person-gear"></i> Buyer Account
      </router-link>
    </template>

    <!-- ============================================================
         DELIVERY PARTNER LINKS
         ============================================================ -->
    <template v-else-if="role === 'DELIVERY_PARTNER'">
      <div class="sidebar-section-title">LOGISTICS PORTAL</div>
      <router-link to="/delivery/dashboard" class="nav-link" :class="{ active: activeMenu === 'dashboard' }">
        <i class="bi bi-speedometer2"></i> Hub Overview
      </router-link>
      <router-link to="/delivery/available" class="nav-link" :class="{ active: activeMenu === 'available' }">
        <i class="bi bi-geo-alt"></i> Available Runs
      </router-link>
      <router-link to="/delivery/my-deliveries" class="nav-link" :class="{ active: activeMenu === 'my' }">
        <i class="bi bi-truck"></i> Active Deliveries
      </router-link>
      <router-link to="/delivery/profile" class="nav-link" :class="{ active: activeMenu === 'profile' }">
        <i class="bi bi-person-gear"></i> Partner Profile
      </router-link>
    </template>

    <!-- ============================================================
         ADMIN LINKS
         ============================================================ -->
    <template v-else-if="role === 'ADMIN'">
      <div class="sidebar-section-title">ADMINISTRATION</div>
      <router-link to="/admin/dashboard" class="nav-link" :class="{ active: activeMenu === 'dashboard' }">
        <i class="bi bi-shield-check"></i> System Analytics
      </router-link>
      <router-link to="/admin/users" class="nav-link" :class="{ active: activeMenu === 'users' }">
        <i class="bi bi-people"></i> Manage Users
      </router-link>
      <router-link to="/admin/products" class="nav-link" :class="{ active: activeMenu === 'products' }">
        <i class="bi bi-box-seam"></i> Manage Produce
      </router-link>
      <router-link to="/admin/orders" class="nav-link" :class="{ active: activeMenu === 'orders' }">
        <i class="bi bi-receipt-cutoff"></i> Global Orders
      </router-link>
      <router-link to="/admin/reports" class="nav-link" :class="{ active: activeMenu === 'reports' }">
        <i class="bi bi-graph-up-arrow"></i> Reports
      </router-link>
      <router-link to="/agri-info" class="nav-link" :class="{ active: activeMenu === 'agri-info' }">
        <i class="bi bi-megaphone"></i> Agri Announcements
      </router-link>
    </template>
  </div>
</template>

<style scoped>
.sidebar-section-title {
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.05em;
  color: #6c757d;
  padding: 0.35rem 0.75rem;
  text-transform: uppercase;
}

.support-card {
  background: linear-gradient(135deg, #1b4332 0%, #2d6a4f 100%);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.support-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(27, 67, 50, 0.35);
}

.support-icon {
  width: 38px;
  height: 38px;
  background: rgba(255, 255, 255, 0.15);
}
</style>

<script setup>
import { onMounted, ref, computed } from 'vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import { notificationService } from '../../services/notificationService'

const notifications = ref([])
const loading = ref(true)
const activeFilter = ref('ALL') // 'ALL' | 'ORDERS' | 'BIDS' | 'PRICE' | 'HARVEST'

const loadNotifications = async () => {
  loading.value = true
  try {
    notifications.value = await notificationService.getNotifications()
  } catch (err) {
    console.error('Failed to load notifications', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadNotifications()
})

const filteredNotifications = computed(() => {
  if (activeFilter.value === 'ORDERS') {
    return notifications.value.filter(n => n.type?.includes('ORDER'))
  }
  if (activeFilter.value === 'BIDS') {
    return notifications.value.filter(n => n.type?.includes('BID'))
  }
  if (activeFilter.value === 'PRICE') {
    return notifications.value.filter(n => n.type?.includes('PRICE'))
  }
  if (activeFilter.value === 'HARVEST') {
    return notifications.value.filter(n => n.type?.includes('HARVEST'))
  }
  return notifications.value
})

const markAsRead = async (id) => {
  try {
    await notificationService.markAsRead(id)
    const item = notifications.value.find(n => n.id === id)
    if (item) item.readStatus = true
  } catch (err) {
    console.error(err)
  }
}

const markAllRead = async () => {
  try {
    await notificationService.markAllAsRead()
    notifications.value.forEach(n => (n.readStatus = true))
  } catch (err) {
    console.error(err)
  }
}

const getNotificationIcon = (type) => {
  if (!type) return 'bi-bell-fill'
  if (type.includes('BID')) return 'bi-tags-fill'
  if (type.includes('ORDER')) return 'bi-bag-check-fill'
  if (type.includes('PRICE')) return 'bi-graph-down-arrow'
  if (type.includes('HARVEST')) return 'bi-basket2-fill'
  if (type.includes('CHAT')) return 'bi-chat-dots-fill'
  if (type.includes('VIDEO')) return 'bi-camera-video-fill'
  return 'bi-bell-fill'
}

const getNotificationBg = (type) => {
  if (!type) return 'bg-secondary'
  if (type.includes('BID')) return 'bg-warning text-dark'
  if (type.includes('ORDER')) return 'bg-success text-white'
  if (type.includes('PRICE')) return 'bg-info text-white'
  if (type.includes('HARVEST')) return 'bg-success text-white'
  if (type.includes('VIDEO')) return 'bg-primary text-white'
  return 'bg-secondary text-white'
}
</script>

<template>
  <div class="container py-4">
    <div class="card border-0 shadow-sm p-4 rounded-4 bg-white">
      <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
          <h4 class="fw-bold mb-1"><i class="bi bi-bell-fill text-warning me-2"></i>Alerts & Notification Center</h4>
          <p class="text-muted small mb-0">Live alerts on bids, orders, harvest drops, and price modifications.</p>
        </div>
        <button
          v-if="notifications.some(n => !n.readStatus)"
          class="btn btn-outline-success btn-sm rounded-pill px-3"
          @click="markAllRead"
        >
          <i class="bi bi-check2-all me-1"></i> Mark All as Read
        </button>
      </div>

      <!-- Filter Tabs -->
      <div class="nav nav-pills bg-light p-1 rounded-pill border d-inline-flex mb-4 flex-wrap gap-1">
        <button
          class="nav-link rounded-pill px-3 py-1 small fw-semibold"
          :class="{ active: activeFilter === 'ALL' }"
          @click="activeFilter = 'ALL'"
        >
          All ({{ notifications.length }})
        </button>
        <button
          class="nav-link rounded-pill px-3 py-1 small fw-semibold"
          :class="{ active: activeFilter === 'ORDERS' }"
          @click="activeFilter = 'ORDERS'"
        >
          <i class="bi bi-bag-check me-1"></i> Order Alerts
        </button>
        <button
          class="nav-link rounded-pill px-3 py-1 small fw-semibold"
          :class="{ active: activeFilter === 'BIDS' }"
          @click="activeFilter = 'BIDS'"
        >
          <i class="bi bi-tags me-1"></i> Bid Alerts
        </button>
        <button
          class="nav-link rounded-pill px-3 py-1 small fw-semibold"
          :class="{ active: activeFilter === 'PRICE' }"
          @click="activeFilter = 'PRICE'"
        >
          <i class="bi bi-graph-down me-1"></i> Price Changes
        </button>
        <button
          class="nav-link rounded-pill px-3 py-1 small fw-semibold"
          :class="{ active: activeFilter === 'HARVEST' }"
          @click="activeFilter = 'HARVEST'"
        >
          <i class="bi bi-basket me-1"></i> Harvest Drops
        </button>
      </div>

      <LoadingSpinner v-if="loading" message="Loading notifications..." />

      <div v-else-if="filteredNotifications.length > 0" class="d-flex flex-column gap-2">
        <div
          v-for="n in filteredNotifications"
          :key="n.id"
          class="p-3 rounded-4 border d-flex justify-content-between align-items-start gap-3 transition-all"
          :class="!n.readStatus ? 'bg-light border-success-subtle shadow-sm' : 'bg-white border-light'"
        >
          <div class="d-flex align-items-start gap-3">
            <div
              class="rounded-circle p-2 d-flex align-items-center justify-content-center flex-shrink-0"
              :class="getNotificationBg(n.type)"
              style="width: 44px; height: 44px;"
            >
              <i class="bi fs-5" :class="getNotificationIcon(n.type)"></i>
            </div>
            <div>
              <div class="d-flex align-items-center gap-2 mb-1">
                <h6 class="fw-bold mb-0" :class="!n.readStatus ? 'text-success' : 'text-dark'">{{ n.title }}</h6>
                <span v-if="!n.readStatus" class="badge bg-danger rounded-pill px-2 py-0" style="font-size: 0.65rem;">NEW</span>
                <span class="badge bg-light text-secondary border rounded-pill" style="font-size: 0.65rem;">{{ n.type }}</span>
              </div>
              <p class="small text-secondary mb-1" style="line-height: 1.4;">{{ n.message }}</p>
              <div class="small text-muted" style="font-size: 0.75rem;">
                <i class="bi bi-clock me-1"></i>{{ new Date(n.createdAt).toLocaleString() }}
              </div>
            </div>
          </div>

          <div class="d-flex align-items-center gap-2 flex-shrink-0">
            <router-link
              v-if="n.type?.includes('ORDER')"
              to="/buyer/orders"
              class="btn btn-sm btn-outline-success rounded-pill px-3"
            >
              View Order
            </router-link>
            <router-link
              v-else-if="n.type?.includes('BID')"
              to="/buyer/my-bids"
              class="btn btn-sm btn-outline-warning text-dark rounded-pill px-3"
            >
              View Bid
            </router-link>
            <router-link
              v-else-if="n.type?.includes('HARVEST')"
              to="/daily-harvest"
              class="btn btn-sm btn-outline-success rounded-pill px-3"
            >
              View Harvest
            </router-link>
            <router-link
              v-else-if="n.type?.includes('CHAT')"
              to="/messages"
              class="btn btn-sm btn-outline-primary rounded-pill px-3"
            >
              Reply
            </router-link>

            <button
              v-if="!n.readStatus"
              class="btn btn-sm btn-light rounded-pill px-3"
              @click="markAsRead(n.id)"
            >
              Mark Read
            </button>
          </div>
        </div>
      </div>

      <EmptyState
        v-else
        icon="bi-bell-slash"
        title="No Notifications Found"
        description="You have caught up with all activity in this category!"
      />
    </div>
  </div>
</template>

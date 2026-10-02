<script setup>
import { onMounted, ref } from 'vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import { notificationService } from '../../services/notificationService'

const notifications = ref([])
const loading = ref(true)

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
</script>

<template>
  <div class="container py-4">
    <div class="card border-0 shadow-sm p-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h4 class="fw-bold mb-1"><i class="bi bi-bell-fill text-warning me-2"></i>Notifications</h4>
          <p class="text-muted small mb-0">System updates on bids, orders, shipments, and customer reviews.</p>
        </div>
        <button
          v-if="notifications.some(n => !n.readStatus)"
          class="btn btn-outline-success btn-sm"
          @click="markAllRead"
        >
          <i class="bi bi-check2-all me-1"></i> Mark All as Read
        </button>
      </div>

      <LoadingSpinner v-if="loading" message="Loading notifications..." />

      <div v-else-if="notifications.length > 0" class="list-group list-group-flush">
        <div
          v-for="n in notifications"
          :key="n.id"
          class="list-group-item d-flex justify-content-between align-items-start py-3 px-2 rounded mb-2 border"
          :class="!n.readStatus ? 'bg-light border-success-subtle' : 'border-light'"
        >
          <div class="d-flex align-items-start gap-3">
            <div class="rounded-circle p-2 d-flex align-items-center justify-content-center" :class="!n.readStatus ? 'bg-success text-white' : 'bg-secondary bg-opacity-10 text-muted'" style="width: 40px; height: 40px;">
              <i class="bi" :class="n.type?.includes('BID') ? 'bi-tag' : (n.type?.includes('ORDER') ? 'bi-bag' : 'bi-bell')"></i>
            </div>
            <div>
              <div class="d-flex align-items-center gap-2 mb-1">
                <h6 class="fw-bold mb-0" :class="!n.readStatus ? 'text-success' : 'text-dark'">{{ n.title }}</h6>
                <span v-if="!n.readStatus" class="badge bg-danger rounded-pill" style="font-size: 0.65rem;">NEW</span>
              </div>
              <p class="small text-secondary mb-1">{{ n.message }}</p>
              <div class="small text-muted" style="font-size: 0.75rem;">{{ new Date(n.createdAt).toLocaleString() }}</div>
            </div>
          </div>
          <button
            v-if="!n.readStatus"
            class="btn btn-sm btn-outline-secondary"
            @click="markAsRead(n.id)"
          >
            Mark Read
          </button>
        </div>
      </div>

      <EmptyState
        v-else
        icon="bi-bell-slash"
        title="No Notifications"
        description="You have caught up with all activity! New alerts on your bids and deliveries will appear here."
      />
    </div>
  </div>
</template>

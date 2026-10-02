<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import DeliveryCard from '../../components/deliveries/DeliveryCard.vue'
import { deliveryService } from '../../services/deliveryService'

const deliveries = ref([])
const loading = ref(true)

const loadMyDeliveries = async () => {
  loading.value = true
  try {
    deliveries.value = await deliveryService.getMyDeliveries()
  } catch (err) {
    console.error('Failed to load my deliveries', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMyDeliveries()
})

const handleUpdateStatus = async (id, status) => {
  try {
    await deliveryService.updateDeliveryStatus(id, status)
    loadMyDeliveries()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to update delivery status.')
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="my" />
      </div>

      <div class="col-lg-9">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="fw-bold mb-1">My Assigned Deliveries</h4>
            <p class="text-muted small mb-0">Update transit checkpoints from pickup to buyer delivery.</p>
          </div>
          <router-link to="/delivery/available" class="btn btn-farmora btn-sm">
            <i class="bi bi-plus-lg me-1"></i> Accept More
          </router-link>
        </div>

        <LoadingSpinner v-if="loading" message="Loading assigned runs..." />

        <div v-else-if="deliveries.length > 0">
          <DeliveryCard
            v-for="del in deliveries"
            :key="del.id"
            :delivery="del"
            :isAvailableView="false"
            @updateStatus="handleUpdateStatus"
          />
        </div>

        <EmptyState
          v-else
          icon="bi-truck"
          title="No Assigned Deliveries"
          description="You currently have no active deliveries. Browse the available list to accept trips."
          actionText="Browse Available Deliveries"
          actionRoute="/delivery/available"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import DeliveryCard from '../../components/deliveries/DeliveryCard.vue'
import { deliveryService } from '../../services/deliveryService'

const router = useRouter()
const deliveries = ref([])
const loading = ref(true)

const loadAvailable = async () => {
  loading.value = true
  try {
    deliveries.value = await deliveryService.getAvailableDeliveries()
  } catch (err) {
    console.error('Failed to load available deliveries', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAvailable()
})

const handleAccept = async (id) => {
  try {
    await deliveryService.acceptDelivery(id)
    router.push('/delivery/my-deliveries')
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to accept delivery.')
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="available" />
      </div>

      <div class="col-lg-9">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="fw-bold mb-1">Available Delivery Opportunities</h4>
            <p class="text-muted small mb-0">Packaged farm orders ready for pickup and transport to buyers.</p>
          </div>
        </div>

        <LoadingSpinner v-if="loading" message="Checking for available delivery runs..." />

        <div v-else-if="deliveries.length > 0">
          <DeliveryCard
            v-for="del in deliveries"
            :key="del.id"
            :delivery="del"
            :isAvailableView="true"
            @accept="handleAccept"
          />
        </div>

        <EmptyState
          v-else
          icon="bi-box-seam"
          title="No Delivery Runs Available"
          description="There are currently no orders ready for pickup. Check back soon as farmers prepare their harvests!"
        />
      </div>
    </div>
  </div>
</template>

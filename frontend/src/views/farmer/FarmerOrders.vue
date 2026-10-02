<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import OrderCard from '../../components/orders/OrderCard.vue'
import { orderService } from '../../services/orderService'

const orders = ref([])
const loading = ref(true)

const loadOrders = async () => {
  loading.value = true
  try {
    orders.value = await orderService.getMyOrders()
  } catch (err) {
    console.error('Failed to load orders', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOrders()
})

const handleUpdateStatus = async (orderId, status) => {
  try {
    await orderService.updateOrderStatus(orderId, status)
    loadOrders()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to update order status.')
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="orders" />
      </div>

      <div class="col-lg-9">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="fw-bold mb-1">Farmer Orders & Logistics</h4>
            <p class="text-muted small mb-0">Track confirmed purchases and mark orders ready for pickup by delivery partners.</p>
          </div>
        </div>

        <LoadingSpinner v-if="loading" message="Loading your orders..." />

        <div v-else-if="orders.length > 0">
          <OrderCard
            v-for="order in orders"
            :key="order.id"
            :order="order"
            role="FARMER"
            @updateStatus="handleUpdateStatus"
          />
        </div>

        <EmptyState
          v-else
          icon="bi-receipt"
          title="No Orders Yet"
          description="Once you accept incoming buyer bids, confirmed orders will be tracked here."
        />
      </div>
    </div>
  </div>
</template>

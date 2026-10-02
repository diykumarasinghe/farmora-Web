<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { deliveryService } from '../../services/deliveryService'

const route = useRoute()

const delivery = ref(null)
const loading = ref(true)
const updating = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

onMounted(async () => {
  try {
    delivery.value = await deliveryService.getDeliveryById(route.params.id)
  } catch (err) {
    errorMsg.value = 'Could not load delivery details.'
  } finally {
    loading.value = false
  }
})

const updateStatus = async (newStatus) => {
  updating.value = true
  errorMsg.value = ''
  try {
    delivery.value = await deliveryService.updateDeliveryStatus(delivery.value.id, newStatus)
    successMsg.value = `Status updated to ${newStatus}.`
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to update status.'
  } finally {
    updating.value = false
  }
}

const statusColor = (status) => {
  switch (status) {
    case 'AVAILABLE': return 'bg-success'
    case 'ASSIGNED': return 'bg-warning text-dark'
    case 'PICKED_UP': return 'bg-info text-dark'
    case 'IN_TRANSIT': return 'bg-primary'
    case 'DELIVERED': return 'bg-success'
    case 'CANCELLED': return 'bg-danger'
    default: return 'bg-secondary'
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
        <LoadingSpinner v-if="loading" message="Loading delivery details..." />

        <div v-else-if="delivery" class="card border-0 shadow-sm p-4">
          <!-- Header -->
          <div class="d-flex justify-content-between align-items-start mb-4 pb-3 border-bottom">
            <div>
              <span class="badge px-3 py-2 rounded-pill me-2" :class="statusColor(delivery.status)">
                {{ delivery.status }}
              </span>
              <h4 class="fw-bold d-inline">Delivery Run #{{ delivery.id }}</h4>
            </div>
            <router-link to="/delivery/my-deliveries" class="btn btn-outline-secondary btn-sm">
              <i class="bi bi-arrow-left me-1"></i> Back
            </router-link>
          </div>

          <div v-if="successMsg" class="alert alert-success small alert-dismissible" role="alert">
            {{ successMsg }}
            <button type="button" class="btn-close" @click="successMsg = ''"></button>
          </div>
          <div v-if="errorMsg" class="alert alert-danger small" role="alert">{{ errorMsg }}</div>

          <!-- Route Info -->
          <div class="row g-4 mb-4">
            <div class="col-md-6">
              <div class="card border-0 bg-light p-3 h-100">
                <div class="fw-bold mb-2 text-danger">
                  <i class="bi bi-geo-alt-fill me-2"></i>Pickup Location
                </div>
                <p class="small text-dark mb-0">{{ delivery.pickupAddress }}</p>
              </div>
            </div>
            <div class="col-md-6">
              <div class="card border-0 bg-light p-3 h-100">
                <div class="fw-bold mb-2 text-success">
                  <i class="bi bi-pin-map-fill me-2"></i>Drop-off Destination
                </div>
                <p class="small text-dark mb-0">{{ delivery.deliveryAddress }}</p>
              </div>
            </div>
          </div>

          <!-- Order Details -->
          <div class="mb-4">
            <h5 class="fw-bold mb-3">Order Information</h5>
            <div class="row g-3">
              <div class="col-sm-6">
                <div class="small text-muted">Produce</div>
                <div class="fw-bold">{{ delivery.order?.product?.name }}</div>
              </div>
              <div class="col-sm-6">
                <div class="small text-muted">Quantity</div>
                <div class="fw-bold">{{ delivery.order?.quantity }} {{ delivery.order?.product?.unit }}</div>
              </div>
              <div class="col-sm-6">
                <div class="small text-muted">Buyer (Recipient)</div>
                <div class="fw-bold">{{ delivery.order?.buyer?.fullName }}</div>
                <div class="small text-muted">{{ delivery.order?.buyer?.phone || delivery.order?.buyer?.email }}</div>
              </div>
              <div class="col-sm-6">
                <div class="small text-muted">Farmer (Sender)</div>
                <div class="fw-bold">{{ delivery.order?.farmer?.fullName }}</div>
                <div class="small text-muted">{{ delivery.order?.farmer?.phone || delivery.order?.farmer?.email }}</div>
              </div>
              <div class="col-sm-6" v-if="delivery.assignedAt">
                <div class="small text-muted">Assigned At</div>
                <div class="fw-bold">{{ new Date(delivery.assignedAt).toLocaleString() }}</div>
              </div>
              <div class="col-sm-6" v-if="delivery.pickedUpAt">
                <div class="small text-muted">Picked Up At</div>
                <div class="fw-bold">{{ new Date(delivery.pickedUpAt).toLocaleString() }}</div>
              </div>
              <div class="col-sm-6" v-if="delivery.deliveredAt">
                <div class="small text-muted">Delivered At</div>
                <div class="fw-bold text-success">{{ new Date(delivery.deliveredAt).toLocaleString() }}</div>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="d-flex flex-wrap gap-2 pt-3 border-top">
            <button
              v-if="delivery.status === 'ASSIGNED'"
              class="btn btn-warning fw-semibold"
              :disabled="updating"
              @click="updateStatus('PICKED_UP')"
            >
              <span v-if="updating" class="spinner-border spinner-border-sm me-1"></span>
              <i class="bi bi-box-arrow-up me-1"></i> Confirm Pickup
            </button>
            <button
              v-if="delivery.status === 'PICKED_UP'"
              class="btn btn-info text-dark fw-semibold"
              :disabled="updating"
              @click="updateStatus('IN_TRANSIT')"
            >
              <span v-if="updating" class="spinner-border spinner-border-sm me-1"></span>
              <i class="bi bi-truck me-1"></i> Start Transit
            </button>
            <button
              v-if="delivery.status === 'IN_TRANSIT'"
              class="btn btn-success fw-semibold px-4"
              :disabled="updating"
              @click="updateStatus('DELIVERED')"
            >
              <span v-if="updating" class="spinner-border spinner-border-sm me-1"></span>
              <i class="bi bi-check2-all me-1"></i> Mark Delivered
            </button>
            <span
              v-if="delivery.status === 'DELIVERED'"
              class="badge bg-success px-4 py-3 fw-bold fs-6"
            >
              <i class="bi bi-patch-check-fill me-1"></i> Delivery Complete!
            </span>
          </div>
        </div>

        <div v-else class="alert alert-danger">{{ errorMsg || 'Delivery not found.' }}</div>
      </div>
    </div>
  </div>
</template>

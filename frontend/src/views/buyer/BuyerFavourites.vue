<script setup>
import { ref, onMounted } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import { buyerService } from '../../services/buyerService'

const favouriteFarms = ref([])
const loading = ref(true)
const removingId = ref(null)

const loadFavourites = async () => {
  loading.value = true
  try {
    favouriteFarms.value = await buyerService.getFavouriteFarms()
  } catch (err) {
    console.error('Failed to load favourite farms', err)
  } finally {
    loading.value = false
  }
}

const removeFavourite = async (farmerId) => {
  removingId.value = farmerId
  try {
    await buyerService.removeFavouriteFarm(farmerId)
    favouriteFarms.value = favouriteFarms.value.filter(f => f.farmer?.id !== farmerId)
  } catch (err) {
    console.error('Failed to remove favourite', err)
  } finally {
    removingId.value = null
  }
}

onMounted(() => {
  loadFavourites()
})
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="favourites" />
      </div>

      <div class="col-lg-9">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h4 class="fw-bold mb-1"><i class="bi bi-heart-fill text-danger me-2"></i>My Favourite Farms</h4>
            <p class="text-muted small mb-0">Directly connect with your preferred local farms and stay updated on fresh harvests.</p>
          </div>
          <router-link to="/products" class="btn btn-outline-success btn-sm">
            <i class="bi bi-search me-1"></i> Browse More Farms & Produce
          </router-link>
        </div>

        <LoadingSpinner v-if="loading" message="Loading your saved farms..." />

        <div v-else-if="favouriteFarms.length > 0" class="row g-3">
          <div v-for="farm in favouriteFarms" :key="farm.id || farm.farmer?.id" class="col-md-6">
            <div class="card h-100 border-0 shadow-sm rounded-4 overflow-hidden p-3 bg-white">
              <div class="d-flex gap-3 align-items-start">
                <div
                  class="rounded-3 bg-success-subtle text-success d-flex align-items-center justify-content-center"
                  style="width: 72px; height: 72px; min-width: 72px; font-size: 1.8rem;"
                >
                  <img
                    v-if="farm.farmer?.profileImageUrl"
                    :src="farm.farmer.profileImageUrl"
                    alt="Farm Avatar"
                    class="rounded-3 w-100 h-100 object-fit-cover"
                  />
                  <i v-else class="bi bi-tree"></i>
                </div>

                <div class="flex-grow-1">
                  <div class="d-flex justify-content-between align-items-start">
                    <div>
                      <h6 class="fw-bold mb-0 text-dark">{{ farm.farmName }}</h6>
                      <div class="small text-muted mb-1">{{ farm.farmer?.fullName }}</div>
                    </div>
                    <button
                      class="btn btn-link text-danger p-0"
                      title="Remove from Favourites"
                      @click="removeFavourite(farm.farmer?.id)"
                      :disabled="removingId === farm.farmer?.id"
                    >
                      <i class="bi bi-heart-fill fs-5"></i>
                    </button>
                  </div>

                  <div class="d-flex align-items-center gap-2 flex-wrap mb-2">
                    <span v-if="farm.verificationStatus === 'VERIFIED'" class="badge bg-success-subtle text-success border border-success-subtle" style="font-size: 0.7rem;">
                      <i class="bi bi-patch-check-fill me-1"></i> Verified
                    </span>
                    <span v-if="farm.farmingMethod" class="badge bg-light text-dark border" style="font-size: 0.7rem;">
                      🌱 {{ farm.farmingMethod }}
                    </span>
                    <span v-if="farm.averageRating > 0" class="badge bg-warning-subtle text-dark" style="font-size: 0.7rem;">
                      ★ {{ farm.averageRating }}
                    </span>
                  </div>

                  <div class="small text-muted mb-3">
                    <i class="bi bi-geo-alt-fill text-danger me-1"></i>
                    {{ farm.district ? farm.district + ', ' + farm.province : farm.farmAddress || 'Sri Lanka' }}
                  </div>

                  <div class="d-flex gap-2">
                    <router-link
                      :to="`/farms/${farm.farmer?.id}`"
                      class="btn btn-farmora btn-sm flex-grow-1 text-center"
                    >
                      View Farm & Produce
                    </router-link>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <EmptyState
          v-else
          icon="bi-heart"
          title="No Favourite Farms Yet"
          description="Explore Sri Lankan agricultural producers, click 'Add to Favourite Farms' on any farm profile, and easily track your favourite suppliers here."
          actionText="Discover Farms"
          actionRoute="/products"
        />
      </div>
    </div>
  </div>
</template>

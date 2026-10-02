<script setup>
import { ref, onMounted } from 'vue'
import { SAMPLE_FARMS } from '../../utils/agriculturalData'
import { farmService } from '../../services/farmService'

const nearbyFarms = ref(SAMPLE_FARMS.slice(0, 3))
const loading = ref(false)

onMounted(async () => {
  try {
    const list = await farmService.getFarms()
    if (list && list.length > 0) {
      nearbyFarms.value = list.slice(0, 3)
    }
  } catch (e) {
    console.warn('Error loading nearby farms', e)
  }
})

// Calculate distance mock or relative
const getDistance = (index) => {
  const distances = ['2.4 km', '5.1 km', '12.3 km', '18.7 km']
  return distances[index % distances.length]
}
</script>

<template>
  <div class="nearby-farms-widget mb-4">
    <!-- Header -->
    <div class="card border-0 shadow-sm rounded-4 p-3 mb-3 bg-white">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h6 class="fw-bold text-dark mb-0 d-flex align-items-center gap-2">
          <i class="bi bi-compass-fill text-farmora-green"></i>
          <span>Nearby Farms</span>
        </h6>
        <router-link to="/farm-discovery" class="text-farmora-green fw-semibold text-decoration-none small d-flex align-items-center gap-1">
          <span>View All</span>
          <i class="bi bi-arrow-right-short fs-6"></i>
        </router-link>
      </div>

      <!-- Farm List -->
      <div class="d-flex flex-column gap-3">
        <div
          v-for="(farm, idx) in nearbyFarms"
          :key="farm.id"
          class="d-flex align-items-center justify-content-between p-2 rounded-3 farm-item-row"
        >
          <div class="d-flex align-items-center gap-2 overflow-hidden">
            <img
              :src="farm.imageUrl || 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=100'"
              :alt="farm.farmName"
              class="rounded-3 object-fit-cover flex-shrink-0"
              style="width: 44px; height: 44px;"
            />
            <div class="overflow-hidden">
              <div class="fw-bold text-truncate text-dark small" :title="farm.farmName">
                {{ farm.farmName }}
              </div>
              <div class="d-flex align-items-center gap-1 text-muted" style="font-size: 0.72rem;">
                <span class="text-warning">★ {{ farm.averageRating || '4.8' }}</span>
                <span>({{ farm.totalReviews || '80' }})</span>
                <span class="mx-1">•</span>
                <span class="text-truncate">{{ getDistance(idx) }} • {{ farm.district }}</span>
              </div>
            </div>
          </div>

          <router-link
            :to="`/farms/${farm.id}`"
            class="btn btn-sm btn-outline-success py-1 px-2 fw-medium ms-2 flex-shrink-0"
            style="font-size: 0.78rem;"
          >
            View
          </router-link>
        </div>
      </div>
    </div>

    <!-- Request a Product Banner -->
    <div class="card border-0 shadow-sm rounded-4 p-3 bg-light-green text-center">
      <div class="d-flex align-items-start gap-2 mb-2 text-start">
        <div class="request-icon rounded-circle d-flex align-items-center justify-content-center flex-shrink-0">
          <i class="bi bi-patch-question-fill text-farmora-green fs-5"></i>
        </div>
        <div>
          <div class="fw-bold text-dark small">Can't find what you need?</div>
          <div class="text-muted" style="font-size: 0.75rem;">
            Ask farmers directly or request custom bulk harvest.
          </div>
        </div>
      </div>
      <router-link
        to="/contact"
        class="btn btn-farmora-outline w-100 py-1-5 fw-semibold mt-1"
        style="font-size: 0.82rem;"
      >
        Request a Produce &rarr;
      </router-link>
    </div>
  </div>
</template>

<style scoped>
.text-farmora-green {
  color: #2d6a4f !important;
}

.farm-item-row {
  background: #fdfdfd;
  border: 1px solid #f3f4f6;
  transition: all 0.2s ease;
}

.farm-item-row:hover {
  background: #f8faf8;
  border-color: #d1fae5;
  transform: translateX(2px);
}

.bg-light-green {
  background: linear-gradient(135deg, #f0fdf4 0%, #e8f5e9 100%);
  border: 1px dashed #86efac !important;
}

.request-icon {
  width: 36px;
  height: 36px;
  background: #dcfce7;
}

.py-1-5 {
  padding-top: 0.4rem;
  padding-bottom: 0.4rem;
}
</style>

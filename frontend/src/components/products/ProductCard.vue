<script setup>
import { computed } from 'vue'
import { useProductStore } from '../../stores/products'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const productStore = useProductStore()

const defaultImage = 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=600&q=80'

const productImage = computed(() => {
  return props.product.imageUrl || defaultImage
})

const isFreshToday = computed(() => {
  if (props.product.isFreshToday) return true
  const todayStr = new Date().toISOString().split('T')[0]
  return props.product.harvestDate === todayStr
})

const isFav = computed(() => {
  return productStore.isFavorite(props.product.id)
})

const toggleFav = (e) => {
  e.preventDefault()
  e.stopPropagation()
  productStore.toggleFavorite(props.product.id)
}

const displayFarmer = computed(() => {
  if (typeof props.product.farmer === 'string') return props.product.farmer
  return props.product.farmer?.fullName || 'Verified Farmer'
})

const displayLocation = computed(() => {
  return props.product.location || props.product.district || 'Sri Lanka'
})

const farmingMethodLabel = computed(() => {
  return props.product.farmingMethod || 'Organic'
})

const isInStock = computed(() => {
  return props.product.status === 'AVAILABLE' && Number(props.product.quantity || 0) > 0
})
</script>

<template>
  <div class="card h-100 border-0 shadow-sm overflow-hidden product-card position-relative">
    <!-- Image Container -->
    <div class="product-image-wrapper position-relative overflow-hidden">
      <img
        :src="productImage"
        :alt="product.name"
        class="card-img-top product-img"
        @error="$event.target.src = defaultImage"
      />

      <!-- Fresh Today Badge -->
      <span
        v-if="isFreshToday"
        class="position-absolute top-0 start-0 m-2 badge fresh-badge px-2 py-1 shadow-sm d-flex align-items-center gap-1"
      >
        <i class="bi bi-patch-check-fill text-white"></i>
        <span>Fresh Today</span>
      </span>

      <!-- Heart / Wishlist Toggle -->
      <button
        class="position-absolute top-0 end-0 m-2 btn btn-sm fav-btn rounded-circle d-flex align-items-center justify-content-center"
        :class="{ active: isFav }"
        @click="toggleFav"
        title="Add to Favourites"
        type="button"
      >
        <i class="bi" :class="isFav ? 'bi-heart-fill text-danger' : 'bi-heart text-secondary'"></i>
      </button>

      <!-- Category Pill -->
      <span class="position-absolute bottom-0 start-0 m-2 badge category-badge px-2 py-1">
        {{ product.category }}
      </span>
    </div>

    <!-- Body -->
    <div class="card-body p-3 d-flex flex-column">
      <!-- Title & Rating -->
      <div class="d-flex justify-content-between align-items-start mb-1">
        <h6 class="card-title fw-bold text-truncate mb-0 text-dark" :title="product.name" style="font-size: 1.02rem;">
          {{ product.name }}
        </h6>
        <div v-if="product.rating" class="d-flex align-items-center text-warning small ms-1 flex-shrink-0" style="font-size: 0.78rem;">
          <i class="bi bi-star-fill me-1"></i>
          <span class="fw-bold text-dark">{{ product.rating }}</span>
        </div>
      </div>

      <!-- Farmer & Location -->
      <div class="farmer-location-meta mb-2">
        <div class="text-muted small fw-medium text-truncate">
          {{ displayFarmer }}
        </div>
        <div class="text-secondary small d-flex align-items-center gap-1" style="font-size: 0.78rem;">
          <i class="bi bi-geo-alt-fill text-danger" style="font-size: 0.75rem;"></i>
          <span class="text-truncate">{{ displayLocation }}</span>
        </div>
      </div>

      <!-- Tags: Farming Method & Stock Status -->
      <div class="d-flex align-items-center gap-2 mb-3 flex-wrap">
        <span class="badge badge-tag method-badge">
          {{ farmingMethodLabel }}
        </span>
        <span
          class="badge badge-tag"
          :class="isInStock ? 'badge-in-stock' : 'badge-out-stock'"
        >
          {{ isInStock ? 'In Stock' : 'Out of Stock' }}
        </span>
        <span v-if="product.quantity" class="small text-muted ms-auto" style="font-size: 0.75rem;">
          {{ product.quantity }} {{ product.unit || 'kg' }}
        </span>
      </div>

      <!-- Price Section -->
      <div class="mt-auto pt-2 border-top">
        <div class="d-flex align-items-baseline justify-content-between">
          <div>
            <span class="fs-5 fw-bold text-farmora-green">
              Rs. {{ Number(product.price).toLocaleString() }}
            </span>
            <span class="text-muted small ms-1">/ {{ product.unit || 'kg' }}</span>
          </div>

          <!-- Wholesale price indicator if available -->
          <div v-if="product.wholesalePrice" class="small text-muted text-end" style="font-size: 0.73rem;">
            <span class="d-block text-secondary">Wholesale</span>
            <span class="fw-semibold text-dark">Rs. {{ Number(product.wholesalePrice).toLocaleString() }}</span>
          </div>
        </div>

        <!-- Action Button -->
        <router-link
          :to="`/products/${product.id}`"
          class="btn btn-farmora w-100 mt-2 py-1-5 fw-semibold d-flex align-items-center justify-content-center gap-2"
          style="font-size: 0.88rem;"
        >
          <span>View Details</span>
          <i class="bi bi-arrow-right-short fs-5 lh-1"></i>
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  border-radius: 14px;
  background: #ffffff;
  transition: transform 0.25s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.25s ease;
  border: 1px solid rgba(0, 0, 0, 0.06) !important;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(45, 106, 79, 0.12) !important;
}

.product-image-wrapper {
  height: 190px;
  background: #f1f5f1;
}

.product-img {
  height: 100%;
  width: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-card:hover .product-img {
  transform: scale(1.05);
}

.fresh-badge {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #ffffff;
  font-size: 0.72rem;
  font-weight: 600;
  border-radius: 20px;
  letter-spacing: 0.02em;
}

.fav-btn {
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  transition: transform 0.2s ease, background-color 0.2s ease;
}

.fav-btn:hover {
  transform: scale(1.1);
  background: #ffffff;
}

.category-badge {
  background: rgba(17, 24, 39, 0.75);
  color: #ffffff;
  font-size: 0.7rem;
  font-weight: 500;
  backdrop-filter: blur(4px);
  border-radius: 6px;
}

.badge-tag {
  font-size: 0.72rem;
  font-weight: 600;
  padding: 0.25rem 0.6rem;
  border-radius: 6px;
}

.method-badge {
  background: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #c8e6c9;
}

.badge-in-stock {
  background: #e0f2fe;
  color: #0369a1;
  border: 1px solid #bae6fd;
}

.badge-out-stock {
  background: #fee2e2;
  color: #b91c1c;
  border: 1px solid #fecaca;
}

.py-1-5 {
  padding-top: 0.45rem;
  padding-bottom: 0.45rem;
}
</style>

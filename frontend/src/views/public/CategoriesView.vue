<script setup>
import { computed } from 'vue'
import { AGRICULTURAL_CATEGORIES } from '../../utils/agriculturalData'
import { useProductStore } from '../../stores/products'

const productStore = useProductStore()
const categoryCounts = computed(() => productStore.categoryCounts)
</script>

<template>
  <div class="categories-view bg-light min-vh-100 py-4">
    <div class="container-xxl">
      <!-- Hero -->
      <div class="categories-hero p-4 p-md-5 rounded-4 mb-4 text-white shadow-sm d-flex flex-wrap justify-content-between align-items-center">
        <div>
          <div class="badge bg-warning text-dark px-3 py-1-5 rounded-pill mb-2 fw-bold text-uppercase" style="font-size: 0.72rem;">
            <i class="bi bi-grid-3x3-gap-fill me-1"></i> Agricultural Classifications
          </div>
          <h2 class="fw-bold mb-1">11 Sri Lankan Agricultural Categories</h2>
          <p class="text-white-75 mb-0" style="max-width: 600px;">
            Explore our curated agricultural categories, cultivated across Sri Lanka's microclimates with verified organic and sustainable farming methods.
          </p>
        </div>

        <router-link to="/products" class="btn btn-warning fw-bold px-3 py-2 text-dark shadow-sm mt-3 mt-md-0">
          Browse All Marketplace Produce &rarr;
        </router-link>
      </div>

      <!-- Categories Grid -->
      <div class="row g-4">
        <div
          v-for="cat in AGRICULTURAL_CATEGORIES"
          :key="cat.id"
          class="col-md-6 col-lg-4"
        >
          <div class="card h-100 border-0 shadow-sm rounded-4 overflow-hidden category-showcase-card bg-white position-relative">
            <!-- Image Header -->
            <div class="position-relative overflow-hidden" style="height: 180px;">
              <img
                :src="cat.image"
                :alt="cat.name"
                class="w-100 h-100 object-fit-cover cat-cover-img"
              />
              <div class="position-absolute top-0 end-0 m-3 badge bg-white bg-opacity-95 text-dark px-3 py-1-5 rounded-pill shadow-sm fw-bold">
                <i class="bi bi-stack text-farmora-green me-1"></i>
                {{ categoryCounts[cat.name] || cat.count }} Listings
              </div>
              <div class="position-absolute bottom-0 start-0 m-3 badge bg-dark bg-opacity-75 text-white fs-5 p-2 rounded-circle shadow-sm">
                {{ cat.svgIcon }}
              </div>
            </div>

            <!-- Body -->
            <div class="card-body p-4 d-flex flex-column">
              <h4 class="fw-bold text-dark mb-2">{{ cat.name }}</h4>
              <p class="text-secondary small leading-relaxed mb-3 flex-grow-1">
                {{ cat.description }}
              </p>

              <!-- Popular Crops -->
              <div class="mb-3">
                <div class="text-muted small fw-bold mb-2">Key Sri Lankan Varieties:</div>
                <div class="d-flex flex-wrap gap-1">
                  <span
                    v-for="(crop, cidx) in cat.popularCrops"
                    :key="cidx"
                    class="badge bg-light text-dark border px-2 py-1 rounded"
                    style="font-size: 0.75rem;"
                  >
                    {{ crop }}
                  </span>
                </div>
              </div>

              <!-- Button -->
              <router-link
                :to="`/products?category=${encodeURIComponent(cat.name)}`"
                class="btn btn-farmora w-100 py-2 fw-semibold d-flex align-items-center justify-content-center gap-2 mt-auto"
              >
                <span>Explore {{ cat.name }}</span>
                <i class="bi bi-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.categories-hero {
  background: linear-gradient(135deg, #1b4332 0%, #2d6a4f 100%);
}

.text-white-75 {
  color: rgba(255, 255, 255, 0.75);
}

.text-farmora-green {
  color: #2d6a4f !important;
}

.category-showcase-card {
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  border: 1px solid rgba(0, 0, 0, 0.06) !important;
}

.category-showcase-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(45, 106, 79, 0.12) !important;
}

.cat-cover-img {
  transition: transform 0.35s ease;
}

.category-showcase-card:hover .cat-cover-img {
  transform: scale(1.06);
}

.py-1-5 {
  padding-top: 0.35rem;
  padding-bottom: 0.35rem;
}
</style>

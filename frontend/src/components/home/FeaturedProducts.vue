<script setup>
import { ref, onMounted } from 'vue'
import { productService } from '../../services/productService'

const products = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const data = await productService.getProducts({ size: 6, status: 'AVAILABLE' })
    products.value = data.content || data || []
  } catch (err) {
    console.error('Featured products load error:', err)
    products.value = []
  } finally {
    loading.value = false
  }
})

const placeholderImages = [
  'https://images.unsplash.com/photo-1592924357229-c5e7af823ea0?auto=format&fit=crop&w=500&q=80',
  'https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=500&q=80',
  'https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?auto=format&fit=crop&w=500&q=80',
  'https://images.unsplash.com/photo-1562329265-95a6d7a83440?auto=format&fit=crop&w=500&q=80',
  'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?auto=format&fit=crop&w=500&q=80',
  'https://images.unsplash.com/photo-1519058082700-08a0b56da9b4?auto=format&fit=crop&w=500&q=80'
]

const getProductImage = (product, idx) => {
  return product.imageUrl || placeholderImages[idx % placeholderImages.length]
}
</script>

<template>
  <section class="py-5 bg-white">
    <div class="container">
      <!-- Header -->
      <div class="d-flex justify-content-between align-items-end mb-4">
        <div>
          <h2 class="fw-bold mb-1">Featured Products</h2>
          <p class="text-muted small mb-0">Freshly listed by verified local farmers</p>
        </div>
        <router-link to="/products" class="btn btn-fp-outline">
          View All <i class="bi bi-arrow-right ms-1"></i>
        </router-link>
      </div>

      <!-- Loading Skeleton -->
      <div v-if="loading" class="row g-4">
        <div v-for="n in 6" :key="n" class="col-6 col-md-4 col-lg-2">
          <div class="fp-skeleton">
            <div class="fp-skel-img"></div>
            <div class="p-3">
              <div class="skel-bar mb-2"></div>
              <div class="skel-bar short mb-3"></div>
              <div class="skel-btn"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Product Grid -->
      <div v-else-if="products.length > 0" class="row g-4">
        <div v-for="(product, idx) in products" :key="product.id" class="col-6 col-md-4 col-lg-2">
          <router-link :to="`/products/${product.id}`" class="fp-card text-decoration-none">
            <div class="fp-img-wrap">
              <img
                :src="getProductImage(product, idx)"
                :alt="product.name"
                class="fp-img"
              />
              <span class="fp-badge">
                <i class="bi bi-star-fill me-1"></i>Fresh
              </span>
            </div>
            <div class="fp-body p-3">
              <div class="fp-name fw-bold mb-1">{{ product.name }}</div>
              <div class="fp-meta text-muted small mb-2">
                <i class="bi bi-geo-alt-fill me-1 text-success"></i>
                {{ product.location || 'Sri Lanka' }}
              </div>
              <div class="d-flex justify-content-between align-items-center">
                <div class="fp-price">LKR {{ product.pricePerUnit?.toLocaleString() }}<span class="fp-unit">/{{ product.unit }}</span></div>
              </div>
              <div class="fp-farmer small text-muted mt-1">
                <i class="bi bi-person-fill me-1"></i>{{ product.farmerName || product.farmer?.fullName || 'Farmer' }}
              </div>
              <div class="fp-btn mt-2 w-100 text-center">View Details</div>
            </div>
          </router-link>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-5 text-muted">
        <i class="bi bi-basket2 display-4 mb-3 d-block opacity-25"></i>
        <p>No products listed yet. Check back soon!</p>
        <router-link to="/register" class="btn btn-farmora-green">List Your Produce</router-link>
      </div>
    </div>
  </section>
</template>

<style scoped>
.btn-fp-outline {
  border: 1.5px solid #2d6a4f;
  color: #2d6a4f;
  border-radius: 20px;
  font-size: 0.83rem;
  font-weight: 600;
  padding: 0.4rem 1rem;
  background: transparent;
  white-space: nowrap;
  transition: all 0.2s;
}

.btn-fp-outline:hover { background: #2d6a4f; color: #fff; }

.fp-card {
  display: block;
  border-radius: 14px;
  background: #fff;
  border: 1.5px solid #f1f5f1;
  overflow: hidden;
  transition: all 0.25s;
  color: inherit;
}

.fp-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(45,106,79,0.12);
  border-color: #2d6a4f;
}

.fp-img-wrap {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
}

.fp-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.fp-card:hover .fp-img { transform: scale(1.06); }

.fp-badge {
  position: absolute;
  top: 0.5rem;
  left: 0.5rem;
  background: #2d6a4f;
  color: #fff;
  font-size: 0.68rem;
  font-weight: 700;
  padding: 0.2rem 0.5rem;
  border-radius: 20px;
}

.fp-name {
  font-size: 0.88rem;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.fp-meta { font-size: 0.75rem; }

.fp-price {
  font-size: 0.85rem;
  font-weight: 800;
  color: #1b4332;
}

.fp-unit {
  font-size: 0.7rem;
  font-weight: 500;
  color: #6b7280;
}

.fp-farmer { font-size: 0.72rem; }

.fp-btn {
  background: #d8f3dc;
  color: #1b4332;
  font-size: 0.77rem;
  font-weight: 700;
  padding: 0.4rem 0;
  border-radius: 8px;
  transition: background 0.2s;
}

.fp-card:hover .fp-btn {
  background: #2d6a4f;
  color: #fff;
}

/* Skeleton */
.fp-skeleton {
  border-radius: 14px;
  background: #f9fafb;
  border: 1.5px solid #f1f5f1;
  overflow: hidden;
  animation: shimmer 1.5s infinite;
}

.fp-skel-img { aspect-ratio: 1; background: #e5e7eb; }

.skel-bar {
  height: 12px;
  background: #e5e7eb;
  border-radius: 6px;
  width: 100%;
}

.skel-bar.short { width: 60%; }

.skel-btn {
  height: 28px;
  background: #e5e7eb;
  border-radius: 8px;
}

@keyframes shimmer {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.btn-farmora-green {
  background: #2d6a4f;
  color: #fff;
  border-radius: 8px;
  font-weight: 600;
}
</style>

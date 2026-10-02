<template>
  <section class="py-5 bg-light category-section">
    <div class="container">
      <div class="d-flex justify-content-between align-items-end mb-4">
        <div>
          <h2 class="fw-bold mb-1">Shop by Category</h2>
          <p class="text-muted small mb-0">Explore fresh produce from local farmers</p>
        </div>
        <router-link to="/products" class="btn btn-view-all">
          View All Products <i class="bi bi-arrow-right ms-1"></i>
        </router-link>
      </div>

      <div class="row g-3">
        <div v-for="cat in categories" :key="cat.name" class="col-6 col-sm-4 col-lg-2">
          <router-link :to="`/products?category=${cat.name}`" class="category-card text-decoration-none">
            <div class="cat-img-wrap">
              <img :src="cat.image" :alt="cat.name" class="cat-img" />
              <div class="cat-overlay"></div>
            </div>
            <div class="cat-label fw-semibold">{{ cat.label }}</div>
          </router-link>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
const categories = [
  {
    name: 'VEGETABLE',
    label: 'Vegetables',
    image: 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=400&q=80'
  },
  {
    name: 'FRUIT',
    label: 'Fruits',
    image: 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?auto=format&fit=crop&w=400&q=80'
  },
  {
    name: 'GRAIN',
    label: 'Grains',
    image: 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?auto=format&fit=crop&w=400&q=80'
  },
  {
    name: 'LEAFY_GREENS',
    label: 'Leafy Greens',
    image: 'https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=400&q=80'
  },
  {
    name: 'SPICE',
    label: 'Spices',
    image: 'https://images.unsplash.com/photo-1596040033229-a9821ebd058d?auto=format&fit=crop&w=400&q=80'
  },
  {
    name: 'DAIRY',
    label: 'Dairy & Eggs',
    image: 'https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=400&q=80'
  }
]
</script>

<style scoped>
.category-section {
  background: #f8f9f4 !important;
}

.btn-view-all {
  border: 1.5px solid #2d6a4f;
  color: #2d6a4f;
  background: transparent;
  border-radius: 20px;
  font-size: 0.83rem;
  font-weight: 600;
  padding: 0.4rem 1rem;
  white-space: nowrap;
  transition: all 0.2s;
}

.btn-view-all:hover {
  background: #2d6a4f;
  color: #fff;
}

.category-card {
  display: block;
  text-align: center;
  cursor: pointer;
}

.cat-img-wrap {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  aspect-ratio: 1;
  margin-bottom: 0.6rem;
  transition: transform 0.25s;
}

.category-card:hover .cat-img-wrap {
  transform: translateY(-4px);
  box-shadow: 0 10px 24px rgba(0,0,0,0.12);
}

.cat-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.category-card:hover .cat-img {
  transform: scale(1.05);
}

.cat-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.25), transparent 60%);
}

.cat-label {
  font-size: 0.85rem;
  color: #1f2937;
}
</style>

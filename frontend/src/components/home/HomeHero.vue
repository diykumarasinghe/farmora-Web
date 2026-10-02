<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const isAuthenticated = computed(() => authStore.isAuthenticated)
</script>

<template>
  <section class="hero-section position-relative overflow-hidden">
    <div class="container py-5">
      <div class="row align-items-center g-4 py-lg-4">
        <!-- Left: Text Content -->
        <div class="col-lg-6 pe-lg-5">
          <p class="hero-eyebrow mb-3">
            <span class="text-success fw-semibold">Fresh Produce</span>
            <span class="mx-2 text-muted">·</span>
            <span class="text-success fw-semibold">Fair Prices</span>
            <span class="mx-2 text-muted">·</span>
            <span class="text-success fw-semibold">Brighter Tomorrow</span>
          </p>

          <h1 class="hero-headline mb-4">
            Connecting Farmers to a
            <span class="text-green-highlight">Better Tomorrow</span>
          </h1>

          <p class="hero-desc mb-5">
            Farmora is Sri Lanka's digital agricultural marketplace, helping farmers get fair prices, buyers access fresh local produce, and delivery partners keep the nation moving.
          </p>

          <div class="d-flex flex-wrap gap-3 mb-5">
            <router-link to="/products" class="btn btn-hero-primary">
              <i class="bi bi-cart3 me-2"></i>Browse Fresh Produce
              <i class="bi bi-arrow-right ms-2"></i>
            </router-link>
            <a href="#how-it-works" class="btn btn-hero-secondary">
              <i class="bi bi-play-circle-fill me-2 text-success"></i>Watch How It Works
            </a>
          </div>

          <!-- Stats Row -->
          <div class="hero-stats row g-0">
            <div class="col-3 hero-stat text-center">
              <div class="stat-value">500+</div>
              <div class="stat-label">Active Farmers</div>
            </div>
            <div class="col-3 hero-stat text-center">
              <div class="stat-value">10,000+</div>
              <div class="stat-label">Happy Customers</div>
            </div>
            <div class="col-3 hero-stat text-center">
              <div class="stat-value">100%</div>
              <div class="stat-label">Local Produce</div>
            </div>
            <div class="col-3 hero-stat text-center border-0">
              <div class="stat-value">24/7</div>
              <div class="stat-label">Support</div>
            </div>
          </div>
        </div>

        <!-- Right: Hero Image -->
        <div class="col-lg-6 text-center position-relative">
          <div class="hero-image-wrap">
            <img
              src="https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=900&q=85"
              alt="Happy Sri Lankan farmer holding fresh vegetables"
              class="hero-img"
            />
            <!-- Floating badge -->
            <div class="hero-float-badge">
              <div class="d-flex align-items-center gap-2 p-2">
                <div class="float-icon">
                  <i class="bi bi-flower2 text-success"></i>
                </div>
                <div class="lh-sm">
                  <div class="fw-bold small" style="font-size: 0.75rem;">From Our Farms</div>
                  <div class="text-muted" style="font-size: 0.68rem;">To Your Family</div>
                </div>
              </div>
            </div>
            <!-- Cursive overlay -->
            <div class="hero-cursive">
              <span>Good Food,<br/>Stronger Communities</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Slide Dots (decorative) -->
    <div class="hero-dots">
      <span class="dot active"></span>
      <span class="dot"></span>
      <span class="dot"></span>
    </div>
  </section>
</template>

<style scoped>
.hero-section {
  background: #ffffff;
  padding-bottom: 1rem;
  border-bottom: 1px solid #f1f5f1;
}

.hero-eyebrow {
  font-size: 0.82rem;
  letter-spacing: 0.01em;
  text-transform: uppercase;
}

.hero-headline {
  font-size: clamp(2rem, 4.5vw, 3.2rem);
  font-weight: 900;
  line-height: 1.1;
  color: #111827;
  letter-spacing: -0.02em;
}

.text-green-highlight {
  color: #2d6a4f;
}

.hero-desc {
  font-size: 1rem;
  color: #6b7280;
  max-width: 500px;
  line-height: 1.7;
}

.btn-hero-primary {
  background: #1b4332;
  color: #fff;
  border: 2px solid #1b4332;
  border-radius: 10px;
  font-weight: 700;
  padding: 0.75rem 1.6rem;
  font-size: 0.95rem;
  transition: all 0.25s;
  display: inline-flex;
  align-items: center;
}

.btn-hero-primary:hover {
  background: #2d6a4f;
  border-color: #2d6a4f;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(27, 67, 50, 0.3);
}

.btn-hero-secondary {
  background: #fff;
  color: #111827;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  font-weight: 600;
  padding: 0.75rem 1.6rem;
  font-size: 0.95rem;
  transition: all 0.25s;
  display: inline-flex;
  align-items: center;
}

.btn-hero-secondary:hover {
  border-color: #2d6a4f;
  color: #2d6a4f;
  transform: translateY(-2px);
}

/* Stats */
.hero-stats {
  border-top: 1.5px solid #e5e7eb;
  padding-top: 1.5rem;
}

.hero-stat {
  border-right: 1.5px solid #e5e7eb;
  padding: 0.5rem 0;
}

.stat-value {
  font-size: 1.4rem;
  font-weight: 800;
  color: #111827;
  line-height: 1.2;
}

.stat-label {
  font-size: 0.72rem;
  color: #6b7280;
  margin-top: 0.15rem;
}

/* Hero Image */
.hero-image-wrap {
  position: relative;
  display: inline-block;
}

.hero-img {
  width: 100%;
  max-height: 500px;
  object-fit: cover;
  border-radius: 16px;
  display: block;
}

.hero-float-badge {
  position: absolute;
  bottom: 2rem;
  right: -1rem;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  min-width: 150px;
}

.float-icon {
  width: 36px;
  height: 36px;
  background: #d8f3dc;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  flex-shrink: 0;
}

.hero-cursive {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  font-family: 'Georgia', cursive;
  font-style: italic;
  font-size: 1.1rem;
  color: #2d6a4f;
  text-align: right;
  line-height: 1.4;
  font-weight: 700;
  text-shadow: 0 1px 2px rgba(255,255,255,0.8);
}

/* Dots */
.hero-dots {
  display: flex;
  justify-content: center;
  gap: 6px;
  padding: 1.5rem 0 0.5rem;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #d1d5db;
  transition: background 0.2s;
}

.dot.active {
  background: #2d6a4f;
  width: 24px;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .hero-float-badge { display: none; }
  .hero-cursive { display: none; }
  .hero-img { max-height: 280px; }
}
</style>

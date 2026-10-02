<template>
  <section class="py-5 bg-light why-section">
    <div class="container">
      <div class="row g-5 align-items-center">
        <!-- Left: Features Grid -->
        <div class="col-lg-7">
          <h2 class="fw-bold mb-1">Why Choose Farmora?</h2>
          <p class="text-muted mb-4">We make agricultural trade transparent, fair, and efficient for everyone.</p>

          <div class="row g-3">
            <div v-for="feat in features" :key="feat.title" class="col-6">
              <div class="why-card p-3 h-100">
                <div class="why-icon mb-2" :class="feat.iconBg">
                  <i :class="feat.icon"></i>
                </div>
                <h6 class="fw-bold mb-1">{{ feat.title }}</h6>
                <p class="small text-muted mb-0">{{ feat.desc }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Right: Testimonial -->
        <div class="col-lg-5">
          <div class="testimonial-card p-4 p-lg-5">
            <div class="quote-mark">"</div>
            <p class="testimonial-text mb-4">
              "Farmora helped me reach more buyers and get better prices for my harvest. It's a game changer!"
            </p>
            <div class="d-flex align-items-center gap-3">
              <img
                src="https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=100&q=80"
                alt="Nimal Perera"
                class="testimonial-avatar"
              />
              <div>
                <div class="fw-bold text-dark">— Nimal Perera</div>
                <div class="small text-muted">Vegetable Farmer, Kandy</div>
                <div class="mt-1">
                  <i v-for="n in 5" :key="n" class="bi bi-star-fill text-warning small"></i>
                </div>
              </div>
            </div>
            <div class="testimonial-dots mt-3">
              <span class="t-dot active"></span>
              <span class="t-dot"></span>
              <span class="t-dot"></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
const features = [
  {
    title: 'Direct Connection',
    desc: 'Buy directly from farmers with no middlemen.',
    icon: 'bi bi-hand-thumbs-up-fill',
    iconBg: 'icon-green'
  },
  {
    title: 'Fair Prices',
    desc: 'More value for everyone through transparent bidding.',
    icon: 'bi bi-currency-exchange',
    iconBg: 'icon-blue'
  },
  {
    title: 'Fresh & Local',
    desc: 'Support local agriculture and get the freshest produce.',
    icon: 'bi bi-flower2',
    iconBg: 'icon-green'
  },
  {
    title: 'Reliable Delivery',
    desc: 'From farm to your doorstep, every order tracked.',
    icon: 'bi bi-truck',
    iconBg: 'icon-orange'
  }
]
</script>

<style scoped>
.why-section { background: #f8f9f4 !important; }

.why-card {
  background: #fff;
  border-radius: 12px;
  border: 1.5px solid #f1f5f1;
  transition: box-shadow 0.2s, border-color 0.2s;
}

.why-card:hover {
  box-shadow: 0 6px 20px rgba(45,106,79,0.1);
  border-color: #d8f3dc;
}

.why-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.icon-green { background: #d8f3dc; color: #2d6a4f; }
.icon-blue { background: #dbeafe; color: #2563eb; }
.icon-orange { background: #fef3c7; color: #d97706; }

.testimonial-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.07);
  border: 1.5px solid #f1f5f1;
  position: relative;
}

.quote-mark {
  font-size: 4rem;
  line-height: 1;
  color: #2d6a4f;
  font-family: Georgia, serif;
  font-weight: 900;
  margin-bottom: -1rem;
  opacity: 0.3;
}

.testimonial-text {
  font-size: 1rem;
  color: #374151;
  line-height: 1.7;
  font-style: italic;
}

.testimonial-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #d8f3dc;
}

.testimonial-dots {
  display: flex;
  gap: 5px;
}

.t-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #d1d5db;
}

.t-dot.active {
  background: #2d6a4f;
  width: 20px;
  border-radius: 4px;
}
</style>

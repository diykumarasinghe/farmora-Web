<template>
  <section id="how-it-works" class="py-5 bg-white how-section">
    <div class="container">
      <div class="text-center mb-5">
        <span class="section-badge mb-2 d-inline-block">Simple Process</span>
        <h2 class="fw-bold mb-2">How Farmora Works</h2>
        <p class="text-muted" style="max-width: 520px; margin: 0 auto;">From the field to your doorstep — four simple steps that make fresh produce accessible to everyone.</p>
      </div>

      <div class="row g-0 align-items-start justify-content-center">
        <div v-for="(step, idx) in steps" :key="step.title" class="col-6 col-md-3 step-col">
          <div class="step-card text-center px-3 pb-4">
            <div class="step-connector" v-if="idx < steps.length - 1"></div>
            <div class="step-num-wrap mx-auto mb-3">
              <div class="step-num">{{ idx + 1 }}</div>
            </div>
            <div class="step-icon-wrap mx-auto mb-3">
              <i :class="step.icon"></i>
            </div>
            <h6 class="fw-bold mb-1">{{ step.title }}</h6>
            <p class="small text-muted mb-0">{{ step.desc }}</p>
          </div>
        </div>
      </div>

      <div class="text-center mt-4">
        <router-link to="/register" class="btn btn-hiw-primary me-3">
          Get Started <i class="bi bi-arrow-right ms-1"></i>
        </router-link>
        <router-link to="/about" class="btn btn-hiw-secondary">Learn More</router-link>
      </div>
    </div>
  </section>
</template>

<script setup>
const steps = [
  {
    icon: 'bi bi-box-seam',
    title: 'Farmer Lists Product',
    desc: 'Verified farmers upload produce with photos, prices, and harvest details.'
  },
  {
    icon: 'bi bi-hand-index-fill',
    title: 'Buyer Places Bid',
    desc: 'Registered buyers browse listings and submit competitive price offers.'
  },
  {
    icon: 'bi bi-check2-circle',
    title: 'Farmer Accepts',
    desc: 'Farmer reviews bids, accepts the best offer, and prepares the order.'
  },
  {
    icon: 'bi bi-truck',
    title: 'Delivery Partner Ships',
    desc: 'A certified partner accepts the delivery and brings it to the buyer.'
  }
]
</script>

<style scoped>
.how-section { background: #fff !important; }

.section-badge {
  background: #d8f3dc;
  color: #1b4332;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.step-col { position: relative; }

.step-card { position: relative; }

.step-connector {
  position: absolute;
  top: 28px;
  left: 60%;
  width: 80%;
  height: 2px;
  background: linear-gradient(to right, #2d6a4f, #d8f3dc);
  z-index: 0;
}

.step-num-wrap {
  position: relative;
  z-index: 1;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #2d6a4f;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-num {
  font-size: 0.85rem;
  font-weight: 800;
  color: #fff;
}

.step-icon-wrap {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #d8f3dc;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: #2d6a4f;
  transition: background 0.25s, color 0.25s;
}

.step-card:hover .step-icon-wrap {
  background: #2d6a4f;
  color: #fff;
}

.btn-hiw-primary {
  background: #1b4332;
  color: #fff;
  border-radius: 10px;
  font-weight: 700;
  padding: 0.65rem 1.6rem;
  border: 2px solid #1b4332;
  transition: all 0.2s;
}

.btn-hiw-primary:hover {
  background: #2d6a4f;
  border-color: #2d6a4f;
  color: #fff;
  transform: translateY(-2px);
}

.btn-hiw-secondary {
  background: transparent;
  color: #2d6a4f;
  border: 2px solid #d8f3dc;
  border-radius: 10px;
  font-weight: 600;
  padding: 0.65rem 1.6rem;
  transition: all 0.2s;
}

.btn-hiw-secondary:hover {
  border-color: #2d6a4f;
  background: #f0faf2;
}

@media (max-width: 576px) {
  .step-connector { display: none; }
}
</style>

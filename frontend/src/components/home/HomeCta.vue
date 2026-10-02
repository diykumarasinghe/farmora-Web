<template>
  <section class="cta-section py-5">
    <div class="container">
      <div class="cta-card p-5 text-center">
        <span class="cta-badge mb-3 d-inline-block">Join 10,000+ Members</span>
        <h2 class="fw-bold text-white mb-3">Ready to Transform Your Agricultural Trade?</h2>
        <p class="text-white-75 mb-4" style="max-width: 540px; margin: 0 auto;">
          Join thousands of farmers, wholesale buyers, and logistics professionals on Farmora today. Start for free.
        </p>
        <div class="d-flex flex-wrap gap-3 justify-content-center">
          <router-link to="/register" class="btn btn-cta-primary">
            <i class="bi bi-person-plus me-2"></i>Create Free Account
          </router-link>
          <router-link to="/products" class="btn btn-cta-secondary">
            <i class="bi bi-grid-3x3-gap me-2"></i>Browse Marketplace
          </router-link>
        </div>

        <!-- Floating logos row -->
        <div class="cta-trust mt-5 d-flex flex-wrap justify-content-center align-items-center gap-4">
          <div class="trust-item">
            <i class="bi bi-patch-check-fill text-success me-1"></i>
            <span>Verified Farmers</span>
          </div>
          <div class="trust-sep d-none d-md-block">·</div>
          <div class="trust-item">
            <i class="bi bi-shield-lock-fill text-success me-1"></i>
            <span>Secure Payments</span>
          </div>
          <div class="trust-sep d-none d-md-block">·</div>
          <div class="trust-item">
            <i class="bi bi-truck text-success me-1"></i>
            <span>Fast Delivery</span>
          </div>
          <div class="trust-sep d-none d-md-block">·</div>
          <div class="trust-item">
            <i class="bi bi-headset text-success me-1"></i>
            <span>24/7 Support</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.cta-section { background: #f8f9f4; padding-bottom: 3rem; }

.cta-card {
  background: linear-gradient(135deg, #1b4332 0%, #2d6a4f 60%, #40916c 100%);
  border-radius: 20px;
  position: relative;
  overflow: hidden;
}

.cta-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 80% 20%, rgba(255,255,255,0.06), transparent 50%);
  pointer-events: none;
}

.cta-badge {
  background: rgba(255,255,255,0.15);
  color: #fff;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.3rem 0.9rem;
  border-radius: 20px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border: 1px solid rgba(255,255,255,0.25);
}

.text-white-75 { color: rgba(255,255,255,0.75); }

.btn-cta-primary {
  background: #fff;
  color: #1b4332;
  font-weight: 700;
  border-radius: 10px;
  padding: 0.75rem 1.8rem;
  border: 2px solid #fff;
  transition: all 0.25s;
}

.btn-cta-primary:hover {
  background: transparent;
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.2);
}

.btn-cta-secondary {
  background: transparent;
  color: #fff;
  font-weight: 600;
  border-radius: 10px;
  padding: 0.75rem 1.8rem;
  border: 2px solid rgba(255,255,255,0.4);
  transition: all 0.25s;
}

.btn-cta-secondary:hover {
  background: rgba(255,255,255,0.1);
  border-color: rgba(255,255,255,0.8);
  transform: translateY(-2px);
}

.cta-trust { border-top: 1px solid rgba(255,255,255,0.15); padding-top: 1.5rem; }

.trust-item {
  color: rgba(255,255,255,0.85);
  font-size: 0.85rem;
  font-weight: 500;
}

.trust-sep { color: rgba(255,255,255,0.3); font-size: 1.2rem; }
</style>

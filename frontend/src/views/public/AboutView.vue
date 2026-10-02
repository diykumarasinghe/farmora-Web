<template>
  <div>
    <!-- Page Hero -->
    <section class="py-5 text-white text-center" style="background: linear-gradient(135deg, #1b4332, #2d6a4f);">
      <div class="container py-3">
        <h1 class="fw-bold display-5 mb-2">About Farmora</h1>
        <p class="text-white-50 lead">Sri Lanka's Modern Agricultural Marketplace</p>
      </div>
    </section>

    <div class="container py-5">
      <!-- Mission -->
      <div class="row g-5 align-items-center mb-5">
        <div class="col-lg-6">
          <span class="badge bg-farmora-light text-farmora-green px-3 py-2 fw-bold mb-3 d-inline-block">Our Mission</span>
          <h2 class="fw-bold mb-3">Empowering Every Link in the Agricultural Chain</h2>
          <p class="text-secondary leading-relaxed mb-3">
            Farmora was founded with a singular purpose — to eliminate the exploitative middlemen that have long suppressed farmer incomes and inflated prices for buyers. We believe technology should serve agriculture, not complicate it.
          </p>
          <p class="text-secondary">
            By creating a direct, transparent digital exchange, we empower farmers to earn fair prices for their hard work, enable buyers to source authentic fresh produce directly from the source, and provide delivery partners with flexible, well-paying logistics opportunities.
          </p>
        </div>
        <div class="col-lg-6">
          <img
            src="https://images.unsplash.com/photo-1464226184884-fa280b87c399?auto=format&fit=crop&w=800&q=80"
            alt="Sri Lankan farmers"
            class="img-fluid rounded-4 shadow-lg"
          />
        </div>
      </div>

      <!-- Values -->
      <div class="row g-4 mb-5">
        <div class="col-12 text-center mb-2">
          <h2 class="fw-bold">Our Core Values</h2>
          <p class="text-muted">Principles that guide every decision we make at Farmora</p>
        </div>
        <div class="col-md-4">
          <div class="card border-0 shadow-sm p-4 h-100 text-center">
            <div class="rounded-circle bg-farmora-light d-inline-flex p-3 mb-3 mx-auto">
              <i class="bi bi-transparency text-farmora-green fs-3"></i>
            </div>
            <h5 class="fw-bold">Transparency</h5>
            <p class="text-muted small">Every bid, every price, every transaction — visible and fair to all parties on the platform.</p>
          </div>
        </div>
        <div class="col-md-4">
          <div class="card border-0 shadow-sm p-4 h-100 text-center">
            <div class="rounded-circle bg-farmora-light d-inline-flex p-3 mb-3 mx-auto">
              <i class="bi bi-people-fill text-farmora-green fs-3"></i>
            </div>
            <h5 class="fw-bold">Community First</h5>
            <p class="text-muted small">We put the farming community at the center of everything. Your success is our success.</p>
          </div>
        </div>
        <div class="col-md-4">
          <div class="card border-0 shadow-sm p-4 h-100 text-center">
            <div class="rounded-circle bg-farmora-light d-inline-flex p-3 mb-3 mx-auto">
              <i class="bi bi-shield-check text-farmora-green fs-3"></i>
            </div>
            <h5 class="fw-bold">Trust & Safety</h5>
            <p class="text-muted small">Verified accounts, secure JWT authentication, and role-based access protect every participant.</p>
          </div>
        </div>
      </div>

      <!-- How It Works -->
      <div class="card border-0 shadow-sm p-4 p-lg-5 bg-light mb-5">
        <div class="text-center mb-4">
          <h2 class="fw-bold">How Farmora Works</h2>
          <p class="text-muted">A simple, powerful 4-step process from farm to doorstep</p>
        </div>
        <div class="row g-4">
          <div class="col-sm-6 col-lg-3">
            <div class="d-flex flex-column align-items-center text-center">
              <div class="rounded-circle bg-success text-white d-flex align-items-center justify-content-center fw-bold fs-4 mb-3" style="width: 56px; height: 56px;">1</div>
              <h6 class="fw-bold">Farmer Lists Harvest</h6>
              <p class="small text-muted">Verified farmers upload produce listings with photos, pricing, and harvest details.</p>
            </div>
          </div>
          <div class="col-sm-6 col-lg-3">
            <div class="d-flex flex-column align-items-center text-center">
              <div class="rounded-circle bg-warning text-dark d-flex align-items-center justify-content-center fw-bold fs-4 mb-3" style="width: 56px; height: 56px;">2</div>
              <h6 class="fw-bold">Buyer Bids Competitively</h6>
              <p class="small text-muted">Registered buyers browse and submit direct price offers to the farmer.</p>
            </div>
          </div>
          <div class="col-sm-6 col-lg-3">
            <div class="d-flex flex-column align-items-center text-center">
              <div class="rounded-circle bg-info text-dark d-flex align-items-center justify-content-center fw-bold fs-4 mb-3" style="width: 56px; height: 56px;">3</div>
              <h6 class="fw-bold">Farmer Accepts & Packs</h6>
              <p class="small text-muted">Farmer reviews offers, accepts the best bid, and prepares the order for pickup.</p>
            </div>
          </div>
          <div class="col-sm-6 col-lg-3">
            <div class="d-flex flex-column align-items-center text-center">
              <div class="rounded-circle bg-success text-white d-flex align-items-center justify-content-center fw-bold fs-4 mb-3" style="width: 56px; height: 56px;">4</div>
              <h6 class="fw-bold">Partner Delivers</h6>
              <p class="small text-muted">A certified delivery partner accepts the run and brings produce directly to the buyer.</p>
            </div>
          </div>
        </div>
      </div>

      <!-- CTA -->
      <div class="text-center py-4">
        <h3 class="fw-bold mb-3">Ready to join the Farmora community?</h3>
        <router-link to="/register" class="btn btn-farmora btn-lg px-5 rounded-pill">
          Create Your Free Account
        </router-link>
      </div>
    </div>
  </div>
</template>

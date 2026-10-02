<template>
  <section class="py-4 bg-white border-bottom">
    <div class="container">
      <div class="row g-0 g-md-4 role-cards-row">
        <!-- Farmers -->
        <div class="col-6 col-md-3">
          <div class="role-card h-100 p-3 p-lg-4">
            <div class="role-icon mb-2">
              <i class="bi bi-flower2"></i>
            </div>
            <h6 class="fw-bold text-dark mb-1">For Farmers</h6>
            <p class="small text-muted mb-2">List your produce, get fair bids, and grow your business.</p>
            <router-link to="/register" class="role-link">
              Learn More <i class="bi bi-arrow-right ms-1"></i>
            </router-link>
          </div>
        </div>

        <!-- Buyers -->
        <div class="col-6 col-md-3">
          <div class="role-card h-100 p-3 p-lg-4">
            <div class="role-icon buyer-icon mb-2">
              <i class="bi bi-cart3"></i>
            </div>
            <h6 class="fw-bold text-dark mb-1">For Buyers</h6>
            <p class="small text-muted mb-2">Discover fresh, local produce and support local farmers.</p>
            <router-link to="/register" class="role-link">
              Learn More <i class="bi bi-arrow-right ms-1"></i>
            </router-link>
          </div>
        </div>

        <!-- Delivery Partners -->
        <div class="col-6 col-md-3">
          <div class="role-card h-100 p-3 p-lg-4">
            <div class="role-icon delivery-icon mb-2">
              <i class="bi bi-truck"></i>
            </div>
            <h6 class="fw-bold text-dark mb-1">For Delivery Partners</h6>
            <p class="small text-muted mb-2">Earn by delivering fresh produce to communities.</p>
            <router-link to="/register" class="role-link">
              Learn More <i class="bi bi-arrow-right ms-1"></i>
            </router-link>
          </div>
        </div>

        <!-- Administrators -->
        <div class="col-6 col-md-3">
          <div class="role-card h-100 p-3 p-lg-4">
            <div class="role-icon admin-icon mb-2">
              <i class="bi bi-shield-check"></i>
            </div>
            <h6 class="fw-bold text-dark mb-1">For Administrators</h6>
            <p class="small text-muted mb-2">Manage users, monitor activity and ensure a safe marketplace.</p>
            <router-link to="/login" class="role-link">
              Learn More <i class="bi bi-arrow-right ms-1"></i>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.role-cards-row {
  border-radius: 12px;
  overflow: hidden;
}

.role-card {
  border-right: 1px solid #f1f5f1;
  transition: background 0.2s;
}

.role-card:hover {
  background: #f9fef9;
}

.role-card:last-child {
  border-right: none;
}

.role-icon {
  width: 44px;
  height: 44px;
  background: #d8f3dc;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  color: #2d6a4f;
}

.buyer-icon { background: #dbeafe; color: #2563eb; }
.delivery-icon { background: #fef3c7; color: #d97706; }
.admin-icon { background: #f3e8ff; color: #7c3aed; }

.role-link {
  color: #2d6a4f;
  font-size: 0.82rem;
  font-weight: 600;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  transition: gap 0.2s;
}

.role-link:hover { text-decoration: underline; }

@media (max-width: 576px) {
  .role-card { border-right: none; border-bottom: 1px solid #f1f5f1; }
}
</style>

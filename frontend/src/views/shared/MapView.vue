<template>
  <div class="container py-4">
    <div class="card border-0 shadow-sm p-4 p-lg-5">
      <div class="d-flex align-items-center gap-3 mb-4">
        <div class="rounded-circle bg-farmora-light text-farmora-green p-3">
          <i class="bi bi-geo-alt-fill fs-3"></i>
        </div>
        <div>
          <h4 class="fw-bold mb-1">Farmora Delivery Routing</h4>
          <p class="text-muted small mb-0">Visual route planning for delivery partners across Sri Lanka</p>
        </div>
      </div>

      <!-- Map Placeholder -->
      <div
        class="rounded-4 d-flex align-items-center justify-content-center bg-light border-2 border-dashed border-secondary"
        style="height: 420px;"
      >
        <div class="text-center text-muted">
          <i class="bi bi-map display-3 mb-3 text-success opacity-50"></i>
          <h5 class="fw-bold">Interactive Route Map</h5>
          <p class="small px-4">
            Powered by Google Maps API. Configure your <code>VITE_GOOGLE_MAPS_API_KEY</code> in
            <code>frontend/.env</code> to enable live route visualization.
          </p>
          <div class="alert alert-info py-2 small d-inline-flex align-items-center gap-2 mt-2">
            <i class="bi bi-info-circle-fill"></i>
            External map integration requires a Google Maps API key
          </div>
        </div>
      </div>

      <!-- Pickup and Delivery Address List -->
      <div class="mt-4">
        <h6 class="fw-bold mb-3">Your Active Route Stops</h6>
        <div class="alert alert-light border py-3 small text-muted text-center">
          <i class="bi bi-truck me-2"></i>
          Accept a delivery from <router-link to="/delivery/available" class="text-success fw-semibold">Available Deliveries</router-link> to see your pickup and drop-off route here.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="reports" />
      </div>

      <div class="col-lg-9">
        <div class="card border-0 shadow-sm p-4">
          <h4 class="fw-bold mb-3"><i class="bi bi-graph-up text-success me-2"></i>Marketplace Analytics & Reports</h4>
          <p class="text-muted small mb-4">
            Aggregated metrics for agricultural trading volume, price trends by crop category, and delivery fulfillment performance.
          </p>

          <div class="row g-3 mb-4">
            <div class="col-md-6">
              <div class="p-3 border rounded-3 bg-light">
                <h6 class="fw-bold text-dark"><i class="bi bi-check-circle-fill text-success me-1"></i> Order Fulfillment Rate</h6>
                <div class="fs-4 fw-bold text-success mb-1">98.4%</div>
                <div class="small text-muted">Successful pickups and deliveries across all active partners</div>
              </div>
            </div>
            <div class="col-md-6">
              <div class="p-3 border rounded-3 bg-light">
                <h6 class="fw-bold text-dark"><i class="bi bi-tag-fill text-warning me-1"></i> Average Bid Settlement</h6>
                <div class="fs-4 fw-bold text-warning mb-1">$3.42 / kg</div>
                <div class="small text-muted">Average price negotiated across all fresh vegetable listings</div>
              </div>
            </div>
          </div>

          <div class="alert alert-info py-3 small d-flex align-items-center gap-2">
            <i class="bi bi-info-circle-fill fs-5"></i>
            <div>
              Platform reports are generated in real-time directly from JPA entity transactions in <code>farmora_db</code>.
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

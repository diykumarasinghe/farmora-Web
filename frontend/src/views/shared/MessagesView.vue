<template>
  <div class="container py-4">
    <div class="card border-0 shadow-sm p-4 p-lg-5">
      <div class="d-flex align-items-center gap-3 mb-4">
        <div class="rounded-circle bg-farmora-light text-farmora-green p-3">
          <i class="bi bi-chat-dots-fill fs-3"></i>
        </div>
        <div>
          <h4 class="fw-bold mb-1">Direct Messaging</h4>
          <p class="text-muted small mb-0">Chat directly with farmers, buyers, and delivery partners</p>
        </div>
      </div>

      <div
        class="rounded-4 d-flex align-items-center justify-content-center bg-light border"
        style="min-height: 400px;"
      >
        <div class="text-center text-muted px-4">
          <i class="bi bi-chat-square-dots display-3 mb-3 text-success opacity-50"></i>
          <h5 class="fw-bold text-dark">Real-time Messaging</h5>
          <p class="small mb-3">
            Secure WebSocket-based messaging between farmers, buyers, and delivery partners will be available in a future Farmora update. This feature requires a WebSocket server configuration.
          </p>
          <div class="alert alert-info py-2 small d-inline-flex align-items-center gap-2">
            <i class="bi bi-info-circle-fill"></i>
            Real-time chat requires WebSocket/STOMP + SockJS backend integration (Phase 2)
          </div>
          <div class="mt-3">
            <p class="small text-muted">
              In the meantime, contact details for farmers and buyers are visible within bid and order details.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

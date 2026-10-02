<script setup>
import { ref } from 'vue'

const form = ref({
  name: '',
  email: '',
  subject: '',
  message: '',
  role: ''
})

const submitted = ref(false)

const handleSubmit = () => {
  // In a real implementation this would call a contact API endpoint
  // For now we show a success confirmation to the user
  submitted.value = true
}
</script>

<template>
  <div>
    <!-- Page Hero -->
    <section class="py-5 text-white text-center" style="background: linear-gradient(135deg, #1b4332, #2d6a4f);">
      <div class="container py-3">
        <h1 class="fw-bold display-5 mb-2">Get in Touch</h1>
        <p class="text-white-50 lead">We'd love to hear from farmers, buyers, and delivery partners</p>
      </div>
    </section>

    <div class="container py-5">
      <div class="row g-5">
        <!-- Contact Info -->
        <div class="col-lg-5">
          <h2 class="fw-bold mb-4">Farmora Support Center</h2>
          <p class="text-muted mb-4">
            Whether you're a farmer needing help listing your first harvest, a buyer with questions about bidding, or a delivery partner wanting to join our logistics network — we're here for you.
          </p>

          <div class="d-flex flex-column gap-3 mb-4">
            <div class="d-flex align-items-start gap-3 p-3 rounded-3 bg-light">
              <div class="text-success fs-4 mt-1"><i class="bi bi-geo-alt-fill"></i></div>
              <div>
                <div class="fw-bold">Farmora Headquarters</div>
                <div class="small text-muted">100 AgriTech Boulevard, Colombo 03, Sri Lanka</div>
              </div>
            </div>
            <div class="d-flex align-items-start gap-3 p-3 rounded-3 bg-light">
              <div class="text-success fs-4 mt-1"><i class="bi bi-telephone-fill"></i></div>
              <div>
                <div class="fw-bold">Phone Support</div>
                <div class="small text-muted">+94 (11) 789-4560<br>Mon – Sat: 8:00 AM – 6:00 PM</div>
              </div>
            </div>
            <div class="d-flex align-items-start gap-3 p-3 rounded-3 bg-light">
              <div class="text-success fs-4 mt-1"><i class="bi bi-envelope-fill"></i></div>
              <div>
                <div class="fw-bold">Email</div>
                <div class="small text-muted">support@farmora.marketplace<br>farmers@farmora.marketplace</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Contact Form -->
        <div class="col-lg-7">
          <div class="card border-0 shadow-sm p-4 p-md-5">
            <div v-if="submitted" class="text-center py-4">
              <div class="rounded-circle bg-farmora-light d-inline-flex p-4 text-farmora-green mb-3">
                <i class="bi bi-check-circle-fill display-5"></i>
              </div>
              <h4 class="fw-bold mb-2">Message Received!</h4>
              <p class="text-muted mb-4">Thank you for reaching out. Our team will respond within 1 business day.</p>
              <button class="btn btn-farmora-outline" @click="submitted = false">Send Another Message</button>
            </div>

            <form v-else @submit.prevent="handleSubmit">
              <h4 class="fw-bold mb-4">Send us a Message</h4>

              <div class="row g-3 mb-3">
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Full Name</label>
                  <input
                    type="text"
                    class="form-control"
                    v-model="form.name"
                    placeholder="Your full name"
                    required
                  />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Email</label>
                  <input
                    type="email"
                    class="form-control"
                    v-model="form.email"
                    placeholder="your@email.com"
                    required
                  />
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label small fw-semibold">I am a...</label>
                <select v-model="form.role" class="form-select">
                  <option value="">Select your role</option>
                  <option value="farmer">Farmer</option>
                  <option value="buyer">Buyer</option>
                  <option value="delivery">Delivery Partner</option>
                  <option value="other">Other</option>
                </select>
              </div>

              <div class="mb-3">
                <label class="form-label small fw-semibold">Subject</label>
                <input
                  type="text"
                  class="form-control"
                  v-model="form.subject"
                  placeholder="Brief description of your enquiry"
                  required
                />
              </div>

              <div class="mb-4">
                <label class="form-label small fw-semibold">Message</label>
                <textarea
                  class="form-control"
                  rows="5"
                  v-model="form.message"
                  placeholder="Describe how we can help you..."
                  required
                ></textarea>
              </div>

              <button type="submit" class="btn btn-farmora w-100 py-2 fw-bold">
                <i class="bi bi-send me-2"></i>Send Message
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

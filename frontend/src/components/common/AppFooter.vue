<template>
  <footer class="farmora-footer pt-5 pb-4">
    <div class="container">
      <div class="row g-4">
        <!-- Brand Column -->
        <div class="col-lg-4 col-md-6">
          <div class="d-flex align-items-center gap-2 mb-3">
            <div class="footer-brand-icon">
              <i class="bi bi-flower2"></i>
            </div>
            <div>
              <div class="footer-brand-name">FARMORA</div>
              <div class="footer-brand-tagline">Grow Together</div>
            </div>
          </div>
          <p class="footer-desc mb-3">
            Sri Lanka's digital agricultural marketplace connecting hardworking farmers with local buyers and dedicated delivery partners for transparent, fair-price trade.
          </p>
          <div class="d-flex gap-3">
            <a href="#" class="footer-social"><i class="bi bi-facebook"></i></a>
            <a href="#" class="footer-social"><i class="bi bi-twitter-x"></i></a>
            <a href="#" class="footer-social"><i class="bi bi-instagram"></i></a>
            <a href="#" class="footer-social"><i class="bi bi-linkedin"></i></a>
          </div>
        </div>

        <!-- Marketplace Links -->
        <div class="col-lg-2 col-6">
          <h6 class="footer-heading">Marketplace</h6>
          <ul class="footer-links">
            <li><router-link to="/products">Browse Produce</router-link></li>
            <li><router-link to="/about">How It Works</router-link></li>
            <li><router-link to="/register">Sell on Farmora</router-link></li>
            <li><router-link to="/register">Delivery Jobs</router-link></li>
          </ul>
        </div>

        <!-- Portals -->
        <div class="col-lg-2 col-6">
          <h6 class="footer-heading">Portals</h6>
          <ul class="footer-links">
            <li><router-link to="/login">Farmer Portal</router-link></li>
            <li><router-link to="/login">Buyer Portal</router-link></li>
            <li><router-link to="/login">Delivery Portal</router-link></li>
            <li><router-link to="/login">Admin Center</router-link></li>
          </ul>
        </div>

        <!-- Contact -->
        <div class="col-lg-4 col-md-6">
          <h6 class="footer-heading">Get in Touch</h6>
          <ul class="footer-links">
            <li><i class="bi bi-geo-alt-fill text-success me-2"></i>100 AgriTech Blvd, Colombo, Sri Lanka</li>
            <li><i class="bi bi-telephone-fill text-success me-2"></i>+94 (11) 789-4560</li>
            <li><i class="bi bi-envelope-fill text-success me-2"></i>support@farmora.marketplace</li>
            <li><i class="bi bi-clock-fill text-success me-2"></i>Mon – Sat: 8:00 AM – 6:00 PM</li>
          </ul>

          <!-- Newsletter -->
          <div class="mt-3">
            <div class="input-group newsletter-form">
              <input type="email" class="form-control" placeholder="Your email address" />
              <button class="btn btn-newsletter">Subscribe</button>
            </div>
          </div>
        </div>
      </div>

      <hr class="footer-divider my-4" />

      <div class="d-flex flex-column flex-md-row justify-content-between align-items-center gap-2">
        <div class="footer-copy">© 2026 Farmora Marketplace. All rights reserved.</div>
        <div class="d-flex gap-4">
          <a href="#" class="footer-legal">Privacy Policy</a>
          <a href="#" class="footer-legal">Terms of Service</a>
          <a href="#" class="footer-legal">Fair Trade Standards</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.farmora-footer {
  background: #0d2818;
  color: #9ca3af;
}

.footer-brand-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #2d6a4f, #40916c);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 1.4rem;
  flex-shrink: 0;
}

.footer-brand-name {
  font-size: 1.05rem;
  font-weight: 800;
  color: #fff;
  letter-spacing: 0.05em;
  line-height: 1.1;
}

.footer-brand-tagline {
  font-size: 0.65rem;
  color: #40916c;
  font-weight: 500;
}

.footer-desc {
  font-size: 0.85rem;
  line-height: 1.7;
  color: #6b7280;
}

.footer-social {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: rgba(255,255,255,0.06);
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  transition: all 0.2s;
  font-size: 0.95rem;
}

.footer-social:hover {
  background: #2d6a4f;
  color: #fff;
}

.footer-heading {
  color: #d1fae5;
  font-weight: 700;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 1rem;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
}

.footer-links li, .footer-links a {
  font-size: 0.83rem;
  color: #6b7280;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-links a:hover { color: #4ade80; }

.footer-divider { border-color: rgba(255,255,255,0.08); }

.footer-copy {
  font-size: 0.8rem;
  color: #4b5563;
}

.footer-legal {
  font-size: 0.8rem;
  color: #4b5563;
  text-decoration: none;
  transition: color 0.2s;
}

.footer-legal:hover { color: #9ca3af; }

/* Newsletter */
.newsletter-form .form-control {
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(255,255,255,0.1);
  color: #e5e7eb;
  font-size: 0.82rem;
  border-radius: 8px 0 0 8px;
}

.newsletter-form .form-control::placeholder { color: #6b7280; }

.newsletter-form .form-control:focus {
  background: rgba(255,255,255,0.08);
  border-color: #2d6a4f;
  box-shadow: none;
  color: #e5e7eb;
}

.btn-newsletter {
  background: #2d6a4f;
  color: #fff;
  font-size: 0.82rem;
  font-weight: 600;
  border-radius: 0 8px 8px 0;
  border: none;
  transition: background 0.2s;
  white-space: nowrap;
}

.btn-newsletter:hover { background: #1b4332; color: #fff; }
</style>

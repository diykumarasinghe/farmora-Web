<script setup>
import { onMounted, ref } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'
import { bidService } from '../../services/bidService'
import { orderService } from '../../services/orderService'
import { analyticsService } from '../../services/analyticsService'
import { buyerService } from '../../services/buyerService'

const loading = ref(true)
const activeTab = ref('overview') // 'overview' | 'bids' | 'farms' | 'saved'

const myBids = ref([])
const myOrders = ref([])
const favFarms = ref([])
const savedProducts = ref([])

const loadBuyerData = async () => {
  loading.value = true
  try {
    const [bids, orders, farms, saved] = await Promise.all([
      bidService.getMyBids().catch(() => []),
      orderService.getMyOrders().catch(() => []),
      buyerService.getFavouriteFarms().catch(() => []),
      analyticsService.getSavedProducts().catch(() => [])
    ])
    myBids.value = bids || []
    myOrders.value = orders || []
    favFarms.value = farms || []
    savedProducts.value = saved || []
  } catch (err) {
    console.error('Failed to load buyer data', err)
  } finally {
    loading.value = false
  }
}

const removeSaved = async (productId) => {
  try {
    await analyticsService.removeSavedProduct(productId)
    savedProducts.value = savedProducts.value.filter(p => p.id !== productId)
  } catch (err) {
    console.error('Failed to remove saved product', err)
  }
}

const removeFavouriteFarm = async (farmerId) => {
  try {
    await buyerService.removeFavouriteFarm(farmerId)
    favFarms.value = favFarms.value.filter(f => f.farmer?.id !== farmerId && f.id !== farmerId)
  } catch (err) {
    console.error('Failed to remove favorite farm', err)
  }
}

const withdrawBid = async (bidId) => {
  if (!confirm('Withdraw this bid offer?')) return
  try {
    await bidService.withdrawBid(bidId)
    const b = myBids.value.find(item => item.id === bidId)
    if (b) b.status = 'WITHDRAWN'
  } catch (err) {
    console.error('Failed to withdraw bid', err)
  }
}

onMounted(() => {
  loadBuyerData()
})
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="dashboard" />
      </div>

      <div class="col-lg-9">
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
          <div>
            <h3 class="fw-bold mb-1">Buyer Dashboard & Procurement Center</h3>
            <p class="text-muted small mb-0">Track active bids, purchase history, favorite farms, and saved produce.</p>
          </div>
          <router-link to="/products" class="btn btn-farmora btn-sm d-flex align-items-center gap-2 shadow-sm">
            <i class="bi bi-search"></i>
            <span>Explore Marketplace</span>
          </router-link>
        </div>

        <!-- Navigation Tabs -->
        <div class="nav nav-pills bg-white p-1 rounded-pill border shadow-sm mb-4 d-inline-flex">
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-bold"
            :class="{ active: activeTab === 'overview' }"
            @click="activeTab = 'overview'"
          >
            <i class="bi bi-speedometer2 me-1"></i> Overview
          </button>
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-bold"
            :class="{ active: activeTab === 'bids' }"
            @click="activeTab = 'bids'"
          >
            <i class="bi bi-tags-fill me-1"></i> Active Bids ({{ myBids.filter(b => b.status === 'PENDING' || b.status === 'COUNTERED').length }})
          </button>
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-bold"
            :class="{ active: activeTab === 'farms' }"
            @click="activeTab = 'farms'"
          >
            <i class="bi bi-heart-fill text-danger me-1"></i> Favorite Farms ({{ favFarms.length }})
          </button>
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-bold"
            :class="{ active: activeTab === 'saved' }"
            @click="activeTab = 'saved'"
          >
            <i class="bi bi-bookmark-fill text-warning me-1"></i> Saved Products ({{ savedProducts.length }})
          </button>
        </div>

        <LoadingSpinner v-if="loading" message="Loading buyer procurement records..." />

        <template v-else>
          <!-- ============================================================
               TAB 1: OVERVIEW & PURCHASE HISTORY
               ============================================================ -->
          <div v-if="activeTab === 'overview'">
            <!-- Metric Highlights -->
            <div class="row g-3 mb-4">
              <div class="col-sm-6 col-md-3">
                <div class="card border-0 shadow-sm p-3 rounded-4 bg-white">
                  <div class="d-flex align-items-center justify-content-between">
                    <div>
                      <div class="text-muted small fw-semibold">Active Bids</div>
                      <div class="fs-4 fw-bold text-warning">{{ myBids.filter(b => b.status === 'PENDING' || b.status === 'COUNTERED').length }}</div>
                    </div>
                    <div class="rounded-circle bg-warning bg-opacity-10 text-warning p-3">
                      <i class="bi bi-clock-history fs-4"></i>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-sm-6 col-md-3">
                <div class="card border-0 shadow-sm p-3 rounded-4 bg-white">
                  <div class="d-flex align-items-center justify-content-between">
                    <div>
                      <div class="text-muted small fw-semibold">Accepted Bids</div>
                      <div class="fs-4 fw-bold text-success">{{ myBids.filter(b => b.status === 'ACCEPTED').length }}</div>
                    </div>
                    <div class="rounded-circle bg-success bg-opacity-10 text-success p-3">
                      <i class="bi bi-check2-circle fs-4"></i>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-sm-6 col-md-3">
                <div class="card border-0 shadow-sm p-3 rounded-4 bg-white">
                  <div class="d-flex align-items-center justify-content-between">
                    <div>
                      <div class="text-muted small fw-semibold">Total Orders</div>
                      <div class="fs-4 fw-bold text-primary">{{ myOrders.length }}</div>
                    </div>
                    <div class="rounded-circle bg-primary bg-opacity-10 text-primary p-3">
                      <i class="bi bi-bag-check-fill fs-4"></i>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-sm-6 col-md-3">
                <div class="card border-0 shadow-sm p-3 rounded-4 bg-white">
                  <div class="d-flex align-items-center justify-content-between">
                    <div>
                      <div class="text-muted small fw-semibold">Favorite Farms</div>
                      <div class="fs-4 fw-bold text-danger">{{ favFarms.length }}</div>
                    </div>
                    <div class="rounded-circle bg-danger bg-opacity-10 text-danger p-3">
                      <i class="bi bi-heart-fill fs-4"></i>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Recent Purchases -->
            <div class="card border-0 shadow-sm rounded-4 bg-white mb-4">
              <div class="card-header bg-white border-0 pt-3 px-4 d-flex justify-content-between align-items-center">
                <h5 class="fw-bold mb-0">Recent Purchase History</h5>
                <router-link to="/buyer/orders" class="small text-decoration-none text-success fw-bold">
                  View All Orders & Invoices <i class="bi bi-arrow-right"></i>
                </router-link>
              </div>
              <div class="card-body p-0">
                <div v-if="myOrders.length > 0" class="table-responsive">
                  <table class="table table-hover align-middle mb-0">
                    <thead class="table-light small text-muted">
                      <tr>
                        <th>Order ID</th>
                        <th>Produce</th>
                        <th>Farmer</th>
                        <th>Fulfillment</th>
                        <th>Total Amount</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody class="small">
                      <tr v-for="order in myOrders.slice(0, 5)" :key="order.id">
                        <td class="fw-bold">#{{ order.id }}</td>
                        <td>{{ order.product?.name }}</td>
                        <td>{{ order.farmer?.fullName }}</td>
                        <td><span class="badge bg-light text-secondary border rounded-pill">{{ order.fulfillmentType || 'DELIVERY' }}</span></td>
                        <td class="text-success fw-bold">Rs. {{ Number(order.totalAmount).toLocaleString() }}</td>
                        <td>
                          <span
                            class="badge rounded-pill"
                            :class="order.status === 'DELIVERED' ? 'bg-success' : 'bg-warning text-dark'"
                          >
                            {{ order.status }}
                          </span>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                <div v-else class="p-4 text-center text-muted">
                  <p class="mb-0">No purchases yet. Explore the marketplace to order direct from farmers!</p>
                </div>
              </div>
            </div>
          </div>

          <!-- ============================================================
               TAB 2: ACTIVE BIDS TRACKER
               ============================================================ -->
          <div v-else-if="activeTab === 'bids'">
            <div class="card border-0 shadow-sm rounded-4 p-4 bg-white mb-4">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-bold mb-0"><i class="bi bi-tags-fill text-warning me-2"></i>Active Bid Negotiations</h5>
                <router-link to="/buyer/my-bids" class="btn btn-outline-success btn-sm rounded-pill px-3">
                  Open Bids Center
                </router-link>
              </div>

              <div v-if="myBids.length > 0" class="d-flex flex-column gap-3">
                <div
                  v-for="bid in myBids"
                  :key="bid.id"
                  class="border rounded-4 p-3 bg-light bg-opacity-50 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3"
                >
                  <div>
                    <div class="d-flex align-items-center gap-2 mb-1">
                      <h6 class="fw-bold text-dark mb-0">{{ bid.product?.name }}</h6>
                      <span
                        class="badge rounded-pill"
                        :class="bid.status === 'PENDING' ? 'bg-warning text-dark' : (bid.status === 'ACCEPTED' ? 'bg-success' : 'bg-secondary')"
                      >
                        {{ bid.status }}
                      </span>
                      <span v-if="bid.isExpired" class="badge bg-danger rounded-pill">EXPIRED</span>
                    </div>
                    <div class="small text-muted mb-1">
                      Offered: <strong>Rs. {{ bid.amount }} / {{ bid.product?.unit }}</strong> &bull; Qty: {{ bid.quantity }} {{ bid.product?.unit }}
                    </div>
                    <div v-if="bid.counterAmount" class="small text-primary fw-semibold">
                      <i class="bi bi-arrow-left-right me-1"></i> Counter Offer: Rs. {{ bid.counterAmount }} by {{ bid.counterBy }}
                    </div>
                    <div v-if="bid.expiresAt" class="small text-secondary" style="font-size: 0.72rem;">
                      <i class="bi bi-stopwatch text-danger me-1"></i>
                      Deadline: {{ new Date(bid.expiresAt).toLocaleString() }}
                    </div>
                  </div>

                  <div class="d-flex align-items-center gap-2">
                    <button
                      v-if="bid.status === 'PENDING' || bid.status === 'COUNTERED'"
                      class="btn btn-outline-danger btn-sm rounded-pill px-3"
                      @click="withdrawBid(bid.id)"
                    >
                      Withdraw
                    </button>
                    <router-link to="/buyer/my-bids" class="btn btn-primary btn-sm rounded-pill px-3">
                      View Negotiation
                    </router-link>
                  </div>
                </div>
              </div>
              <EmptyState
                v-else
                icon="bi-tag"
                title="No Bids Placed"
                description="You have not placed any bids on farm listings. Find crops in marketplace to place an offer."
              />
            </div>
          </div>

          <!-- ============================================================
               TAB 3: FAVORITE FARMS
               ============================================================ -->
          <div v-else-if="activeTab === 'farms'">
            <div class="card border-0 shadow-sm rounded-4 p-4 bg-white mb-4">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-bold mb-0"><i class="bi bi-heart-fill text-danger me-2"></i>Saved & Favorite Farms</h5>
                <router-link to="/farms" class="btn btn-outline-success btn-sm rounded-pill px-3">
                  Discover More Farms
                </router-link>
              </div>

              <div v-if="favFarms.length > 0" class="row g-3">
                <div v-for="fav in favFarms" :key="fav.id" class="col-md-6">
                  <div class="card border rounded-4 p-3 bg-light h-100 d-flex flex-column justify-content-between">
                    <div>
                      <div class="d-flex justify-content-between align-items-start mb-2">
                        <div class="d-flex align-items-center gap-2">
                          <div class="avatar-small rounded-circle bg-success text-white d-flex align-items-center justify-content-center fw-bold" style="width: 36px; height: 36px;">
                            {{ fav.farmer?.fullName ? fav.farmer.fullName.charAt(0) : 'F' }}
                          </div>
                          <div>
                            <h6 class="fw-bold mb-0 text-dark">{{ fav.farmer?.fullName }}</h6>
                            <small class="text-muted">{{ fav.farmer?.address || 'Sri Lanka' }}</small>
                          </div>
                        </div>
                        <button
                          class="btn btn-outline-danger btn-sm rounded-circle p-1 border-0"
                          @click="removeFavouriteFarm(fav.farmer?.id || fav.id)"
                          title="Remove from favorites"
                        >
                          <i class="bi bi-trash"></i>
                        </button>
                      </div>
                      <p class="small text-secondary mb-3">
                        Direct trusted supplier for authentic organic high-altitude harvests.
                      </p>
                    </div>

                    <div class="d-flex gap-2">
                      <router-link :to="`/farms/${fav.farmer?.id}`" class="btn btn-outline-success btn-sm w-100 rounded-pill">
                        View Profile
                      </router-link>
                      <router-link to="/messages" class="btn btn-success btn-sm rounded-pill px-3">
                        <i class="bi bi-chat-dots-fill"></i>
                      </router-link>
                    </div>
                  </div>
                </div>
              </div>
              <EmptyState
                v-else
                icon="bi-heart"
                title="No Favorite Farms Yet"
                description="Browse local Sri Lankan farms and save your favorites for instant direct ordering."
              />
            </div>
          </div>

          <!-- ============================================================
               TAB 4: SAVED PRODUCTS (BOOKMARKS)
               ============================================================ -->
          <div v-else-if="activeTab === 'saved'">
            <div class="card border-0 shadow-sm rounded-4 p-4 bg-white mb-4">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-bold mb-0"><i class="bi bi-bookmark-fill text-warning me-2"></i>Saved Produce & Bookmarks</h5>
                <router-link to="/products" class="btn btn-outline-success btn-sm rounded-pill px-3">
                  Browse Marketplace
                </router-link>
              </div>

              <div v-if="savedProducts.length > 0" class="row g-3">
                <div v-for="prod in savedProducts" :key="prod.id" class="col-md-6 col-lg-4">
                  <div class="card border rounded-4 overflow-hidden h-100 d-flex flex-column shadow-sm">
                    <img
                      :src="prod.imageUrl || 'https://images.unsplash.com/photo-1597362925123-77861d3fbac7?auto=format&fit=crop&w=400&q=80'"
                      class="card-img-top object-fit-cover"
                      style="height: 140px;"
                      alt="Crop"
                    />
                    <div class="p-3 d-flex flex-column flex-grow-1">
                      <div class="d-flex justify-content-between align-items-start mb-1">
                        <h6 class="fw-bold text-dark mb-0">{{ prod.name }}</h6>
                        <button
                          class="btn btn-link text-danger p-0 text-decoration-none"
                          @click="removeSaved(prod.id)"
                          title="Remove bookmark"
                        >
                          <i class="bi bi-bookmark-x-fill fs-5"></i>
                        </button>
                      </div>
                      <small class="text-muted mb-2">{{ prod.category }} &bull; {{ prod.location }}</small>
                      <div class="fs-5 fw-bold text-success mb-3">
                        Rs. {{ Number(prod.price).toFixed(2) }} <span class="small text-muted fw-normal">/ {{ prod.unit }}</span>
                      </div>
                      <div class="mt-auto">
                        <router-link :to="`/products/${prod.id}`" class="btn btn-success btn-sm w-100 rounded-pill">
                          View & Order Now
                        </router-link>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <EmptyState
                v-else
                icon="bi-bookmark-dash"
                title="No Saved Products"
                description="You can bookmark fresh crops on the marketplace to order later with one click."
              />
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

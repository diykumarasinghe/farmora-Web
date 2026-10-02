import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// Public Views
import HomeView from '../views/public/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  },
  routes: [
    // ============================================================
    // PUBLIC ROUTES (no auth required)
    // ============================================================
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/public/AboutView.vue'),
    },
    {
      path: '/contact',
      name: 'contact',
      component: () => import('../views/public/ContactView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/public/LoginView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/public/RegisterView.vue'),
      meta: { guestOnly: true },
    },
    {
      path: '/products',
      name: 'products',
      component: () => import('../views/buyer/MarketplaceView.vue'),
    },
    {
      path: '/marketplace',
      name: 'marketplace',
      component: () => import('../views/buyer/MarketplaceView.vue'),
    },
    {
      path: '/products/:id',
      name: 'product-details',
      component: () => import('../views/buyer/ProductDetailsView.vue'),
    },
    {
      path: '/marketplace/:id',
      name: 'marketplace-details',
      component: () => import('../views/buyer/ProductDetailsView.vue'),
    },
    {
      path: '/categories',
      name: 'categories',
      component: () => import('../views/public/CategoriesView.vue'),
    },
    {
      path: '/agricultural-map',
      name: 'agricultural-map',
      component: () => import('../views/shared/AgriculturalMapView.vue'),
    },
    {
      path: '/map',
      name: 'map',
      component: () => import('../views/shared/AgriculturalMapView.vue'),
    },
    {
      path: '/farm-discovery',
      name: 'farm-discovery',
      component: () => import('../views/shared/FarmDiscoveryView.vue'),
    },
    {
      path: '/farms',
      name: 'farms',
      component: () => import('../views/shared/FarmDiscoveryView.vue'),
    },
    {
      path: '/price-trends',
      name: 'price-trends',
      component: () => import('../views/shared/PriceTrendsView.vue'),
    },
    {
      path: '/pricing',
      name: 'pricing',
      component: () => import('../views/shared/PriceTrendsView.vue'),
    },
    {
      path: '/daily-harvest',
      name: 'daily-harvest',
      component: () => import('../views/farmer/DailyHarvestView.vue'),
    },
    {
      path: '/agri-info',
      name: 'agricultural-info',
      component: () => import('../views/shared/AgriculturalInfoView.vue'),
    },
    {
      path: '/crop-calendar',
      name: 'crop-calendar',
      component: () => import('../views/shared/AgriculturalInfoView.vue'),
    },
    {
      path: '/farms/:id',
      name: 'public-farm-profile',
      component: () => import('../views/public/PublicFarmProfile.vue'),
    },
    {
      path: '/ai-tools',
      name: 'ai-tools',
      component: () => import('../views/shared/AiCropIntelligenceView.vue'),
    },
    {
      path: '/ai-assistant',
      name: 'ai-assistant',
      component: () => import('../views/shared/AiCropIntelligenceView.vue'),
    },
    {
      path: '/ai-intelligence',
      name: 'ai-intelligence',
      component: () => import('../views/shared/AiCropIntelligenceView.vue'),
    },

    // ============================================================
    // SHARED PROTECTED ROUTES (any authenticated user)
    // ============================================================
    {
      path: '/notifications',
      name: 'notifications',
      component: () => import('../views/shared/NotificationsView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/messages',
      name: 'messages',
      component: () => import('../views/shared/MessagesView.vue'),
      meta: { requiresAuth: true },
    },

    // ============================================================
    // FARMER ROUTES
    // ============================================================
    {
      path: '/farmer/dashboard',
      name: 'farmer-dashboard',
      component: () => import('../views/farmer/FarmerDashboard.vue'),
      meta: { requiresAuth: true, roles: ['FARMER'] },
    },
    {
      path: '/farmer/add-product',
      name: 'farmer-add-product',
      component: () => import('../views/farmer/AddProductView.vue'),
      meta: { requiresAuth: true, roles: ['FARMER'] },
    },
    {
      path: '/farmer/my-products',
      name: 'farmer-my-products',
      component: () => import('../views/farmer/MyProducts.vue'),
      meta: { requiresAuth: true, roles: ['FARMER'] },
    },
    {
      path: '/farmer/daily-harvest',
      name: 'farmer-daily-harvest',
      component: () => import('../views/farmer/DailyHarvestView.vue'),
      meta: { requiresAuth: true, roles: ['FARMER'] },
    },
    {
      path: '/farmer/edit-product/:id',
      name: 'farmer-edit-product',
      component: () => import('../views/farmer/EditProductView.vue'),
      meta: { requiresAuth: true, roles: ['FARMER'] },
    },
    {
      path: '/farmer/bids',
      name: 'farmer-bids',
      component: () => import('../views/farmer/ProductBids.vue'),
      meta: { requiresAuth: true, roles: ['FARMER'] },
    },
    {
      path: '/farmer/orders',
      name: 'farmer-orders',
      component: () => import('../views/farmer/FarmerOrders.vue'),
      meta: { requiresAuth: true, roles: ['FARMER'] },
    },
    {
      path: '/farmer/profile',
      name: 'farmer-profile',
      component: () => import('../views/farmer/FarmerProfile.vue'),
      meta: { requiresAuth: true, roles: ['FARMER'] },
    },
    {
      path: '/farmer/certifications',
      name: 'farmer-certifications',
      component: () => import('../views/farmer/FarmerCertifications.vue'),
      meta: { requiresAuth: true, roles: ['FARMER'] },
    },
    {
      path: '/farmer/verification',
      name: 'farmer-verification',
      component: () => import('../views/farmer/FarmerVerification.vue'),
      meta: { requiresAuth: true, roles: ['FARMER'] },
    },
    {
      path: '/farmer/media',
      name: 'farmer-media',
      component: () => import('../views/farmer/FarmerMedia.vue'),
      meta: { requiresAuth: true, roles: ['FARMER'] },
    },
    {
      path: '/farmer/methods',
      name: 'farmer-methods',
      component: () => import('../views/farmer/FarmerMethods.vue'),
      meta: { requiresAuth: true, roles: ['FARMER'] },
    },
    {
      path: '/farmer/credibility',
      name: 'farmer-credibility',
      component: () => import('../views/farmer/FarmerCredibility.vue'),
      meta: { requiresAuth: true, roles: ['FARMER'] },
    },

    // ============================================================
    // BUYER ROUTES
    // ============================================================
    {
      path: '/buyer/dashboard',
      name: 'buyer-dashboard',
      component: () => import('../views/buyer/BuyerDashboard.vue'),
      meta: { requiresAuth: true, roles: ['BUYER'] },
    },
    {
      path: '/buyer/my-bids',
      name: 'buyer-my-bids',
      component: () => import('../views/buyer/MyBids.vue'),
      meta: { requiresAuth: true, roles: ['BUYER'] },
    },
    {
      path: '/buyer/orders',
      name: 'buyer-orders',
      component: () => import('../views/buyer/BuyerOrders.vue'),
      meta: { requiresAuth: true, roles: ['BUYER'] },
    },
    {
      path: '/buyer/favourites',
      name: 'buyer-favourites',
      component: () => import('../views/buyer/BuyerFavourites.vue'),
      meta: { requiresAuth: true, roles: ['BUYER'] },
    },
    {
      path: '/buyer/profile',
      name: 'buyer-profile',
      component: () => import('../views/buyer/BuyerProfile.vue'),
      meta: { requiresAuth: true, roles: ['BUYER'] },
    },

    // ============================================================
    // DELIVERY PARTNER ROUTES
    // ============================================================
    {
      path: '/delivery/dashboard',
      name: 'delivery-dashboard',
      component: () => import('../views/delivery/DeliveryDashboard.vue'),
      meta: { requiresAuth: true, roles: ['DELIVERY_PARTNER'] },
    },
    {
      path: '/delivery/available',
      name: 'delivery-available',
      component: () => import('../views/delivery/AvailableDeliveries.vue'),
      meta: { requiresAuth: true, roles: ['DELIVERY_PARTNER'] },
    },
    {
      path: '/delivery/my-deliveries',
      name: 'delivery-my',
      component: () => import('../views/delivery/MyDeliveries.vue'),
      meta: { requiresAuth: true, roles: ['DELIVERY_PARTNER'] },
    },
    {
      path: '/delivery/:id',
      name: 'delivery-details',
      component: () => import('../views/delivery/DeliveryDetails.vue'),
      meta: { requiresAuth: true, roles: ['DELIVERY_PARTNER'] },
    },
    {
      path: '/delivery/profile',
      name: 'delivery-profile',
      component: () => import('../views/delivery/DeliveryProfile.vue'),
      meta: { requiresAuth: true, roles: ['DELIVERY_PARTNER'] },
    },

    // ============================================================
    // ADMIN ROUTES
    // ============================================================
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: () => import('../views/admin/AdminDashboard.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('../views/admin/ManageUsers.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    {
      path: '/admin/products',
      name: 'admin-products',
      component: () => import('../views/admin/ManageProducts.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    {
      path: '/admin/orders',
      name: 'admin-orders',
      component: () => import('../views/admin/ManageOrders.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    {
      path: '/admin/reports',
      name: 'admin-reports',
      component: () => import('../views/admin/ReportsView.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] },
    },

    // ============================================================
    // 404 NOT FOUND — must be last
    // ============================================================
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/shared/NotFoundView.vue'),
    },
  ],
})

// ============================================================
// NAVIGATION GUARD — role-based access control
// ============================================================
router.beforeEach((to, from) => {
  // Get auth store lazily (must be inside the guard)
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated
  const userRole = authStore.role

  // Redirect authenticated users away from login/register
  if (to.meta.guestOnly && isAuthenticated) {
    const dashboardMap = {
      FARMER: '/farmer/dashboard',
      BUYER: '/buyer/dashboard',
      DELIVERY_PARTNER: '/delivery/dashboard',
      ADMIN: '/admin/dashboard',
    }
    return dashboardMap[userRole] || '/'
  }

  // Protected route: user must be authenticated
  if (to.meta.requiresAuth && !isAuthenticated) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // Role-restricted route: user must have the correct role
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!isAuthenticated) {
      return { path: '/login', query: { redirect: to.fullPath } }
    }
    if (!to.meta.roles.includes(userRole)) {
      // Redirect unauthorized user to their own dashboard
      const dashboardMap = {
        FARMER: '/farmer/dashboard',
        BUYER: '/buyer/dashboard',
        DELIVERY_PARTNER: '/delivery/dashboard',
        ADMIN: '/admin/dashboard',
      }
      return dashboardMap[userRole] || '/'
    }
  }

  // Allow navigation
})

export default router

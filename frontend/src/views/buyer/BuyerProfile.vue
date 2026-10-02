<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import { useAuthStore } from '../../stores/auth'
import { authService } from '../../services/authService'
import { buyerService } from '../../services/buyerService'
import { PROVINCES, getDistrictsForProvince } from '../../utils/sriLankaRegions'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

const activeTab = ref('profile') // 'profile', 'addresses', 'security'

const editMode = ref(false)
const saving = ref(false)
const successMsg = ref('')
const errorMsg = ref('')

// Profile Form
const profileForm = ref({
  fullName: '',
  phone: '',
  address: '',
  profileImageUrl: '',
  buyerType: 'INDIVIDUAL',
  businessName: '',
  businessRegNumber: ''
})

// Password Form
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const changingPassword = ref(false)
const passwordError = ref('')
const passwordSuccess = ref('')

// Delivery Addresses State
const addresses = ref([])
const loadingAddresses = ref(false)
const showAddressModal = ref(false)
const editingAddressId = ref(null)
const addressSaving = ref(false)
const addressError = ref('')

const addressForm = ref({
  label: '',
  recipientName: '',
  phone: '',
  streetAddress: '',
  city: '',
  district: 'Colombo',
  province: 'Western',
  postalCode: '',
  isDefault: false
})

const addressDistricts = computed(() => {
  return getDistrictsForProvince(addressForm.value.province)
})

watch(() => addressForm.value.province, (newProv) => {
  const dists = getDistrictsForProvince(newProv)
  if (dists.length > 0 && !dists.includes(addressForm.value.district)) {
    addressForm.value.district = dists[0]
  }
})

// Populate user profile
const populateUser = (u) => {
  if (u) {
    profileForm.value.fullName = u.fullName || ''
    profileForm.value.phone = u.phone || ''
    profileForm.value.address = u.address || ''
    profileForm.value.profileImageUrl = u.profileImageUrl || ''
    profileForm.value.buyerType = u.buyerType || 'INDIVIDUAL'
    profileForm.value.businessName = u.businessName || ''
    profileForm.value.businessRegNumber = u.businessRegNumber || ''
  }
}

// Favorite Categories & Sourcing Preferences
const availableCategories = [
  { id: 'vegetables', name: 'Fresh Vegetables & Greens', icon: 'bi-flower1' },
  { id: 'fruits', name: 'Tropical Fruits & Melons', icon: 'bi-apple' },
  { id: 'spices', name: 'Ceylon Spices & Essential Herbs', icon: 'bi-fire' },
  { id: 'grains', name: 'Traditional Rice & Pulses', icon: 'bi-tree' },
  { id: 'organic', name: '100% Organic Certified Crops', icon: 'bi-patch-check-fill' },
  { id: 'export', name: 'Export-Grade A Quality Produce', icon: 'bi-globe' }
]

const selectedCategories = ref(
  JSON.parse(localStorage.getItem('farmora_buyer_fav_categories') || '["vegetables", "fruits"]')
)

const toggleCategory = (catId) => {
  if (selectedCategories.value.includes(catId)) {
    selectedCategories.value = selectedCategories.value.filter(c => c !== catId)
  } else {
    selectedCategories.value.push(catId)
  }
  localStorage.setItem('farmora_buyer_fav_categories', JSON.stringify(selectedCategories.value))
}

watch(user, (u) => {
  populateUser(u)
}, { immediate: true })

// Load Delivery Addresses
const loadAddresses = async () => {
  loadingAddresses.value = true
  try {
    addresses.value = await buyerService.getMyAddresses()
  } catch (err) {
    console.error('Failed to load delivery addresses', err)
  } finally {
    loadingAddresses.value = false
  }
}

onMounted(() => {
  loadAddresses()
})

const openEdit = () => {
  successMsg.value = ''
  errorMsg.value = ''
  editMode.value = true
}

const cancelEdit = () => {
  populateUser(user.value)
  editMode.value = false
}

const saveProfile = async () => {
  saving.value = true
  errorMsg.value = ''
  try {
    const updatedUser = await authService.updateProfile(profileForm.value)
    authStore.$patch({ user: updatedUser })
    successMsg.value = 'Buyer profile updated successfully!'
    editMode.value = false
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to save profile changes.'
  } finally {
    saving.value = false
  }
}

// Delivery Address CRUD
const openAddAddressModal = () => {
  editingAddressId.value = null
  addressError.value = ''
  addressForm.value = {
    label: '',
    recipientName: user.value?.fullName || '',
    phone: user.value?.phone || '',
    streetAddress: '',
    city: '',
    district: 'Colombo',
    province: 'Western',
    postalCode: '',
    isDefault: addresses.value.length === 0
  }
  showAddressModal.value = true
}

const openEditAddressModal = (addr) => {
  editingAddressId.value = addr.id
  addressError.value = ''
  addressForm.value = {
    label: addr.label || '',
    recipientName: addr.recipientName || '',
    phone: addr.phone || '',
    streetAddress: addr.streetAddress || '',
    city: addr.city || '',
    district: addr.district || 'Colombo',
    province: addr.province || 'Western',
    postalCode: addr.postalCode || '',
    isDefault: !!addr.isDefault
  }
  showAddressModal.value = true
}

const saveAddress = async () => {
  if (!addressForm.value.label || !addressForm.value.streetAddress) {
    addressError.value = 'Please provide an address label and street address.'
    return
  }
  addressSaving.value = true
  addressError.value = ''
  try {
    if (editingAddressId.value) {
      await buyerService.updateAddress(editingAddressId.value, addressForm.value)
    } else {
      await buyerService.addAddress(addressForm.value)
    }
    showAddressModal.value = false
    await loadAddresses()
  } catch (err) {
    addressError.value = err.response?.data?.message || 'Failed to save delivery address.'
  } finally {
    addressSaving.value = false
  }
}

const deleteAddress = async (id) => {
  if (!confirm('Are you sure you want to remove this delivery address?')) return
  try {
    await buyerService.deleteAddress(id)
    await loadAddresses()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to delete address.')
  }
}

const setDefaultAddress = async (id) => {
  try {
    await buyerService.setDefaultAddress(id)
    await loadAddresses()
  } catch (err) {
    alert(err.response?.data?.message || 'Failed to update default address.')
  }
}

// Password Change
const changePassword = async () => {
  passwordError.value = ''
  passwordSuccess.value = ''
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordError.value = 'New passwords do not match.'
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    passwordError.value = 'Password must be at least 6 characters.'
    return
  }
  changingPassword.value = true
  try {
    await authService.changePassword({
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    })
    passwordSuccess.value = 'Password changed successfully!'
    passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
  } catch (err) {
    passwordError.value = err.response?.data?.message || 'Incorrect current password.'
  } finally {
    changingPassword.value = false
  }
}
</script>

<template>
  <div class="container py-4">
    <div class="row g-4">
      <div class="col-lg-3">
        <DashboardSidebar activeMenu="profile" />
      </div>

      <div class="col-lg-9">
        <!-- Buyer Header Card -->
        <div class="card border-0 shadow-sm p-4 mb-4 bg-white rounded-3">
          <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 pb-3 border-bottom">
            <div class="d-flex align-items-center gap-3">
              <div
                class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center shadow-sm"
                style="width: 76px; height: 76px; min-width: 76px; font-size: 2rem;"
              >
                <img
                  v-if="profileForm.profileImageUrl"
                  :src="profileForm.profileImageUrl"
                  alt="Buyer Avatar"
                  class="rounded-circle w-100 h-100 object-fit-cover"
                />
                <i v-else class="bi bi-person-fill"></i>
              </div>
              <div>
                <div class="d-flex align-items-center gap-2 flex-wrap">
                  <h4 class="fw-bold mb-0">
                    {{ profileForm.buyerType !== 'INDIVIDUAL' && profileForm.businessName ? profileForm.businessName : user?.fullName }}
                  </h4>
                  
                  <!-- Buyer Type Badge -->
                  <span v-if="profileForm.buyerType === 'RESTAURANT'" class="badge bg-warning-subtle text-dark border border-warning px-2 py-1">
                    <i class="bi bi-cup-hot-fill text-warning me-1"></i> Restaurant Account
                  </span>
                  <span v-else-if="profileForm.buyerType === 'SUPERMARKET'" class="badge bg-primary-subtle text-primary border border-primary px-2 py-1">
                    <i class="bi bi-shop me-1"></i> Supermarket Account
                  </span>
                  <span v-else-if="profileForm.buyerType === 'EXPORTER'" class="badge bg-info-subtle text-dark border border-info px-2 py-1">
                    <i class="bi bi-globe me-1"></i> Exporter Account
                  </span>
                  <span v-else class="badge bg-secondary-subtle text-secondary px-2 py-1">
                    <i class="bi bi-person-badge me-1"></i> Individual Buyer
                  </span>
                </div>

                <div class="text-muted small mt-1 d-flex align-items-center gap-3 flex-wrap">
                  <span><i class="bi bi-person me-1"></i>Contact: {{ user?.fullName }}</span>
                  <span><i class="bi bi-envelope me-1"></i>{{ user?.email }}</span>
                  <span v-if="profileForm.businessRegNumber" class="font-monospace">
                    <i class="bi bi-file-earmark-text me-1"></i>BR: {{ profileForm.businessRegNumber }}
                  </span>
                </div>
              </div>
            </div>

            <div class="d-flex gap-2">
              <button v-if="activeTab === 'profile' && !editMode" class="btn btn-outline-primary btn-sm" @click="openEdit">
                <i class="bi bi-pencil-fill me-1"></i> Edit Profile
              </button>
            </div>
          </div>

          <!-- Alert Messages -->
          <div v-if="successMsg" class="alert alert-success alert-dismissible py-2 small mt-3 mb-0">
            <i class="bi bi-check-circle-fill me-2"></i>{{ successMsg }}
            <button class="btn-close" @click="successMsg = ''"></button>
          </div>
          <div v-if="errorMsg" class="alert alert-danger py-2 small mt-3 mb-0">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ errorMsg }}
          </div>
        </div>

        <!-- Navigation Tabs -->
        <ul class="nav nav-pills bg-white p-2 rounded-3 shadow-sm mb-4 gap-2">
          <li class="nav-item">
            <button
              class="nav-link"
              :class="{ active: activeTab === 'profile' }"
              @click="activeTab = 'profile'"
            >
              <i class="bi bi-person-lines-fill me-1"></i> Account Details
            </button>
          </li>
          <li class="nav-item">
            <button
              class="nav-link"
              :class="{ active: activeTab === 'addresses' }"
              @click="activeTab = 'addresses'"
            >
              <i class="bi bi-geo-alt-fill me-1"></i> Delivery Addresses ({{ addresses.length }})
            </button>
          </li>
          <li class="nav-item">
            <button
              class="nav-link"
              :class="{ active: activeTab === 'preferences' }"
              @click="activeTab = 'preferences'"
            >
              <i class="bi bi-heart-fill me-1 text-danger"></i> Favorite Categories & Farms
            </button>
          </li>
          <li class="nav-item">
            <button
              class="nav-link"
              :class="{ active: activeTab === 'security' }"
              @click="activeTab = 'security'"
            >
              <i class="bi bi-shield-lock-fill me-1"></i> Security & Password
            </button>
          </li>
        </ul>

        <!-- TAB 1: Account Details -->
        <div v-if="activeTab === 'profile'" class="card border-0 shadow-sm p-4 bg-white rounded-3">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="fw-bold mb-0"><i class="bi bi-card-text text-primary me-2"></i>Buyer Profile Information</h5>
          </div>

          <!-- READ MODE -->
          <div v-if="!editMode" class="row g-3">
            <div class="col-md-6">
              <div class="small text-muted mb-1">Account Category</div>
              <div class="p-2 bg-light rounded fw-semibold text-uppercase">{{ profileForm.buyerType }}</div>
            </div>
            <div class="col-md-6" v-if="profileForm.buyerType !== 'INDIVIDUAL'">
              <div class="small text-muted mb-1">Company / Business Name</div>
              <div class="p-2 bg-light rounded fw-semibold">{{ profileForm.businessName || '—' }}</div>
            </div>

            <div class="col-md-6">
              <div class="small text-muted mb-1">Primary Contact Name</div>
              <div class="p-2 bg-light rounded fw-semibold">{{ user?.fullName }}</div>
            </div>
            <div class="col-md-6">
              <div class="small text-muted mb-1">Email Address</div>
              <div class="p-2 bg-light rounded fw-semibold">{{ user?.email }}</div>
            </div>

            <div class="col-md-6">
              <div class="small text-muted mb-1">Phone Number</div>
              <div class="p-2 bg-light rounded fw-semibold">{{ user?.phone || '—' }}</div>
            </div>
            <div class="col-md-6" v-if="profileForm.buyerType !== 'INDIVIDUAL'">
              <div class="small text-muted mb-1">Business Registration / Tax No.</div>
              <div class="p-2 bg-light rounded fw-semibold font-monospace">{{ profileForm.businessRegNumber || '—' }}</div>
            </div>

            <div class="col-12">
              <div class="small text-muted mb-1">Primary Shipping / Billing Address</div>
              <div class="p-2 bg-light rounded fw-semibold">{{ user?.address || '—' }}</div>
            </div>
          </div>

          <!-- EDIT MODE -->
          <form v-else @submit.prevent="saveProfile">
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label small fw-semibold">Account Category <span class="text-danger">*</span></label>
                <select class="form-select" v-model="profileForm.buyerType">
                  <option value="INDIVIDUAL">Individual Consumer</option>
                  <option value="RESTAURANT">Restaurant & Hotel Kitchen</option>
                  <option value="SUPERMARKET">Supermarket / Retail Chain</option>
                  <option value="EXPORTER">Produce Exporter</option>
                </select>
              </div>

              <div class="col-md-6" v-if="profileForm.buyerType !== 'INDIVIDUAL'">
                <label class="form-label small fw-semibold">Business / Company Name <span class="text-danger">*</span></label>
                <input type="text" class="form-control" v-model="profileForm.businessName" required />
              </div>

              <div class="col-md-6">
                <label class="form-label small fw-semibold">Contact Person Name <span class="text-danger">*</span></label>
                <input type="text" class="form-control" v-model="profileForm.fullName" required />
              </div>

              <div class="col-md-6">
                <label class="form-label small fw-semibold">Phone Number</label>
                <input type="tel" class="form-control" v-model="profileForm.phone" placeholder="+94 77 XXX XXXX" />
              </div>

              <div class="col-md-6" v-if="profileForm.buyerType !== 'INDIVIDUAL'">
                <label class="form-label small fw-semibold">Business Reg No. / Tax ID</label>
                <input type="text" class="form-control" v-model="profileForm.businessRegNumber" placeholder="e.g. PV-12345" />
              </div>

              <div class="col-md-6">
                <label class="form-label small fw-semibold">Profile Avatar URL</label>
                <input type="url" class="form-control" v-model="profileForm.profileImageUrl" placeholder="https://..." />
              </div>

              <div class="col-12">
                <label class="form-label small fw-semibold">Primary Address</label>
                <textarea class="form-control" rows="2" v-model="profileForm.address" placeholder="Full address"></textarea>
              </div>

              <div class="col-12 d-flex gap-2 mt-3">
                <button type="submit" class="btn btn-primary" :disabled="saving">
                  <span v-if="saving" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="bi bi-save me-1"></i> Save Changes
                </button>
                <button type="button" class="btn btn-outline-secondary" @click="cancelEdit">Cancel</button>
              </div>
            </div>
          </form>
        </div>

        <!-- TAB 2: Delivery Addresses Management -->
        <div v-if="activeTab === 'addresses'" class="card border-0 shadow-sm p-4 bg-white rounded-3">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
              <h5 class="fw-bold mb-0"><i class="bi bi-geo-alt-fill text-danger me-2"></i>Delivery Address Management</h5>
              <p class="text-muted small mb-0">Manage multiple delivery locations for your restaurants, branches, warehouses, or home.</p>
            </div>
            <button class="btn btn-primary btn-sm d-flex align-items-center gap-1" @click="openAddAddressModal">
              <i class="bi bi-plus-lg"></i> Add Address
            </button>
          </div>

          <!-- Addresses List -->
          <div v-if="addresses.length > 0" class="row g-3">
            <div v-for="addr in addresses" :key="addr.id" class="col-md-6">
              <div class="card h-100 border rounded-3 p-3 shadow-sm position-relative">
                <div class="d-flex justify-content-between align-items-start mb-2">
                  <div>
                    <h6 class="fw-bold mb-1 text-dark">
                      <i class="bi bi-geo-alt text-primary me-1"></i>{{ addr.label }}
                    </h6>
                    <span v-if="addr.isDefault" class="badge bg-success-subtle text-success border border-success-subtle">
                      Default Delivery Location
                    </span>
                  </div>
                  <div class="dropdown">
                    <button class="btn btn-light btn-sm" type="button" @click="openEditAddressModal(addr)" title="Edit">
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-light btn-sm text-danger ms-1" type="button" @click="deleteAddress(addr.id)" title="Delete">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </div>

                <div class="small text-secondary mb-2">
                  <div><strong>Recipient:</strong> {{ addr.recipientName }}</div>
                  <div v-if="addr.phone"><strong>Phone:</strong> {{ addr.phone }}</div>
                  <div class="mt-1">
                    {{ addr.streetAddress }}<span v-if="addr.city">, {{ addr.city }}</span><span v-if="addr.district">, {{ addr.district }}</span><span v-if="addr.province">, {{ addr.province }} Province</span>
                    <span v-if="addr.postalCode"> ({{ addr.postalCode }})</span>
                  </div>
                </div>

                <div class="mt-auto pt-2 border-top d-flex justify-content-end" v-if="!addr.isDefault">
                  <button class="btn btn-outline-secondary btn-sm" style="font-size: 0.75rem;" @click="setDefaultAddress(addr.id)">
                    Set as Default
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="text-center py-5 bg-light rounded-3 text-muted">
            <i class="bi bi-geo-alt display-4 opacity-50 mb-2"></i>
            <h6 class="fw-bold">No Delivery Addresses Added Yet</h6>
            <p class="small">Add your delivery locations so our transport partners can deliver orders directly to your doorstep.</p>
            <button class="btn btn-primary btn-sm" @click="openAddAddressModal">
              <i class="bi bi-plus-lg me-1"></i> Add Your First Address
            </button>
          </div>
        </div>

        <!-- TAB: Favorite Categories & Preferences -->
        <div v-if="activeTab === 'preferences'" class="card border-0 shadow-sm p-4 bg-white rounded-3">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <div>
              <h5 class="fw-bold mb-1"><i class="bi bi-heart-fill text-danger me-2"></i>Favorite Categories & Farm Preferences</h5>
              <p class="text-muted small mb-0">Select your preferred wholesale product types to personalize catalog recommendations.</p>
            </div>
            <router-link to="/buyer/favourites" class="btn btn-outline-danger btn-sm d-flex align-items-center gap-1">
              <i class="bi bi-heart"></i>
              <span>View Saved Farms</span>
            </router-link>
          </div>

          <div class="row g-3 mb-4">
            <div v-for="cat in availableCategories" :key="cat.id" class="col-md-6 col-lg-4">
              <div
                class="p-3 rounded-3 border d-flex align-items-center justify-content-between cursor-pointer"
                :class="selectedCategories.includes(cat.id) ? 'border-primary bg-primary-subtle bg-opacity-10 shadow-sm' : 'bg-light'"
                @click="toggleCategory(cat.id)"
                style="cursor: pointer; transition: all 0.2s;"
              >
                <div class="d-flex align-items-center gap-2">
                  <i :class="cat.icon" class="fs-5 text-primary"></i>
                  <span class="small fw-semibold text-dark">{{ cat.name }}</span>
                </div>
                <input
                  type="checkbox"
                  class="form-check-input"
                  :checked="selectedCategories.includes(cat.id)"
                />
              </div>
            </div>
          </div>

          <div class="card bg-light border-0 p-3 rounded-3 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
            <div>
              <h6 class="fw-bold text-dark mb-1"><i class="bi bi-bookmark-heart text-danger me-2"></i>Direct Sourcing from Saved Farms</h6>
              <p class="text-muted small mb-0">You can also bookmark specific verified farms directly from the marketplace or their farm profiles.</p>
            </div>
            <router-link to="/products" class="btn btn-primary btn-sm flex-shrink-0">
              Browse Farms & Produce
            </router-link>
          </div>
        </div>

        <!-- TAB 3: Security & Password -->
        <div v-if="activeTab === 'security'" class="card border-0 shadow-sm p-4 bg-white rounded-3">
          <h5 class="fw-bold mb-4">
            <i class="bi bi-shield-lock-fill text-primary me-2"></i>Change Password
          </h5>
          <div v-if="passwordSuccess" class="alert alert-success alert-dismissible py-2 small mb-3">
            <i class="bi bi-check-circle-fill me-2"></i>{{ passwordSuccess }}
            <button class="btn-close" @click="passwordSuccess = ''"></button>
          </div>
          <div v-if="passwordError" class="alert alert-danger py-2 small mb-3">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ passwordError }}
          </div>
          <form @submit.prevent="changePassword">
            <div class="row g-3">
              <div class="col-12">
                <label class="form-label small fw-semibold">Current Password</label>
                <input type="password" class="form-control" v-model="passwordForm.currentPassword" required />
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">New Password</label>
                <input type="password" class="form-control" v-model="passwordForm.newPassword" required minlength="6" />
              </div>
              <div class="col-md-6">
                <label class="form-label small fw-semibold">Confirm New Password</label>
                <input type="password" class="form-control" v-model="passwordForm.confirmPassword" required minlength="6" />
              </div>
              <div class="col-12">
                <button type="submit" class="btn btn-outline-primary fw-semibold btn-sm" :disabled="changingPassword">
                  <span v-if="changingPassword" class="spinner-border spinner-border-sm me-1"></span>
                  <i v-else class="bi bi-lock-fill me-1"></i> Update Password
                </button>
              </div>
            </div>
          </form>
        </div>

      </div>
    </div>

    <!-- Modal: Add / Edit Address -->
    <div
      v-if="showAddressModal"
      class="modal fade show d-block"
      tabindex="-1"
      style="background-color: rgba(0, 0, 0, 0.5);"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header border-bottom">
            <h5 class="modal-title fw-bold">
              <i class="bi bi-geo-alt-fill text-danger me-2"></i>
              {{ editingAddressId ? 'Edit Delivery Address' : 'Add New Delivery Address' }}
            </h5>
            <button type="button" class="btn-close" @click="showAddressModal = false"></button>
          </div>
          <div class="modal-body">
            <div v-if="addressError" class="alert alert-danger py-1 small mb-3">{{ addressError }}</div>
            <form @submit.prevent="saveAddress">
              <div class="mb-3">
                <label class="form-label small fw-semibold">Address Label <span class="text-danger">*</span></label>
                <input
                  type="text"
                  class="form-control"
                  v-model="addressForm.label"
                  placeholder="e.g. Main Kitchen, Central Warehouse, Home"
                  required
                />
              </div>

              <div class="row g-2 mb-3">
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Recipient Name</label>
                  <input type="text" class="form-control" v-model="addressForm.recipientName" />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Phone Number</label>
                  <input type="tel" class="form-control" v-model="addressForm.phone" placeholder="+94 77 XXX XXXX" />
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label small fw-semibold">Street Address <span class="text-danger">*</span></label>
                <input
                  type="text"
                  class="form-control"
                  v-model="addressForm.streetAddress"
                  placeholder="Street name, building no, unit"
                  required
                />
              </div>

              <div class="row g-2 mb-3">
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Province</label>
                  <select class="form-select form-select-sm" v-model="addressForm.province">
                    <option v-for="prov in PROVINCES" :key="prov" :value="prov">{{ prov }}</option>
                  </select>
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">District</label>
                  <select class="form-select form-select-sm" v-model="addressForm.district">
                    <option v-for="dist in addressDistricts" :key="dist" :value="dist">{{ dist }}</option>
                  </select>
                </div>
              </div>

              <div class="row g-2 mb-3">
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">City</label>
                  <input type="text" class="form-control form-control-sm" v-model="addressForm.city" placeholder="e.g. Colombo 03" />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Postal Code</label>
                  <input type="text" class="form-control form-control-sm" v-model="addressForm.postalCode" placeholder="00300" />
                </div>
              </div>

              <div class="form-check mb-2">
                <input class="form-check-input" type="checkbox" id="defaultAddrCheck" v-model="addressForm.isDefault" />
                <label class="form-check-label small" for="defaultAddrCheck">
                  Set as default delivery address for all purchases
                </label>
              </div>

              <div class="modal-footer border-top-0 px-0 pb-0">
                <button type="button" class="btn btn-light btn-sm" @click="showAddressModal = false">Cancel</button>
                <button type="submit" class="btn btn-primary btn-sm" :disabled="addressSaving">
                  <span v-if="addressSaving" class="spinner-border spinner-border-sm me-1"></span>
                  Save Address
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.nav-pills .nav-link {
  color: #495057;
  font-weight: 500;
  border-radius: 8px;
  padding: 0.55rem 0.95rem;
  transition: all 0.2s ease-in-out;
}
.nav-pills .nav-link:hover {
  background-color: #f0f7ff;
  color: #0d6efd;
}
.nav-pills .nav-link.active {
  background-color: #0d6efd;
  color: white;
}
</style>

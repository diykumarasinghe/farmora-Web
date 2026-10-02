<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import GpsMapPicker from '../../components/common/GpsMapPicker.vue'
import { useAuthStore } from '../../stores/auth'
import { authService } from '../../services/authService'
import { farmService } from '../../services/farmService'
import { PROVINCES, getDistrictsForProvince } from '../../utils/sriLankaRegions'

const authStore = useAuthStore()
const user = computed(() => authStore.user)

// Alerts & Loaders
const successMsg = ref('')
const errorMsg = ref('')
const loading = ref(false)
const saving = ref(false)
const archiving = ref(false)

// Modals
const showEditModal = ref(false)
const activeEditSection = ref('all') // 'all', 'personal', 'farm', 'about', 'location', 'experience'
const showPhotoModal = ref(false)
const showMapModal = ref(false)
const showArchiveConfirmModal = ref(false)
const showDeleteSectionModal = ref(false)
const sectionToDelete = ref('')

// Default fallback images
const defaultCover = 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?auto=format&fit=crop&w=1600&q=80'
const defaultAvatar = 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=500&q=80'
const defaultGallery = [
  'https://images.unsplash.com/photo-1592417817098-8f3d6ef2364c?auto=format&fit=crop&w=500&q=80',
  'https://images.unsplash.com/photo-1500651230702-0e2d8a49d4ad?auto=format&fit=crop&w=500&q=80',
  'https://images.unsplash.com/photo-1574943320219-553eb213f72d?auto=format&fit=crop&w=500&q=80',
  'https://images.unsplash.com/photo-1628352081506-83c43123ed6d?auto=format&fit=crop&w=500&q=80'
]

// Personal Profile Form
const personalForm = ref({
  fullName: 'Kamal Perera',
  email: 'kamal.perera@gmail.com',
  phone: '+94 77 123 4567',
  nicNumber: '199512345678',
  dateOfBirth: '1990-05-12',
  profileImageUrl: defaultAvatar
})

// Farm Profile Form
const farmForm = ref({
  farmName: 'Sunrise Valley Farm',
  farmSize: 12,
  farmSizeUnit: 'Acres',
  primaryCrops: 'Tomatoes, Carrots, Lettuce',
  farmType: 'Organic',
  establishedYear: 2018,
  farmerDetails: 'At Sunrise Valley Farm, we are committed to sustainable farming practices and providing fresh, healthy produce to local communities. We use organic methods and take pride in maintaining high quality standards in everything we grow.',
  tagline: 'Growing healthy food for a better tomorrow',
  quote: 'Healthy food, healthy people, healthier planet.',
  farmAddress: '123/4, Aluvihare Road, Matale',
  district: 'Matale',
  province: 'Central',
  latitude: 7.4673,
  longitude: 80.6235,
  farmingExperience: 5,
  specialization: 'Vegetable Cultivation',
  farmingMethodsList: 'Organic, Mulching, Drip Irrigation',
  skills: 'Crop Management, Soil Conservation, Pest Control',
  certifications: 'Organic Certified',
  coverImageUrl: defaultCover,
  isArchived: false,
  photoUrls: '',
  videoUrl: '',
  contactPhone: '+94 77 123 4567',
  contactEmail: 'kamal.perera@gmail.com'
})

// Server stats
const verificationStatus = ref('VERIFIED')
const averageRating = ref(4.8)
const totalReviews = ref(12)
const farmId = ref(null)

// Photos gallery list
const photoList = ref([...defaultGallery])
const newPhotoInput = ref('')

// Temporary editing buffer
const editBuffer = ref({})

// Available Districts for chosen Province
const availableDistricts = computed(() => {
  return getDistrictsForProvince(farmForm.value.province)
})

// Watch province changes
watch(() => farmForm.value.province, (newProv) => {
  const dists = getDistrictsForProvince(newProv)
  if (dists.length > 0 && !dists.includes(farmForm.value.district)) {
    farmForm.value.district = dists[0]
  }
})

// Load Profile Data
const loadProfile = async () => {
  loading.value = true
  if (user.value) {
    personalForm.value.fullName = user.value.fullName || personalForm.value.fullName
    personalForm.value.email = user.value.email || personalForm.value.email
    personalForm.value.phone = user.value.phone || personalForm.value.phone
    if (user.value.profileImageUrl) {
      personalForm.value.profileImageUrl = user.value.profileImageUrl
    }
  }

  try {
    const farm = await farmService.getMyFarmProfile()
    if (farm) {
      farmId.value = farm.id || null
      farmForm.value.farmName = farm.farmName || (user.value?.fullName ? `${user.value.fullName}'s Farm` : 'Sunrise Valley Farm')
      farmForm.value.farmSize = farm.farmSize !== null && farm.farmSize !== undefined ? farm.farmSize : 12
      farmForm.value.farmSizeUnit = farm.farmSizeUnit || 'Acres'
      farmForm.value.primaryCrops = farm.primaryCrops || 'Tomatoes, Carrots, Lettuce'
      farmForm.value.farmType = farm.farmType || 'Organic'
      farmForm.value.establishedYear = farm.establishedYear || 2018
      farmForm.value.farmerDetails = farm.farmerDetails || 'At Sunrise Valley Farm, we are committed to sustainable farming practices and providing fresh, healthy produce to local communities. We use organic methods and take pride in maintaining high quality standards in everything we grow.'
      farmForm.value.tagline = farm.tagline || 'Growing healthy food for a better tomorrow'
      farmForm.value.quote = farm.quote || 'Healthy food, healthy people, healthier planet.'
      farmForm.value.farmAddress = farm.farmAddress || user.value?.address || '123/4, Aluvihare Road, Matale'
      farmForm.value.district = farm.district || 'Matale'
      farmForm.value.province = farm.province || 'Central'
      farmForm.value.latitude = farm.latitude || 7.4673
      farmForm.value.longitude = farm.longitude || 80.6235
      farmForm.value.farmingExperience = farm.farmingExperience !== null && farm.farmingExperience !== undefined ? farm.farmingExperience : 5
      farmForm.value.specialization = farm.specialization || 'Vegetable Cultivation'
      farmForm.value.farmingMethodsList = farm.farmingMethodsList || 'Organic, Mulching, Drip Irrigation'
      farmForm.value.skills = farm.skills || 'Crop Management, Soil Conservation, Pest Control'
      farmForm.value.certifications = farm.certifications || 'Organic Certified'
      farmForm.value.nicNumber = farm.nicNumber || '199512345678'
      farmForm.value.dateOfBirth = farm.dateOfBirth || '1990-05-12'
      farmForm.value.coverImageUrl = farm.coverImageUrl || defaultCover
      farmForm.value.isArchived = Boolean(farm.isArchived)
      farmForm.value.videoUrl = farm.videoUrl || ''

      verificationStatus.value = farm.verificationStatus || 'VERIFIED'
      averageRating.value = farm.averageRating || 4.8
      totalReviews.value = farm.totalReviews || 12

      if (farm.photoUrls) {
        try {
          const parsed = JSON.parse(farm.photoUrls)
          if (Array.isArray(parsed) && parsed.length > 0) {
            photoList.value = parsed
          }
        } catch {
          const split = farm.photoUrls.split(',').map(s => s.trim()).filter(Boolean)
          if (split.length > 0) photoList.value = split
        }
      }
    }
  } catch (err) {
    console.warn('Could not fetch farm profile from backend, using active state', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadProfile()
})

// Open Section Editor
const openEditSection = (section = 'all') => {
  activeEditSection.value = section
  editBuffer.value = {
    // Personal
    fullName: personalForm.value.fullName,
    email: personalForm.value.email,
    phone: personalForm.value.phone,
    nicNumber: farmForm.value.nicNumber,
    dateOfBirth: farmForm.value.dateOfBirth,
    // Farm
    farmName: farmForm.value.farmName,
    farmSize: farmForm.value.farmSize,
    farmSizeUnit: farmForm.value.farmSizeUnit,
    primaryCrops: farmForm.value.primaryCrops,
    farmType: farmForm.value.farmType,
    establishedYear: farmForm.value.establishedYear,
    // About
    farmerDetails: farmForm.value.farmerDetails,
    tagline: farmForm.value.tagline,
    quote: farmForm.value.quote,
    // Location
    farmAddress: farmForm.value.farmAddress,
    district: farmForm.value.district,
    province: farmForm.value.province,
    latitude: farmForm.value.latitude,
    longitude: farmForm.value.longitude,
    // Experience
    farmingExperience: farmForm.value.farmingExperience,
    specialization: farmForm.value.specialization,
    farmingMethodsList: farmForm.value.farmingMethodsList,
    skills: farmForm.value.skills,
    certifications: farmForm.value.certifications
  }
  showEditModal.value = true
}

// Save Section / Full Profile
const saveEditedSection = async () => {
  saving.value = true
  successMsg.value = ''
  errorMsg.value = ''

  try {
    // Apply buffer to local state
    if (activeEditSection.value === 'personal' || activeEditSection.value === 'all') {
      personalForm.value.fullName = editBuffer.value.fullName
      personalForm.value.phone = editBuffer.value.phone
      farmForm.value.nicNumber = editBuffer.value.nicNumber
      farmForm.value.dateOfBirth = editBuffer.value.dateOfBirth
    }

    if (activeEditSection.value === 'farm' || activeEditSection.value === 'all') {
      farmForm.value.farmName = editBuffer.value.farmName
      farmForm.value.farmSize = editBuffer.value.farmSize
      farmForm.value.farmSizeUnit = editBuffer.value.farmSizeUnit
      farmForm.value.primaryCrops = editBuffer.value.primaryCrops
      farmForm.value.farmType = editBuffer.value.farmType
      farmForm.value.establishedYear = editBuffer.value.establishedYear
    }

    if (activeEditSection.value === 'about' || activeEditSection.value === 'all') {
      farmForm.value.farmerDetails = editBuffer.value.farmerDetails
      farmForm.value.tagline = editBuffer.value.tagline
      farmForm.value.quote = editBuffer.value.quote
    }

    if (activeEditSection.value === 'location' || activeEditSection.value === 'all') {
      farmForm.value.farmAddress = editBuffer.value.farmAddress
      farmForm.value.district = editBuffer.value.district
      farmForm.value.province = editBuffer.value.province
      farmForm.value.latitude = editBuffer.value.latitude
      farmForm.value.longitude = editBuffer.value.longitude
    }

    if (activeEditSection.value === 'experience' || activeEditSection.value === 'all') {
      farmForm.value.farmingExperience = editBuffer.value.farmingExperience
      farmForm.value.specialization = editBuffer.value.specialization
      farmForm.value.farmingMethodsList = editBuffer.value.farmingMethodsList
      farmForm.value.skills = editBuffer.value.skills
      farmForm.value.certifications = editBuffer.value.certifications
    }

    // Update user auth profile
    try {
      const updatedUser = await authService.updateProfile({
        fullName: personalForm.value.fullName,
        phone: personalForm.value.phone,
        address: farmForm.value.farmAddress,
        profileImageUrl: personalForm.value.profileImageUrl
      })
      if (updatedUser) {
        authStore.$patch({ user: updatedUser })
      }
    } catch (e) {
      console.warn('User auth sync skipped', e)
    }

    // Save to FarmProfile API
    const payload = {
      ...farmForm.value,
      contactPhone: personalForm.value.phone,
      contactEmail: personalForm.value.email,
      photoUrls: JSON.stringify(photoList.value),
      isArchived: farmForm.value.isArchived
    }

    const saved = await farmService.saveFarmProfile(payload)
    if (saved) {
      farmId.value = saved.id
    }

    successMsg.value = 'Profile updated successfully!'
    showEditModal.value = false
    setTimeout(() => { successMsg.value = '' }, 4000)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to save changes. Please try again.'
  } finally {
    saving.value = false
  }
}

// Archive / Unarchive Profile Toggle
const toggleArchive = async () => {
  archiving.value = true
  errorMsg.value = ''
  successMsg.value = ''
  try {
    farmForm.value.isArchived = !farmForm.value.isArchived
    try {
      await farmService.toggleArchiveProfile()
    } catch {
      // Fallback: save via standard profile update
      await farmService.saveFarmProfile({
        ...farmForm.value,
        isArchived: farmForm.value.isArchived
      })
    }
    showArchiveConfirmModal.value = false
    successMsg.value = farmForm.value.isArchived
      ? 'Profile archived! Your farm is now temporarily hidden from buyers.'
      : 'Profile restored! Your farm is now active and visible to buyers.'
    setTimeout(() => { successMsg.value = '' }, 5000)
  } catch (err) {
    errorMsg.value = 'Failed to update profile archive status.'
    // revert
    farmForm.value.isArchived = !farmForm.value.isArchived
  } finally {
    archiving.value = false
  }
}

// Delete / Clear specific section
const confirmDeleteSection = (sectionName) => {
  sectionToDelete.value = sectionName
  showDeleteSectionModal.value = true
}

const executeDeleteSection = async () => {
  if (sectionToDelete.value === 'about') {
    farmForm.value.farmerDetails = ''
    farmForm.value.quote = ''
  } else if (sectionToDelete.value === 'skills') {
    farmForm.value.skills = ''
    farmForm.value.specialization = ''
    farmForm.value.farmingMethodsList = ''
  } else if (sectionToDelete.value === 'photos') {
    photoList.value = []
  }

  showDeleteSectionModal.value = false
  await saveEditedSection()
  successMsg.value = `Section '${sectionToDelete.value}' has been cleared.`
}

// Photo updates
const updateProfilePhoto = (url) => {
  if (!url) return
  personalForm.value.profileImageUrl = url
  authService.updateProfile({ profileImageUrl: url }).then(u => {
    if (u) authStore.$patch({ user: u })
  }).catch(() => {})
  farmService.saveFarmProfile({ ...farmForm.value, photoUrls: JSON.stringify(photoList.value) }).catch(() => {})
  showPhotoModal.value = false
  successMsg.value = 'Profile photo updated successfully!'
}

const addPhotoToGallery = () => {
  if (!newPhotoInput.value.trim()) return
  photoList.value.push(newPhotoInput.value.trim())
  newPhotoInput.value = ''
  farmService.saveFarmProfile({ ...farmForm.value, photoUrls: JSON.stringify(photoList.value) }).catch(() => {})
  successMsg.value = 'Photo added to your gallery!'
}

const removeGalleryPhoto = (index) => {
  photoList.value.splice(index, 1)
  farmService.saveFarmProfile({ ...farmForm.value, photoUrls: JSON.stringify(photoList.value) }).catch(() => {})
}

// Handle Map GPS Update
const handleGpsUpdate = ({ lat, lng }) => {
  farmForm.value.latitude = Number(lat.toFixed(6))
  farmForm.value.longitude = Number(lng.toFixed(6))
}

const saveMapLocation = async () => {
  showMapModal.value = false
  await saveEditedSection()
  successMsg.value = 'Farm coordinates updated successfully!'
}
</script>

<template>
  <div class="farmer-profile-page container-fluid py-4 px-md-5">
    <div class="row g-4">
      <!-- Left Sidebar Column -->
      <div class="col-xl-3 col-lg-4">
        <DashboardSidebar activeMenu="profile" />
      </div>

      <!-- Main Profile Column -->
      <div class="col-xl-9 col-lg-8">
        <!-- Toast / Alerts -->
        <div v-if="successMsg" class="alert alert-success alert-dismissible shadow-sm fade show mb-4 border-0 d-flex align-items-center" role="alert">
          <i class="bi bi-check-circle-fill fs-5 me-2 text-success"></i>
          <div>{{ successMsg }}</div>
          <button type="button" class="btn-close ms-auto" @click="successMsg = ''"></button>
        </div>

        <div v-if="errorMsg" class="alert alert-danger alert-dismissible shadow-sm fade show mb-4 border-0 d-flex align-items-center" role="alert">
          <i class="bi bi-exclamation-triangle-fill fs-5 me-2 text-danger"></i>
          <div>{{ errorMsg }}</div>
          <button type="button" class="btn-close ms-auto" @click="errorMsg = ''"></button>
        </div>

        <!-- Archived Profile Warning Banner -->
        <div v-if="farmForm.isArchived" class="alert alert-warning border-warning shadow-sm mb-4 d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-3">
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-eye-slash-fill fs-4 text-warning-emphasis"></i>
            <div>
              <strong>Profile is currently ARCHIVED.</strong>
              <div class="small text-muted">Your farm is hidden from wholesale buyers. Restore anytime to reactivate.</div>
            </div>
          </div>
          <button class="btn btn-sm btn-success fw-bold text-nowrap" @click="toggleArchive">
            <i class="bi bi-arrow-counterclockwise me-1"></i> Restore Profile
          </button>
        </div>

        <!-- Top Header Action Bar -->
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
          <div>
            <h2 class="fw-bold text-dark mb-1 page-title">Farmer Profile</h2>
            <p class="text-muted mb-0 small">
              Manage your personal and farm information. Keep your profile updated to build trust with buyers.
            </p>
          </div>
          <div class="d-flex align-items-center gap-2">
            <button class="btn btn-farmora-primary d-flex align-items-center gap-2 shadow-sm" @click="openEditSection('all')">
              <i class="bi bi-pencil"></i>
              <span>Edit Profile</span>
            </button>

            <!-- More Actions Dropdown -->
            <div class="dropdown">
              <button
                class="btn btn-outline-secondary btn-icon-more shadow-sm"
                type="button"
                data-bs-toggle="dropdown"
                aria-expanded="false"
                title="More actions"
              >
                <i class="bi bi-three-dots-vertical"></i>
              </button>
              <ul class="dropdown-menu dropdown-menu-end shadow border-0 py-2">
                <li>
                  <a class="dropdown-item py-2 d-flex align-items-center gap-2" href="#" @click.prevent="openEditSection('all')">
                    <i class="bi bi-pencil-square text-success"></i> Edit All Information
                  </a>
                </li>
                <li>
                  <a class="dropdown-item py-2 d-flex align-items-center gap-2" href="#" @click.prevent="showPhotoModal = true">
                    <i class="bi bi-camera text-primary"></i> Change Cover & Photos
                  </a>
                </li>
                <li v-if="user?.id">
                  <router-link :to="`/farms/${user.id}`" target="_blank" class="dropdown-item py-2 d-flex align-items-center gap-2">
                    <i class="bi bi-box-arrow-up-right text-info"></i> View Public Farm Page
                  </router-link>
                </li>
                <li><hr class="dropdown-divider my-1"></li>
                <li>
                  <a
                    class="dropdown-item py-2 d-flex align-items-center gap-2 text-danger"
                    href="#"
                    @click.prevent="showArchiveConfirmModal = true"
                  >
                    <i class="bi" :class="farmForm.isArchived ? 'bi-eye' : 'bi-archive'"></i>
                    {{ farmForm.isArchived ? 'Restore Public Visibility' : 'Archive Profile' }}
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- ========================================================
             HERO BANNER & FARMER HEADER CARD
             ======================================================== -->
        <div class="card border-0 shadow-sm rounded-4 overflow-hidden mb-4 bg-white profile-hero-card">
          <!-- Panoramic Cover Image -->
          <div class="position-relative hero-cover-container">
            <img
              :src="farmForm.coverImageUrl || defaultCover"
              alt="Farm Landscape"
              class="w-100 h-100 object-fit-cover hero-cover-img"
              @error="$event.target.src = defaultCover"
            />
            <div class="cover-gradient-overlay"></div>
            <button
              class="btn btn-sm btn-light btn-cover-edit position-absolute top-0 end-0 m-3 shadow-sm d-flex align-items-center gap-1"
              @click="showPhotoModal = true"
              title="Change cover photo"
            >
              <i class="bi bi-camera-fill text-muted"></i>
              <span class="d-none d-sm-inline small">Edit Cover</span>
            </button>
          </div>

          <!-- Hero Bottom Body with Overlapping Avatar & Stats -->
          <div class="card-body p-4 pt-0">
            <div class="d-flex flex-column flex-lg-row justify-content-between align-items-lg-center gap-4">
              <!-- Avatar + Name/Location info -->
              <div class="d-flex flex-column flex-sm-row align-items-sm-center gap-3 avatar-info-group">
                <!-- Farmer Avatar with Camera Overlay Button -->
                <div class="position-relative avatar-wrapper flex-shrink-0">
                  <img
                    :src="personalForm.profileImageUrl || defaultAvatar"
                    alt="Farmer Avatar"
                    class="rounded-circle avatar-img shadow"
                    @error="$event.target.src = defaultAvatar"
                  />
                  <button
                    class="btn btn-camera-overlay position-absolute bottom-0 end-0 rounded-circle shadow d-flex align-items-center justify-content-center"
                    @click="showPhotoModal = true"
                    title="Change Profile Photo"
                  >
                    <i class="bi bi-camera-fill text-white" style="font-size: 0.85rem;"></i>
                  </button>
                </div>

                <!-- Identity Details -->
                <div class="mt-2 mt-sm-4 pt-1">
                  <div class="d-flex align-items-center gap-2 flex-wrap mb-1">
                    <h3 class="fw-bold text-dark mb-0 fs-4">{{ personalForm.fullName || 'Kamal Perera' }}</h3>
                    <span class="badge verified-pill d-flex align-items-center gap-1">
                      <i class="bi bi-check-circle-fill text-success"></i>
                      <span>FARMORA VERIFIED</span>
                    </span>
                  </div>

                  <div class="farm-title fw-semibold text-secondary mb-1">
                    {{ farmForm.farmName || 'Sunrise Valley Farm' }}
                  </div>

                  <div class="d-flex align-items-center gap-1 text-muted small mb-1">
                    <i class="bi bi-geo-alt-fill text-success"></i>
                    <span>{{ farmForm.district || 'Matale' }}, {{ farmForm.province || 'Central' }} Province</span>
                  </div>

                  <div class="motto-quote text-muted small fst-italic">
                    "{{ farmForm.tagline || 'Growing healthy food for a better tomorrow' }}"
                  </div>
                </div>
              </div>

              <!-- Right Stat Pills (Experience, Farm Size, Buyer Rating) -->
              <div class="d-flex align-items-center gap-3 mt-3 mt-lg-4 pt-1 flex-wrap stats-row">
                <!-- Stat 1: Years Experience -->
                <div class="stat-pill-card d-flex align-items-center gap-2 px-3 py-2 rounded-3 shadow-2xs">
                  <div class="stat-icon-circle bg-emerald-light d-flex align-items-center justify-content-center">
                    <i class="bi bi-flower1 text-success fs-5"></i>
                  </div>
                  <div>
                    <div class="fw-bold text-dark fs-6 lh-1">{{ farmForm.farmingExperience || 5 }}+</div>
                    <div class="text-muted stat-label">Years Experience</div>
                  </div>
                </div>

                <!-- Stat 2: Farm Size -->
                <div class="stat-pill-card d-flex align-items-center gap-2 px-3 py-2 rounded-3 shadow-2xs">
                  <div class="stat-icon-circle bg-mint-light d-flex align-items-center justify-content-center">
                    <i class="bi bi-grid-3x3-gap-fill text-success fs-5"></i>
                  </div>
                  <div>
                    <div class="fw-bold text-dark fs-6 lh-1">{{ farmForm.farmSize || 12 }} {{ farmForm.farmSizeUnit || 'Acres' }}</div>
                    <div class="text-muted stat-label">Farm Size</div>
                  </div>
                </div>

                <!-- Stat 3: Buyer Rating -->
                <div class="stat-pill-card d-flex align-items-center gap-2 px-3 py-2 rounded-3 shadow-2xs">
                  <div class="stat-icon-circle bg-amber-light d-flex align-items-center justify-content-center">
                    <i class="bi bi-star-fill text-warning fs-5"></i>
                  </div>
                  <div>
                    <div class="fw-bold text-dark fs-6 lh-1">{{ averageRating || '4.8' }}</div>
                    <div class="text-muted stat-label">Buyer Rating</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ========================================================
             SIX MODULAR PROFILE CARDS (2x3 Grid)
             ======================================================== -->
        <div class="row g-4">
          <!-- CARD 1: Personal Information -->
          <div class="col-lg-6">
            <div class="card border-0 shadow-sm rounded-4 h-100 bg-white profile-card">
              <div class="card-header bg-transparent border-0 d-flex align-items-center justify-content-between p-4 pb-2">
                <div class="d-flex align-items-center gap-2">
                  <div class="card-icon-pill bg-success-subtle text-success">
                    <i class="bi bi-person-fill"></i>
                  </div>
                  <h5 class="fw-bold mb-0 text-dark card-title">Personal Information</h5>
                </div>
                <button class="btn btn-edit-section btn-sm d-flex align-items-center gap-1" @click="openEditSection('personal')">
                  <i class="bi bi-pencil"></i>
                  <span>Edit</span>
                </button>
              </div>

              <div class="card-body p-4 pt-3">
                <div class="kv-list">
                  <div class="kv-row d-flex justify-content-between py-2 border-bottom">
                    <span class="kv-label text-muted">Full Name</span>
                    <span class="kv-val fw-semibold text-dark">{{ personalForm.fullName || 'Kamal Perera' }}</span>
                  </div>
                  <div class="kv-row d-flex justify-content-between py-2 border-bottom">
                    <span class="kv-label text-muted">Email</span>
                    <span class="kv-val fw-semibold text-dark">{{ personalForm.email || 'kamal.perera@gmail.com' }}</span>
                  </div>
                  <div class="kv-row d-flex justify-content-between py-2 border-bottom">
                    <span class="kv-label text-muted">Phone</span>
                    <span class="kv-val fw-semibold text-dark">{{ personalForm.phone || '+94 77 123 4567' }}</span>
                  </div>
                  <div class="kv-row d-flex justify-content-between py-2 border-bottom">
                    <span class="kv-label text-muted">NIC Number</span>
                    <span class="kv-val fw-semibold text-dark">{{ farmForm.nicNumber || '199512345678' }}</span>
                  </div>
                  <div class="kv-row d-flex justify-content-between py-2">
                    <span class="kv-label text-muted">Date of Birth</span>
                    <span class="kv-val fw-semibold text-dark">{{ farmForm.dateOfBirth || '1990-05-12' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- CARD 2: Farm Information -->
          <div class="col-lg-6">
            <div class="card border-0 shadow-sm rounded-4 h-100 bg-white profile-card">
              <div class="card-header bg-transparent border-0 d-flex align-items-center justify-content-between p-4 pb-2">
                <div class="d-flex align-items-center gap-2">
                  <div class="card-icon-pill bg-success-subtle text-success">
                    <i class="bi bi-bank"></i>
                  </div>
                  <h5 class="fw-bold mb-0 text-dark card-title">Farm Information</h5>
                </div>
                <button class="btn btn-edit-section btn-sm d-flex align-items-center gap-1" @click="openEditSection('farm')">
                  <i class="bi bi-pencil"></i>
                  <span>Edit</span>
                </button>
              </div>

              <div class="card-body p-4 pt-3">
                <div class="kv-list">
                  <div class="kv-row d-flex justify-content-between py-2 border-bottom">
                    <span class="kv-label text-muted">Farm Name</span>
                    <span class="kv-val fw-semibold text-dark">{{ farmForm.farmName || 'Sunrise Valley Farm' }}</span>
                  </div>
                  <div class="kv-row d-flex justify-content-between py-2 border-bottom">
                    <span class="kv-label text-muted">Farm Size</span>
                    <span class="kv-val fw-semibold text-dark">{{ farmForm.farmSize || 12 }} {{ farmForm.farmSizeUnit || 'Acres' }}</span>
                  </div>
                  <div class="kv-row d-flex justify-content-between py-2 border-bottom">
                    <span class="kv-label text-muted">Primary Crops</span>
                    <span class="kv-val fw-semibold text-dark">{{ farmForm.primaryCrops || 'Tomatoes, Carrots, Lettuce' }}</span>
                  </div>
                  <div class="kv-row d-flex justify-content-between py-2 border-bottom">
                    <span class="kv-label text-muted">Farm Type</span>
                    <span class="kv-val fw-semibold text-dark">{{ farmForm.farmType || 'Organic' }}</span>
                  </div>
                  <div class="kv-row d-flex justify-content-between py-2">
                    <span class="kv-label text-muted">Established Year</span>
                    <span class="kv-val fw-semibold text-dark">{{ farmForm.establishedYear || 2018 }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- CARD 3: Profile Photo & Gallery -->
          <div class="col-lg-6">
            <div class="card border-0 shadow-sm rounded-4 h-100 bg-white profile-card">
              <div class="card-header bg-transparent border-0 d-flex align-items-center justify-content-between p-4 pb-2">
                <div class="d-flex align-items-center gap-2">
                  <div class="card-icon-pill bg-success-subtle text-success">
                    <i class="bi bi-images"></i>
                  </div>
                  <h5 class="fw-bold mb-0 text-dark card-title">Profile Photo</h5>
                </div>
              </div>

              <div class="card-body p-4 pt-3">
                <!-- Gallery Row matching the exact UI -->
                <div class="photo-preview-grid mb-3">
                  <!-- Farmer Portrait Box -->
                  <div class="portrait-preview-box rounded-3 overflow-hidden shadow-2xs">
                    <img
                      :src="personalForm.profileImageUrl || defaultAvatar"
                      alt="Farmer Main Photo"
                      class="w-100 h-100 object-fit-cover"
                    />
                  </div>

                  <!-- 3 Landscape Thumbnails -->
                  <div class="landscape-thumb-box rounded-3 overflow-hidden shadow-2xs">
                    <img
                      :src="photoList[0] || defaultGallery[0]"
                      alt="Farm Field 1"
                      class="w-100 h-100 object-fit-cover"
                    />
                  </div>

                  <div class="landscape-thumb-box rounded-3 overflow-hidden shadow-2xs">
                    <img
                      :src="photoList[1] || defaultGallery[1]"
                      alt="Farm Field 2"
                      class="w-100 h-100 object-fit-cover"
                    />
                  </div>

                  <!-- Last Thumbnail with +3 Badge -->
                  <div class="landscape-thumb-box rounded-3 overflow-hidden shadow-2xs position-relative cursor-pointer" @click="showPhotoModal = true">
                    <img
                      :src="photoList[2] || defaultGallery[2]"
                      alt="Farm Field 3"
                      class="w-100 h-100 object-fit-cover"
                    />
                    <div class="more-photos-overlay position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center text-white fw-bold">
                      +{{ Math.max(photoList.length - 2, 3) }}
                    </div>
                  </div>
                </div>

                <!-- Change Photo Button & Meta -->
                <div class="d-flex flex-column flex-sm-row align-items-sm-center justify-content-between gap-2 mt-3 pt-2">
                  <button class="btn btn-outline-success btn-change-photo d-flex align-items-center justify-content-center gap-2" @click="showPhotoModal = true">
                    <i class="bi bi-upload"></i>
                    <span>Change Photo</span>
                  </button>
                  <span class="text-muted small text-sm-end">JPG, PNG (Max 5MB)</span>
                </div>
              </div>
            </div>
          </div>

          <!-- CARD 4: About My Farm -->
          <div class="col-lg-6">
            <div class="card border-0 shadow-sm rounded-4 h-100 bg-white profile-card">
              <div class="card-header bg-transparent border-0 d-flex align-items-center justify-content-between p-4 pb-2">
                <div class="d-flex align-items-center gap-2">
                  <div class="card-icon-pill bg-success-subtle text-success">
                    <i class="bi bi-file-earmark-text"></i>
                  </div>
                  <h5 class="fw-bold mb-0 text-dark card-title">About My Farm</h5>
                </div>
                <button class="btn btn-edit-section btn-sm d-flex align-items-center gap-1" @click="openEditSection('about')">
                  <i class="bi bi-pencil"></i>
                  <span>Edit</span>
                </button>
              </div>

              <div class="card-body p-4 pt-3">
                <p class="about-text text-secondary mb-3 lh-base">
                  {{ farmForm.farmerDetails || 'At Sunrise Valley Farm, we are committed to sustainable farming practices and providing fresh, healthy produce to local communities. We use organic methods and take pride in maintaining high quality standards in everything we grow.' }}
                </p>

                <!-- Highlight Quote Box -->
                <div class="quote-callout rounded-3 p-3 d-flex align-items-center gap-3">
                  <div class="quote-icon-circle flex-shrink-0">
                    <i class="bi bi-flower1 text-success fs-5"></i>
                  </div>
                  <div class="quote-content fw-semibold text-success-emphasis">
                    "{{ farmForm.quote || 'Healthy food, healthy people, healthier planet.' }}"
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- CARD 5: Location -->
          <div class="col-lg-6">
            <div class="card border-0 shadow-sm rounded-4 h-100 bg-white profile-card">
              <div class="card-header bg-transparent border-0 d-flex align-items-center justify-content-between p-4 pb-2">
                <div class="d-flex align-items-center gap-2">
                  <div class="card-icon-pill bg-success-subtle text-success">
                    <i class="bi bi-geo-alt-fill"></i>
                  </div>
                  <h5 class="fw-bold mb-0 text-dark card-title">Location</h5>
                </div>
                <button class="btn btn-edit-section btn-sm d-flex align-items-center gap-1" @click="openEditSection('location')">
                  <i class="bi bi-pencil"></i>
                  <span>Edit</span>
                </button>
              </div>

              <div class="card-body p-4 pt-3">
                <div class="row g-3 align-items-center">
                  <!-- Text Details -->
                  <div class="col-sm-7">
                    <div class="kv-list">
                      <div class="kv-row py-1 border-bottom d-flex justify-content-between">
                        <span class="kv-label text-muted small">Address</span>
                        <span class="kv-val fw-semibold text-dark small text-end">{{ farmForm.farmAddress || '123/4, Aluvihare Road, Matale' }}</span>
                      </div>
                      <div class="kv-row py-1 border-bottom d-flex justify-content-between">
                        <span class="kv-label text-muted small">District</span>
                        <span class="kv-val fw-semibold text-dark small">{{ farmForm.district || 'Matale' }}</span>
                      </div>
                      <div class="kv-row py-1 border-bottom d-flex justify-content-between">
                        <span class="kv-label text-muted small">Province</span>
                        <span class="kv-val fw-semibold text-dark small">{{ farmForm.province || 'Central' }} Province</span>
                      </div>
                      <div class="kv-row py-1 border-bottom d-flex justify-content-between">
                        <span class="kv-label text-muted small">Latitude</span>
                        <span class="kv-val font-monospace text-dark small">{{ farmForm.latitude || 7.4673 }}</span>
                      </div>
                      <div class="kv-row py-1 d-flex justify-content-between">
                        <span class="kv-label text-muted small">Longitude</span>
                        <span class="kv-val font-monospace text-dark small">{{ farmForm.longitude || 80.6235 }}</span>
                      </div>
                    </div>
                  </div>

                  <!-- Map Preview Box -->
                  <div class="col-sm-5">
                    <div class="map-preview-card rounded-3 overflow-hidden border shadow-2xs position-relative text-center p-3 bg-light">
                      <div class="map-bg-pattern mb-2 position-relative">
                        <div class="map-pin-pulse">
                          <i class="bi bi-geo-alt-fill text-success fs-2"></i>
                        </div>
                      </div>
                      <button class="btn btn-outline-success btn-sm w-100 mt-2 d-flex align-items-center justify-content-center gap-1" @click="showMapModal = true">
                        <i class="bi bi-map"></i>
                        <span>View on Map</span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- CARD 6: Experience & Skills -->
          <div class="col-lg-6">
            <div class="card border-0 shadow-sm rounded-4 h-100 bg-white profile-card">
              <div class="card-header bg-transparent border-0 d-flex align-items-center justify-content-between p-4 pb-2">
                <div class="d-flex align-items-center gap-2">
                  <div class="card-icon-pill bg-success-subtle text-success">
                    <i class="bi bi-trophy-fill"></i>
                  </div>
                  <h5 class="fw-bold mb-0 text-dark card-title">Experience & Skills</h5>
                </div>
                <button class="btn btn-edit-section btn-sm d-flex align-items-center gap-1" @click="openEditSection('experience')">
                  <i class="bi bi-pencil"></i>
                  <span>Edit</span>
                </button>
              </div>

              <div class="card-body p-4 pt-3">
                <div class="kv-list">
                  <div class="kv-row d-flex justify-content-between py-2 border-bottom">
                    <span class="kv-label text-muted">Farming Experience</span>
                    <span class="kv-val fw-semibold text-dark">{{ farmForm.farmingExperience || 5 }}+ years</span>
                  </div>
                  <div class="kv-row d-flex justify-content-between py-2 border-bottom">
                    <span class="kv-label text-muted">Specialization</span>
                    <span class="kv-val fw-semibold text-dark">{{ farmForm.specialization || 'Vegetable Cultivation' }}</span>
                  </div>
                  <div class="kv-row d-flex justify-content-between py-2 border-bottom">
                    <span class="kv-label text-muted">Farming Methods</span>
                    <span class="kv-val fw-semibold text-dark text-end">{{ farmForm.farmingMethodsList || 'Organic, Mulching, Drip Irrigation' }}</span>
                  </div>
                  <div class="kv-row d-flex justify-content-between py-2 border-bottom">
                    <span class="kv-label text-muted">Certifications</span>
                    <span class="kv-val fw-semibold text-success text-end">{{ farmForm.certifications || 'Organic Certified' }}</span>
                  </div>
                  <div class="kv-row d-flex justify-content-between py-2">
                    <span class="kv-label text-muted">Skills</span>
                    <span class="kv-val fw-semibold text-dark text-end">{{ farmForm.skills || 'Crop Management, Soil Conservation, Pest Control' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ========================================================
             BOTTOM ACTION: ARCHIVE PROFILE BAR
             ======================================================== -->
        <div class="archive-bar mt-4 pt-3 d-flex flex-column flex-sm-row align-items-sm-center gap-3">
          <button
            class="btn btn-outline-danger btn-archive-action d-flex align-items-center gap-2 text-nowrap px-3 py-2"
            @click="showArchiveConfirmModal = true"
          >
            <i class="bi" :class="farmForm.isArchived ? 'bi-arrow-counterclockwise' : 'bi-trash'"></i>
            <span>{{ farmForm.isArchived ? 'Restore Profile' : 'Archive Profile' }}</span>
          </button>
          <div class="text-muted small">
            Temporarily hide your profile from buyers. You can restore it anytime.
          </div>
        </div>

      </div>
    </div>

    <!-- ========================================================
         MODAL: EDIT PROFILE / SECTIONS
         ======================================================== -->
    <div v-if="showEditModal" class="modal fade show d-block modal-backdrop-custom" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 rounded-4 shadow-lg overflow-hidden">
          <div class="modal-header bg-light border-0 px-4 py-3">
            <h5 class="modal-title fw-bold text-dark d-flex align-items-center gap-2">
              <i class="bi bi-pencil-square text-success"></i>
              <span v-if="activeEditSection === 'all'">Edit Complete Farmer Profile</span>
              <span v-else-if="activeEditSection === 'personal'">Edit Personal Information</span>
              <span v-else-if="activeEditSection === 'farm'">Edit Farm Details</span>
              <span v-else-if="activeEditSection === 'about'">Edit About My Farm</span>
              <span v-else-if="activeEditSection === 'location'">Edit Location Coordinates</span>
              <span v-else-if="activeEditSection === 'experience'">Edit Experience & Skills</span>
            </h5>
            <button type="button" class="btn-close" @click="showEditModal = false"></button>
          </div>

          <div class="modal-body p-4" style="max-height: 75vh; overflow-y: auto;">
            <!-- PERSONAL INFORMATION SECTION -->
            <div v-if="activeEditSection === 'personal' || activeEditSection === 'all'" class="mb-4">
              <h6 class="fw-bold text-success border-bottom pb-2 mb-3">
                <i class="bi bi-person me-1"></i> Personal Details
              </h6>
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Full Name *</label>
                  <input type="text" class="form-control" v-model="editBuffer.fullName" required />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Contact Phone</label>
                  <input type="tel" class="form-control" v-model="editBuffer.phone" />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">NIC Number</label>
                  <input type="text" class="form-control" v-model="editBuffer.nicNumber" placeholder="e.g. 199512345678" />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Date of Birth</label>
                  <input type="date" class="form-control" v-model="editBuffer.dateOfBirth" />
                </div>
              </div>
            </div>

            <!-- FARM INFORMATION SECTION -->
            <div v-if="activeEditSection === 'farm' || activeEditSection === 'all'" class="mb-4">
              <h6 class="fw-bold text-success border-bottom pb-2 mb-3">
                <i class="bi bi-bank me-1"></i> Farm Information
              </h6>
              <div class="row g-3">
                <div class="col-md-8">
                  <label class="form-label small fw-semibold">Farm Name *</label>
                  <input type="text" class="form-control" v-model="editBuffer.farmName" required />
                </div>
                <div class="col-md-4">
                  <label class="form-label small fw-semibold">Established Year</label>
                  <input type="number" min="1900" max="2099" class="form-control" v-model="editBuffer.establishedYear" />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Farm Size</label>
                  <div class="input-group">
                    <input type="number" step="0.1" class="form-control" v-model.number="editBuffer.farmSize" />
                    <select class="form-select" style="max-width: 120px;" v-model="editBuffer.farmSizeUnit">
                      <option value="Acres">Acres</option>
                      <option value="Hectares">Hectares</option>
                      <option value="Perches">Perches</option>
                    </select>
                  </div>
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Farm Type</label>
                  <select class="form-select" v-model="editBuffer.farmType">
                    <option value="Organic">Organic</option>
                    <option value="Traditional">Traditional</option>
                    <option value="Hydroponic">Hydroponic</option>
                    <option value="Greenhouse">Greenhouse</option>
                    <option value="Conventional">Conventional</option>
                  </select>
                </div>
                <div class="col-12">
                  <label class="form-label small fw-semibold">Primary Crops</label>
                  <input type="text" class="form-control" v-model="editBuffer.primaryCrops" placeholder="Tomatoes, Carrots, Lettuce" />
                </div>
              </div>
            </div>

            <!-- ABOUT SECTION -->
            <div v-if="activeEditSection === 'about' || activeEditSection === 'all'" class="mb-4">
              <h6 class="fw-bold text-success border-bottom pb-2 mb-3">
                <i class="bi bi-file-earmark-text me-1"></i> Story & Motto
              </h6>
              <div class="row g-3">
                <div class="col-12">
                  <label class="form-label small fw-semibold">Farm Motto / Tagline</label>
                  <input type="text" class="form-control" v-model="editBuffer.tagline" placeholder="e.g. Growing healthy food for a better tomorrow" />
                </div>
                <div class="col-12">
                  <label class="form-label small fw-semibold">Quote Callout</label>
                  <input type="text" class="form-control" v-model="editBuffer.quote" placeholder="e.g. Healthy food, healthy people, healthier planet." />
                </div>
                <div class="col-12">
                  <label class="form-label small fw-semibold">About My Farm Description</label>
                  <textarea class="form-control" rows="4" v-model="editBuffer.farmerDetails" placeholder="Describe your farm background, heritage, and values..."></textarea>
                </div>
              </div>
            </div>

            <!-- LOCATION SECTION -->
            <div v-if="activeEditSection === 'location' || activeEditSection === 'all'" class="mb-4">
              <h6 class="fw-bold text-success border-bottom pb-2 mb-3">
                <i class="bi bi-geo-alt me-1"></i> Farm Location
              </h6>
              <div class="row g-3">
                <div class="col-12">
                  <label class="form-label small fw-semibold">Physical Street Address</label>
                  <input type="text" class="form-control" v-model="editBuffer.farmAddress" placeholder="e.g. 123/4, Aluvihare Road, Matale" />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Province</label>
                  <select class="form-select" v-model="editBuffer.province">
                    <option v-for="p in PROVINCES" :key="p" :value="p">{{ p }} Province</option>
                  </select>
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">District</label>
                  <select class="form-select" v-model="editBuffer.district">
                    <option v-for="d in getDistrictsForProvince(editBuffer.province)" :key="d" :value="d">{{ d }}</option>
                  </select>
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Latitude</label>
                  <input type="number" step="0.000001" class="form-control font-monospace" v-model.number="editBuffer.latitude" />
                </div>
                <div class="col-md-6">
                  <label class="form-label small fw-semibold">Longitude</label>
                  <input type="number" step="0.000001" class="form-control font-monospace" v-model.number="editBuffer.longitude" />
                </div>
              </div>
            </div>

            <!-- EXPERIENCE SECTION -->
            <div v-if="activeEditSection === 'experience' || activeEditSection === 'all'" class="mb-4">
              <h6 class="fw-bold text-success border-bottom pb-2 mb-3">
                <i class="bi bi-trophy me-1"></i> Experience & Skills
              </h6>
              <div class="row g-3">
                <div class="col-md-4">
                  <label class="form-label small fw-semibold">Farming Experience (Years)</label>
                  <input type="number" min="0" class="form-control" v-model.number="editBuffer.farmingExperience" />
                </div>
                <div class="col-md-8">
                  <label class="form-label small fw-semibold">Specialization</label>
                  <input type="text" class="form-control" v-model="editBuffer.specialization" placeholder="e.g. Vegetable Cultivation" />
                </div>
                <div class="col-12">
                  <label class="form-label small fw-semibold">Farming Methods</label>
                  <input type="text" class="form-control" v-model="editBuffer.farmingMethodsList" placeholder="e.g. Organic, Mulching, Drip Irrigation" />
                </div>
                <div class="col-12">
                  <label class="form-label small fw-semibold">Certifications</label>
                  <input type="text" class="form-control" v-model="editBuffer.certifications" placeholder="e.g. Organic Certified, SL-GAP" />
                </div>
                <div class="col-12">
                  <label class="form-label small fw-semibold">Skills</label>
                  <input type="text" class="form-control" v-model="editBuffer.skills" placeholder="e.g. Crop Management, Soil Conservation, Pest Control" />
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer bg-light border-0 px-4 py-3">
            <button type="button" class="btn btn-light" @click="showEditModal = false">Cancel</button>
            <button type="button" class="btn btn-farmora-primary d-flex align-items-center gap-2" @click="saveEditedSection" :disabled="saving">
              <span v-if="saving" class="spinner-border spinner-border-sm"></span>
              <i v-else class="bi bi-check2"></i>
              <span>Save Changes</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================================
         MODAL: CHANGE PHOTO & GALLERY
         ======================================================== -->
    <div v-if="showPhotoModal" class="modal fade show d-block modal-backdrop-custom" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 rounded-4 shadow-lg overflow-hidden">
          <div class="modal-header bg-light border-0 px-4 py-3">
            <h5 class="modal-title fw-bold text-dark d-flex align-items-center gap-2">
              <i class="bi bi-camera text-success"></i>
              <span>Update Profile Photo & Farm Gallery</span>
            </h5>
            <button type="button" class="btn-close" @click="showPhotoModal = false"></button>
          </div>

          <div class="modal-body p-4">
            <!-- Profile Avatar Update -->
            <div class="mb-4 pb-3 border-bottom">
              <h6 class="fw-bold text-dark mb-2">Farmer Avatar Image</h6>
              <div class="d-flex align-items-center gap-3">
                <img :src="personalForm.profileImageUrl || defaultAvatar" class="rounded-circle avatar-modal-preview shadow-sm" alt="Preview" />
                <div class="flex-grow-1">
                  <label class="form-label small fw-semibold text-muted">Avatar Image URL</label>
                  <div class="input-group">
                    <input type="url" class="form-control" v-model="personalForm.profileImageUrl" placeholder="https://images.unsplash.com/..." />
                    <button class="btn btn-success" type="button" @click="updateProfilePhoto(personalForm.profileImageUrl)">Save Avatar</button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Cover Image Update -->
            <div class="mb-4 pb-3 border-bottom">
              <h6 class="fw-bold text-dark mb-2">Cover Banner Image</h6>
              <div class="input-group">
                <input type="url" class="form-control" v-model="farmForm.coverImageUrl" placeholder="Cover image URL..." />
                <button class="btn btn-outline-success" type="button" @click="saveEditedSection">Save Cover</button>
              </div>
            </div>

            <!-- Farm Photo Gallery -->
            <div>
              <h6 class="fw-bold text-dark mb-2">Farm Gallery Photos ({{ photoList.length }})</h6>
              <div class="input-group mb-3">
                <input
                  type="url"
                  class="form-control"
                  v-model="newPhotoInput"
                  placeholder="Paste image URL (e.g. harvest, field, produce)..."
                  @keyup.enter="addPhotoToGallery"
                />
                <button class="btn btn-success" type="button" @click="addPhotoToGallery">
                  <i class="bi bi-plus-lg me-1"></i> Add to Gallery
                </button>
              </div>

              <!-- Gallery Grid -->
              <div class="row g-2">
                <div v-for="(img, idx) in photoList" :key="idx" class="col-4 col-md-3">
                  <div class="position-relative rounded-2 overflow-hidden border shadow-2xs group-hover-box" style="height: 90px;">
                    <img :src="img" class="w-100 h-100 object-fit-cover" alt="Gallery item" />
                    <button
                      class="btn btn-danger btn-sm position-absolute top-0 end-0 m-1 p-0 rounded-circle d-flex align-items-center justify-content-center"
                      style="width: 22px; height: 22px;"
                      @click="removeGalleryPhoto(idx)"
                      title="Remove"
                    >
                      <i class="bi bi-x"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer bg-light border-0 px-4 py-3">
            <button type="button" class="btn btn-light" @click="showPhotoModal = false">Close</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================================
         MODAL: GPS MAP PICKER
         ======================================================== -->
    <div v-if="showMapModal" class="modal fade show d-block modal-backdrop-custom" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 rounded-4 shadow-lg overflow-hidden">
          <div class="modal-header bg-light border-0 px-4 py-3">
            <h5 class="modal-title fw-bold text-dark d-flex align-items-center gap-2">
              <i class="bi bi-geo-alt-fill text-success"></i>
              <span>Farm Gate GPS Pin Placement</span>
            </h5>
            <button type="button" class="btn-close" @click="showMapModal = false"></button>
          </div>

          <div class="modal-body p-4">
            <p class="text-muted small mb-3">
              Click anywhere on the map or drag the marker to pinpoint the exact location of your farm gate.
            </p>
            <div class="rounded-3 overflow-hidden border mb-3">
              <GpsMapPicker
                :latitude="farmForm.latitude"
                :longitude="farmForm.longitude"
                @update:location="handleGpsUpdate"
                height="380px"
              />
            </div>
            <div class="d-flex align-items-center justify-content-between p-2 bg-light rounded">
              <span class="small text-muted">Selected Coordinates:</span>
              <span class="fw-bold font-monospace text-dark small">{{ farmForm.latitude }}, {{ farmForm.longitude }}</span>
            </div>
          </div>

          <div class="modal-footer bg-light border-0 px-4 py-3">
            <button type="button" class="btn btn-light" @click="showMapModal = false">Cancel</button>
            <button type="button" class="btn btn-farmora-primary" @click="saveMapLocation">
              <i class="bi bi-check2 me-1"></i> Save Coordinates
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================================
         MODAL: CONFIRM ARCHIVE PROFILE
         ======================================================== -->
    <div v-if="showArchiveConfirmModal" class="modal fade show d-block modal-backdrop-custom" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 rounded-4 shadow-lg overflow-hidden">
          <div class="modal-header border-0 px-4 pt-4 pb-2">
            <h5 class="modal-title fw-bold text-dark d-flex align-items-center gap-2">
              <i class="bi bi-exclamation-triangle-fill text-warning"></i>
              <span>{{ farmForm.isArchived ? 'Restore Farmer Profile' : 'Archive Farmer Profile' }}</span>
            </h5>
            <button type="button" class="btn-close" @click="showArchiveConfirmModal = false"></button>
          </div>

          <div class="modal-body px-4 py-3">
            <p class="text-secondary" v-if="!farmForm.isArchived">
              Are you sure you want to archive your farm profile?
              Archiving will <strong>temporarily hide your farm from wholesale buyers</strong> in the marketplace.
              Your data remains completely safe, and you can restore it anytime with a single click.
            </p>
            <p class="text-secondary" v-else>
              Are you ready to restore your farm profile? Your profile and produce will immediately become visible to wholesale buyers across Sri Lanka.
            </p>
          </div>

          <div class="modal-footer border-0 px-4 pb-4 pt-2">
            <button type="button" class="btn btn-light" @click="showArchiveConfirmModal = false">Cancel</button>
            <button
              type="button"
              class="btn"
              :class="farmForm.isArchived ? 'btn-success' : 'btn-danger'"
              @click="toggleArchive"
              :disabled="archiving"
            >
              <span v-if="archiving" class="spinner-border spinner-border-sm me-1"></span>
              <span>{{ farmForm.isArchived ? 'Confirm Restore' : 'Confirm Archive' }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================================
         MODAL: DELETE / CLEAR SECTION CONFIRMATION
         ======================================================== -->
    <div v-if="showDeleteSectionModal" class="modal fade show d-block modal-backdrop-custom" tabindex="-1">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 rounded-4 shadow-lg overflow-hidden">
          <div class="modal-header border-0 px-4 pt-4 pb-2">
            <h5 class="modal-title fw-bold text-danger d-flex align-items-center gap-2">
              <i class="bi bi-trash3-fill"></i>
              <span>Clear Section Content</span>
            </h5>
            <button type="button" class="btn-close" @click="showDeleteSectionModal = false"></button>
          </div>

          <div class="modal-body px-4 py-3">
            <p class="text-secondary">
              Are you sure you want to clear the content of <strong>{{ sectionToDelete }}</strong>?
              This action will reset the section.
            </p>
          </div>

          <div class="modal-footer border-0 px-4 pb-4 pt-2">
            <button type="button" class="btn btn-light" @click="showDeleteSectionModal = false">Cancel</button>
            <button type="button" class="btn btn-danger" @click="executeDeleteSection">Clear Content</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* Page & Theme Custom Colors */
.farmer-profile-page {
  background-color: #f8faf8;
  min-height: 90vh;
}

.page-title {
  color: #111827;
  letter-spacing: -0.02em;
}

/* Primary Farmora Button */
.btn-farmora-primary {
  background-color: #1b4332;
  color: #ffffff;
  border: 1px solid #1b4332;
  font-weight: 600;
  border-radius: 8px;
  padding: 0.55rem 1.25rem;
  transition: all 0.2s ease;
}

.btn-farmora-primary:hover {
  background-color: #143527;
  border-color: #143527;
  color: #ffffff;
  transform: translateY(-1px);
}

.btn-icon-more {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  border-color: #e5e7eb;
}

/* Hero Banner Card */
.profile-hero-card {
  border: 1px solid #edf2ed;
}

.hero-cover-container {
  height: 180px;
  background-color: #2d6a4f;
}

.hero-cover-img {
  filter: brightness(0.95);
}

.cover-gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.1) 0%, rgba(0, 0, 0, 0.3) 100%);
}

.btn-cover-edit {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(4px);
  border: none;
  font-weight: 500;
}

/* Avatar Group */
.avatar-wrapper {
  margin-top: -55px;
  width: 110px;
  height: 110px;
}

.avatar-img {
  width: 110px;
  height: 110px;
  object-fit: cover;
  border: 4px solid #ffffff;
  background: #ffffff;
}

.avatar-modal-preview {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border: 3px solid #2d6a4f;
}

.btn-camera-overlay {
  width: 32px;
  height: 32px;
  background-color: #111827;
  border: 2px solid #ffffff;
  cursor: pointer;
  transition: transform 0.2s;
}

.btn-camera-overlay:hover {
  transform: scale(1.1);
  background-color: #1b4332;
}

/* Verified Pill */
.verified-pill {
  background-color: #d1e7dd;
  color: #0f5132;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  padding: 0.35rem 0.65rem;
  border-radius: 20px;
}

/* Stats Pill Cards */
.stat-pill-card {
  background-color: #ffffff;
  border: 1px solid #edf2ed;
  min-width: 130px;
}

.stat-icon-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.bg-emerald-light {
  background-color: #e8f5e9;
}

.bg-mint-light {
  background-color: #e0f2f1;
}

.bg-amber-light {
  background-color: #fff8e1;
}

.stat-label {
  font-size: 0.72rem;
}

/* 6 Grid Cards Styling */
.profile-card {
  border: 1px solid #edf2ed;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.profile-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05) !important;
}

.card-title {
  font-size: 1.05rem;
}

.card-icon-pill {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
}

.btn-edit-section {
  background-color: #f1f8f3;
  color: #1b4332;
  border: none;
  font-weight: 600;
  border-radius: 6px;
  padding: 0.3rem 0.75rem;
  font-size: 0.82rem;
  transition: all 0.2s ease;
}

.btn-edit-section:hover {
  background-color: #1b4332;
  color: #ffffff;
}

/* Key Value Pairs */
.kv-label {
  font-size: 0.88rem;
}

.kv-val {
  font-size: 0.88rem;
}

/* Photo Preview Grid */
.photo-preview-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr 1fr;
  gap: 8px;
  height: 95px;
}

.portrait-preview-box,
.landscape-thumb-box {
  background-color: #f3f4f6;
  height: 100%;
}

.more-photos-overlay {
  background: rgba(0, 0, 0, 0.55);
  font-size: 1.1rem;
  backdrop-filter: blur(2px);
}

.btn-change-photo {
  border-radius: 8px;
  font-size: 0.84rem;
  font-weight: 600;
  padding: 0.4rem 1rem;
}

/* Quote Callout */
.quote-callout {
  background-color: #edf7f0;
  border-left: 4px solid #2d6a4f;
}

.quote-icon-circle {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background-color: rgba(45, 106, 79, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}

.quote-content {
  font-size: 0.92rem;
  font-style: italic;
}

/* Location Preview */
.map-preview-card {
  background-color: #f8faf8;
}

.map-bg-pattern {
  height: 70px;
  background-image: radial-gradient(#d1e7dd 1.5px, transparent 1.5px);
  background-size: 10px 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.map-pin-pulse {
  animation: pulse-pin 2s infinite ease-in-out;
}

@keyframes pulse-pin {
  0% { transform: scale(1); }
  50% { transform: scale(1.15); }
  100% { transform: scale(1); }
}

/* Archive Bar */
.archive-bar {
  border-top: 1px solid #e5e7eb;
}

.btn-archive-action {
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.86rem;
}

/* Modal Backdrop */
.modal-backdrop-custom {
  background-color: rgba(17, 24, 39, 0.6);
  backdrop-filter: blur(3px);
}

.shadow-2xs {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.cursor-pointer {
  cursor: pointer;
}
</style>

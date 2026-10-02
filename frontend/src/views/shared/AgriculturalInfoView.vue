<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { agriInfoService } from '../../services/agriInfoService'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'

const authStore = useAuthStore()
const currentUser = computed(() => authStore.user)
const canPost = computed(() => authStore.role === 'FARMER' || authStore.role === 'ADMIN')

const activeTab = ref('ALL') // 'ALL', 'ADVICE', 'CROP_CALENDAR', 'WEATHER_ALERT', 'GOV_ANNOUNCEMENT'
const selectedProvince = ref('')
const posts = ref([])
const loading = ref(true)

// Create modal
const showCreateModal = ref(false)
const createLoading = ref(false)
const postForm = ref({
  title: '',
  category: 'ADVICE',
  content: '',
  province: 'All Island',
  targetCrop: '',
  season: 'Year Round',
  isPinned: false,
  sourceUrl: ''
})

const provinces = [
  'All Island',
  'Central Province',
  'Western Province',
  'North Western Province',
  'North Central Province',
  'Southern Province',
  'Northern Province',
  'Eastern Province',
  'Uva Province',
  'Sabaragamuwa Province'
]

const loadPosts = async () => {
  loading.value = true
  try {
    const categoryParam = activeTab.value === 'ALL' ? null : activeTab.value
    const provinceParam = selectedProvince.value === 'All Island' ? null : selectedProvince.value
    const data = await agriInfoService.getPosts(categoryParam, provinceParam)
    posts.value = data || []
  } catch (err) {
    console.error('Failed to load agri info posts', err)
  } finally {
    loading.value = false
  }
}

const handleTabChange = (tab) => {
  activeTab.value = tab
  loadPosts()
}

const handleProvinceFilter = () => {
  loadPosts()
}

const handleCreatePost = async () => {
  if (!postForm.value.title.trim() || !postForm.value.content.trim()) return
  createLoading.value = true
  try {
    await agriInfoService.createPost(postForm.value)
    showCreateModal.value = false
    postForm.value = {
      title: '',
      category: 'ADVICE',
      content: '',
      province: 'All Island',
      targetCrop: '',
      season: 'Year Round',
      isPinned: false,
      sourceUrl: ''
    }
    loadPosts()
  } catch (err) {
    console.error('Failed to create post', err)
  } finally {
    createLoading.value = false
  }
}

const handleDeletePost = async (id) => {
  if (!confirm('Are you sure you want to delete this advisory?')) return
  try {
    await agriInfoService.deletePost(id)
    posts.value = posts.value.filter(p => p.id !== id)
  } catch (err) {
    console.error('Failed to delete post', err)
  }
}

const getCategoryBadgeClass = (category) => {
  switch (category) {
    case 'WEATHER_ALERT': return 'bg-danger text-white'
    case 'GOV_ANNOUNCEMENT': return 'bg-primary text-white'
    case 'CROP_CALENDAR': return 'bg-success text-white'
    case 'ADVICE': return 'bg-warning text-dark'
    case 'COMMUNITY_FORUM': return 'bg-info text-dark'
    case 'EDUCATION': return 'bg-purple text-white'
    default: return 'bg-secondary text-white'
  }
}

const getCategoryIcon = (category) => {
  switch (category) {
    case 'WEATHER_ALERT': return 'bi-cloud-lightning-rain-fill'
    case 'GOV_ANNOUNCEMENT': return 'bi-bank2'
    case 'CROP_CALENDAR': return 'bi-calendar-week-fill'
    case 'ADVICE': return 'bi-lightbulb-fill'
    case 'COMMUNITY_FORUM': return 'bi-chat-quote-fill'
    case 'EDUCATION': return 'bi-mortarboard-fill'
    default: return 'bi-info-circle-fill'
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<template>
  <div class="container py-4">
    <!-- Header Banner -->
    <div class="card border-0 shadow-sm rounded-4 overflow-hidden mb-4 bg-gradient-agri text-white p-4 p-lg-5">
      <div class="row align-items-center">
        <div class="col-lg-8">
          <span class="badge bg-white text-success px-3 py-1 rounded-pill fw-bold mb-2">
            🌱 Sri Lanka Agrarian Knowledge Portal
          </span>
          <h2 class="fw-bold mb-2">Agricultural Information System</h2>
          <p class="lead mb-3" style="font-size: 1.05rem;">
            Seasonal crop cultivation schedules, regional monsoon advisories, pest control tips, and government agricultural grants.
          </p>
          <div class="d-flex flex-wrap gap-2">
            <button
              v-if="canPost"
              class="btn btn-light btn-sm fw-bold rounded-pill px-3 shadow-sm text-success"
              @click="showCreateModal = true"
            >
              <i class="bi bi-pencil-square me-1"></i> Publish Advice / Advisory
            </button>
            <router-link to="/daily-harvest" class="btn btn-outline-light btn-sm rounded-pill px-3">
              <i class="bi bi-basket2-fill me-1"></i> View Live Harvests
            </router-link>
          </div>
        </div>
        <div class="col-lg-4 text-center text-lg-end d-none d-lg-block">
          <i class="bi bi-tree display-1 opacity-50"></i>
        </div>
      </div>
    </div>

    <!-- Filter & Category Tabs -->
    <div class="card border-0 shadow-sm rounded-4 p-3 mb-4 bg-white">
      <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
        <!-- Tab Pills -->
        <div class="nav nav-pills flex-nowrap overflow-x-auto gap-1">
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-semibold text-nowrap"
            :class="{ active: activeTab === 'ALL' }"
            @click="handleTabChange('ALL')"
          >
            All Updates
          </button>
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-semibold text-nowrap"
            :class="{ active: activeTab === 'CROP_CALENDAR' }"
            @click="handleTabChange('CROP_CALENDAR')"
          >
            <i class="bi bi-calendar-check me-1"></i> Crop Calendar
          </button>
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-semibold text-nowrap"
            :class="{ active: activeTab === 'WEATHER_ALERT' }"
            @click="handleTabChange('WEATHER_ALERT')"
          >
            <i class="bi bi-cloud-sun me-1"></i> Weather Alerts
          </button>
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-semibold text-nowrap"
            :class="{ active: activeTab === 'GOV_ANNOUNCEMENT' }"
            @click="handleTabChange('GOV_ANNOUNCEMENT')"
          >
            <i class="bi bi-bank me-1"></i> Government Schemes
          </button>
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-semibold text-nowrap"
            :class="{ active: activeTab === 'ADVICE' }"
            @click="handleTabChange('ADVICE')"
          >
            <i class="bi bi-lightbulb me-1"></i> Farming Advice
          </button>
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-semibold text-nowrap"
            :class="{ active: activeTab === 'COMMUNITY_FORUM' }"
            @click="handleTabChange('COMMUNITY_FORUM')"
          >
            <i class="bi bi-chat-quote me-1"></i> Community Forum
          </button>
          <button
            class="nav-link rounded-pill px-3 py-1 small fw-semibold text-nowrap"
            :class="{ active: activeTab === 'EDUCATION' }"
            @click="handleTabChange('EDUCATION')"
          >
            <i class="bi bi-mortarboard me-1"></i> Farmer Education
          </button>
        </div>

        <!-- Province Filter Dropdown -->
        <div class="d-flex align-items-center gap-2 flex-shrink-0">
          <label class="small text-muted mb-0 fw-semibold">Province:</label>
          <select
            v-model="selectedProvince"
            class="form-select form-select-sm rounded-pill"
            style="min-width: 170px;"
            @change="handleProvinceFilter"
          >
            <option value="">All Regions</option>
            <option v-for="prov in provinces" :key="prov" :value="prov">{{ prov }}</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Posts Feed Grid -->
    <LoadingSpinner v-if="loading" message="Loading agricultural advisories..." />

    <div v-else-if="posts.length > 0" class="row g-4">
      <div v-for="post in posts" :key="post.id" class="col-md-6 col-lg-6">
        <div class="card h-100 border-0 shadow-sm rounded-4 overflow-hidden bg-white p-4 d-flex flex-column position-relative">
          
          <!-- Pinned Ribbon -->
          <div v-if="post.isPinned" class="position-absolute top-0 end-0 m-3">
            <span class="badge bg-danger rounded-pill px-2 py-1 small">
              <i class="bi bi-pin-fill me-1"></i> Pinned
            </span>
          </div>

          <!-- Category and Meta -->
          <div class="d-flex align-items-center gap-2 mb-3">
            <span class="badge rounded-pill px-3 py-1 small fw-bold" :class="getCategoryBadgeClass(post.category)">
              <i class="bi me-1" :class="getCategoryIcon(post.category)"></i>
              {{ post.category.replace('_', ' ') }}
            </span>
            <span class="badge bg-light text-secondary border rounded-pill">
              {{ post.province }}
            </span>
            <span v-if="post.season" class="badge bg-light text-muted border rounded-pill">
              {{ post.season }}
            </span>
          </div>

          <!-- Title -->
          <h5 class="fw-bold text-dark mb-2">{{ post.title }}</h5>

          <!-- Target Crop -->
          <div v-if="post.targetCrop" class="small text-success fw-semibold mb-3">
            <i class="bi bi-tag-fill me-1"></i> Targeted Crop: {{ post.targetCrop }}
          </div>

          <!-- Content Body -->
          <p class="text-secondary small flex-grow-1" style="line-height: 1.6; white-space: pre-line;">
            {{ post.content }}
          </p>

          <!-- Source Link -->
          <div v-if="post.sourceUrl" class="mb-3">
            <a :href="post.sourceUrl" target="_blank" class="small text-decoration-none text-primary fw-bold">
              <i class="bi bi-box-arrow-up-right me-1"></i> Official Department Bulletin
            </a>
          </div>

          <!-- Footer Meta -->
          <div class="border-top pt-3 mt-auto d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-2">
              <div class="avatar-small rounded-circle bg-success text-white d-flex align-items-center justify-content-center fw-bold" style="width: 28px; height: 28px; font-size: 0.75rem;">
                {{ post.author?.fullName ? post.author.fullName.charAt(0) : 'A' }}
              </div>
              <div style="font-size: 0.75rem;">
                <span class="fw-bold text-dark">{{ post.author?.fullName }}</span>
                <span class="text-muted ms-1">({{ post.author?.role }})</span>
              </div>
            </div>

            <div class="d-flex align-items-center gap-2">
              <span class="text-muted small" style="font-size: 0.7rem;">
                {{ new Date(post.createdAt).toLocaleDateString() }}
              </span>
              <button
                v-if="currentUser?.id === post.author?.id || currentUser?.role === 'ADMIN'"
                class="btn btn-outline-danger btn-sm rounded-circle p-1 border-0"
                @click="handleDeletePost(post.id)"
                title="Delete advisory"
              >
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>

        </div>
      </div>
    </div>

    <EmptyState
      v-else
      icon="bi-journal-x"
      title="No Advisories Found"
      description="No notices found matching the selected category and province."
    />

    <!-- Publish Modal -->
    <div v-if="showCreateModal" class="modal-backdrop-custom d-flex align-items-center justify-content-center p-3">
      <div class="modal-card bg-white rounded-4 shadow-lg p-4 w-100" style="max-width: 600px;">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h5 class="fw-bold mb-0">Publish Agricultural Advisory</h5>
          <button class="btn btn-sm btn-light rounded-circle" @click="showCreateModal = false">
            <i class="bi bi-x-lg"></i>
          </button>
        </div>

        <form @submit.prevent="handleCreatePost">
          <div class="mb-3">
            <label class="form-label small fw-semibold">Title</label>
            <input
              v-model="postForm.title"
              type="text"
              class="form-control form-control-sm"
              placeholder="e.g. Maha Sowing Guidelines for Polonnaruwa Basin"
              required
            />
          </div>

          <div class="row g-2 mb-3">
            <div class="col-6">
              <label class="form-label small fw-semibold">Category</label>
              <select v-model="postForm.category" class="form-select form-select-sm">
                <option value="ADVICE">Farming Advice & Tips</option>
                <option value="CROP_CALENDAR">Crop Calendar Guideline</option>
                <option value="WEATHER_ALERT">Weather / Monsoon Alert</option>
                <option value="GOV_ANNOUNCEMENT">Government Scheme</option>
                <option value="COMMUNITY_FORUM">Agricultural Community Forum</option>
                <option value="EDUCATION">Farmer Education Masterclass</option>
              </select>
            </div>
            <div class="col-6">
              <label class="form-label small fw-semibold">Target Province</label>
              <select v-model="postForm.province" class="form-select form-select-sm">
                <option v-for="p in provinces" :key="p" :value="p">{{ p }}</option>
              </select>
            </div>
          </div>

          <div class="row g-2 mb-3">
            <div class="col-6">
              <label class="form-label small fw-semibold">Target Crop</label>
              <input
                v-model="postForm.targetCrop"
                type="text"
                class="form-control form-control-sm"
                placeholder="e.g. Paddy, Tea, Carrots"
              />
            </div>
            <div class="col-6">
              <label class="form-label small fw-semibold">Season</label>
              <input
                v-model="postForm.season"
                type="text"
                class="form-control form-control-sm"
                placeholder="e.g. Maha 2026/27"
              />
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label small fw-semibold">Advisory Content & Details</label>
            <textarea
              v-model="postForm.content"
              rows="4"
              class="form-control form-control-sm"
              placeholder="Provide actionable agricultural recommendations, warnings, or official circular details..."
              required
            ></textarea>
          </div>

          <div class="mb-3">
            <label class="form-label small fw-semibold">Source URL (Optional)</label>
            <input
              v-model="postForm.sourceUrl"
              type="url"
              class="form-control form-control-sm"
              placeholder="https://doa.gov.lk/..."
            />
          </div>

          <div class="form-check mb-4">
            <input
              v-model="postForm.isPinned"
              type="checkbox"
              class="form-check-input"
              id="pinCheck"
            />
            <label class="form-check-label small" for="pinCheck">
              Pin to top of advisory bulletin
            </label>
          </div>

          <div class="d-flex justify-content-end gap-2">
            <button type="button" class="btn btn-outline-secondary btn-sm" @click="showCreateModal = false">
              Cancel
            </button>
            <button type="submit" class="btn btn-success btn-sm px-4" :disabled="createLoading">
              <span v-if="createLoading" class="spinner-border spinner-border-sm me-1"></span>
              Publish Advisory
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bg-gradient-agri {
  background: linear-gradient(135deg, #1b5e20 0%, #2e7d32 50%, #43a047 100%);
}
.modal-backdrop-custom {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  z-index: 1090;
}
</style>

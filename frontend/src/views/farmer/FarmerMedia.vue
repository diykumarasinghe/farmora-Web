<script setup>
import { ref, onMounted, computed } from 'vue'
import DashboardSidebar from '../../components/common/DashboardSidebar.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import { farmService } from '../../services/farmService'

const loading = ref(true)
const saving = ref(false)
const farm = ref(null)
const successMsg = ref('')
const errorMsg = ref('')

const photoList = ref([])
const newPhotoUrl = ref('')
const videoUrl = ref('')

const loadMedia = async () => {
  loading.value = true
  try {
    const data = await farmService.getMyFarmProfile()
    farm.value = data
    videoUrl.value = data?.videoUrl || ''
    if (data?.photoUrls) {
      try {
        photoList.value = JSON.parse(data.photoUrls)
      } catch {
        photoList.value = data.photoUrls.split(',').map(s => s.trim()).filter(Boolean)
      }
    }
  } catch (err) {
    console.error('Failed to load farm media', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMedia()
})

const embedVideoUrl = computed(() => {
  if (!videoUrl.value) return ''
  const url = videoUrl.value.trim()
  if (url.includes('youtube.com/watch?v=')) {
    const videoId = url.split('watch?v=')[1]?.split('&')[0]
    return `https://www.youtube.com/embed/${videoId}`
  } else if (url.includes('youtu.be/')) {
    const videoId = url.split('youtu.be/')[1]?.split('?')[0]
    return `https://www.youtube.com/embed/${videoId}`
  }
  return url
})

const addPhoto = () => {
  if (!newPhotoUrl.value.trim()) return
  photoList.value.push(newPhotoUrl.value.trim())
  newPhotoUrl.value = ''
  saveMediaChanges()
}

const removePhoto = (idx) => {
  photoList.value.splice(idx, 1)
  saveMediaChanges()
}

const saveMediaChanges = async () => {
  saving.value = true
  errorMsg.value = ''
  successMsg.value = ''
  try {
    const payload = {
      ...farm.value,
      videoUrl: videoUrl.value.trim(),
      photoUrls: JSON.stringify(photoList.value)
    }
    const res = await farmService.saveFarmProfile(payload)
    farm.value = res
    successMsg.value = 'Farm photos and video gallery saved!'
  } catch (err) {
    errorMsg.value = err.response?.data?.message || 'Failed to save media changes.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="farmer-media-view py-4 bg-light min-vh-100">
    <div class="container-fluid px-lg-5">
      <div class="row g-4">
        <div class="col-lg-3 col-xl-2">
          <DashboardSidebar activeMenu="media" />
        </div>

        <div class="col-lg-9 col-xl-10">
          <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
            <div>
              <h2 class="fw-bold text-dark mb-1">Farm Photos & Video Gallery</h2>
              <p class="text-muted small mb-0">Showcase your cultivation grounds, irrigation, and fresh harvest to prove farming authenticity.</p>
            </div>
            <button class="btn btn-farmora btn-sm d-flex align-items-center gap-1 shadow-sm" @click="saveMediaChanges" :disabled="saving">
              <span v-if="saving" class="spinner-border spinner-border-sm"></span>
              <i v-else class="bi bi-cloud-arrow-up-fill"></i>
              <span>Save Media Gallery</span>
            </button>
          </div>

          <!-- Alerts -->
          <div v-if="successMsg" class="alert alert-success alert-dismissible py-2 small mb-3">
            <i class="bi bi-check-circle-fill me-2"></i>{{ successMsg }}
            <button class="btn-close" @click="successMsg = ''"></button>
          </div>
          <div v-if="errorMsg" class="alert alert-danger py-2 small mb-3">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>{{ errorMsg }}
          </div>

          <LoadingSpinner v-if="loading" message="Loading farm media..." />

          <template v-else>
            <!-- Introduction Video Card -->
            <div class="card border-0 shadow-sm p-4 bg-white rounded-3 mb-4">
              <h5 class="fw-bold text-dark mb-2">
                <i class="bi bi-youtube text-danger me-2"></i>Farm Introduction Video
              </h5>
              <p class="text-muted small mb-3">Add a YouTube video link featuring a virtual tour of your crops, fields, or harvesting process.</p>

              <div class="input-group mb-3">
                <span class="input-group-text bg-white"><i class="bi bi-link-45deg"></i></span>
                <input
                  type="url"
                  class="form-control"
                  v-model="videoUrl"
                  placeholder="https://www.youtube.com/watch?v=..."
                  @change="saveMediaChanges"
                />
                <button class="btn btn-outline-success" type="button" @click="saveMediaChanges" :disabled="saving">
                  Update Video
                </button>
              </div>

              <!-- Live Video Player -->
              <div v-if="embedVideoUrl" class="ratio ratio-16x9 rounded-3 overflow-hidden shadow-sm mt-2" style="max-height: 380px;">
                <iframe
                  :src="embedVideoUrl"
                  title="Farm Introduction Video"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                  allowfullscreen
                ></iframe>
              </div>
            </div>

            <!-- Photos Gallery Card -->
            <div class="card border-0 shadow-sm p-4 bg-white rounded-3">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <h5 class="fw-bold text-dark mb-0">
                  <i class="bi bi-images text-success me-2"></i>Farm Photo Showcase ({{ photoList.length }})
                </h5>
              </div>
              <p class="text-muted small mb-3">Add image URLs of your harvest crates, seedling beds, and farm environment.</p>

              <div class="input-group mb-4">
                <input
                  type="url"
                  class="form-control"
                  v-model="newPhotoUrl"
                  placeholder="Paste image URL (e.g. https://images.unsplash.com/...)"
                  @keyup.enter="addPhoto"
                />
                <button class="btn btn-farmora" type="button" @click="addPhoto" :disabled="!newPhotoUrl.trim()">
                  <i class="bi bi-plus-lg me-1"></i> Add to Gallery
                </button>
              </div>

              <!-- Gallery Grid -->
              <div v-if="photoList.length > 0" class="row g-3">
                <div v-for="(photo, idx) in photoList" :key="idx" class="col-6 col-md-4 col-lg-3">
                  <div class="card h-100 border shadow-sm rounded-3 overflow-hidden position-relative">
                    <img
                      :src="photo"
                      alt="Farm Photo"
                      class="card-img-top object-fit-cover"
                      style="height: 160px;"
                      @error="$event.target.src = 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?w=500'"
                    />
                    <button
                      type="button"
                      class="btn btn-danger btn-sm position-absolute top-0 end-0 m-2 rounded-circle p-1 d-flex align-items-center justify-content-center shadow"
                      style="width: 26px; height: 26px;"
                      title="Remove Photo"
                      @click="removePhoto(idx)"
                    >
                      <i class="bi bi-trash fs-6"></i>
                    </button>
                  </div>
                </div>
              </div>

              <div v-else class="text-center py-5 text-muted bg-light rounded-3">
                <i class="bi bi-image display-4 opacity-50 mb-2"></i>
                <h6 class="fw-bold">No Photos in Gallery Yet</h6>
                <p class="small">Add image URLs above to showcase your farm to potential buyers.</p>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

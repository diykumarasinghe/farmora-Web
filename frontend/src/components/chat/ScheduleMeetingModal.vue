<script setup>
import { ref } from 'vue'
import { videoService } from '../../services/videoService'

const props = defineProps({
  show: Boolean,
  targetUser: Object
})

const emit = defineEmits(['close', 'meetingScheduled'])

const title = ref('')
const scheduledDate = ref('')
const scheduledTime = ref('10:00')
const notes = ref('')
const loading = ref(false)
const errorMsg = ref('')

const handleSchedule = async () => {
  if (!scheduledDate.value) {
    errorMsg.value = 'Please select a date for the meeting.'
    return
  }
  loading.value = true
  errorMsg.value = ''

  try {
    const fullDateTime = `${scheduledDate.value}T${scheduledTime.value}:00`
    const payload = {
      receiverId: props.targetUser.id,
      title: title.value.trim() || `Crop Consultation with ${props.targetUser.fullName}`,
      scheduledAt: fullDateTime,
      notes: notes.value.trim()
    }

    const created = await videoService.scheduleMeeting(payload)
    emit('meetingScheduled', created)
    emit('close')
  } catch (err) {
    console.error('Failed to schedule video meeting', err)
    errorMsg.value = 'Failed to schedule meeting. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-if="show" class="modal-backdrop-custom d-flex align-items-center justify-content-center p-3">
    <div class="modal-card bg-white rounded-4 shadow-lg p-4 w-100">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <div class="d-flex align-items-center gap-2">
          <div class="p-2 bg-primary bg-opacity-10 text-primary rounded-circle">
            <i class="bi bi-calendar-event fs-5"></i>
          </div>
          <div>
            <h5 class="fw-bold mb-0">Schedule Video Meeting</h5>
            <small class="text-muted">Set up a face-to-face consultation</small>
          </div>
        </div>
        <button class="btn btn-sm btn-light rounded-circle" @click="emit('close')">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>

      <div class="alert alert-secondary py-2 small d-flex align-items-center gap-2 mb-3">
        <i class="bi bi-person-circle"></i>
        <span>Meeting with: <strong>{{ targetUser?.fullName }}</strong> ({{ targetUser?.role }})</span>
      </div>

      <div v-if="errorMsg" class="alert alert-danger py-2 small mb-3">
        {{ errorMsg }}
      </div>

      <form @submit.prevent="handleSchedule">
        <div class="mb-3">
          <label class="form-label small fw-semibold">Agenda / Meeting Topic</label>
          <input
            v-model="title"
            type="text"
            class="form-control form-control-sm"
            placeholder="e.g. Inspect harvest quality before bulk pickup"
          />
        </div>

        <div class="row g-2 mb-3">
          <div class="col-7">
            <label class="form-label small fw-semibold">Date</label>
            <input
              v-model="scheduledDate"
              type="date"
              class="form-control form-control-sm"
              required
            />
          </div>
          <div class="col-5">
            <label class="form-label small fw-semibold">Time Slot</label>
            <select v-model="scheduledTime" class="form-select form-select-sm">
              <option value="08:30">08:30 AM</option>
              <option value="10:00">10:00 AM</option>
              <option value="11:30">11:30 AM</option>
              <option value="14:00">02:00 PM</option>
              <option value="16:00">04:00 PM</option>
              <option value="17:30">05:30 PM</option>
            </select>
          </div>
        </div>

        <div class="mb-4">
          <label class="form-label small fw-semibold">Discussion Notes / Questions</label>
          <textarea
            v-model="notes"
            rows="3"
            class="form-control form-control-sm"
            placeholder="Include questions about crop variety, organic certifications, packing, etc."
          ></textarea>
        </div>

        <div class="d-flex justify-content-end gap-2">
          <button type="button" class="btn btn-outline-secondary btn-sm" @click="emit('close')">
            Cancel
          </button>
          <button type="submit" class="btn btn-primary btn-sm px-3" :disabled="loading">
            <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
            Confirm & Send Invitation
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.modal-backdrop-custom {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  z-index: 1090;
}
.modal-card {
  max-width: 500px;
}
</style>

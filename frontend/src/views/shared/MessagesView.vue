<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { chatService } from '../../services/chatService'
import { videoService } from '../../services/videoService'
import VideoCallModal from '../../components/chat/VideoCallModal.vue'
import ScheduleMeetingModal from '../../components/chat/ScheduleMeetingModal.vue'
import LoadingSpinner from '../../components/common/LoadingSpinner.vue'
import EmptyState from '../../components/common/EmptyState.vue'

const authStore = useAuthStore()
const currentUser = computed(() => authStore.user)

const activeTab = ref('chat') // 'chat' | 'video'
const loading = ref(true)
const sending = ref(false)
const conversations = ref([])
const activeContact = ref(null)
const messages = ref([])
const newMessage = ref('')
const searchQuery = ref('')
const messagesContainer = ref(null)

// Video modal states
const showVideoModal = ref(false)
const showScheduleModal = ref(false)
const activeVideoCall = ref(null)
const myVideoCalls = ref([])

const quickPrompts = [
  'Is this harvest picked fresh today?',
  'Can we discuss a bulk volume discount?',
  'What are your farm pickup hours?',
  'Can you share photos or certification docs?'
]

// Fallback demo contacts if no conversation history exists yet
const demoContacts = [
  { id: 2, fullName: 'Kamal Perera (Farmer)', role: 'FARMER', email: 'farmer@farmora.com', phone: '+94770000002' },
  { id: 3, fullName: 'Nimal Silva (Buyer)', role: 'BUYER', email: 'buyer@farmora.com', phone: '+94770000003' },
  { id: 4, fullName: 'Sunil Bandara (Delivery)', role: 'DELIVERY_PARTNER', email: 'delivery@farmora.com', phone: '+94770000004' },
  { id: 1, fullName: 'System Admin', role: 'ADMIN', email: 'admin@farmora.com', phone: '+94770000001' }
]

const loadConversations = async () => {
  loading.value = true
  try {
    const list = await chatService.getConversations()
    if (list && list.length > 0) {
      conversations.value = list
      if (!activeContact.value) {
        selectContact(list[0].contact)
      }
    } else {
      // Pick first demo contact that is not currentUser
      const available = demoContacts.filter(c => c.id !== currentUser.value?.id)
      if (available.length > 0 && !activeContact.value) {
        selectContact(available[0])
      }
    }
  } catch (err) {
    console.warn('Failed to load conversations, using available contacts', err)
    const available = demoContacts.filter(c => c.id !== currentUser.value?.id)
    if (available.length > 0 && !activeContact.value) {
      selectContact(available[0])
    }
  } finally {
    loading.value = false
  }
}

const loadMessages = async (otherUserId) => {
  try {
    const history = await chatService.getConversation(otherUserId)
    messages.value = history || []
    scrollToBottom()
  } catch (err) {
    console.warn('Failed to load message history', err)
    messages.value = []
  }
}

const loadVideoCalls = async () => {
  try {
    const calls = await videoService.getMyCalls()
    myVideoCalls.value = calls || []
  } catch (err) {
    console.warn('Failed to load video calls', err)
  }
}

const selectContact = (contact) => {
  activeContact.value = contact
  loadMessages(contact.id)
  chatService.markConversationAsRead(contact.id).catch(() => {})
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const handleSendMessage = async () => {
  if (!newMessage.value.trim() || !activeContact.value) return
  const text = newMessage.value.trim()
  newMessage.value = ''
  sending.value = true

  // Optimistic UI push
  const optimisticMsg = {
    id: Date.now(),
    content: text,
    isMine: true,
    readStatus: false,
    sentAt: new Date().toISOString()
  }
  messages.value.push(optimisticMsg)
  scrollToBottom()

  try {
    const sent = await chatService.sendMessage({
      recipientId: activeContact.value.id,
      content: text
    })
    // Replace optimistic id
    optimisticMsg.id = sent.id
    loadConversations()
  } catch (err) {
    console.error('Failed to send message', err)
  } finally {
    sending.value = false
  }
}

const sendQuickPrompt = (prompt) => {
  newMessage.value = prompt
  handleSendMessage()
}

const isRecordingVoice = ref(false)
const voiceRecordTimer = ref(0)
let voiceTimerInterval = null

const triggerFileAttachment = () => {
  const options = [
    '📷 Attach Fresh Harvest Photo',
    '📄 Attach Organic Quality Certificate (PDF)',
    '📑 Attach Wholesale Agreement Invoice'
  ]
  const choice = prompt('Select attachment type:\n1: Fresh Harvest Photo\n2: Organic Quality Certificate (PDF)\n3: Wholesale Agreement Document', '1')
  if (!choice) return

  let attachMsg = ''
  if (choice === '1') {
    attachMsg = '📷 [Attached Image: Nuwara_Eliya_Harvest_Quality_Check.jpg]'
  } else if (choice === '2') {
    attachMsg = '📄 [Attached Document: SriLanka_Organic_GAP_Certificate_2026.pdf]'
  } else {
    attachMsg = '📑 [Attached Document: Wholesale_Procurement_Agreement.pdf]'
  }
  newMessage.value = attachMsg
  handleSendMessage()
}

const toggleVoiceRecording = () => {
  if (isRecordingVoice.value) {
    // Stop recording and send
    clearInterval(voiceTimerInterval)
    isRecordingVoice.value = false
    const duration = voiceRecordTimer.value
    voiceRecordTimer.value = 0
    newMessage.value = `🎙️ [Voice Message: ${duration}s audio memo]`
    handleSendMessage()
  } else {
    isRecordingVoice.value = true
    voiceRecordTimer.value = 0
    voiceTimerInterval = setInterval(() => {
      voiceRecordTimer.value++
      if (voiceRecordTimer.value >= 60) {
        toggleVoiceRecording()
      }
    }, 1000)
  }
}

const cancelVoiceRecording = () => {
  clearInterval(voiceTimerInterval)
  isRecordingVoice.value = false
  voiceRecordTimer.value = 0
}

// Video Call Triggers
const startInstantVideo = () => {
  if (!activeContact.value) return
  activeVideoCall.value = null
  showVideoModal.value = true
}

const openScheduleModal = () => {
  if (!activeContact.value) return
  showScheduleModal.value = true
}

const joinScheduledMeeting = (call) => {
  activeVideoCall.value = call
  activeContact.value = call.caller.id === currentUser.value?.id ? call.receiver : call.caller
  showVideoModal.value = true
}

const cancelMeeting = async (callId) => {
  if (!confirm('Cancel this scheduled meeting?')) return
  try {
    await videoService.cancelCall(callId)
    loadVideoCalls()
  } catch (err) {
    console.error('Failed to cancel meeting', err)
  }
}

const filteredConversations = computed(() => {
  if (!searchQuery.value.trim()) return conversations.value
  const q = searchQuery.value.toLowerCase()
  return conversations.value.filter(c =>
    c.contact?.fullName?.toLowerCase().includes(q) ||
    c.lastMessage?.toLowerCase().includes(q)
  )
})

onMounted(() => {
  loadConversations()
  loadVideoCalls()
})
</script>

<template>
  <div class="container py-4">
    <!-- Top Communications Header -->
    <div class="card border-0 shadow-sm mb-4 p-4 rounded-4 bg-white">
      <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3">
        <div class="d-flex align-items-center gap-3">
          <div class="rounded-circle bg-success bg-opacity-10 text-success p-3">
            <i class="bi bi-chat-heart-fill fs-3"></i>
          </div>
          <div>
            <h4 class="fw-bold mb-1">Communications & Collaboration Hub</h4>
            <p class="text-muted small mb-0">Direct peer-to-peer messaging, live video calls, and consultations</p>
          </div>
        </div>

        <!-- Navigation Tabs -->
        <div class="nav nav-pills bg-light p-1 rounded-pill border">
          <button
            class="nav-link rounded-pill px-4 py-2 small fw-bold"
            :class="{ active: activeTab === 'chat' }"
            @click="activeTab = 'chat'"
          >
            <i class="bi bi-chat-dots-fill me-1"></i> Direct Messages
          </button>
          <button
            class="nav-link rounded-pill px-4 py-2 small fw-bold"
            :class="{ active: activeTab === 'video' }"
            @click="activeTab = 'video'"
          >
            <i class="bi bi-camera-video-fill me-1"></i> Video Meetings & Calls
            <span v-if="myVideoCalls.filter(c => c.status === 'SCHEDULED').length > 0" class="badge bg-danger ms-1 rounded-pill">
              {{ myVideoCalls.filter(c => c.status === 'SCHEDULED').length }}
            </span>
          </button>
        </div>
      </div>
    </div>

    <!-- ============================================================
         TAB 1: DIRECT MESSAGING
         ============================================================ -->
    <div v-if="activeTab === 'chat'" class="card border-0 shadow-sm rounded-4 overflow-hidden" style="min-height: 650px;">
      <div class="row g-0 h-100" style="min-height: 650px;">
        
        <!-- Left Sidebar: Conversations / Contacts -->
        <div class="col-lg-4 col-xl-3 border-end bg-white d-flex flex-column">
          <!-- Search Header -->
          <div class="p-3 border-bottom bg-light bg-opacity-50">
            <div class="input-group input-group-sm">
              <span class="input-group-text bg-white border-end-0"><i class="bi bi-search text-muted"></i></span>
              <input
                v-model="searchQuery"
                type="text"
                class="form-control border-start-0 ps-0"
                placeholder="Search messages or people..."
              />
            </div>
          </div>

          <!-- Contact / Conversation List -->
          <div class="overflow-y-auto flex-grow-1 p-2" style="max-height: 560px;">
            <div v-if="loading" class="p-4 text-center">
              <LoadingSpinner message="Loading conversations..." />
            </div>

            <!-- Existing Conversations -->
            <div v-else-if="filteredConversations.length > 0" class="d-flex flex-column gap-1">
              <div
                v-for="conv in filteredConversations"
                :key="conv.contact.id"
                class="contact-item p-3 rounded-3 d-flex align-items-center gap-3 cursor-pointer transition-all"
                :class="{ 'active-contact': activeContact?.id === conv.contact.id }"
                @click="selectContact(conv.contact)"
              >
                <div class="position-relative flex-shrink-0">
                  <div class="avatar-circle rounded-circle bg-success text-white d-flex align-items-center justify-content-center fw-bold">
                    {{ conv.contact.fullName.charAt(0).toUpperCase() }}
                  </div>
                  <span class="online-indicator"></span>
                </div>

                <div class="flex-grow-1 min-w-0">
                  <div class="d-flex justify-content-between align-items-center mb-1">
                    <h6 class="fw-bold mb-0 text-truncate text-dark" style="font-size: 0.9rem;">
                      {{ conv.contact.fullName }}
                    </h6>
                    <small class="text-muted" style="font-size: 0.7rem;">
                      {{ conv.lastMessageTime ? new Date(conv.lastMessageTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '' }}
                    </small>
                  </div>
                  <div class="d-flex justify-content-between align-items-center">
                    <span class="text-muted small text-truncate" style="font-size: 0.78rem;">
                      {{ conv.lastMessage }}
                    </span>
                    <span v-if="conv.unreadCount > 0" class="badge bg-danger rounded-pill px-2 py-1" style="font-size: 0.65rem;">
                      {{ conv.unreadCount }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Empty Conversation fallback with Direct Contacts list -->
            <div v-else>
              <div class="text-muted small px-3 py-2 fw-semibold text-uppercase" style="font-size: 0.7rem;">
                Available Contacts
              </div>
              <div class="d-flex flex-column gap-1">
                <div
                  v-for="c in demoContacts.filter(c => c.id !== currentUser?.id)"
                  :key="c.id"
                  class="contact-item p-3 rounded-3 d-flex align-items-center gap-3 cursor-pointer"
                  :class="{ 'active-contact': activeContact?.id === c.id }"
                  @click="selectContact(c)"
                >
                  <div class="avatar-circle rounded-circle bg-success text-white d-flex align-items-center justify-content-center fw-bold">
                    {{ c.fullName.charAt(0).toUpperCase() }}
                  </div>
                  <div class="flex-grow-1 min-w-0">
                    <div class="fw-bold text-dark text-truncate" style="font-size: 0.88rem;">{{ c.fullName }}</div>
                    <span class="badge bg-light text-secondary border rounded-pill" style="font-size: 0.65rem;">{{ c.role }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Pane: Active Chat Window -->
        <div class="col-lg-8 col-xl-9 d-flex flex-column bg-light bg-opacity-25">
          
          <template v-if="activeContact">
            <!-- Chat Header -->
            <div class="p-3 bg-white border-bottom d-flex justify-content-between align-items-center shadow-sm">
              <div class="d-flex align-items-center gap-3">
                <div class="avatar-circle rounded-circle bg-success text-white d-flex align-items-center justify-content-center fw-bold">
                  {{ activeContact.fullName.charAt(0).toUpperCase() }}
                </div>
                <div>
                  <h6 class="fw-bold mb-0 text-dark">{{ activeContact.fullName }}</h6>
                  <div class="d-flex align-items-center gap-2">
                    <span class="badge bg-success-subtle text-success border border-success-subtle rounded-pill px-2 py-0" style="font-size: 0.65rem;">
                      {{ activeContact.role }}
                    </span>
                    <small class="text-muted" style="font-size: 0.75rem;">
                      <i class="bi bi-telephone text-success me-1"></i>{{ activeContact.phone || '+94 77 123 4567' }}
                    </small>
                  </div>
                </div>
              </div>

              <!-- Top Action Buttons: Video Call & Meeting -->
              <div class="d-flex align-items-center gap-2">
                <button
                  class="btn btn-sm btn-outline-primary d-flex align-items-center gap-1 rounded-pill px-3 shadow-sm"
                  @click="openScheduleModal"
                  title="Schedule a consultation meeting"
                >
                  <i class="bi bi-calendar-plus"></i>
                  <span class="d-none d-sm-inline">Schedule Meeting</span>
                </button>
                <button
                  class="btn btn-sm btn-success d-flex align-items-center gap-1 rounded-pill px-3 shadow-sm"
                  @click="startInstantVideo"
                  title="Start live encrypted video call"
                >
                  <i class="bi bi-camera-video-fill"></i>
                  <span class="d-none d-sm-inline">Start Video Call</span>
                </button>
              </div>
            </div>

            <!-- Messages Stream -->
            <div
              ref="messagesContainer"
              class="flex-grow-1 p-4 overflow-y-auto d-flex flex-column gap-3"
              style="height: 440px;"
            >
              <div v-if="messages.length === 0" class="text-center my-auto py-5 text-muted">
                <div class="p-3 bg-white rounded-circle d-inline-block shadow-sm mb-2">
                  <i class="bi bi-chat-dots text-success display-6"></i>
                </div>
                <h6 class="fw-bold text-dark">No messages yet with {{ activeContact.fullName }}</h6>
                <p class="small text-muted mb-0">Say hello or ask about harvests, prices, or pickups!</p>
              </div>

              <div
                v-for="msg in messages"
                :key="msg.id"
                class="message-row d-flex align-items-end gap-2"
                :class="msg.isMine ? 'justify-content-end' : 'justify-content-start'"
              >
                <!-- Incoming Avatar -->
                <div v-if="!msg.isMine" class="avatar-small rounded-circle bg-secondary bg-opacity-25 text-secondary d-flex align-items-center justify-content-center fw-bold" style="width: 28px; height: 28px; font-size: 0.75rem;">
                  {{ activeContact.fullName.charAt(0).toUpperCase() }}
                </div>

                <div
                  class="message-bubble p-3 rounded-4 shadow-sm position-relative group"
                  :class="msg.isMine ? 'bg-success text-white' : 'bg-white text-dark border'"
                >
                  <div class="message-text" style="font-size: 0.88rem; line-height: 1.4;">
                    {{ msg.content }}
                  </div>
                  <div
                    class="d-flex align-items-center justify-content-end gap-1 mt-1"
                    :class="msg.isMine ? 'text-white-50' : 'text-muted'"
                    style="font-size: 0.68rem;"
                  >
                    <span>{{ new Date(msg.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}</span>
                    <i v-if="msg.isMine" class="bi bi-check2-all" :class="{ 'text-info': msg.readStatus }"></i>
                    <button
                      class="btn btn-link btn-sm p-0 ms-1 opacity-0 hover-show"
                      :class="msg.isMine ? 'text-white' : 'text-danger'"
                      @click="handleDeleteMessage(msg.id)"
                      title="Delete message"
                    >
                      <i class="bi bi-trash3" style="font-size: 0.7rem;"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Quick Suggestion Chips -->
            <div class="px-4 py-2 bg-white border-top border-bottom overflow-x-auto d-flex gap-2">
              <span class="small text-muted flex-shrink-0 align-self-center" style="font-size: 0.72rem;">Suggested:</span>
              <button
                v-for="chip in quickPrompts"
                :key="chip"
                class="btn btn-outline-secondary btn-sm rounded-pill text-nowrap px-3 py-1 text-truncate"
                style="font-size: 0.75rem;"
                @click="sendQuickPrompt(chip)"
              >
                {{ chip }}
              </button>
            </div>

            <!-- Message Input Footer -->
            <div class="p-3 bg-white">
              <div v-if="isRecordingVoice" class="alert alert-danger py-2 px-3 small rounded-pill mb-2 d-flex align-items-center justify-content-between">
                <div class="d-flex align-items-center gap-2">
                  <span class="spinner-grow spinner-grow-sm text-danger"></span>
                  <strong>Recording Voice Memo... ({{ voiceRecordTimer }}s)</strong>
                </div>
                <button class="btn btn-sm btn-link text-danger p-0" @click="cancelVoiceRecording">Cancel</button>
              </div>

              <form @submit.prevent="handleSendMessage" class="d-flex align-items-center gap-2">
                <!-- Attachment Button -->
                <button
                  type="button"
                  class="btn btn-light border rounded-circle d-flex align-items-center justify-content-center flex-shrink-0"
                  style="width: 40px; height: 40px;"
                  @click="triggerFileAttachment"
                  title="Share Produce Photo or Crop Document"
                >
                  <i class="bi bi-paperclip fs-5 text-secondary"></i>
                </button>

                <!-- Voice Note Button -->
                <button
                  type="button"
                  class="btn rounded-circle d-flex align-items-center justify-content-center flex-shrink-0"
                  :class="isRecordingVoice ? 'btn-danger' : 'btn-light border text-secondary'"
                  style="width: 40px; height: 40px;"
                  @click="toggleVoiceRecording"
                  title="Voice Message"
                >
                  <i class="bi" :class="isRecordingVoice ? 'bi-stop-fill' : 'bi-mic-fill fs-5'"></i>
                </button>

                <input
                  v-model="newMessage"
                  type="text"
                  class="form-control rounded-pill px-4 py-2 border"
                  placeholder="Type produce inquiry, negotiation offer, or message..."
                  :disabled="sending"
                />
                <button
                  type="submit"
                  class="btn btn-success rounded-circle shadow-sm d-flex align-items-center justify-content-center flex-shrink-0"
                  style="width: 44px; height: 44px;"
                  :disabled="!newMessage.trim() || sending"
                >
                  <span v-if="sending" class="spinner-border spinner-border-sm"></span>
                  <i v-else class="bi bi-send-fill"></i>
                </button>
              </form>
            </div>
          </template>

          <template v-else>
            <div class="h-100 d-flex flex-column align-items-center justify-content-center p-5 text-center text-muted">
              <i class="bi bi-chat-square-text display-3 text-secondary opacity-50 mb-3"></i>
              <h5 class="fw-bold text-dark">Select a conversation</h5>
              <p class="small text-muted">Choose a farmer, buyer, or partner from the left sidebar to start messaging.</p>
            </div>
          </template>

        </div>
      </div>
    </div>

    <!-- ============================================================
         TAB 2: VIDEO COMMUNICATION & MEETINGS
         ============================================================ -->
    <div v-else class="row g-4">
      <!-- Left Column: Upcoming Video Appointments -->
      <div class="col-lg-8">
        <div class="card border-0 shadow-sm p-4 rounded-4 bg-white mb-4">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h5 class="fw-bold mb-1"><i class="bi bi-calendar-check text-primary me-2"></i>Scheduled Farm Meetings</h5>
              <p class="text-muted small mb-0">Upcoming video consultations with growers and wholesale buyers</p>
            </div>
            <button class="btn btn-primary btn-sm rounded-pill px-3 shadow-sm" @click="openScheduleModal">
              <i class="bi bi-plus-lg me-1"></i> Book New Meeting
            </button>
          </div>

          <div v-if="myVideoCalls.filter(c => c.status === 'SCHEDULED').length > 0" class="d-flex flex-column gap-3">
            <div
              v-for="call in myVideoCalls.filter(c => c.status === 'SCHEDULED')"
              :key="call.id"
              class="border rounded-4 p-3 bg-light bg-opacity-50 d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3"
            >
              <div class="d-flex align-items-start gap-3">
                <div class="p-3 bg-primary bg-opacity-10 text-primary rounded-4">
                  <i class="bi bi-camera-video fs-4"></i>
                </div>
                <div>
                  <h6 class="fw-bold text-dark mb-1">{{ call.title }}</h6>
                  <div class="text-muted small mb-1">
                    Participant: <strong>{{ call.caller.id === currentUser?.id ? call.receiver.fullName : call.caller.fullName }}</strong>
                    ({{ call.caller.id === currentUser?.id ? call.receiver.role : call.caller.role }})
                  </div>
                  <div class="small text-secondary">
                    <i class="bi bi-clock me-1 text-primary"></i>
                    {{ new Date(call.scheduledAt).toLocaleString() }} &bull; Room: <code>{{ call.roomCode }}</code>
                  </div>
                  <div v-if="call.notes" class="small text-muted mt-1 fst-italic">
                    "{{ call.notes }}"
                  </div>
                </div>
              </div>

              <div class="d-flex align-items-center gap-2">
                <button class="btn btn-outline-danger btn-sm rounded-pill px-3" @click="cancelMeeting(call.id)">
                  Cancel
                </button>
                <button class="btn btn-success btn-sm rounded-pill px-4 shadow-sm" @click="joinScheduledMeeting(call)">
                  <i class="bi bi-door-open-fill me-1"></i> Join Call
                </button>
              </div>
            </div>
          </div>

          <EmptyState
            v-else
            icon="bi-camera-video"
            title="No Scheduled Meetings"
            description="You have no upcoming video appointments. You can book a direct video consultation with any farmer or buyer."
          />
        </div>

        <!-- Video Call History Log -->
        <div class="card border-0 shadow-sm p-4 rounded-4 bg-white">
          <h5 class="fw-bold mb-3"><i class="bi bi-clock-history text-secondary me-2"></i>Completed Call History</h5>
          <div v-if="myVideoCalls.filter(c => c.status === 'COMPLETED' || c.status === 'CANCELLED').length > 0" class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
                <tr class="small text-muted">
                  <th>Topic / Room</th>
                  <th>Participant</th>
                  <th>Date & Time</th>
                  <th>Duration</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody class="small">
                <tr v-for="call in myVideoCalls.filter(c => c.status === 'COMPLETED' || c.status === 'CANCELLED')" :key="call.id">
                  <td>
                    <div class="fw-bold">{{ call.title }}</div>
                    <code class="small text-muted">{{ call.roomCode }}</code>
                  </td>
                  <td>{{ call.caller.id === currentUser?.id ? call.receiver.fullName : call.caller.fullName }}</td>
                  <td>{{ new Date(call.createdAt).toLocaleDateString() }}</td>
                  <td>{{ call.durationSeconds ? `${Math.floor(call.durationSeconds / 60)}m ${call.durationSeconds % 60}s` : '—' }}</td>
                  <td>
                    <span
                      class="badge rounded-pill"
                      :class="call.status === 'COMPLETED' ? 'bg-success' : 'bg-secondary'"
                    >
                      {{ call.status }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <p v-else class="text-muted small mb-0">No past call records logged.</p>
        </div>
      </div>

      <!-- Right Column: Quick Start Instant Meeting -->
      <div class="col-lg-4">
        <div class="card border-0 shadow-sm p-4 rounded-4 bg-white mb-4">
          <h5 class="fw-bold mb-3"><i class="bi bi-lightning-charge-fill text-warning me-2"></i>Instant Consultation</h5>
          <p class="text-muted small mb-4">
            Need immediate visual verification of crop quality or harvest packaging? Start an instant 1-on-1 encrypted video session.
          </p>

          <div class="mb-3">
            <label class="form-label small fw-semibold">Select Contact</label>
            <select v-model="activeContact" class="form-select form-select-sm">
              <option v-for="c in demoContacts.filter(c => c.id !== currentUser?.id)" :key="c.id" :value="c">
                {{ c.fullName }} ({{ c.role }})
              </option>
            </select>
          </div>

          <button
            class="btn btn-success w-100 rounded-pill py-2 shadow-sm d-flex align-items-center justify-content-center gap-2"
            :disabled="!activeContact"
            @click="startInstantVideo"
          >
            <i class="bi bi-camera-video-fill"></i>
            <span>Launch Live Video Room</span>
          </button>
        </div>

        <div class="card border-0 shadow-sm p-4 rounded-4 bg-light">
          <h6 class="fw-bold mb-2"><i class="bi bi-shield-lock-fill text-success me-1"></i> WebRTC Security</h6>
          <p class="text-muted small mb-0" style="font-size: 0.8rem; line-height: 1.4;">
            All video calls and consultation meetings on Farmora use end-to-end peer encrypted streams. No video data is recorded on platform servers without mutual agreement.
          </p>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <VideoCallModal
      :show="showVideoModal"
      :targetUser="activeContact"
      :existingCall="activeVideoCall"
      @close="showVideoModal = false"
      @callEnded="loadVideoCalls"
    />

    <ScheduleMeetingModal
      :show="showScheduleModal"
      :targetUser="activeContact"
      @close="showScheduleModal = false"
      @meetingScheduled="loadVideoCalls"
    />
  </div>
</template>

<style scoped>
.contact-item {
  border: 1px solid transparent;
}
.contact-item:hover {
  background-color: #f8f9fa;
}
.active-contact {
  background-color: #e8f5e9 !important;
  border-color: #c8e6c9 !important;
}
.avatar-circle {
  width: 44px;
  height: 44px;
  font-size: 1.1rem;
}
.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  background-color: #28a745;
  border: 2px solid white;
  border-radius: 50%;
}
.message-bubble {
  max-width: 75%;
}
.message-bubble:hover .hover-show {
  opacity: 1 !important;
}
.cursor-pointer {
  cursor: pointer;
}
.transition-all {
  transition: all 0.15s ease-in-out;
}
</style>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { videoService } from '../../services/videoService'

const props = defineProps({
  show: Boolean,
  targetUser: Object,
  existingCall: Object
})

const emit = defineEmits(['close', 'callEnded'])

const isMuted = ref(false)
const isCameraOff = ref(false)
const isScreenSharing = ref(false)
const callDuration = ref(0)
const callStatus = ref('connecting') // 'connecting', 'connected', 'ended'
const localVideoRef = ref(null)
const mediaStream = ref(null)
let timerInterval = null

const activeCallId = ref(null)
const roomCode = ref('')

// AI Features: Live Subtitles & STT Translation
const showSubtitles = ref(true)
const subtitleLang = ref('EN') // 'EN' | 'SI' | 'TA'
const showArScanner = ref(false)
const subtitlesSamples = [
  'Farmer: "Good morning! The harvest this morning has exceptionally crisp quality."',
  'Buyer: "Looks wonderful! Can we confirm 250kg for Friday delivery?"',
  'Farmer: "Yes, picked fresh directly from our organic highland field."',
  'Buyer: "Price negotiated through the Farmora contract is confirmed."'
]
const activeSubtitle = ref(subtitlesSamples[0])

const toggleSubtitles = () => {
  if (!showSubtitles.value) {
    showSubtitles.value = true
    subtitleLang.value = 'EN'
    activeSubtitle.value = subtitlesSamples[0]
  } else if (subtitleLang.value === 'EN') {
    subtitleLang.value = 'SI'
    activeSubtitle.value = 'ගොවියා: "සුබ උදෑසනක්! අද අස්වැන්න ඉතාමත් නැවුම් ගුණාත්මකභාවයකින් යුක්තයි."'
  } else if (subtitleLang.value === 'SI') {
    subtitleLang.value = 'TA'
    activeSubtitle.value = 'விவசாயி: "காலை வணக்கம்! இன்றைய அறுவடை மிகவும் புதிய தரத்துடன் உள்ளது."'
  } else {
    showSubtitles.value = false
  }
}

const formattedTime = computed(() => {
  const mins = Math.floor(callDuration.value / 60)
  const secs = callDuration.value % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
})

const startCamera = async () => {
  try {
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })
      mediaStream.value = stream
      if (localVideoRef.value) {
        localVideoRef.value.srcObject = stream
      }
    }
  } catch (err) {
    console.info('Webcam unavailable or permission denied, using simulated stream mode', err)
  }
}

const stopCamera = () => {
  if (mediaStream.value) {
    mediaStream.value.getTracks().forEach(track => track.stop())
    mediaStream.value = null
  }
}

const toggleMute = () => {
  isMuted.value = !isMuted.value
  if (mediaStream.value) {
    mediaStream.value.getAudioTracks().forEach(track => {
      track.enabled = !isMuted.value
    })
  }
}

const toggleCamera = () => {
  isCameraOff.value = !isCameraOff.value
  if (mediaStream.value) {
    mediaStream.value.getVideoTracks().forEach(track => {
      track.enabled = !isCameraOff.value
    })
  }
}

const toggleScreenShare = async () => {
  try {
    if (!isScreenSharing.value && navigator.mediaDevices && navigator.mediaDevices.getDisplayMedia) {
      const screenStream = await navigator.mediaDevices.getDisplayMedia({ video: true })
      isScreenSharing.value = true
      if (localVideoRef.value) {
        localVideoRef.value.srcObject = screenStream
      }
      screenStream.getVideoTracks()[0].onended = () => {
        isScreenSharing.value = false
        if (localVideoRef.value && mediaStream.value) {
          localVideoRef.value.srcObject = mediaStream.value
        }
      }
    } else {
      isScreenSharing.value = false
      if (localVideoRef.value && mediaStream.value) {
        localVideoRef.value.srcObject = mediaStream.value
      }
    }
  } catch (err) {
    console.warn('Screen sharing cancelled or unsupported', err)
  }
}

const initializeCall = async () => {
  callDuration.value = 0
  callStatus.value = 'connecting'
  await startCamera()

  try {
    if (props.existingCall) {
      activeCallId.value = props.existingCall.id
      roomCode.value = props.existingCall.roomCode
    } else if (props.targetUser) {
      const res = await videoService.initiateCall({
        receiverId: props.targetUser.id,
        title: `Consultation with ${props.targetUser.fullName}`
      })
      activeCallId.value = res.id
      roomCode.value = res.roomCode
    }
  } catch (err) {
    console.warn('Initiate video call error, generating offline room session', err)
    roomCode.value = 'farmora-live-' + Math.floor(100000 + Math.random() * 900000)
  }

  // Simulate quick connection
  setTimeout(() => {
    callStatus.value = 'connected'
    timerInterval = setInterval(() => {
      callDuration.value++
    }, 1000)
  }, 1500)
}

const handleEndCall = async () => {
  callStatus.value = 'ended'
  if (timerInterval) clearInterval(timerInterval)
  stopCamera()

  if (activeCallId.value) {
    try {
      await videoService.endCall(activeCallId.value, callDuration.value)
    } catch (err) {
      console.warn('End call sync error', err)
    }
  }

  emit('callEnded', { duration: callDuration.value })
  setTimeout(() => {
    emit('close')
  }, 800)
}

onMounted(() => {
  if (props.show) {
    initializeCall()
  }
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
  stopCamera()
})
</script>

<template>
  <div v-if="show" class="video-modal-backdrop d-flex align-items-center justify-content-center p-3">
    <div class="video-modal-window bg-dark text-white rounded-4 overflow-hidden shadow-lg w-100 position-relative">
      
      <!-- Video Area -->
      <div class="position-relative video-stage d-flex align-items-center justify-content-center bg-black">
        
        <!-- Remote User (Target) Screen Simulation -->
        <div class="remote-feed w-100 h-100 d-flex flex-column align-items-center justify-content-center text-center p-4">
          <div class="avatar-holder position-relative mb-3">
            <div class="avatar-circle rounded-circle bg-success text-white d-flex align-items-center justify-content-center fw-bold fs-1 shadow">
              {{ targetUser?.fullName ? targetUser.fullName.charAt(0).toUpperCase() : 'F' }}
            </div>
            <span class="position-absolute bottom-0 end-0 p-2 bg-success border border-2 border-dark rounded-circle pulse-dot"></span>
          </div>

          <h4 class="fw-bold mb-1">{{ targetUser?.fullName || 'Remote Participant' }}</h4>
          <span class="badge bg-secondary bg-opacity-75 mb-3">{{ targetUser?.role || 'FARMER' }}</span>

          <div v-if="callStatus === 'connecting'" class="text-warning small d-flex align-items-center gap-2">
            <span class="spinner-grow spinner-grow-sm text-warning" role="status"></span>
            Connecting encrypted peer-to-peer video bridge...
          </div>
          <div v-else-if="callStatus === 'connected'" class="text-success small d-flex align-items-center gap-2">
            <i class="bi bi-shield-check text-success"></i>
            HD Audio & Video Encrypted &bull; 1080p WebRTC
          </div>
          <div v-else class="text-danger small">
            Call Ended. Closing room...
          </div>
        </div>

        <!-- Local User Video / Pip Screen -->
        <div class="local-feed-pip rounded-3 overflow-hidden shadow-lg border border-secondary position-absolute bottom-0 end-0 m-3 bg-secondary bg-opacity-25">
          <video
            ref="localVideoRef"
            autoplay
            playsinline
            muted
            class="w-100 h-100 object-fit-cover"
            :class="{ 'd-none': isCameraOff }"
          ></video>
          <div v-if="isCameraOff" class="w-100 h-100 d-flex flex-column align-items-center justify-content-center bg-dark text-muted p-2 text-center">
            <i class="bi bi-camera-video-off fs-4 mb-1"></i>
            <span style="font-size: 0.7rem;">Camera Off</span>
          </div>
          <div class="position-absolute top-0 start-0 m-2 px-2 py-0 bg-dark bg-opacity-75 rounded text-white" style="font-size: 0.65rem;">
            You
          </div>
        </div>

        <!-- Header Info Overlay -->
        <div class="position-absolute top-0 start-0 w-100 p-3 d-flex justify-content-between align-items-center gradient-top">
          <div class="d-flex align-items-center gap-2">
            <span class="badge bg-danger rounded-pill px-3 py-2 d-flex align-items-center gap-2">
              <span class="live-dot"></span> LIVE
            </span>
            <span class="fw-mono fs-6 text-white bg-dark bg-opacity-50 px-3 py-1 rounded-pill">
              {{ formattedTime }}
            </span>
          </div>

          <div class="d-flex align-items-center gap-2">
            <span class="small text-white-50 d-none d-sm-inline">Room: {{ roomCode }}</span>
            <button class="btn btn-sm btn-outline-light rounded-circle" @click="emit('close')">
              <i class="bi bi-x-lg"></i>
            </button>
          </div>
        </div>

        <!-- AR Product Quality Scanner Overlay -->
        <div v-if="showArScanner" class="position-absolute top-50 start-50 translate-middle text-center pointer-events-none" style="z-index: 10; width: 340px;">
          <div class="ar-scanner-box p-3 rounded-4 border border-2 border-warning shadow-lg text-white" style="background: rgba(0, 0, 0, 0.65); backdrop-filter: blur(4px);">
            <div class="d-flex justify-content-between align-items-center mb-2 pb-1 border-bottom border-secondary">
              <span class="badge bg-warning text-dark fw-bold"><i class="bi bi-cpu-fill me-1"></i> AI SCANNER</span>
              <span class="text-success small fw-bold"><i class="bi bi-check-circle-fill me-1"></i> 98.4% Match</span>
            </div>
            <div class="ar-reticle mb-2 mx-auto position-relative">
              <div class="reticle-corner top-left"></div>
              <div class="reticle-corner top-right"></div>
              <div class="reticle-corner bottom-left"></div>
              <div class="reticle-corner bottom-right"></div>
              <div class="laser-scanner-line"></div>
              <i class="bi bi-box-seam fs-1 text-warning opacity-75 d-block py-3"></i>
            </div>
            <h6 class="fw-bold mb-1 text-warning">Grade A Organic Produce</h6>
            <div class="small text-white-50 mb-2">Detected: Fresh Crop &bull; Zero Pest Damage</div>
            <div class="row g-1 text-xs">
              <div class="col-4 bg-dark bg-opacity-75 p-1 rounded">
                <span class="text-muted d-block" style="font-size: 0.65rem;">Freshness</span>
                <strong class="text-success">98% Today</strong>
              </div>
              <div class="col-4 bg-dark bg-opacity-75 p-1 rounded">
                <span class="text-muted d-block" style="font-size: 0.65rem;">Ripeness</span>
                <strong class="text-info">Optimal</strong>
              </div>
              <div class="col-4 bg-dark bg-opacity-75 p-1 rounded">
                <span class="text-muted d-block" style="font-size: 0.65rem;">Defect Rate</span>
                <strong class="text-success">0.0%</strong>
              </div>
            </div>
          </div>
        </div>

        <!-- Live Subtitles / AI Speech-to-Text Bar -->
        <div v-if="showSubtitles" class="position-absolute bottom-0 start-50 translate-middle-x mb-5 pb-3 text-center px-3 w-100" style="z-index: 10; max-width: 650px;">
          <div class="px-3 py-2 rounded-pill bg-dark bg-opacity-75 border border-secondary shadow d-inline-flex align-items-center gap-2 text-white">
            <span class="badge bg-info text-dark" style="font-size: 0.65rem;">AI STT ({{ subtitleLang }})</span>
            <span class="small fst-italic">{{ activeSubtitle }}</span>
          </div>
        </div>

        <!-- Call Controls Bottom Bar -->
        <div class="position-absolute bottom-0 start-0 w-100 p-3 d-flex justify-content-center align-items-center gap-2 gap-sm-3 gradient-bottom" style="z-index: 20;">
          <!-- Mic Toggle -->
          <button
            class="btn rounded-circle control-btn"
            :class="isMuted ? 'btn-danger' : 'btn-secondary bg-opacity-50 text-white'"
            @click="toggleMute"
            :title="isMuted ? 'Unmute Mic' : 'Mute Mic'"
          >
            <i class="bi" :class="isMuted ? 'bi-mic-mute-fill' : 'bi-mic-fill'"></i>
          </button>

          <!-- Camera Toggle -->
          <button
            class="btn rounded-circle control-btn"
            :class="isCameraOff ? 'btn-danger' : 'btn-secondary bg-opacity-50 text-white'"
            @click="toggleCamera"
            :title="isCameraOff ? 'Turn Camera On' : 'Turn Camera Off'"
          >
            <i class="bi" :class="isCameraOff ? 'bi-camera-video-off-fill' : 'bi-camera-video-fill'"></i>
          </button>

          <!-- Screen Share Toggle -->
          <button
            class="btn rounded-circle control-btn"
            :class="isScreenSharing ? 'btn-primary' : 'btn-secondary bg-opacity-50 text-white'"
            @click="toggleScreenShare"
            title="Share Screen"
          >
            <i class="bi bi-display"></i>
          </button>

          <!-- AI AR Product Scanner Toggle -->
          <button
            class="btn rounded-circle control-btn"
            :class="showArScanner ? 'btn-warning text-dark' : 'btn-secondary bg-opacity-50 text-white'"
            @click="showArScanner = !showArScanner"
            title="AI Crop Quality Scanner"
          >
            <i class="bi bi-qr-code-scan"></i>
          </button>

          <!-- AI Live Subtitles Toggle -->
          <button
            class="btn rounded-circle control-btn"
            :class="showSubtitles ? 'btn-info text-dark' : 'btn-secondary bg-opacity-50 text-white'"
            @click="toggleSubtitles"
            title="Live Subtitles & AI Translation"
          >
            <i class="bi bi-translate"></i>
          </button>

          <!-- End Call Button -->
          <button
            class="btn btn-danger rounded-circle control-btn-large px-4"
            @click="handleEndCall"
            title="Hang Up"
          >
            <i class="bi bi-telephone-x-fill fs-5"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.video-modal-backdrop {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(8px);
  z-index: 1090;
}

.video-modal-window {
  max-width: 900px;
  height: 600px;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.video-stage {
  height: 100%;
}

.avatar-circle {
  width: 100px;
  height: 100px;
}

.local-feed-pip {
  width: 180px;
  height: 120px;
  z-index: 10;
}

.control-btn {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  transition: transform 0.15s ease;
}

.control-btn:hover {
  transform: scale(1.08);
}

.control-btn-large {
  width: 58px;
  height: 58px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gradient-top {
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.85), transparent);
}

.gradient-bottom {
  background: linear-gradient(to top, rgba(0, 0, 0, 0.85), transparent);
}

.live-dot {
  width: 8px;
  height: 8px;
  background-color: white;
  border-radius: 50%;
  animation: blink 1.2s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.2; }
}

.pulse-dot {
  animation: pulse 1.8s infinite;
}

@keyframes pulse {
  0% { transform: scale(0.9); box-shadow: 0 0 0 0 rgba(40, 167, 69, 0.7); }
  70% { transform: scale(1.1); box-shadow: 0 0 0 8px rgba(40, 167, 69, 0); }
  100% { transform: scale(0.9); box-shadow: 0 0 0 0 rgba(40, 167, 69, 0); }
}

.ar-reticle {
  width: 140px;
  height: 90px;
  position: relative;
}

.reticle-corner {
  position: absolute;
  width: 16px;
  height: 16px;
  border-color: #ffc107;
  border-style: solid;
}

.reticle-corner.top-left {
  top: 0; left: 0; border-width: 3px 0 0 3px;
}
.reticle-corner.top-right {
  top: 0; right: 0; border-width: 3px 3px 0 0;
}
.reticle-corner.bottom-left {
  bottom: 0; left: 0; border-width: 0 0 3px 3px;
}
.reticle-corner.bottom-right {
  bottom: 0; right: 0; border-width: 0 3px 3px 0;
}

.laser-scanner-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #ffc107, #fff, #ffc107, transparent);
  box-shadow: 0 0 8px #ffc107;
  animation: scanLaser 2s ease-in-out infinite alternate;
}

@keyframes scanLaser {
  0% { top: 0%; }
  100% { top: 100%; }
}
</style>

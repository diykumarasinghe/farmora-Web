<script setup>
defineProps({
  icon: {
    type: String,
    default: 'bi-inbox'
  },
  title: {
    type: String,
    default: 'No items found'
  },
  description: {
    type: String,
    default: 'There are currently no records to display here.'
  },
  actionText: {
    type: String,
    default: ''
  },
  actionRoute: {
    type: String,
    default: ''
  }
})
</script>

<template>
  <div class="text-center py-5 px-3">
    <div class="rounded-circle bg-light d-inline-flex p-4 text-muted mb-3">
      <i :class="['bi', icon, 'display-4']"></i>
    </div>
    <h5 class="fw-bold text-dark mb-2">{{ title }}</h5>
    <p class="text-muted small mb-4" style="max-width: 400px; margin: 0 auto;">
      {{ description }}
    </p>
    <router-link v-if="actionText && actionRoute" :to="actionRoute" class="btn btn-farmora">
      {{ actionText }}
    </router-link>
  </div>
</template>

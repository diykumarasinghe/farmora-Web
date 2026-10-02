<script setup>
defineProps({
  message: {
    type: String,
    default: 'Loading data...'
  }
})
</script>

<template>
  <div class="d-flex flex-column align-items-center justify-content-center py-5">
    <div class="spinner-border text-success mb-3" style="width: 3rem; height: 3rem;" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
    <div class="text-muted fw-medium">{{ message }}</div>
  </div>
</template>

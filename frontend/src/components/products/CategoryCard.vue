<script setup>
defineProps({
  category: {
    type: Object,
    required: true
  },
  count: {
    type: Number,
    default: 0
  },
  isSelected: {
    type: Boolean,
    default: false
  }
})

defineEmits(['select'])
</script>

<template>
  <button
    class="category-card btn text-center border-0 p-2 d-flex flex-column align-items-center justify-content-center"
    :class="{ active: isSelected }"
    @click="$emit('select', category.name)"
    type="button"
  >
    <!-- Circular Image Container -->
    <div class="category-icon-wrapper rounded-circle mb-2 position-relative overflow-hidden">
      <img
        :src="category.image"
        :alt="category.name"
        class="category-img w-100 h-100 object-fit-cover"
        @error="$event.target.style.display = 'none'"
      />
      <div class="category-emoji position-absolute top-50 start-50 translate-middle">
        <span>{{ category.svgIcon }}</span>
      </div>
    </div>

    <!-- Category Label & Count -->
    <div class="category-name fw-semibold text-truncate w-100" :title="category.name">
      {{ category.name }}
    </div>
    <div class="category-count text-muted small">
      ({{ count || category.count }})
    </div>
  </button>
</template>

<style scoped>
.category-card {
  min-width: 86px;
  max-width: 98px;
  background: transparent;
  border-radius: 12px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.category-card:hover {
  background: #f1f8f3;
  transform: translateY(-2px);
}

.category-card.active {
  background: #e8f5e9;
  border: 1.5px solid #2d6a4f !important;
}

.category-icon-wrapper {
  width: 58px;
  height: 58px;
  background: #f3f4f6;
  border: 2px solid #e5e7eb;
  transition: all 0.2s ease;
}

.category-card:hover .category-icon-wrapper {
  border-color: #2d6a4f;
  transform: scale(1.05);
}

.category-card.active .category-icon-wrapper {
  border-color: #2d6a4f;
  box-shadow: 0 0 0 3px rgba(45, 106, 79, 0.2);
}

.category-img {
  opacity: 0.88;
  transition: opacity 0.2s ease;
}

.category-card:hover .category-img {
  opacity: 1;
}

.category-emoji {
  font-size: 1.35rem;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(2px);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.category-name {
  font-size: 0.82rem;
  color: #1f2937;
  line-height: 1.2;
}

.category-card.active .category-name {
  color: #2d6a4f;
  font-weight: 700;
}

.category-count {
  font-size: 0.72rem;
}
</style>

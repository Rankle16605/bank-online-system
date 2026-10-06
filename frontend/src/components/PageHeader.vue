<template>
  <div class="icbc-page-header" :class="{ 'animate-fadeInUp': animated }">
    <div class="header-accent"></div>
    <div class="header-content">
      <h2>{{ title }}</h2>
      <p v-if="subtitle" class="header-subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.extra" class="header-extra">
      <slot name="extra" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  animated: { type: Boolean, default: true }
})
</script>

<style scoped>
.icbc-page-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
  padding: 0;
}
.header-accent {
  width: 3px;
  height: 36px;
  background: var(--icbc-red);
  border-radius: 2px;
  flex-shrink: 0;
}
.header-content {
  flex: 1;
}
.header-content h2 {
  font-size: 22px;
  font-weight: 600;
  color: #1A1A1A;
  margin: 0;
  line-height: 1.3;
}
.header-subtitle {
  font-size: 13px;
  color: #999;
  margin: 2px 0 0;
}
.header-extra {
  flex-shrink: 0;
}
</style>

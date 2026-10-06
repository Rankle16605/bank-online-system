<template>
  <a-card :bordered="false" class="stat-card card-hover" :class="[`stat-${color}`, { 'animate-fadeInUp': animated }]">
    <div class="stat-body">
      <div class="stat-icon" :class="`icon-${color}`">
        <component :is="icon" />
      </div>
      <div class="stat-info">
        <div class="stat-title">{{ title }}</div>
        <div class="stat-value" :class="`text-${color}`">
          <span v-if="prefix" class="stat-prefix">{{ prefix }}</span>
          <span class="stat-number">{{ formattedValue }}</span>
          <span v-if="suffix" class="stat-suffix">{{ suffix }}</span>
        </div>
        <div v-if="trend !== undefined" class="stat-trend" :class="trendDirection">
          <span class="trend-icon">{{ trend >= 0 ? '↑' : '↓' }}</span>
          <span>{{ Math.abs(trend).toFixed(1) }}%</span>
          <span class="trend-label">{{ trendLabel }}</span>
        </div>
      </div>
    </div>
    <div class="stat-bg-pattern"></div>
  </a-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  value: { type: [Number, String], default: 0 },
  prefix: { type: String, default: '' },
  suffix: { type: String, default: '' },
  icon: { type: [Object, Function], default: null },
  color: { type: String, default: 'red' },
  trend: { type: Number, default: undefined },
  trendLabel: { type: String, default: '较上月' },
  precision: { type: Number, default: 0 },
  animated: { type: Boolean, default: true }
})

const formattedValue = computed(() => {
  const num = Number(props.value) || 0
  return num.toLocaleString('zh-CN', {
    minimumFractionDigits: props.precision,
    maximumFractionDigits: props.precision
  })
})

const trendDirection = computed(() => props.trend >= 0 ? 'up' : 'down')
</script>

<style scoped>
.stat-card {
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  height: 100%;
}
.stat-card :deep(.ant-card-body) {
  padding: 20px;
}
.stat-body {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  position: relative;
  z-index: 1;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}
.icon-red { background: #FFF1F0; color: #C8102E; }
.icon-green { background: #F6FFED; color: #52C41A; }
.icon-gold { background: #FFFBF0; color: #C4A265; }
.icon-blue { background: #E6F7FF; color: #1890FF; }
.stat-info {
  flex: 1;
  min-width: 0;
}
.stat-title {
  font-size: 13px;
  color: #999999;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 4px;
}
.text-red { color: #C8102E; }
.text-green { color: #52C41A; }
.text-gold { color: #C4A265; }
.text-blue { color: #1890FF; }
.stat-prefix, .stat-suffix {
  font-size: 16px;
  font-weight: 600;
}
.stat-number {
  font-variant-numeric: tabular-nums;
}
.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}
.stat-trend.up .trend-icon { color: #FF4D4F; }
.stat-trend.down .trend-icon { color: #52C41A; }
.trend-label { color: #999999; margin-left: 2px; }
.stat-bg-pattern {
  position: absolute;
  top: 0;
  right: 0;
  width: 80px;
  height: 80px;
  border-radius: 0 0 0 80px;
  opacity: 0.04;
}
.stat-red .stat-bg-pattern { background: #C8102E; }
.stat-green .stat-bg-pattern { background: #52C41A; }
.stat-gold .stat-bg-pattern { background: #C4A265; }
.stat-blue .stat-bg-pattern { background: #1890FF; }
</style>

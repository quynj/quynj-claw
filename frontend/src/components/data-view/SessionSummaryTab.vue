<template>
  <dl v-if="sessionStore.summary" class="kv-list summary-list">
    <template v-for="item in rows" :key="item.key">
      <dt>{{ item.label }}</dt>
      <dd>{{ item.value }}</dd>
    </template>
  </dl>
  <el-empty v-else description="No summary" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useSessionStore } from '../../stores/sessionStore'

const sessionStore = useSessionStore()

const rows = computed(() => {
  const summary = sessionStore.summary
  if (!summary) return []
  return [
    { key: 'sessionId', label: 'sessionId', value: summary.sessionId },
    { key: 'title', label: 'title', value: summary.title },
    { key: 'agentName', label: 'agentName', value: summary.agentName },
    { key: 'status', label: 'status', value: summary.status },
    { key: 'messageCount', label: 'messageCount', value: summary.messageCount },
    { key: 'traceCount', label: 'traceCount', value: summary.traceCount },
    { key: 'durationMs', label: 'duration', value: formatDuration(summary.durationMs) },
    { key: 'createdAt', label: 'createdAt', value: formatDateTime(summary.createdAt) },
    { key: 'updatedAt', label: 'updatedAt', value: formatDateTime(summary.updatedAt) }
  ]
})

function formatDuration(value: number) {
  if (!Number.isFinite(value) || value <= 0) return '0 ms'
  if (value < 1000) return `${value} ms`
  const totalSeconds = value / 1000
  if (totalSeconds < 60) return `${formatNumber(totalSeconds)} s`
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = Math.round(totalSeconds % 60)
  return `${minutes} min ${seconds.toString().padStart(2, '0')} s`
}

function formatDateTime(value: string) {
  if (!value) return ''
  const normalized = value.replace('T', ' ')
  const [date, time = ''] = normalized.split(' ')
  return `${date} ${time.slice(0, 8)}`.trim()
}

function formatNumber(value: number) {
  return Number.isInteger(value) ? value.toString() : value.toFixed(2).replace(/\.?0+$/, '')
}
</script>

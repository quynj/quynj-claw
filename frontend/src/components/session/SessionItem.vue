<template>
  <button class="session-item" :class="{ active }" @click="$emit('select')">
    <span class="status-dot" :class="session.status" />
    <span class="session-main">
      <strong>{{ session.title }}</strong>
      <small>{{ session.lastMessagePreview || session.agentName + ' · ' + session.modelName }}</small>
      <time>{{ formatTime(session.updatedAt) }}</time>
    </span>
    <span class="session-actions" @click.stop>
      <el-button :icon="Edit3" text circle @click="$emit('rename', session.id, session.title)" />
      <el-button :icon="Trash2" text circle @click="$emit('delete', session.id)" />
    </span>
  </button>
</template>

<script setup lang="ts">
import { Edit3, Trash2 } from 'lucide-vue-next'
import type { ChatSession } from '../../types/session'

defineProps<{ session: ChatSession; active: boolean }>()
defineEmits<{
  select: []
  rename: [id: string, title: string]
  delete: [id: string]
}>()

function formatTime(value: string) {
  return new Date(value).toLocaleString()
}
</script>

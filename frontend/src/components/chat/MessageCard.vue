<template>
  <article class="message-card" :class="[message.role, { active }]">
    <header>
      <strong>{{ message.name }}</strong>
      <span>{{ message.role }}</span>
      <time>{{ new Date(message.createdAt).toLocaleTimeString() }}</time>
    </header>
    <ContentBlockRenderer v-for="(block, index) in message.content" :key="index" :block="block" />
  </article>
</template>

<script setup lang="ts">
import ContentBlockRenderer from './blocks/ContentBlockRenderer.vue'
import type { AgentMessage } from '../../types/message'

defineProps<{ message: AgentMessage; active?: boolean }>()
</script>

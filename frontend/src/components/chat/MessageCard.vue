<template>
  <article class="message-card" :class="[message.role, { active }]">
    <img class="message-avatar" :src="avatarUrl" :alt="`${displayName} avatar`" />
    <div class="message-body">
      <header>
        <strong>{{ displayName }}</strong>
        <time>{{ createdAtLabel }}</time>
      </header>
      <ContentBlockRenderer
        v-for="(block, index) in nonImageBlocks"
        :key="index"
        :block="block"
        :streaming="streaming"
      />
      <ImageStack v-if="imageBlocks.length" :images="imageBlocks" :compact="imageBlocks.length > 1" />
      <footer v-if="generating" class="message-generating">
        <span />
        <span />
        <span />
        <em>正在生成回复</em>
      </footer>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ContentBlockRenderer from './blocks/ContentBlockRenderer.vue'
import ImageStack from './blocks/ImageStack.vue'
import type { AgentMessage, ImageBlock } from '../../types/message'

const props = defineProps<{
  agentAvatarUrl: string
  agentName?: string
  message: AgentMessage
  active?: boolean
  streaming?: boolean
  generating?: boolean
  userAvatarUrl: string
}>()
const imageBlocks = computed(() => props.message.content.filter((block): block is ImageBlock => block.type === 'image'))
const nonImageBlocks = computed(() => props.message.content.filter((block) => block.type !== 'image'))
const displayName = computed(() => {
  if (props.message.role === 'user') return 'you'
  if (props.message.role === 'assistant') return props.message.name || props.agentName || 'Sunday'
  return props.message.name || props.message.role
})
const avatarUrl = computed(() => (props.message.role === 'user' ? props.userAvatarUrl : props.agentAvatarUrl))
const createdAtLabel = computed(() =>
  new Date(props.message.createdAt).toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
  })
)
</script>

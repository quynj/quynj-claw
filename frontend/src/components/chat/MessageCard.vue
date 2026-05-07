<template>
  <article class="message-card" :class="[message.role, { active }]">
    <header>
      <strong>{{ message.name }}</strong>
      <span>{{ message.role }}</span>
      <time>{{ new Date(message.createdAt).toLocaleTimeString() }}</time>
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
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ContentBlockRenderer from './blocks/ContentBlockRenderer.vue'
import ImageStack from './blocks/ImageStack.vue'
import type { AgentMessage, ImageBlock } from '../../types/message'

const props = defineProps<{ message: AgentMessage; active?: boolean; streaming?: boolean; generating?: boolean }>()
const imageBlocks = computed(() => props.message.content.filter((block): block is ImageBlock => block.type === 'image'))
const nonImageBlocks = computed(() => props.message.content.filter((block) => block.type !== 'image'))
</script>

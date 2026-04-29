<template>
  <div class="markdown-block" v-html="html" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import type { TextBlock } from '../../../types/message'

const props = defineProps<{ block: TextBlock }>()
const html = computed(() => marked.parse(props.block.text || ''))
</script>

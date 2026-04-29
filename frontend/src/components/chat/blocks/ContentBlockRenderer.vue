<template>
  <TextBlock v-if="block.type === 'text'" :block="block" />
  <ErrorBlock v-else-if="block.type === 'error'" :block="block" />
  <ToolUseBlock v-else-if="block.type === 'tool_use'" :block="block" />
  <ToolResultBlock v-else-if="block.type === 'tool_result'" :block="block" />
  <RawJsonViewer v-else :value="block" />
</template>

<script setup lang="ts">
import TextBlock from './TextBlock.vue'
import ErrorBlock from './ErrorBlock.vue'
import ToolUseBlock from './ToolUseBlock.vue'
import ToolResultBlock from './ToolResultBlock.vue'
import RawJsonViewer from './RawJsonViewer.vue'
import type { ContentBlock } from '../../../types/message'

defineProps<{ block: ContentBlock }>()
</script>

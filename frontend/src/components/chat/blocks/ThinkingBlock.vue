<template>
  <el-collapse v-model="activeNames" class="thinking-block">
    <el-collapse-item title="Thinking" name="thinking">
      <p>{{ block.thinking }}</p>
      <RawJsonViewer v-if="hasMetadata" :value="block.metadata" />
    </el-collapse-item>
  </el-collapse>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import RawJsonViewer from './RawJsonViewer.vue'
import type { ThinkingBlock } from '../../../types/message'

const props = defineProps<{ block: ThinkingBlock; streaming?: boolean }>()
const activeNames = ref<string[]>(props.streaming ? ['thinking'] : [])
const hasMetadata = computed(() => Boolean(props.block.metadata && Object.keys(props.block.metadata).length))

watch(
  () => props.streaming,
  (streaming) => {
    activeNames.value = streaming ? ['thinking'] : []
  },
  { immediate: true }
)
</script>

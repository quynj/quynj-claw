<template>
  <el-empty v-if="runtimeStore.traces.length === 0" description="Trace is empty" />
  <RawJsonViewer v-else :value="runtimeStore.traces" />
</template>

<script setup lang="ts">
import RawJsonViewer from '../chat/blocks/RawJsonViewer.vue'
import { useRuntimeStore } from '../../stores/runtimeStore'
const runtimeStore = useRuntimeStore()
</script>

<template>
  <el-collapse class="tool-block">
    <el-collapse-item :title="`Tool result · ${block.name}`" name="tool">
      <RawJsonViewer :value="block.output" />
    </el-collapse-item>
  </el-collapse>
</template>

<script setup lang="ts">
import RawJsonViewer from './RawJsonViewer.vue'
import type { ToolResultBlock } from '../../../types/message'
defineProps<{ block: ToolResultBlock }>()
</script>

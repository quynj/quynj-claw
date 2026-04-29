<template>
  <el-collapse class="tool-block">
    <el-collapse-item :title="`Tool use · ${block.name}`" name="tool">
      <RawJsonViewer :value="block.input" />
    </el-collapse-item>
  </el-collapse>
</template>

<script setup lang="ts">
import RawJsonViewer from './RawJsonViewer.vue'
import type { ToolUseBlock } from '../../../types/message'
defineProps<{ block: ToolUseBlock }>()
</script>

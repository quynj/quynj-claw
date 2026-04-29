<template>
  <el-alert type="error" :title="block.message" :description="block.detail" show-icon :closable="false" />
</template>

<script setup lang="ts">
import type { ErrorBlock } from '../../../types/message'
defineProps<{ block: ErrorBlock }>()
</script>

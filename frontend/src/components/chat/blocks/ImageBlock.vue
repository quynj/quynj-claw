<template>
  <ImageStack :images="[block]" />
</template>

<script setup lang="ts">
import ImageStack from './ImageStack.vue'
import type { ImageBlock } from '../../../types/message'

defineProps<{ block: ImageBlock }>()
</script>

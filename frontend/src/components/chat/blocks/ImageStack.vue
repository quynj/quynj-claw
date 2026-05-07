<template>
  <div class="image-stack" :class="{ compact }">
    <button
      v-for="(image, index) in visibleImages"
      :key="`${image.url}:${index}`"
      class="image-stack-item"
      type="button"
      :style="compact ? stackStyle(index) : undefined"
      @click.stop="open(index)"
    >
      <img :src="image.url" :alt="image.alt || 'image attachment'" loading="lazy" />
      <span v-if="compact && index === visibleImages.length - 1 && hiddenCount > 0" class="image-count">
        +{{ hiddenCount }}
      </span>
    </button>
    <el-dialog
      v-model="previewOpen"
      class="image-preview-dialog"
      width="min(92vw, 980px)"
      append-to-body
      @keydown.left.prevent="previous"
      @keydown.right.prevent="next"
      @opened="focusPreview"
    >
      <div ref="previewRef" class="image-preview-stage" tabindex="0">
        <button
          v-if="canNavigate"
          class="image-preview-nav previous"
          type="button"
          aria-label="上一张图片"
          @click="previous"
        >
          ‹
        </button>
        <img v-if="activeImage" class="image-preview-full" :src="activeImage.url" :alt="activeImage.alt || 'image preview'" />
        <button
          v-if="canNavigate"
          class="image-preview-nav next"
          type="button"
          aria-label="下一张图片"
          @click="next"
        >
          ›
        </button>
      </div>
      <template #footer>
        <span class="image-preview-caption">
          {{ activeImage?.alt || '' }}
          <em v-if="canNavigate">{{ activeIndex + 1 }} / {{ images.length }}</em>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ImageBlock } from '../../../types/message'

const props = defineProps<{ images: ImageBlock[]; compact?: boolean }>()
const previewOpen = ref(false)
const activeIndex = ref(0)
const previewRef = ref<HTMLElement>()

const visibleImages = computed(() => (props.compact ? props.images.slice(0, 3) : props.images))
const hiddenCount = computed(() => Math.max(props.images.length - visibleImages.value.length, 0))
const activeImage = computed(() => props.images[activeIndex.value])
const canNavigate = computed(() => props.images.length > 1)

function open(index: number) {
  activeIndex.value = index
  previewOpen.value = true
}

function previous() {
  if (!canNavigate.value) return
  activeIndex.value = (activeIndex.value - 1 + props.images.length) % props.images.length
}

function next() {
  if (!canNavigate.value) return
  activeIndex.value = (activeIndex.value + 1) % props.images.length
}

function focusPreview() {
  previewRef.value?.focus()
}

function stackStyle(index: number) {
  return {
    transform: `translate(${index * 18}px, ${index * 8}px) rotate(${index * 2 - 2}deg)`,
    zIndex: String(visibleImages.value.length - index)
  }
}
</script>

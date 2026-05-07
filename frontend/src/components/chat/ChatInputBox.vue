<template>
  <form
    class="chat-input"
    :class="{ 'drag-over': dragging }"
    @submit.prevent="submit"
    @dragenter.prevent="dragging = true"
    @dragover.prevent="dragging = true"
    @dragleave.prevent="dragging = false"
    @drop.prevent="handleDrop"
    @paste="handlePaste"
  >
    <div v-if="attachments.length" class="attachment-tray">
      <div v-for="(attachment, index) in attachments" :key="attachment.id" class="attachment-chip">
        <button class="attachment-preview-button" type="button" @click="openPreview(index)">
          <img :src="attachment.url" :alt="attachment.fileName" />
        </button>
        <div>
          <strong>{{ attachment.fileName }}</strong>
          <span>{{ formatSize(attachment.size) }}</span>
        </div>
        <el-button
          :icon="X"
          circle
          text
          :disabled="busy"
          native-type="button"
          @click="removeAttachment(attachment.id)"
        />
      </div>
    </div>
    <el-input
      v-model="text"
      type="textarea"
      :autosize="{ minRows: 2, maxRows: 6 }"
      resize="none"
      placeholder="Message Sunday..."
      :disabled="busy"
      @keydown.enter.exact.prevent="submit"
    />
    <div class="chat-input-bar">
      <div class="chat-input-tools">
        <input ref="fileInputRef" type="file" accept="image/*" multiple hidden @change="handleFileInput" />
        <el-tooltip content="添加图片">
          <el-button
            :icon="Paperclip"
            circle
            plain
            :disabled="disabled || busy || !sessionId"
            native-type="button"
            @click="fileInputRef?.click()"
          />
        </el-tooltip>
        <el-tooltip :content="voiceTooltip">
          <el-button
            :icon="Mic"
            circle
            plain
            :class="{ listening }"
            :disabled="disabled || busy || !speechSupported"
            native-type="button"
            @click="toggleVoice"
          />
        </el-tooltip>
      </div>
      <span v-if="inputStatusLabel" class="input-status" :class="status">{{ inputStatusLabel }}</span>
      <span v-else />
      <el-tooltip :content="actionTooltip">
        <el-button
          v-if="loading"
          type="danger"
          :icon="Square"
          :loading="cancelling"
          circle
          native-type="button"
          @click="emit('cancel')"
        />
        <el-button
          v-else
          type="primary"
          :icon="SendHorizontal"
          :disabled="disabled || uploading || !canSend"
          circle
          native-type="submit"
        />
      </el-tooltip>
    </div>
    <el-dialog
      v-model="previewOpen"
      class="image-preview-dialog"
      width="min(92vw, 980px)"
      append-to-body
      @keydown.left.prevent="previousPreview"
      @keydown.right.prevent="nextPreview"
      @opened="focusPreview"
    >
      <div ref="previewRef" class="image-preview-stage" tabindex="0">
        <button
          v-if="canNavigatePreview"
          class="image-preview-nav previous"
          type="button"
          aria-label="上一张图片"
          @click="previousPreview"
        >
          ‹
        </button>
        <img
          v-if="activeAttachment"
          class="image-preview-full"
          :src="activeAttachment.url"
          :alt="activeAttachment.fileName"
        />
        <button
          v-if="canNavigatePreview"
          class="image-preview-nav next"
          type="button"
          aria-label="下一张图片"
          @click="nextPreview"
        >
          ›
        </button>
      </div>
      <template #footer>
        <span class="image-preview-caption">
          {{ activeAttachment?.fileName || '' }}
          <em v-if="canNavigatePreview">{{ previewIndex + 1 }} / {{ attachments.length }}</em>
        </span>
      </template>
    </el-dialog>
  </form>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Mic, Paperclip, SendHorizontal, Square, X } from 'lucide-vue-next'
import { uploadImage } from '../../api/message'
import type { MessageAttachment } from '../../types/message'

const props = defineProps<{
  sessionId?: string
  disabled?: boolean
  loading?: boolean
  cancelling?: boolean
  status?: string
}>()
const emit = defineEmits<{ send: [text: string, attachments: MessageAttachment[]]; cancel: [] }>()
const text = ref('')
const attachments = ref<MessageAttachment[]>([])
const uploading = ref(false)
const dragging = ref(false)
const listening = ref(false)
const fileInputRef = ref<HTMLInputElement>()
const previewOpen = ref(false)
const previewIndex = ref(0)
const previewRef = ref<HTMLElement>()
const speechSupported = typeof window !== 'undefined' && ('SpeechRecognition' in window || 'webkitSpeechRecognition' in window)
let recognition: BrowserSpeechRecognition | undefined
const busy = computed(() => props.loading || props.cancelling || uploading.value)
const canSend = computed(() => Boolean(text.value.trim()) || attachments.value.length > 0)
const activeAttachment = computed(() => attachments.value[previewIndex.value])
const canNavigatePreview = computed(() => attachments.value.length > 1)

const inputStatusLabel = computed(() => {
  if (props.cancelling) return '正在取消'
  if (uploading.value) return '图片上传中'
  return ''
})

const actionTooltip = computed(() => (props.loading ? '停止回复' : '发送消息'))
const voiceTooltip = computed(() => {
  if (!speechSupported) return '当前浏览器不支持语音输入'
  return listening.value ? '停止语音输入' : '语音输入'
})

function submit() {
  if (busy.value || !canSend.value) return
  const value = text.value.trim()
  emit('send', value, [...attachments.value])
  text.value = ''
  attachments.value = []
}

async function addFiles(files: FileList | File[]) {
  if (!props.sessionId || props.disabled || props.loading) return
  const available = Math.max(6 - attachments.value.length, 0)
  const images = Array.from(files)
    .filter((file) => file.type.startsWith('image/'))
    .slice(0, available)
  if (!images.length) return
  uploading.value = true
  try {
    const results = await Promise.allSettled(images.map((file) => uploadImage(props.sessionId!, file)))
    let failed = 0
    for (const result of results) {
      if (result.status === 'fulfilled') {
        attachments.value.push(result.value)
      } else {
        failed += 1
      }
    }
    if (failed > 0) {
      ElMessage.error(`${failed} 张图片上传失败`)
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '图片上传失败')
  } finally {
    uploading.value = false
    if (fileInputRef.value) fileInputRef.value.value = ''
  }
}

function handleFileInput(event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files) addFiles(input.files)
}

function handleDrop(event: DragEvent) {
  dragging.value = false
  if (event.dataTransfer?.files) addFiles(event.dataTransfer.files)
}

function handlePaste(event: ClipboardEvent) {
  const files = Array.from(event.clipboardData?.files || []).filter((file) => file.type.startsWith('image/'))
  if (!files.length) return
  event.preventDefault()
  addFiles(files)
}

function removeAttachment(id: string) {
  attachments.value = attachments.value.filter((attachment) => attachment.id !== id)
}

function openPreview(index: number) {
  previewIndex.value = index
  previewOpen.value = true
}

function previousPreview() {
  if (!canNavigatePreview.value) return
  previewIndex.value = (previewIndex.value - 1 + attachments.value.length) % attachments.value.length
}

function nextPreview() {
  if (!canNavigatePreview.value) return
  previewIndex.value = (previewIndex.value + 1) % attachments.value.length
}

function focusPreview() {
  previewRef.value?.focus()
}

function formatSize(size: number) {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

function toggleVoice() {
  if (!speechSupported) return
  if (listening.value) {
    recognition?.stop()
    return
  }
  const SpeechRecognitionConstructor = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognitionConstructor) return
  recognition = new SpeechRecognitionConstructor()
  recognition.lang = navigator.language || 'zh-CN'
  recognition.interimResults = true
  recognition.continuous = false
  const original = text.value
  recognition.onresult = (event) => {
    const transcript = Array.from(event.results)
      .map((result) => result[0]?.transcript || '')
      .join('')
    text.value = `${original}${original && transcript ? ' ' : ''}${transcript}`
  }
  recognition.onend = () => {
    listening.value = false
  }
  listening.value = true
  recognition.start()
}

onBeforeUnmount(() => recognition?.stop())

interface BrowserSpeechRecognitionEvent extends Event {
  results: SpeechRecognitionResultList
}

interface BrowserSpeechRecognition extends EventTarget {
  lang: string
  interimResults: boolean
  continuous: boolean
  onresult: ((event: BrowserSpeechRecognitionEvent) => void) | null
  onend: (() => void) | null
  start: () => void
  stop: () => void
}

declare global {
  interface Window {
    SpeechRecognition?: new () => BrowserSpeechRecognition
    webkitSpeechRecognition?: new () => BrowserSpeechRecognition
  }
}
</script>

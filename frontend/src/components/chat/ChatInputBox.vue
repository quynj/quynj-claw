<template>
  <form class="chat-input" @submit.prevent="submit">
    <el-input
      v-model="text"
      type="textarea"
      :autosize="{ minRows: 2, maxRows: 6 }"
      resize="none"
      placeholder="Message Sunday..."
      @keydown.meta.enter.prevent="submit"
      @keydown.ctrl.enter.prevent="submit"
    />
    <el-button type="primary" :icon="SendHorizontal" :loading="loading" :disabled="disabled || !text.trim()" native-type="submit" />
  </form>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { SendHorizontal } from 'lucide-vue-next'

defineProps<{ disabled?: boolean; loading?: boolean }>()
const emit = defineEmits<{ send: [text: string] }>()
const text = ref('')

function submit() {
  const value = text.value.trim()
  if (!value) return
  emit('send', value)
  text.value = ''
}
</script>

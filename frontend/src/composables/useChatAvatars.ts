import { computed, ref } from 'vue'

export const chatAvatarUrls = [
  new URL('../assets/avatars/avatar-01.svg', import.meta.url).href,
  new URL('../assets/avatars/avatar-02.svg', import.meta.url).href,
  new URL('../assets/avatars/avatar-03.svg', import.meta.url).href,
  new URL('../assets/avatars/avatar-04.svg', import.meta.url).href,
  new URL('../assets/avatars/avatar-05.svg', import.meta.url).href,
  new URL('../assets/avatars/avatar-06.svg', import.meta.url).href,
  new URL('../assets/avatars/avatar-07.svg', import.meta.url).href,
  new URL('../assets/avatars/avatar-08.svg', import.meta.url).href,
  new URL('../assets/avatars/avatar-09.svg', import.meta.url).href,
  new URL('../assets/avatars/avatar-10.svg', import.meta.url).href,
]

const STORAGE_KEY = 'quynj-claw-chat-avatars'
const userAvatarIndex = ref(0)
const agentAvatarIndex = ref(1)

loadAvatarSelection()

export function useChatAvatars() {
  const userAvatarUrl = computed(() => chatAvatarUrls[userAvatarIndex.value])
  const agentAvatarUrl = computed(() => chatAvatarUrls[agentAvatarIndex.value])

  function randomizeUserAvatar() {
    userAvatarIndex.value = randomDifferentIndex(userAvatarIndex.value)
    saveAvatarSelection()
  }

  function randomizeAgentAvatar() {
    agentAvatarIndex.value = randomDifferentIndex(agentAvatarIndex.value)
    saveAvatarSelection()
  }

  function randomizeBothAvatars() {
    randomizeUserAvatar()
    randomizeAgentAvatar()
  }

  return {
    agentAvatarUrl,
    chatAvatarUrls,
    randomizeAgentAvatar,
    randomizeBothAvatars,
    randomizeUserAvatar,
    userAvatarUrl,
  }
}

function randomDifferentIndex(current: number) {
  if (chatAvatarUrls.length <= 1) return 0
  let next = current
  while (next === current) {
    next = Math.floor(Math.random() * chatAvatarUrls.length)
  }
  return next
}

function loadAvatarSelection() {
  if (typeof window === 'undefined') return
  try {
    const saved = JSON.parse(window.localStorage.getItem(STORAGE_KEY) || '{}') as Partial<{
      user: number
      agent: number
    }>
    if (typeof saved.user === 'number' && saved.user >= 0 && saved.user < chatAvatarUrls.length) {
      userAvatarIndex.value = saved.user
    }
    if (typeof saved.agent === 'number' && saved.agent >= 0 && saved.agent < chatAvatarUrls.length) {
      agentAvatarIndex.value = saved.agent
    }
  } catch {
    window.localStorage.removeItem(STORAGE_KEY)
  }
}

function saveAvatarSelection() {
  if (typeof window === 'undefined') return
  window.localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({
      agent: agentAvatarIndex.value,
      user: userAvatarIndex.value,
    })
  )
}

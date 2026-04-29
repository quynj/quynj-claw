import { request } from './http'
import type { AgentMessage } from '../types/message'

export function listMessages(sessionId: string) {
  return request<AgentMessage[]>(`/api/sessions/${sessionId}/messages`)
}

export function sendMessage(sessionId: string, text: string, stream = true) {
  return request<{ message: AgentMessage }>(`/api/sessions/${sessionId}/messages`, {
    method: 'POST',
    body: JSON.stringify({ text, stream })
  })
}

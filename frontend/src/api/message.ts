import { request } from './http'
import type { AgentMessage, MessageAttachment } from '../types/message'

export function listMessages(sessionId: string) {
  return request<AgentMessage[]>(`/api/sessions/${sessionId}/messages`)
}

export function uploadImage(sessionId: string, file: File) {
  const body = new FormData()
  body.append('file', file)
  return request<MessageAttachment>(`/api/sessions/${sessionId}/files`, {
    method: 'POST',
    body
  })
}

export function sendMessage(sessionId: string, text: string, attachments: MessageAttachment[] = [], stream = true) {
  return request<{ message: AgentMessage }>(`/api/sessions/${sessionId}/messages`, {
    method: 'POST',
    body: JSON.stringify({ text, stream, attachments })
  })
}

export function cancelCurrentMessage(sessionId: string) {
  return request<void>(`/api/sessions/${sessionId}/messages/current/cancel`, { method: 'POST' })
}

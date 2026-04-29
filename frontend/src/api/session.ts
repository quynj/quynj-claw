import { request } from './http'
import type { ChatSession, SessionSummary } from '../types/session'

export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

export function listSessions(keyword = '') {
  return request<PageResult<ChatSession>>(`/api/sessions?keyword=${encodeURIComponent(keyword)}&page=1&pageSize=50`)
}

export function createSession(payload: Partial<ChatSession>) {
  return request<ChatSession>('/api/sessions', { method: 'POST', body: JSON.stringify(payload) })
}

export function updateSession(id: string, payload: Partial<ChatSession>) {
  return request<ChatSession>(`/api/sessions/${id}`, { method: 'PATCH', body: JSON.stringify(payload) })
}

export function deleteSession(id: string) {
  return request<void>(`/api/sessions/${id}`, { method: 'DELETE' })
}

export function getSummary(id: string) {
  return request<SessionSummary>(`/api/sessions/${id}/summary`)
}

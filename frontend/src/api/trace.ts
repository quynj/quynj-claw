import { request } from './http'
import type { TraceSpan } from '../types/trace'

export function listTraces(sessionId: string) {
  return request<TraceSpan[]>(`/api/sessions/${sessionId}/traces`)
}

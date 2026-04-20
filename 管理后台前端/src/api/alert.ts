import request from '@/utils/request'

export function getAlertList(params: any) {
  return request.get('/scm/alerts', { params })
}

export function getUnreadCount() {
  return request.get('/scm/alerts/unread-count')
}

export function markAsRead(id: number) {
  return request.post(`/scm/alerts/${id}/read`)
}

export function markAllAsRead() {
  return request.post('/scm/alerts/read-all')
}

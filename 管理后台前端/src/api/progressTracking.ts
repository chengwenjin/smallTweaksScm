import request from '@/utils/request'

export function getProgressList(params: any) {
  return request.get('/scm/production-progress', { params })
}

export function getProgressDetail(id: number) {
  return request.get(`/scm/production-progress/${id}`)
}

export function getProgressByOrder(orderId: number) {
  return request.get(`/scm/production-progress/order/${orderId}`)
}

export function createProgressForOrder(orderId: number) {
  return request.post(`/scm/production-progress/order/${orderId}/create`)
}

export function updateProgress(id: number, data: any) {
  return request.put(`/scm/production-progress/${id}`, data)
}

export function updateProgressStatus(id: number, status: number) {
  return request.put(`/scm/production-progress/${id}/status`, { status })
}

export function getShipmentList(params: any) {
  return request.get('/scm/shipments', { params })
}

export function getShipmentDetail(id: number) {
  return request.get(`/scm/shipments/${id}`)
}

export function getLogisticsTracks(shipmentId: number) {
  return request.get(`/scm/shipments/${shipmentId}/tracks`)
}

export function createShipment(orderId: number) {
  return request.post(`/scm/shipments/order/${orderId}/create`)
}

export function updateShipmentStatus(id: number, data: any) {
  return request.put(`/scm/shipments/${id}/status`, data)
}

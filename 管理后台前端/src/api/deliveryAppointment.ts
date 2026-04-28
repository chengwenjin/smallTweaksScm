import request from '@/utils/request'

export function getAppointmentList(params: any) {
  return request.get('/scm/delivery-appointments', { params })
}

export function getAppointmentDetail(id: number) {
  return request.get(`/scm/delivery-appointments/${id}`)
}

export function createAppointment(data: any) {
  return request.post('/scm/delivery-appointments', data)
}

export function confirmAppointment(id: number) {
  return request.put(`/scm/delivery-appointments/${id}/confirm`)
}

export function checkInAppointment(id: number) {
  return request.put(`/scm/delivery-appointments/${id}/check-in`)
}

export function completeAppointment(id: number, warehouseOperator: string) {
  return request.put(`/scm/delivery-appointments/${id}/complete`, { warehouseOperator })
}

export function cancelAppointment(id: number, reason: string) {
  return request.put(`/scm/delivery-appointments/${id}/cancel`, { reason })
}

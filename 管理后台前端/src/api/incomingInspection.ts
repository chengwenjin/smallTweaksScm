import request from '@/utils/request'

export function getInspectionList(params: any) {
  return request.get('/scm/incoming-inspections', { params })
}

export function getInspectionDetail(id: number) {
  return request.get(`/scm/incoming-inspections/${id}`)
}

export function getInspectionsByOrder(orderId: number) {
  return request.get(`/scm/incoming-inspections/order/${orderId}`)
}

export function createInspection(data: any) {
  return request.post('/scm/incoming-inspections', data)
}

export function submitInspection(id: number) {
  return request.put(`/scm/incoming-inspections/${id}/submit`)
}

export function approveInspection(id: number, handlingType: number, remark: string) {
  return request.put(`/scm/incoming-inspections/${id}/approve`, { handlingType, remark })
}

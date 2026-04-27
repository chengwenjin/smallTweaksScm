import request from '@/utils/request'

export function getPurchaseRequestList(params: any) {
  return request.get('/scm/purchase-requests', { params })
}

export function getPurchaseRequestDetail(id: number) {
  return request.get(`/scm/purchase-requests/${id}`)
}

export function createPurchaseRequest(data: any) {
  return request.post('/scm/purchase-requests', data)
}

export function updatePurchaseRequest(id: number, data: any) {
  return request.put(`/scm/purchase-requests/${id}`, data)
}

export function deletePurchaseRequest(id: number) {
  return request.delete(`/scm/purchase-requests/${id}`)
}

export function submitPurchaseRequest(id: number) {
  return request.post(`/scm/purchase-requests/${id}/submit`)
}

export function updatePurchaseRequestStatus(id: number, data: any) {
  return request.put(`/scm/purchase-requests/${id}/status`, data)
}

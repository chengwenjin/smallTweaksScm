import request from '@/utils/request'

export function getPurchaseOrderList(params: any) {
  return request.get('/scm/purchase-orders', { params })
}

export function getPurchaseOrderDetail(id: number) {
  return request.get(`/scm/purchase-orders/${id}`)
}

export function createPurchaseOrder(data: any) {
  return request.post('/scm/purchase-orders', data)
}

export function updatePurchaseOrder(id: number, data: any) {
  return request.put(`/scm/purchase-orders/${id}`, data)
}

export function deletePurchaseOrder(id: number) {
  return request.delete(`/scm/purchase-orders/${id}`)
}

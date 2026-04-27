import request from '@/utils/request'

export function getProductionWorkOrderList(params: any) {
  return request.get('/scm/production-work-orders', { params })
}

export function getProductionWorkOrderDetail(id: number) {
  return request.get(`/scm/production-work-orders/${id}`)
}

export function createProductionWorkOrder(data: any) {
  return request.post('/scm/production-work-orders', data)
}

export function updateProductionWorkOrder(id: number, data: any) {
  return request.put(`/scm/production-work-orders/${id}`, data)
}

export function deleteProductionWorkOrder(id: number) {
  return request.delete(`/scm/production-work-orders/${id}`)
}

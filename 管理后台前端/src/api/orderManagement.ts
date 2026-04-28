import request from '@/utils/request'

export function getOrderList(params: any) {
  return request.get('/scm/orders', { params })
}

export function getOrderDetail(id: number) {
  return request.get(`/scm/orders/${id}`)
}

export function getOrderChanges(id: number) {
  return request.get(`/scm/orders/${id}/changes`)
}

export function createOrder(data: any) {
  return request.post('/scm/orders', data)
}

export function updateOrder(id: number, data: any) {
  return request.put(`/scm/orders/${id}`, data)
}

export function issueOrder(id: number) {
  return request.put(`/scm/orders/${id}/issue`)
}

export function confirmOrder(data: any) {
  return request.post('/scm/orders/confirm', data)
}

export function changeOrder(data: any) {
  return request.post('/scm/orders/change', data)
}

export function cancelOrder(data: any) {
  return request.post('/scm/orders/cancel', data)
}

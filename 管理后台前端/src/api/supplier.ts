import request from '@/utils/request'

export function getSupplierList(params: any) {
  return request.get('/scm/suppliers', { params })
}

export function getSupplierDetail(id: number) {
  return request.get(`/scm/suppliers/${id}`)
}

export function createSupplier(data: any) {
  return request.post('/scm/suppliers', data)
}

export function updateSupplier(id: number, data: any) {
  return request.put(`/scm/suppliers/${id}`, data)
}

export function deleteSupplier(id: number) {
  return request.delete(`/scm/suppliers/${id}`)
}

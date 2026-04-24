import request from '@/utils/request'

export function getBlacklistList(params: any) {
  return request.get('/scm/blacklists', { params })
}

export function getBlacklistDetail(id: number) {
  return request.get(`/scm/blacklists/${id}`)
}

export function checkSupplierBlacklisted(supplierId: number) {
  return request.get(`/scm/blacklists/check/${supplierId}`)
}

export function addToBlacklist(data: any) {
  return request.post('/scm/blacklists', data)
}

export function removeFromBlacklist(id: number, data: any) {
  return request.put(`/scm/blacklists/${id}/remove`, data)
}

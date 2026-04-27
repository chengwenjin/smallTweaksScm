import request from '@/utils/request'

export function getBomList(params: any) {
  return request.get('/scm/boms', { params })
}

export function getBomDetail(id: number) {
  return request.get(`/scm/boms/${id}`)
}

export function createBom(data: any) {
  return request.post('/scm/boms', data)
}

export function updateBom(id: number, data: any) {
  return request.put(`/scm/boms/${id}`, data)
}

export function deleteBom(id: number) {
  return request.delete(`/scm/boms/${id}`)
}

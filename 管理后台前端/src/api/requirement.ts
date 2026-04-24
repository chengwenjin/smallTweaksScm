import request from '@/utils/request'

export function getRequirementList(params: any) {
  return request.get('/scm/requirements', { params })
}

export function getRequirementDetail(id: number) {
  return request.get(`/scm/requirements/${id}`)
}

export function createRequirement(data: any) {
  return request.post('/scm/requirements', data)
}

export function updateRequirement(id: number, data: any) {
  return request.put(`/scm/requirements/${id}`, data)
}

export function deleteRequirement(id: number) {
  return request.delete(`/scm/requirements/${id}`)
}

export function batchDeleteRequirement(ids: number[]) {
  return request.delete('/scm/requirements/batch', { data: ids })
}

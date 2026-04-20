import request from '@/utils/request'

export function getQualificationList(params: any) {
  return request.get('/scm/qualifications', { params })
}

export function getQualificationDetail(id: number) {
  return request.get(`/scm/qualifications/${id}`)
}

export function createQualification(data: any) {
  return request.post('/scm/qualifications', data)
}

export function updateQualification(id: number, data: any) {
  return request.put(`/scm/qualifications/${id}`, data)
}

export function deleteQualification(id: number) {
  return request.delete(`/scm/qualifications/${id}`)
}

export function auditQualification(id: number, data: any) {
  return request.post(`/scm/qualifications/${id}/audit`, data)
}

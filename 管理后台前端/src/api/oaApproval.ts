import request from '@/utils/request'

export function getOaApprovalList(params: any) {
  return request.get('/scm/oa-approvals', { params })
}

export function getOaApprovalDetail(id: number) {
  return request.get(`/scm/oa-approvals/${id}`)
}

export function submitToOa(data: any) {
  return request.post('/scm/oa-approvals/submit', data)
}

export function processApproval(id: number, data: any) {
  return request.post(`/scm/oa-approvals/${id}/process`, data)
}

export function withdrawApproval(id: number) {
  return request.post(`/scm/oa-approvals/${id}/withdraw`)
}

import request from '@/utils/request'

export function getDemandSummaryList(params: any) {
  return request.get('/scm/demand-summaries', { params })
}

export function getDemandSummaryDetail(id: number) {
  return request.get(`/scm/demand-summaries/${id}`)
}

export function generateDemandSummary(data: any) {
  return request.post('/scm/demand-summaries/generate', data)
}

export function confirmDemandSummary(id: number) {
  return request.post(`/scm/demand-summaries/${id}/confirm`)
}

export function deleteDemandSummary(id: number) {
  return request.delete(`/scm/demand-summaries/${id}`)
}

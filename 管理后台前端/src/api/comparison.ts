import request from '@/utils/request'

export function getComparisonList(params: any) {
  return request.get('/scm/comparisons', { params })
}

export function getComparisonDetail(id: number) {
  return request.get(`/scm/comparisons/${id}`)
}

export function createComparison(data: any) {
  return request.post('/scm/comparisons', data)
}

export function startComparison(id: number) {
  return request.post(`/scm/comparisons/${id}/start`)
}

export function getQuotesForComparison(inquiryId: number) {
  return request.get(`/scm/comparisons/quotes/${inquiryId}`)
}

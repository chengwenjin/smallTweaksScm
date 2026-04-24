import request from '@/utils/request'

export function getTenderList(params: any) {
  return request.get('/scm/tenders', { params })
}

export function getTenderDetail(id: number) {
  return request.get(`/scm/tenders/${id}`)
}

export function createTender(data: any) {
  return request.post('/scm/tenders', data)
}

export function updateTender(id: number, data: any) {
  return request.put(`/scm/tenders/${id}`, data)
}

export function deleteTender(id: number) {
  return request.delete(`/scm/tenders/${id}`)
}

export function publishTender(id: number) {
  return request.put(`/scm/tenders/${id}/publish`)
}

export function cancelTender(id: number) {
  return request.put(`/scm/tenders/${id}/cancel`)
}

export function openTender(id: number) {
  return request.put(`/scm/tenders/${id}/open`)
}

export function awardTender(id: number, data: any) {
  return request.post(`/scm/tenders/${id}/award`, data)
}

export function getTenderBids(tenderId: number) {
  return request.get(`/scm/tenders/${tenderId}/bids`)
}

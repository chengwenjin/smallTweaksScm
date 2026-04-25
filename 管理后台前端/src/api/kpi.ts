import request from '@/utils/request'

export function getKpiList(params: any) {
  return request.get('/api/scm/kpis', { params })
}

export function getKpiDetail(id: number) {
  return request.get(`/api/scm/kpis/${id}`)
}

export function calculateKpis(data: any) {
  return request.post('/api/scm/kpis/calculate', null, { params: data })
}

import request from '@/utils/request'

export function getKpiList(params: any) {
  return request.get('/scm/kpis', { params })
}

export function getKpiDetail(id: number) {
  return request.get(`/scm/kpis/${id}`)
}

export function calculateKpis(data: any) {
  return request.post('/scm/kpis/calculate', null, { params: data })
}

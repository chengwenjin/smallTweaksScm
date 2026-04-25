import request from '@/utils/request'

export function getReportList(params: any) {
  return request.get('/api/scm/reports', { params })
}

export function getReportDetail(id: number) {
  return request.get(`/api/scm/reports/${id}`)
}

export function generateReports(data: any) {
  return request.post('/api/scm/reports/generate', null, { params: data })
}

import request from '@/utils/request'

export function getReportList(params: any) {
  return request.get('/scm/reports', { params })
}

export function getReportDetail(id: number) {
  return request.get(`/scm/reports/${id}`)
}

export function generateReports(data: any) {
  return request.post('/scm/reports/generate', null, { params: data })
}

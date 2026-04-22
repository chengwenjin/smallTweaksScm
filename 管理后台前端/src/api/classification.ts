import request from '@/utils/request'

// 批量设置供应商分级分类
export function setClassification(data: any) {
  return request.post('/scm/classification/set', data)
}

// 获取分级分类变更记录
export function getClassificationLogs(params: any) {
  return request.get('/scm/classification/logs', { params })
}

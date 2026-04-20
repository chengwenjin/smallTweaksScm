import request from '@/utils/request'

// 操作日志列表（分页）
export function getOperationLogList(params: any) {
  return request.get('/system/operation-logs', { params })
}

// 操作日志详情
export function getOperationLogDetail(id: number) {
  return request.get(`/system/operation-logs/${id}`)
}

// 删除操作日志
export function deleteOperationLog(id: number) {
  return request.delete(`/system/operation-logs/${id}`)
}

// 批量删除操作日志
export function batchDeleteOperationLogs(ids: string) {
  return request.delete('/system/operation-logs/batch', { params: { ids } })
}

// 清空所有操作日志
export function clearAllOperationLogs() {
  return request.delete('/system/operation-logs/clear')
}

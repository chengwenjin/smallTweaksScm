import request from '@/utils/request'

// 登录日志列表（分页）
export function getLoginLogList(params: any) {
  return request.get('/system/login-logs', { params })
}

// 登录日志详情
export function getLoginLogDetail(id: number) {
  return request.get(`/system/login-logs/${id}`)
}

// 删除登录日志
export function deleteLoginLog(id: number) {
  return request.delete(`/system/login-logs/${id}`)
}

// 批量删除登录日志
export function batchDeleteLoginLogs(ids: string) {
  return request.delete('/system/login-logs/batch', { params: { ids } })
}

// 清空所有登录日志
export function clearAllLoginLogs() {
  return request.delete('/system/login-logs/clear')
}

import request from '@/utils/request'

// API权限列表（分页）
export function getApiPermissionList(params: any) {
  return request.get('/system/api-permissions', { params })
}

// API权限详情
export function getApiPermissionDetail(id: number) {
  return request.get(`/system/api-permissions/${id}`)
}

// 创建API权限
export function createApiPermission(data: any) {
  return request.post('/system/api-permissions', data)
}

// 更新API权限
export function updateApiPermission(id: number, data: any) {
  return request.put(`/system/api-permissions/${id}`, data)
}

// 删除API权限
export function deleteApiPermission(id: number) {
  return request.delete(`/system/api-permissions/${id}`)
}

// 获取所有启用的API权限
export function getAllEnabledPermissions() {
  return request.get('/system/api-permissions/enabled')
}

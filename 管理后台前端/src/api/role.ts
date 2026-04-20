import request from '@/utils/request'

// 角色列表（分页）
export function getRoleList(params: any) {
  return request.get('/system/roles', { params })
}

// 所有角色
export function getAllRoles() {
  return request.get('/system/roles/all')
}

// 角色详情
export function getRoleDetail(id: number) {
  return request.get(`/system/roles/${id}`)
}

// 创建角色
export function createRole(data: any) {
  return request.post('/system/roles', data)
}

// 更新角色
export function updateRole(id: number, data: any) {
  return request.put(`/system/roles/${id}`, data)
}

// 删除角色
export function deleteRole(id: number) {
  return request.delete(`/system/roles/${id}`)
}

// 更新角色状态
export function updateRoleStatus(id: number, data: any) {
  return request.put(`/system/roles/${id}/status`, data)
}

// 分配权限
export function assignPermissions(id: number, data: any) {
  return request.put(`/system/roles/${id}/permissions`, data)
}

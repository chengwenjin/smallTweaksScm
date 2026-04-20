import request from '@/utils/request'

// 用户列表（分页）
export function getUserList(params: any) {
  return request.get('/system/users', { params })
}

// 用户详情
export function getUserDetail(id: number) {
  return request.get(`/system/users/${id}`)
}

// 创建用户
export function createUser(data: any) {
  return request.post('/system/users', data)
}

// 更新用户
export function updateUser(id: number, data: any) {
  return request.put(`/system/users/${id}`, data)
}

// 删除用户
export function deleteUser(id: number) {
  return request.delete(`/system/users/${id}`)
}

// 更新用户状态
export function updateUserStatus(id: number, data: any) {
  return request.put(`/system/users/${id}/status`, data)
}

// 重置密码
export function resetPassword(id: number, data: any) {
  return request.put(`/system/users/${id}/reset-password`, data)
}

// 分配角色
export function assignRoles(id: number, data: any) {
  return request.put(`/system/users/${id}/roles`, data)
}

import request from '@/utils/request'

// 菜单树
export function getMenuTree(params?: any) {
  return request.get('/system/menus/tree', { params })
}

// 菜单详情
export function getMenuDetail(id: number) {
  return request.get(`/system/menus/${id}`)
}

// 创建菜单
export function createMenu(data: any) {
  return request.post('/system/menus', data)
}

// 更新菜单
export function updateMenu(id: number, data: any) {
  return request.put(`/system/menus/${id}`, data)
}

// 删除菜单
export function deleteMenu(id: number) {
  return request.delete(`/system/menus/${id}`)
}

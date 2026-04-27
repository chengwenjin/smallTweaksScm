import request from '@/utils/request'

export function getMaterialInventoryList(params: any) {
  return request.get('/scm/material-inventories', { params })
}

export function getMaterialInventoryDetail(id: number) {
  return request.get(`/scm/material-inventories/${id}`)
}

export function createMaterialInventory(data: any) {
  return request.post('/scm/material-inventories', data)
}

export function updateMaterialInventory(id: number, data: any) {
  return request.put(`/scm/material-inventories/${id}`, data)
}

export function deleteMaterialInventory(id: number) {
  return request.delete(`/scm/material-inventories/${id}`)
}

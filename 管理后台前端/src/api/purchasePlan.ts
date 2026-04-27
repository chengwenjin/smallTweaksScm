import request from '@/utils/request'

export function getPurchasePlanList(params: any) {
  return request.get('/scm/purchase-plans', { params })
}

export function getPurchasePlanDetail(id: number) {
  return request.get(`/scm/purchase-plans/${id}`)
}

export function generateReplenishmentPlan(data: any) {
  return request.post('/scm/purchase-plans/generate-replenishment', data)
}

export function createPurchasePlan(data: any) {
  return request.post('/scm/purchase-plans', data)
}

export function updatePurchasePlan(id: number, data: any) {
  return request.put(`/scm/purchase-plans/${id}`, data)
}

export function deletePurchasePlan(id: number) {
  return request.delete(`/scm/purchase-plans/${id}`)
}

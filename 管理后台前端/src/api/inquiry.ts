import request from '@/utils/request'

export function getInquiryList(params: any) {
  return request.get('/scm/inquiries', { params })
}

export function getInquiryDetail(id: number) {
  return request.get(`/scm/inquiries/${id}`)
}

export function createInquiry(data: any) {
  return request.post('/scm/inquiries', data)
}

export function publishInquiry(id: number) {
  return request.put(`/scm/inquiries/${id}/publish`)
}

export function cancelInquiry(id: number) {
  return request.put(`/scm/inquiries/${id}/cancel`)
}

export function getInquirySuppliers(inquiryId: number) {
  return request.get(`/scm/inquiries/${inquiryId}/suppliers`)
}

export function addInquirySupplier(inquiryId: number, data: any) {
  return request.post(`/scm/inquiries/${inquiryId}/suppliers`, data)
}

export function removeInquirySupplier(inquiryId: number, supplierId: number) {
  return request.delete(`/scm/inquiries/${inquiryId}/suppliers/${supplierId}`)
}

export function submitQuote(inquiryId: number, supplierId: number, data: any) {
  return request.put(`/scm/inquiries/${inquiryId}/suppliers/${supplierId}/quote`, data)
}

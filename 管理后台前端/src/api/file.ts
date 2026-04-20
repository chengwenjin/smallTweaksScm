import request from '@/utils/request'

export function uploadFiles(files: File[]) {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('files', file)
  })
  return request.post('/scm/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function deleteFile(fileUrl: string) {
  return request.delete('/scm/files', { params: { fileUrl } })
}

import request from '@/utils/request'

// 获取验证码
export function getCaptcha() {
  return request.get('/auth/captcha')
}

// 用户登录
export function login(data: any) {
  return request.post('/auth/login', data)
}

// 刷新Token
export function refreshToken(refreshToken: string) {
  return request.post('/auth/refresh', null, { params: { refreshToken } })
}

// 用户登出
export function logout() {
  return request.post('/auth/logout')
}

// 获取用户信息
export function getUserInfo() {
  return request.get('/auth/user-info')
}

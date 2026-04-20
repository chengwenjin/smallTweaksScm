<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">RBAC管理后台</h2>
      <el-form :model="loginForm" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-wrapper">
            <el-input
              v-model="loginForm.captcha"
              placeholder="验证码"
              prefix-icon="Key"
              size="large"
              style="flex: 1"
              @keyup.enter="handleLogin"
            />
            <img
              v-if="captchaImage"
              :src="captchaImage"
              alt="验证码"
              class="captcha-image"
              @click="getCaptcha"
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')

const loginForm = reactive({
  username: 'admin',
  password: '123456',
  captcha: '',
  captchaKey: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 获取验证码
const getCaptcha = async () => {
  try {
    const res = await request.get('/auth/captcha')
    captchaKey.value = res.data.captchaKey
    captchaImage.value = res.data.captchaImage
    loginForm.captchaKey = res.data.captchaKey
  } catch (error) {
    console.error('获取验证码失败', error)
  }
}

// 登录
const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await request.post('/auth/login', loginForm)
        
        // 保存Token
        localStorage.setItem('accessToken', res.data.accessToken)
        localStorage.setItem('refreshToken', res.data.refreshToken)
        
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        // 登录失败，刷新验证码
        getCaptcha()
        loginForm.captcha = ''
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  getCaptcha()
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 20px;
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}

.login-form {
  margin-top: 20px;
}

.captcha-wrapper {
  display: flex;
  gap: 10px;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
}

.login-button {
  width: 100%;
}
</style>

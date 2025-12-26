<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { User, Lock, Phone, Postcard } from '@element-plus/icons-vue'
import { patientApi } from '../../api'
import { useUserStore } from '../../stores/user'
import type { Patient } from '../../types'

const router = useRouter()
const userStore = useUserStore()

const isLogin = ref(true)
const loading = ref(false)
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

const loginForm = reactive({
  phone: '',
  password: ''
})

const registerForm = reactive<Partial<Patient>>({
  patientName: '',
  idCard: '',
  phone: '',
  gender: 'M',
  age: undefined,
  password: ''
})

// 登录表单校验规则
const loginRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ]
}

// 注册表单校验规则
const registerRules: FormRules = {
  patientName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5]+$/, message: '姓名只能包含中文', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为2-20个字符', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { len: 18, message: '身份证号必须为18位', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { len: 11, message: '手机号必须为11位', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: ['blur', 'change'] },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: ['blur', 'change'] }
  ]
}

async function handleLogin() {
  if (!loginFormRef.value) return
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await patientApi.login(loginForm.phone, loginForm.password)
    userStore.setPatient(res.data)
    ElMessage.success('登录成功')
    router.push('/patient/appointment')
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!registerFormRef.value) return
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await patientApi.register(registerForm)
    userStore.setPatient(res.data)
    ElMessage.success('注册成功')
    router.push('/patient/appointment')
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <el-icon :size="48" color="#409eff"><FirstAidKit /></el-icon>
        <h1>门诊挂号系统</h1>
        <p>{{ isLogin ? '患者登录' : '患者注册' }}</p>
      </div>

      <!-- 登录表单 -->
      <el-form v-if="isLogin" ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
        <el-form-item prop="phone">
          <el-input v-model="loginForm.phone" placeholder="手机号" size="large" :prefix-icon="Phone" maxlength="11" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" :prefix-icon="Lock" show-password maxlength="20" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleLogin" class="submit-btn">
            登 录
          </el-button>
        </el-form-item>
        <div class="switch-mode">
          没有账号？<el-link type="primary" @click="isLogin = false">立即注册</el-link>
        </div>
      </el-form>

      <!-- 注册表单 -->
      <el-form v-else ref="registerFormRef" :model="registerForm" :rules="registerRules" class="login-form">
        <el-form-item prop="patientName">
          <el-input v-model="registerForm.patientName" placeholder="姓名（必填，仅中文）" size="large" :prefix-icon="User" maxlength="20" />
          <div class="field-hint required">必填，2-20个中文字符</div>
        </el-form-item>
        <el-form-item prop="idCard">
          <el-input v-model="registerForm.idCard" placeholder="身份证号（必填，18位）" size="large" :prefix-icon="Postcard" maxlength="18" />
          <div class="field-hint required">必填，18位有效身份证号</div>
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="registerForm.phone" placeholder="手机号（必填，11位）" size="large" :prefix-icon="Phone" maxlength="11" />
          <div class="field-hint required">必填，11位手机号，用于登录</div>
        </el-form-item>
        <el-form-item>
          <el-radio-group v-model="registerForm.gender">
            <el-radio value="M">男</el-radio>
            <el-radio value="F">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-input v-model.number="registerForm.age" placeholder="年龄（选填）" size="large" type="number" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="设置密码（必填，6-20位）" size="large" :prefix-icon="Lock" show-password minlength="6" maxlength="20" />
          <div class="field-hint required">必填，6-20位字符</div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleRegister" class="submit-btn">
            注 册
          </el-button>
        </el-form-item>
        <div class="switch-mode">
          已有账号？<el-link type="primary" @click="isLogin = true">立即登录</el-link>
        </div>
      </el-form>

      <div class="admin-link">
        <el-link type="info" @click="router.push('/admin/login')">管理员入口</el-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  margin: 16px 0 8px;
  font-size: 24px;
  color: #303133;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

.submit-btn {
  width: 100%;
}

.switch-mode {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin-top: 16px;
}

.admin-link {
  text-align: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.field-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

.field-hint.required {
  color: #e6a23c;
}
</style>

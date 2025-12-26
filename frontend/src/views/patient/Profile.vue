<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { patientApi } from '../../api'
import { useUserStore } from '../../stores/user'
import type { Patient } from '../../types'

const userStore = useUserStore()
const loading = ref(false)
const editing = ref(false)
const formRef = ref<FormInstance>()

const form = ref<Partial<Patient>>({
  patientName: '',
  idCard: '',
  phone: '',
  gender: 'M',
  age: undefined,
  address: '',
  medicalHistory: ''
})

// 表单校验规则
const rules: FormRules = {
  patientName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5]+$/, message: '姓名只能包含中文', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为2-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '手机号不能为空（用于登录）', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ]
}

// 手机号只允许输入数字
function handlePhoneInput(value: string) {
  form.value.phone = value.replace(/\D/g, '').slice(0, 11)
}

async function loadInfo() {
  if (!userStore.patient?.patientId) return
  loading.value = true
  try {
    const res = await patientApi.getInfo(userStore.patient.patientId)
    form.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 处理年龄：无效值转为 null，避免后端反序列化错误
    const ageValue = form.value.age
    const submitData = {
      ...form.value,
      age: (ageValue === undefined || ageValue === null || (typeof ageValue === 'string' && ageValue === '')) ? null : ageValue
    }
    const res = await patientApi.updateInfo(submitData as Patient)
    userStore.setPatient(res.data)
    ElMessage.success('保存成功')
    editing.value = false
  } finally {
    loading.value = false
  }
}

function cancelEdit() {
  editing.value = false
  loadInfo()
}

onMounted(() => {
  loadInfo()
})
</script>

<template>
  <div class="profile-container">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button v-if="!editing" type="primary" @click="editing = true">
            <el-icon><Edit /></el-icon>编辑
          </el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" :disabled="!editing">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="patientName">
              <el-input v-model="form.patientName" placeholder="请输入中文姓名" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="form.idCard" disabled placeholder="身份证号不可修改" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input
                :model-value="form.phone"
                @input="handlePhoneInput"
                placeholder="请输入11位手机号（用于登录）"
                maxlength="11"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio value="M">男</el-radio>
                <el-radio value="F">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="年龄">
              <el-input
                v-model.number="form.age"
                type="number"
                placeholder="请输入"
                :min="0"
                :max="150"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="家庭住址">
          <el-input v-model="form.address" placeholder="请输入家庭住址" />
        </el-form-item>

        <el-form-item label="既往病史">
          <el-input v-model="form.medicalHistory" type="textarea" :rows="4" placeholder="请描述既往病史（可选）" />
        </el-form-item>

        <el-form-item v-if="editing">
          <el-button type="primary" @click="handleSave" :loading="loading">保存</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

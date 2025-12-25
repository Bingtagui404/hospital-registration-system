<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { patientApi } from '../../api'
import { useUserStore } from '../../stores/user'
import type { Patient } from '../../types'

const userStore = useUserStore()
const loading = ref(false)
const editing = ref(false)

const form = ref<Partial<Patient>>({
  patientName: '',
  idCard: '',
  phone: '',
  gender: 'M',
  age: undefined,
  address: '',
  medicalHistory: ''
})

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
  if (!form.value.patientName) {
    ElMessage.warning('请填写姓名')
    return
  }
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

      <el-form :model="form" label-width="100px" :disabled="!editing">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" required>
              <el-input v-model="form.patientName" placeholder="请输入姓名" />
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
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
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

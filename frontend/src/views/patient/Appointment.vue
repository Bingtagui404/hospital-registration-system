<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deptApi, doctorApi, scheduleApi, registrationApi } from '../../api'
import { useUserStore } from '../../stores/user'
import type { Department, Doctor, Schedule } from '../../types'

const userStore = useUserStore()

const step = ref(0)
const loading = ref(false)

// 数据
const departments = ref<Department[]>([])
const doctors = ref<Doctor[]>([])
const schedules = ref<Schedule[]>([])

// 选中项
const selectedDept = ref<Department | null>(null)
const selectedDate = ref('')
const selectedSchedule = ref<Schedule | null>(null)

// 加载科室列表
async function loadDepartments() {
  loading.value = true
  try {
    const res = await deptApi.list()
    departments.value = res.data
  } finally {
    loading.value = false
  }
}

// 选择科室
function selectDept(dept: Department) {
  selectedDept.value = dept
  step.value = 1
}

// 选择日期并加载可用号源
async function selectDate() {
  if (!selectedDate.value || !selectedDept.value) return
  loading.value = true
  try {
    const res = await scheduleApi.available(selectedDept.value.deptId, selectedDate.value)
    schedules.value = res.data
    step.value = 2
  } finally {
    loading.value = false
  }
}

// 选择号源
function selectSchedule(sch: Schedule) {
  selectedSchedule.value = sch
  step.value = 3
}

// 确认挂号
async function confirmBooking() {
  if (!selectedSchedule.value || !userStore.patient) return

  try {
    await ElMessageBox.confirm(
      `确认预约 ${selectedSchedule.value.doctorName} 医生（${selectedSchedule.value.title}）的 ${selectedSchedule.value.workDate} ${selectedSchedule.value.timeSlot === 'AM' ? '上午' : '下午'} 号源？\n挂号费：¥${selectedSchedule.value.fee}`,
      '确认挂号',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }

  loading.value = true
  try {
    const res = await registrationApi.create(userStore.patient.patientId, selectedSchedule.value.scheduleId)
    ElMessage.success(`挂号成功！挂号单号：${res.data.regNo}，排队号：${res.data.queueNo}`)
    reset()
  } finally {
    loading.value = false
  }
}

// 重置
function reset() {
  step.value = 0
  selectedDept.value = null
  selectedDate.value = ''
  selectedSchedule.value = null
  schedules.value = []
}

// 可选日期范围（今天到7天后）
const disabledDate = (time: Date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const maxDate = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000)
  return time < today || time > maxDate
}

// 初始化
loadDepartments()
</script>

<template>
  <div class="appointment-container">
    <el-card class="step-card">
      <el-steps :active="step" align-center>
        <el-step title="选择科室" />
        <el-step title="选择日期" />
        <el-step title="选择号源" />
        <el-step title="确认预约" />
      </el-steps>
    </el-card>

    <!-- 步骤0: 选择科室 -->
    <el-card v-if="step === 0" class="content-card" v-loading="loading">
      <template #header>
        <span>请选择科室</span>
      </template>
      <div class="dept-grid">
        <div
          v-for="dept in departments"
          :key="dept.deptId"
          class="dept-item"
          @click="selectDept(dept)"
        >
          <el-icon :size="32"><OfficeBuilding /></el-icon>
          <span class="dept-name">{{ dept.deptName }}</span>
        </div>
      </div>
    </el-card>

    <!-- 步骤1: 选择日期 -->
    <el-card v-if="step === 1" class="content-card">
      <template #header>
        <div class="header-with-back">
          <el-button text @click="step = 0"><el-icon><ArrowLeft /></el-icon>返回</el-button>
          <span>已选科室：{{ selectedDept?.deptName }} - 请选择就诊日期</span>
        </div>
      </template>
      <div class="date-picker-wrapper">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          :disabled-date="disabledDate"
          value-format="YYYY-MM-DD"
          size="large"
        />
        <el-button type="primary" size="large" :disabled="!selectedDate" @click="selectDate">
          查询号源
        </el-button>
      </div>
    </el-card>

    <!-- 步骤2: 选择号源 -->
    <el-card v-if="step === 2" class="content-card" v-loading="loading">
      <template #header>
        <div class="header-with-back">
          <el-button text @click="step = 1"><el-icon><ArrowLeft /></el-icon>返回</el-button>
          <span>{{ selectedDept?.deptName }} - {{ selectedDate }} 可预约号源</span>
        </div>
      </template>
      <div v-if="schedules.length === 0" class="empty-tip">
        <el-empty description="暂无可预约号源" />
      </div>
      <div v-else class="schedule-list">
        <div
          v-for="sch in schedules"
          :key="sch.scheduleId"
          class="schedule-item"
          @click="selectSchedule(sch)"
        >
          <div class="doctor-info">
            <span class="doctor-name">{{ sch.doctorName }}</span>
            <el-tag size="small">{{ sch.title }}</el-tag>
          </div>
          <div class="slot-info">
            <el-tag :type="sch.timeSlot === 'AM' ? 'success' : 'warning'">
              {{ sch.timeSlot === 'AM' ? '上午' : '下午' }}
            </el-tag>
            <span class="remaining">剩余 {{ sch.remainingQuota }} 个号</span>
          </div>
          <div class="fee">¥{{ sch.fee }}</div>
        </div>
      </div>
    </el-card>

    <!-- 步骤3: 确认预约 -->
    <el-card v-if="step === 3" class="content-card">
      <template #header>
        <div class="header-with-back">
          <el-button text @click="step = 2"><el-icon><ArrowLeft /></el-icon>返回</el-button>
          <span>确认预约信息</span>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="就诊科室">{{ selectedDept?.deptName }}</el-descriptions-item>
        <el-descriptions-item label="就诊医生">{{ selectedSchedule?.doctorName }}（{{ selectedSchedule?.title }}）</el-descriptions-item>
        <el-descriptions-item label="就诊日期">{{ selectedSchedule?.workDate }}</el-descriptions-item>
        <el-descriptions-item label="就诊时段">{{ selectedSchedule?.timeSlot === 'AM' ? '上午' : '下午' }}</el-descriptions-item>
        <el-descriptions-item label="挂号费">¥{{ selectedSchedule?.fee }}</el-descriptions-item>
        <el-descriptions-item label="就诊人">{{ userStore.patient?.patientName }}</el-descriptions-item>
      </el-descriptions>
      <div class="confirm-actions">
        <el-button size="large" @click="reset">取消</el-button>
        <el-button type="primary" size="large" :loading="loading" @click="confirmBooking">
          确认挂号
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.appointment-container {
  max-width: 900px;
  margin: 0 auto;
}

.step-card {
  margin-bottom: 20px;
}

.content-card {
  min-height: 400px;
}

.header-with-back {
  display: flex;
  align-items: center;
  gap: 16px;
}

.dept-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.dept-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.dept-item:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.dept-name {
  font-size: 16px;
  font-weight: 500;
}

.date-picker-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 60px 0;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.schedule-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.schedule-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.doctor-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.doctor-name {
  font-size: 16px;
  font-weight: 500;
}

.slot-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.remaining {
  color: #67c23a;
  font-size: 14px;
}

.fee {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.confirm-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.empty-tip {
  padding: 60px 0;
}
</style>

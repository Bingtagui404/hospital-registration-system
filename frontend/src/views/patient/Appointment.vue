<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deptApi, doctorApi, scheduleApi, registrationApi } from '../../api'
import { useUserStore } from '../../stores/user'
import type { Department, Doctor, Schedule } from '../../types'

const userStore = useUserStore()

// 模式：dept=按科室, doctor=按医生
const mode = ref<'dept' | 'doctor'>('dept')
const step = ref(0)
const loading = ref(false)
const searchKeyword = ref('')

// 数据
const departments = ref<Department[]>([])
const doctors = ref<Doctor[]>([])
const schedules = ref<Schedule[]>([])
const weekSchedules = ref<Schedule[]>([])

// 选中项
const selectedDept = ref<Department | null>(null)
const selectedDoctor = ref<Doctor | null>(null)
const selectedDate = ref('')
const selectedSchedule = ref<Schedule | null>(null)

// 周历相关
const weekDates = ref<{ date: string; weekday: string; hasSlot: boolean }[]>([])
const weekStartDate = ref('')

// 格式化日期为 YYYY-MM-DD（使用本地时间，避免时区偏移）
function formatLocalDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 初始化周历日期
function initWeekDates() {
  const today = new Date()
  const dates: { date: string; weekday: string; hasSlot: boolean }[] = []
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

  for (let i = 0; i < 7; i++) {
    const d = new Date(today)
    d.setDate(today.getDate() + i)
    const dateStr = formatLocalDate(d)
    dates.push({
      date: dateStr,
      weekday: weekdays[d.getDay()] ?? '',
      hasSlot: false
    })
  }
  weekDates.value = dates
  weekStartDate.value = dates[0]?.date ?? ''
}

// 搜索处理（同时搜索科室和医生）
async function handleSearch() {
  loading.value = true
  try {
    const keyword = searchKeyword.value.trim()
    if (keyword) {
      // 并行搜索科室和医生
      const [deptRes, doctorRes] = await Promise.all([
        deptApi.search(keyword),
        doctorApi.search(keyword)
      ])
      departments.value = deptRes.data
      doctors.value = doctorRes.data
    } else {
      // 无关键词时加载全部
      const [deptRes, doctorRes] = await Promise.all([
        deptApi.list(),
        doctorApi.list()
      ])
      departments.value = deptRes.data
      doctors.value = doctorRes.data
    }
  } finally {
    loading.value = false
  }
}

// 模式切换（保留搜索关键词和结果，只重置选择状态）
watch(mode, () => {
  // 只重置选择状态，不清空搜索结果
  step.value = 0
  selectedDept.value = null
  selectedDoctor.value = null
  selectedDate.value = ''
  selectedSchedule.value = null
  schedules.value = []
  weekSchedules.value = []
})

// 选择科室（按科室模式）
function selectDept(dept: Department) {
  selectedDept.value = dept
  step.value = 1
}

// 选择医生（按医生模式）
async function selectDoctor(doctor: Doctor) {
  selectedDoctor.value = doctor
  initWeekDates()
  await loadDoctorWeekSchedule()
  step.value = 1
}

// 加载医生周排班
async function loadDoctorWeekSchedule() {
  if (!selectedDoctor.value) return
  loading.value = true
  try {
    const lastDate = weekDates.value[weekDates.value.length - 1]
    const endDate = lastDate?.date ?? weekStartDate.value
    const res = await scheduleApi.listByDoctor(
      selectedDoctor.value.doctorId,
      weekStartDate.value,
      endDate
    )
    weekSchedules.value = res.data
    // 标记有号源的日期
    weekDates.value = weekDates.value.map(d => ({
      ...d,
      hasSlot: res.data.some(s => s.workDate === d.date && (s.remainingQuota ?? 0) > 0)
    }))
  } finally {
    loading.value = false
  }
}

// 选择日期（按科室模式）
async function selectDateByDept() {
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

// 选择周历日期（按医生模式）- 不改变步骤，只显示号源列表
function selectWeekDate(dateItem: { date: string; weekday: string; hasSlot: boolean }) {
  if (!dateItem.hasSlot) return
  selectedDate.value = dateItem.date
  // 过滤出该日期的号源，显示在周历下方
  schedules.value = weekSchedules.value.filter(
    s => s.workDate === dateItem.date && (s.remainingQuota ?? 0) > 0
  )
  // 不改变 step，号源列表在 step=1 的周历视图下方显示
}

// 选择号源
function selectSchedule(sch: Schedule) {
  selectedSchedule.value = sch
  // 医生模式：步骤1选号源后直接到步骤3确认
  // 科室模式：步骤2选号源后到步骤3确认
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
  selectedDoctor.value = null
  selectedDate.value = ''
  selectedSchedule.value = null
  schedules.value = []
  weekSchedules.value = []
}

// 返回上一步
function goBack() {
  if (step.value === 1) {
    reset()
  } else if (step.value === 2) {
    selectedDate.value = ''
    schedules.value = []
    step.value = 1
  } else if (step.value === 3) {
    selectedSchedule.value = null
    // 医生模式：从确认页返回到周历（步骤1）
    // 科室模式：从确认页返回到号源列表（步骤2）
    step.value = mode.value === 'doctor' ? 1 : 2
  }
}

// 可选日期范围（今天到7天后）
const disabledDate = (time: Date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const maxDate = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000)
  return time < today || time > maxDate
}

// 当前步骤标题
const stepTitles = computed(() => {
  if (mode.value === 'dept') {
    return ['选择科室', '选择日期', '选择号源', '确认预约']
  } else {
    return ['选择医生', '选择日期', '选择号源', '确认预约']
  }
})

// 格式化时段显示
function formatTimeSlot(slot: string) {
  return slot === 'AM' ? '上午 08:00-12:00' : '下午 14:00-17:00'
}

// 初始化（同时加载科室和医生）
handleSearch()
</script>

<template>
  <div class="appointment-container">
    <!-- 搜索 + 模式切换 -->
    <el-card class="search-card">
      <div class="search-row">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索科室或医生姓名"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
          style="width: 300px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <el-tabs v-model="mode" class="mode-tabs">
        <el-tab-pane label="按科室预约" name="dept" />
        <el-tab-pane label="按医生预约" name="doctor" />
      </el-tabs>
    </el-card>

    <!-- 步骤条 -->
    <el-card class="step-card">
      <el-steps :active="step" align-center>
        <el-step v-for="(title, idx) in stepTitles" :key="idx" :title="title" />
      </el-steps>
    </el-card>

    <!-- 步骤0: 选择科室/医生 -->
    <el-card v-if="step === 0" class="content-card" v-loading="loading">
      <template #header>
        <span>{{ mode === 'dept' ? '请选择科室' : '请选择医生' }}</span>
      </template>

      <!-- 按科室模式 -->
      <div v-if="mode === 'dept'" class="dept-grid">
        <div
          v-for="dept in departments"
          :key="dept.deptId"
          class="dept-item"
          @click="selectDept(dept)"
        >
          <el-icon :size="32"><OfficeBuilding /></el-icon>
          <span class="dept-name">{{ dept.deptName }}</span>
        </div>
        <div v-if="departments.length === 0" class="empty-tip">
          <el-empty description="暂无科室" />
        </div>
      </div>

      <!-- 按医生模式 -->
      <div v-else class="doctor-grid">
        <div
          v-for="doctor in doctors"
          :key="doctor.doctorId"
          class="doctor-card"
          @click="selectDoctor(doctor)"
        >
          <div class="doctor-avatar">
            <el-icon :size="40"><User /></el-icon>
          </div>
          <div class="doctor-details">
            <div class="doctor-name">{{ doctor.doctorName }}</div>
            <el-tag size="small" type="info">{{ doctor.title }}</el-tag>
            <div class="doctor-dept">{{ doctor.deptName }}</div>
            <div v-if="doctor.specialty" class="doctor-specialty">
              擅长：{{ doctor.specialty }}
            </div>
          </div>
        </div>
        <div v-if="doctors.length === 0" class="empty-tip">
          <el-empty description="暂无医生" />
        </div>
      </div>
    </el-card>

    <!-- 步骤1: 选择日期 -->
    <el-card v-if="step === 1" class="content-card" v-loading="loading">
      <template #header>
        <div class="header-with-back">
          <el-button text @click="goBack"><el-icon><ArrowLeft /></el-icon>返回</el-button>
          <span v-if="mode === 'dept'">
            已选科室：{{ selectedDept?.deptName }} - 请选择就诊日期
          </span>
          <span v-else>
            {{ selectedDoctor?.doctorName }} - {{ selectedDoctor?.deptName }} | {{ selectedDoctor?.title }}
          </span>
        </div>
      </template>

      <!-- 按科室模式：日期选择器 -->
      <div v-if="mode === 'dept'" class="date-picker-wrapper">
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          :disabled-date="disabledDate"
          value-format="YYYY-MM-DD"
          size="large"
        />
        <el-button type="primary" size="large" :disabled="!selectedDate" @click="selectDateByDept">
          查询号源
        </el-button>
      </div>

      <!-- 按医生模式：周历视图 -->
      <div v-else class="week-calendar">
        <div v-if="selectedDoctor?.specialty" class="specialty-info">
          擅长：{{ selectedDoctor.specialty }}
        </div>
        <div class="week-dates">
          <div
            v-for="dateItem in weekDates"
            :key="dateItem.date"
            class="week-date-item"
            :class="{
              'has-slot': dateItem.hasSlot,
              'no-slot': !dateItem.hasSlot,
              'selected': selectedDate === dateItem.date
            }"
            @click="selectWeekDate(dateItem)"
          >
            <div class="date-label">{{ dateItem.date.slice(5) }}</div>
            <div class="weekday-label">{{ dateItem.weekday }}</div>
            <div class="slot-status">{{ dateItem.hasSlot ? '有号' : '无' }}</div>
          </div>
        </div>
        <div v-if="selectedDate && schedules.length > 0" class="slot-list">
          <div class="slot-list-header">{{ selectedDate }} 可预约号源：</div>
          <div
            v-for="sch in schedules"
            :key="sch.scheduleId"
            class="slot-item"
            @click="selectSchedule(sch)"
          >
            <div class="slot-time">{{ formatTimeSlot(sch.timeSlot) }}</div>
            <div class="slot-remaining">剩余 {{ sch.remainingQuota }}/{{ sch.totalQuota }}</div>
            <div class="slot-fee">¥{{ sch.fee }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 步骤2: 选择号源（按科室模式才显示，按医生模式在步骤1直接选） -->
    <el-card v-if="step === 2 && mode === 'dept'" class="content-card" v-loading="loading">
      <template #header>
        <div class="header-with-back">
          <el-button text @click="goBack"><el-icon><ArrowLeft /></el-icon>返回</el-button>
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

    <!-- 步骤3: 确认预约（医生模式从步骤1选号源后直接到步骤3） -->
    <el-card v-if="step === 3" class="content-card">
      <template #header>
        <div class="header-with-back">
          <el-button text @click="goBack"><el-icon><ArrowLeft /></el-icon>返回</el-button>
          <span>确认预约信息</span>
        </div>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="就诊科室">
          {{ mode === 'dept' ? selectedDept?.deptName : selectedDoctor?.deptName }}
        </el-descriptions-item>
        <el-descriptions-item label="就诊医生">
          {{ selectedSchedule?.doctorName }}（{{ selectedSchedule?.title }}）
        </el-descriptions-item>
        <el-descriptions-item label="就诊日期">{{ selectedSchedule?.workDate }}</el-descriptions-item>
        <el-descriptions-item label="就诊时段">
          {{ selectedSchedule?.timeSlot === 'AM' ? '上午' : '下午' }}
        </el-descriptions-item>
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

.search-card {
  margin-bottom: 20px;
}

.search-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.mode-tabs {
  margin-bottom: -16px;
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

.doctor-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.doctor-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.doctor-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.doctor-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.doctor-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.doctor-details .doctor-name {
  font-size: 16px;
  font-weight: 600;
}

.doctor-dept {
  font-size: 13px;
  color: #909399;
}

.doctor-specialty {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.date-picker-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 60px 0;
}

.week-calendar {
  padding: 20px 0;
}

.specialty-info {
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
  color: #606266;
}

.week-dates {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}

.week-date-item {
  flex: 1;
  text-align: center;
  padding: 16px 8px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.3s;
}

.week-date-item.has-slot {
  border-color: #67c23a;
  background: #f0f9eb;
}

.week-date-item.has-slot:hover {
  background: #e1f3d8;
}

.week-date-item.no-slot {
  background: #f5f7fa;
  color: #c0c4cc;
  cursor: not-allowed;
}

.week-date-item.selected {
  border-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.3);
}

.date-label {
  font-size: 14px;
  font-weight: 500;
}

.weekday-label {
  font-size: 12px;
  margin-top: 4px;
}

.slot-status {
  font-size: 12px;
  margin-top: 8px;
  font-weight: 500;
}

.week-date-item.has-slot .slot-status {
  color: #67c23a;
}

.slot-list {
  border-top: 1px solid #e4e7ed;
  padding-top: 20px;
}

.slot-list-header {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
}

.slot-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.slot-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.slot-time {
  font-size: 14px;
  font-weight: 500;
}

.slot-remaining {
  color: #67c23a;
}

.slot-fee {
  font-size: 16px;
  font-weight: bold;
  color: #f56c6c;
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

.doctor-info .doctor-name {
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

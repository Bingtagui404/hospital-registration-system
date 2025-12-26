<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { scheduleApi, deptApi, doctorApi } from '../../api'
import type { Schedule, Department, Doctor } from '../../types'

const loading = ref(false)
const schedules = ref<Schedule[]>([])
const departments = ref<Department[]>([])
const doctors = ref<Doctor[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增排班')
const formRef = ref<FormInstance>()

const searchForm = ref({
  deptId: undefined as number | undefined,
  workDate: ''
})

// 分页状态
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const form = ref<Partial<Schedule>>({
  doctorId: undefined,
  workDate: '',
  timeSlot: 'AM',
  totalQuota: 30,
  fee: 50
})

// 编辑时记录已预约数量，用于限制最小总号源
const bookedCount = ref(0)

// 表单校验规则
const rules: FormRules = {
  deptId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  workDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时段', trigger: 'change' }],
  totalQuota: [{ required: true, message: '请输入总号源', trigger: 'blur' }],
  fee: [{ required: true, message: '请输入挂号费', trigger: 'blur' }]
}

// 排班日期限制：不能选择过去日期
function disabledDate(time: Date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time < today
}

async function loadDepartments() {
  const res = await deptApi.list()
  departments.value = res.data
}

async function loadDoctors(deptId?: number) {
  if (!deptId) {
    doctors.value = []
    return
  }
  const res = await doctorApi.listByDept(deptId)
  doctors.value = res.data
}

async function loadSchedules() {
  loading.value = true
  try {
    const res = await scheduleApi.listPage(
      searchForm.value.deptId,
      searchForm.value.workDate,
      currentPage.value,
      pageSize.value
    )
    schedules.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  loadSchedules()
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadSchedules()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  loadSchedules()
}

function showAddDialog() {
  dialogTitle.value = '新增排班'
  bookedCount.value = 0
  form.value = {
    doctorId: undefined,
    workDate: '',
    timeSlot: 'AM',
    totalQuota: 30,
    fee: 50
  }
  dialogVisible.value = true
}

function showEditDialog(row: Schedule) {
  dialogTitle.value = '编辑排班'
  // 计算已预约数量 = 总号源 - 剩余号源
  bookedCount.value = (row.totalQuota || 0) - (row.remainingQuota || 0)
  form.value = { ...row }
  if (row.deptId) {
    loadDoctors(row.deptId)
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    if (form.value.scheduleId) {
      await scheduleApi.update(form.value)
      ElMessage.success('更新成功')
    } else {
      await scheduleApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadSchedules()
  } finally {
    loading.value = false
  }
}

async function handleDelete(row: Schedule) {
  try {
    await ElMessageBox.confirm(
      `确认删除 ${row.doctorName} 在 ${row.workDate} 的排班吗？`,
      '确认删除',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  loading.value = true
  try {
    await scheduleApi.delete(row.scheduleId)
    ElMessage.success('删除成功')
    // 如果当前页只有一条数据且不是第一页，则跳转到上一页
    if (schedules.value.length === 1 && currentPage.value > 1) {
      currentPage.value--
    }
    loadSchedules()
  } finally {
    loading.value = false
  }
}

function handleDeptChange(deptId: number) {
  loadDoctors(deptId)
  form.value.doctorId = undefined
}

onMounted(async () => {
  await loadDepartments()
  loadSchedules()
})
</script>

<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="科室">
          <el-select v-model="searchForm.deptId" placeholder="全部科室" clearable style="width: 150px">
            <el-option v-for="d in departments" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="searchForm.workDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="showAddDialog">新增排班</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="schedules" v-loading="loading" stripe border>
        <el-table-column prop="scheduleId" label="ID" width="70" />
        <el-table-column prop="deptName" label="科室" width="100" />
        <el-table-column label="医生" width="150">
          <template #default="{ row }">
            {{ row.doctorName }}（{{ row.title }}）
          </template>
        </el-table-column>
        <el-table-column prop="workDate" label="日期" width="120" />
        <el-table-column label="时段" width="80">
          <template #default="{ row }">
            <el-tag :type="row.timeSlot === 'AM' ? 'success' : 'warning'" size="small">
              {{ row.timeSlot === 'AM' ? '上午' : '下午' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="号源" width="120">
          <template #default="{ row }">
            {{ row.remainingQuota }} / {{ row.totalQuota }}
          </template>
        </el-table-column>
        <el-table-column prop="fee" label="挂号费" width="90">
          <template #default="{ row }">¥{{ row.fee }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="科室" prop="deptId">
          <el-select v-model="form.deptId" placeholder="选择科室" @change="handleDeptChange" style="width: 100%">
            <el-option v-for="d in departments" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生" prop="doctorId">
          <el-select v-model="form.doctorId" placeholder="选择医生" style="width: 100%">
            <el-option v-for="d in doctors" :key="d.doctorId" :label="`${d.doctorName}（${d.title}）`" :value="d.doctorId" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" prop="workDate">
          <el-date-picker v-model="form.workDate" type="date" placeholder="选择日期（不能选过去日期）" value-format="YYYY-MM-DD" :disabled-date="disabledDate" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时段" prop="timeSlot">
          <el-radio-group v-model="form.timeSlot">
            <el-radio value="AM">上午</el-radio>
            <el-radio value="PM">下午</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="总号源" prop="totalQuota">
          <el-input-number v-model="form.totalQuota" :min="bookedCount || 1" :max="100" />
          <span v-if="bookedCount > 0" style="margin-left: 8px; color: #909399; font-size: 12px">
            （已预约 {{ bookedCount }} 人）
          </span>
        </el-form-item>
        <el-form-item label="挂号费" prop="fee">
          <el-input-number v-model="form.fee" :min="0" :precision="2" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

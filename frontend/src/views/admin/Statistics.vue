<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { registrationApi, deptApi } from '../../api'
import type { Registration, Department } from '../../types'

const loading = ref(false)
const registrations = ref<Registration[]>([])
const departments = ref<Department[]>([])

const searchForm = ref({
  startDate: '',
  endDate: '',
  status: ''
})

// 统计数据
const stats = computed(() => {
  const total = registrations.value.length
  const booked = registrations.value.filter(r => r.status === 'BOOKED').length
  const cancelled = registrations.value.filter(r => r.status === 'CANCELLED').length
  const finished = registrations.value.filter(r => r.status === 'FINISHED').length
  // 有效记录数（非取消），用于科室占比计算
  const validTotal = booked + finished
  const totalFee = registrations.value
    .filter(r => r.status !== 'CANCELLED')
    .reduce((sum, r) => sum + (r.fee || 0), 0)
  return { total, booked, cancelled, finished, validTotal, totalFee }
})

// 按科室统计
const deptStats = computed(() => {
  const map = new Map<string, number>()
  registrations.value.forEach(r => {
    if (r.status !== 'CANCELLED' && r.deptName) {
      map.set(r.deptName, (map.get(r.deptName) || 0) + 1)
    }
  })
  return Array.from(map.entries())
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)
})

async function loadData() {
  loading.value = true
  try {
    const [regRes, deptRes] = await Promise.all([
      registrationApi.list(searchForm.value.startDate, searchForm.value.endDate, searchForm.value.status),
      deptApi.list()
    ])
    registrations.value = regRes.data
    departments.value = deptRes.data
  } finally {
    loading.value = false
  }
}

function getStatusType(status: string) {
  switch (status) {
    case 'BOOKED': return 'primary'
    case 'CANCELLED': return 'info'
    case 'FINISHED': return 'success'
    default: return 'info'
  }
}

function getStatusText(status: string) {
  switch (status) {
    case 'BOOKED': return '待就诊'
    case 'CANCELLED': return '已取消'
    case 'FINISHED': return '已完成'
    default: return status
  }
}

// 结束日期不能早于开始日期
function disabledEndDate(time: Date) {
  if (!searchForm.value.startDate) return false
  const start = new Date(searchForm.value.startDate)
  start.setHours(0, 0, 0, 0)
  return time < start
}

// 开始日期变更时，如果结束日期早于开始日期则清空结束日期
function handleStartDateChange() {
  if (searchForm.value.startDate && searchForm.value.endDate) {
    if (new Date(searchForm.value.endDate) < new Date(searchForm.value.startDate)) {
      searchForm.value.endDate = ''
    }
  }
}

async function handleFinish(row: Registration) {
  try {
    await ElMessageBox.confirm(
      `确认将 ${row.patientName} 的挂号记录标记为已就诊吗？`,
      '确认操作',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }
  loading.value = true
  try {
    const res = await registrationApi.finish(row.regId)
    if (res.code === 200) {
      ElMessage.success('标记成功')
      loadData()
    } else {
      ElMessage.error(res.message || '标记失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="page-container">
    <!-- 搜索 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="searchForm.startDate"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
            clearable
            @change="handleStartDateChange"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="searchForm.endDate"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
            clearable
            :disabled-date="disabledEndDate"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待就诊" value="BOOKED" />
            <el-option label="已完成" value="FINISHED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">总挂号数</div>
      </el-card>
      <el-card class="stat-card stat-booked">
        <div class="stat-value">{{ stats.booked }}</div>
        <div class="stat-label">待就诊</div>
      </el-card>
      <el-card class="stat-card stat-finished">
        <div class="stat-value">{{ stats.finished }}</div>
        <div class="stat-label">已完成</div>
      </el-card>
      <el-card class="stat-card stat-cancelled">
        <div class="stat-value">{{ stats.cancelled }}</div>
        <div class="stat-label">已取消</div>
      </el-card>
      <el-card class="stat-card stat-fee">
        <div class="stat-value">¥{{ stats.totalFee.toFixed(2) }}</div>
        <div class="stat-label">收入总额</div>
      </el-card>
    </div>

    <!-- 科室统计 -->
    <el-row :gutter="20" class="stats-section">
      <el-col :span="6">
        <el-card>
          <template #header>按科室统计</template>
          <div v-if="deptStats.length === 0" class="empty-tip">暂无数据</div>
          <div v-else class="dept-stats">
            <div v-for="item in deptStats" :key="item.name" class="dept-item">
              <span class="dept-name">{{ item.name }}</span>
              <el-progress :percentage="stats.validTotal > 0 ? Math.round(item.count / stats.validTotal * 100) : 0" :stroke-width="12" />
              <span class="dept-count">{{ item.count }}人</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card>
          <template #header>挂号记录列表</template>
          <el-table :data="registrations" v-loading="loading" stripe border max-height="500">
            <el-table-column prop="regNo" label="挂号单号" min-width="160" />
            <el-table-column prop="patientName" label="患者" min-width="90" />
            <el-table-column prop="deptName" label="科室" min-width="100" />
            <el-table-column prop="doctorName" label="医生" min-width="90" />
            <el-table-column prop="workDate" label="日期" min-width="110" />
            <el-table-column label="时段" min-width="70">
              <template #default="{ row }">
                {{ row.timeSlot === 'AM' ? '上午' : '下午' }}
              </template>
            </el-table-column>
            <el-table-column label="状态" min-width="85">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="fee" label="费用" min-width="80">
              <template #default="{ row }">¥{{ row.fee }}</template>
            </el-table-column>
            <el-table-column label="操作" min-width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'BOOKED'"
                  type="success"
                  text
                  size="small"
                  @click="handleFinish(row)"
                >
                  标记已就诊
                </el-button>
                <span v-else class="no-action">-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.stat-booked .stat-value { color: #409eff; }
.stat-finished .stat-value { color: #67c23a; }
.stat-cancelled .stat-value { color: #909399; }
.stat-fee .stat-value { color: #e6a23c; }

.dept-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dept-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dept-name {
  width: 80px;
  flex-shrink: 0;
}

.dept-item .el-progress {
  flex: 1;
}

.dept-count {
  width: 60px;
  text-align: right;
  color: #606266;
}

.empty-tip {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { registrationApi } from '../../api'
import { useUserStore } from '../../stores/user'
import type { Registration } from '../../types'

const userStore = useUserStore()
const loading = ref(false)
const records = ref<Registration[]>([])

async function loadRecords() {
  if (!userStore.patient) return
  loading.value = true
  try {
    const res = await registrationApi.myList(userStore.patient.patientId)
    records.value = res.data
  } finally {
    loading.value = false
  }
}

async function handleCancel(record: Registration) {
  try {
    await ElMessageBox.confirm(
      `确认取消 ${record.workDate} ${record.timeSlot === 'AM' ? '上午' : '下午'} ${record.doctorName} 医生的挂号吗？`,
      '确认退号',
      { confirmButtonText: '确认退号', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  loading.value = true
  try {
    await registrationApi.cancel(record.regId)
    ElMessage.success('退号成功')
    loadRecords()
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
    case 'BOOKED': return '已预约'
    case 'CANCELLED': return '已取消'
    case 'FINISHED': return '已完成'
    default: return status
  }
}

onMounted(() => {
  loadRecords()
})
</script>

<template>
  <div class="records-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的挂号记录</span>
          <el-button type="primary" text @click="loadRecords">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>

      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="regNo" label="挂号单号" width="180" />
        <el-table-column prop="deptName" label="科室" width="100" />
        <el-table-column label="医生" width="150">
          <template #default="{ row }">
            {{ row.doctorName }}（{{ row.title }}）
          </template>
        </el-table-column>
        <el-table-column prop="workDate" label="就诊日期" width="120" />
        <el-table-column label="时段" width="80">
          <template #default="{ row }">
            <el-tag :type="row.timeSlot === 'AM' ? 'success' : 'warning'" size="small">
              {{ row.timeSlot === 'AM' ? '上午' : '下午' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="queueNo" label="排队号" width="80" />
        <el-table-column label="挂号费" width="90">
          <template #default="{ row }">
            ¥{{ row.fee }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="regTime" label="挂号时间" width="180" />
        <el-table-column label="操作" fixed="right" width="100">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'BOOKED'"
              type="danger"
              text
              size="small"
              @click="handleCancel(row)"
            >
              退号
            </el-button>
            <span v-else class="no-action">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="records.length === 0 && !loading" class="empty-tip">
        <el-empty description="暂无挂号记录" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.records-container {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.no-action {
  color: #c0c4cc;
}

.empty-tip {
  padding: 40px 0;
}
</style>

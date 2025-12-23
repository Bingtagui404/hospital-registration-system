<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deptApi } from '../../api'
import type { Department } from '../../types'

const loading = ref(false)
const departments = ref<Department[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增科室')
const form = ref<Partial<Department>>({
  deptName: '',
  description: ''
})

async function loadData() {
  loading.value = true
  try {
    const res = await deptApi.list()
    departments.value = res.data
  } finally {
    loading.value = false
  }
}

function showAddDialog() {
  dialogTitle.value = '新增科室'
  form.value = { deptName: '', description: '' }
  dialogVisible.value = true
}

function showEditDialog(row: Department) {
  dialogTitle.value = '编辑科室'
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.deptName) {
    ElMessage.warning('请填写科室名称')
    return
  }
  loading.value = true
  try {
    if (form.value.deptId) {
      await deptApi.update(form.value)
      ElMessage.success('更新成功')
    } else {
      await deptApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    loading.value = false
  }
}

async function handleDelete(row: Department) {
  try {
    await ElMessageBox.confirm(`确认删除科室「${row.deptName}」吗？`, '确认删除', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  loading.value = true
  try {
    await deptApi.delete(row.deptId)
    ElMessage.success('删除成功')
    loadData()
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
    <el-card>
      <template #header>
        <div class="card-header">
          <span>科室管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>新增科室
          </el-button>
        </div>
      </template>

      <el-table :data="departments" v-loading="loading" stripe border>
        <el-table-column prop="deptId" label="ID" width="80" />
        <el-table-column prop="deptName" label="科室名称" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500">
      <el-form :model="form" label-width="80px">
        <el-form-item label="科室名称" required>
          <el-input v-model="form.deptName" placeholder="请输入科室名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入科室描述" />
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

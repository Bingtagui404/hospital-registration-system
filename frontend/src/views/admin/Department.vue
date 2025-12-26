<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { deptApi } from '../../api'
import type { Department } from '../../types'

const loading = ref(false)
const departments = ref<Department[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增科室')
const formRef = ref<FormInstance>()
const form = ref<Partial<Department>>({
  deptName: '',
  description: ''
})

// 分页状态
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表单校验规则
const rules: FormRules = {
  deptName: [
    { required: true, message: '请输入科室名称', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5]+$/, message: '科室名称只能包含中文', trigger: 'blur' },
    { min: 2, max: 20, message: '科室名称长度为2-20个字符', trigger: 'blur' }
  ]
}

async function loadData() {
  loading.value = true
  try {
    const res = await deptApi.listPage(currentPage.value, pageSize.value)
    departments.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadData()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  loadData()
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
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
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
    // 如果当前页只有一条数据且不是第一页，则跳转到上一页
    if (departments.value.length === 1 && currentPage.value > 1) {
      currentPage.value--
    }
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
        <el-form-item label="科室名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入科室名称" maxlength="20" show-word-limit />
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

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

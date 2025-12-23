<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.logoutPatient()
  router.push('/patient/login')
}
</script>

<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo">
        <el-icon><FirstAidKit /></el-icon>
        <span>门诊挂号系统</span>
      </div>
      <el-menu mode="horizontal" router :default-active="$route.path" class="nav-menu">
        <el-menu-item index="/patient/appointment">
          <el-icon><Calendar /></el-icon>
          预约挂号
        </el-menu-item>
        <el-menu-item index="/patient/records">
          <el-icon><Document /></el-icon>
          我的挂号
        </el-menu-item>
        <el-menu-item index="/patient/profile">
          <el-icon><User /></el-icon>
          个人信息
        </el-menu-item>
      </el-menu>
      <div class="user-info">
        <el-dropdown>
          <span class="user-name">
            <el-icon><User /></el-icon>
            {{ userStore.patient?.patientName }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  margin-right: 40px;
}

.nav-menu {
  flex: 1;
  border-bottom: none;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-name {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #606266;
}

.main-content {
  padding: 20px;
  background: #f5f7fa;
}
</style>

# 医院门诊挂号管理系统 - 前端设计方案

## 一、技术栈选型

### 核心框架
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue.js | 3.4+ | 前端框架（Composition API） |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.x | 状态管理（替代 Vuex，更轻量） |
| Axios | 1.x | HTTP 请求 |
| Element Plus | 2.x | UI 组件库 |
| TypeScript | 5.x | 类型安全 |

### 构建工具
| 技术 | 用途 |
|------|------|
| Vite | 构建打包（比 webpack 更快） |
| pnpm | 包管理器 |

### 辅助工具
| 技术 | 用途 |
|------|------|
| VueUse | 组合式函数工具库 |
| Day.js | 日期处理 |
| unplugin-auto-import | 自动导入 API |
| unplugin-vue-components | 自动导入组件 |

---

## 二、项目目录结构

```
frontend/
├── public/                    # 静态资源
│   └── favicon.ico
├── src/
│   ├── api/                   # API 接口封装（按模块拆分）
│   │   ├── index.ts           # axios 实例配置
│   │   ├── auth.ts            # 患者/管理员认证
│   │   ├── department.ts      # 科室相关
│   │   ├── doctor.ts          # 医生相关
│   │   ├── schedule.ts        # 排班相关
│   │   └── registration.ts    # 挂号相关
│   │
│   ├── components/            # 公共组件
│   │   ├── layout/            # 布局组件
│   │   │   ├── PatientLayout.vue
│   │   │   └── AdminLayout.vue
│   │   └── common/            # 通用组件
│   │       ├── PageHeader.vue
│   │       └── EmptyState.vue
│   │
│   ├── composables/           # 组合式函数（业务逻辑复用）
│   │   ├── useAuth.ts
│   │   └── useMessage.ts
│   │
│   ├── router/                # 路由配置
│   │   ├── index.ts
│   │   └── guards.ts          # 路由守卫
│   │
│   ├── stores/                # Pinia 状态管理
│   │   ├── index.ts
│   │   ├── patient.ts         # 患者状态
│   │   └── admin.ts           # 管理员状态
│   │
│   ├── types/                 # TypeScript 类型定义
│   │   ├── api.ts             # API 响应类型
│   │   └── entity.ts          # 实体类型
│   │
│   ├── views/                 # 页面视图
│   │   ├── patient/           # 患者端页面
│   │   │   ├── Login.vue
│   │   │   ├── Register.vue
│   │   │   ├── Booking.vue
│   │   │   └── MyRecords.vue
│   │   │
│   │   └── admin/             # 管理员端页面
│   │       ├── Login.vue
│   │       ├── Department.vue
│   │       ├── Schedule.vue
│   │       └── Statistics.vue
│   │
│   ├── App.vue
│   └── main.ts
│
├── scripts/                   # 启停脚本
│   ├── dev.sh
│   ├── build.sh
│   └── preview.sh
│
├── index.html
├── vite.config.ts
├── tsconfig.json
├── package.json
└── .env.development           # 开发环境变量
```

**说明**：目录结构遵循以下原则：
1. 每层文件夹文件数不超过 8 个
2. 按功能模块划分，职责清晰
3. 患者端和管理员端视图分离

---

## 三、TypeScript 类型定义

### 3.1 API 响应类型

```typescript
// types/api.ts
export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}
```

### 3.2 实体类型

```typescript
// types/entity.ts

// 科室
export interface Department {
  deptId: number
  deptName: string
  description: string
  status: number
  createTime: string
  updateTime: string
}

// 医生
export interface Doctor {
  doctorId: number
  deptId: number
  doctorName: string
  gender: string
  title: string
  specialty: string
  phone: string
  status: number
  createTime: string
  updateTime: string
  deptName?: string  // 关联字段
}

// 患者
export interface Patient {
  patientId: number
  patientName: string
  idCard: string
  phone: string
  gender: string
  age: number
  address: string
  medicalHistory: string
  password?: string  // 登录时需要，返回时不包含
  createTime: string
  updateTime: string
}

// 排班
export interface Schedule {
  scheduleId: number
  doctorId: number
  workDate: string
  timeSlot: string
  totalQuota: number
  remainingQuota: number
  fee: number
  status: number
  createTime: string
  updateTime: string
  doctorName?: string  // 关联字段
  deptName?: string
  deptId?: number
  title?: string
}

// 挂号记录
export interface Registration {
  regId: number
  regNo: string
  patientId: number
  scheduleId: number
  doctorId: number
  deptId: number
  workDate: string
  timeSlot: string
  queueNo: number
  fee: number
  status: string
  regTime: string
  updateTime: string
  patientName?: string  // 关联字段
  doctorName?: string
  deptName?: string
  title?: string
}

// 管理员
export interface Admin {
  adminId: number
  username: string
  password?: string
  realName: string
  status: number
  createTime: string
}

// 登录请求参数
export interface PatientLoginParams {
  phone: string
  password: string
}

export interface AdminLoginParams {
  username: string
  password: string
}

// 挂号创建参数
export interface RegistrationCreateParams {
  patientId: number
  scheduleId: number
}
```

---

## 四、API 封装设计

### 4.1 Axios 实例配置

```typescript
// api/index.ts
import axios from 'axios'
import type { Result } from '@/types/api'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use((config) => {
  // 可在此添加 token 等认证信息
  return config
})

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data as Result
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res.data
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

### 4.2 各模块 API

| 模块 | 文件 | 接口函数 |
|------|------|----------|
| 认证 | `auth.ts` | `patientRegister()`, `patientLogin()`, `adminLogin()` |
| 科室 | `department.ts` | `getDeptList()`, `addDept()`, `updateDept()`, `deleteDept()` |
| 医生 | `doctor.ts` | `getDoctorList()`, `getDoctorsByDept()` |
| 排班 | `schedule.ts` | `getScheduleList()`, `getAvailableSchedules()`, `addSchedule()`, `updateSchedule()`, `deleteSchedule()` |
| 挂号 | `registration.ts` | `createRegistration()`, `cancelRegistration()`, `getMyRegistrations()`, `getStatistics()` |

---

## 五、路由设计

```typescript
// router/index.ts
const routes = [
  // 患者端路由
  {
    path: '/',
    redirect: '/patient/login'
  },
  {
    path: '/patient',
    component: PatientLayout,
    children: [
      { path: 'login', name: 'PatientLogin', component: () => import('@/views/patient/Login.vue') },
      { path: 'register', name: 'PatientRegister', component: () => import('@/views/patient/Register.vue') },
      { path: 'booking', name: 'Booking', component: () => import('@/views/patient/Booking.vue'), meta: { requiresAuth: true } },
      { path: 'records', name: 'MyRecords', component: () => import('@/views/patient/MyRecords.vue'), meta: { requiresAuth: true } }
    ]
  },

  // 管理员端路由
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: 'login', name: 'AdminLogin', component: () => import('@/views/admin/Login.vue') },
      { path: 'department', name: 'DeptManage', component: () => import('@/views/admin/Department.vue'), meta: { requiresAdmin: true } },
      { path: 'schedule', name: 'ScheduleManage', component: () => import('@/views/admin/Schedule.vue'), meta: { requiresAdmin: true } },
      { path: 'statistics', name: 'Statistics', component: () => import('@/views/admin/Statistics.vue'), meta: { requiresAdmin: true } }
    ]
  }
]
```

### 路由守卫

```typescript
// router/guards.ts
router.beforeEach((to, from, next) => {
  const patientStore = usePatientStore()
  const adminStore = useAdminStore()

  if (to.meta.requiresAuth && !patientStore.isLoggedIn) {
    next({ name: 'PatientLogin' })
  } else if (to.meta.requiresAdmin && !adminStore.isLoggedIn) {
    next({ name: 'AdminLogin' })
  } else {
    next()
  }
})
```

---

## 六、状态管理设计（Pinia）

### 6.1 患者状态

```typescript
// stores/patient.ts
export const usePatientStore = defineStore('patient', () => {
  const patient = ref<Patient | null>(null)

  const isLoggedIn = computed(() => !!patient.value)

  function setPatient(p: Patient) {
    patient.value = p
    localStorage.setItem('patient', JSON.stringify(p))
  }

  function logout() {
    patient.value = null
    localStorage.removeItem('patient')
  }

  function init() {
    const saved = localStorage.getItem('patient')
    if (saved) {
      patient.value = JSON.parse(saved)
    }
  }

  return { patient, isLoggedIn, setPatient, logout, init }
})
```

### 6.2 管理员状态

```typescript
// stores/admin.ts
export const useAdminStore = defineStore('admin', () => {
  const admin = ref<Admin | null>(null)

  const isLoggedIn = computed(() => !!admin.value)

  function setAdmin(a: Admin) {
    admin.value = a
    localStorage.setItem('admin', JSON.stringify(a))
  }

  function logout() {
    admin.value = null
    localStorage.removeItem('admin')
  }

  function init() {
    const saved = localStorage.getItem('admin')
    if (saved) {
      admin.value = JSON.parse(saved)
    }
  }

  return { admin, isLoggedIn, setAdmin, logout, init }
})
```

---

## 七、页面设计与实现思路

### 7.1 患者端页面

#### (1) 登录/注册页 (`Login.vue` + `Register.vue`)

**功能**：
- 手机号 + 密码登录
- 新患者注册（姓名、身份证、手机号、密码等）

**实现思路**：
- 使用 Element Plus 的 `el-form` 组件进行表单验证
- 手机号正则验证、身份证号格式验证
- 登录成功后存储患者信息到 Pinia，跳转到预约页

**关键组件**：
- `el-form` + `el-form-item`
- `el-input`
- `el-button`

---

#### (2) 挂号预约页 (`Booking.vue`)

**功能**：
- 步骤式预约流程：选科室 -> 选医生/日期 -> 选时段 -> 确认

**实现思路**：
- 使用 `el-steps` 实现步骤指示
- 步骤1：调用 `GET /api/dept/list` 获取科室列表，使用卡片或列表展示
- 步骤2：选择日期（el-date-picker），调用 `GET /api/schedule/available?deptId=&workDate=` 获取可用排班
- 步骤3：展示可选时段，显示医生信息、剩余号源、费用
- 步骤4：确认信息，调用 `POST /api/registration` 创建挂号

**状态管理**：
```typescript
const currentStep = ref(0)
const selectedDept = ref<Department | null>(null)
const selectedDate = ref<string>('')
const selectedSchedule = ref<Schedule | null>(null)
```

**关键组件**：
- `el-steps` + `el-step`
- `el-card`
- `el-date-picker`
- `el-table` 或自定义卡片列表
- `el-descriptions`（确认信息展示）

---

#### (3) 我的挂号记录页 (`MyRecords.vue`)

**功能**：
- 展示当前患者的所有挂号记录
- 支持退号操作（状态为"待就诊"的可退号）

**实现思路**：
- 调用 `GET /api/registration/my?patientId=` 获取记录
- 使用表格或卡片列表展示
- 状态标签：待就诊（蓝色）、已就诊（绿色）、已取消（灰色）
- 退号按钮点击后弹出确认框，调用 `PUT /api/registration/cancel/{id}`

**关键组件**：
- `el-table` + `el-table-column`
- `el-tag`（状态标签）
- `el-button`
- `el-popconfirm`（退号确认）

---

### 7.2 管理员端页面

#### (4) 管理员登录页 (`admin/Login.vue`)

**功能**：
- 用户名 + 密码登录

**实现思路**：
- 调用 `POST /api/admin/login`
- 登录成功存储管理员信息，跳转到科室管理页

---

#### (5) 科室管理页 (`Department.vue`)

**功能**：
- 科室列表展示
- 新增、编辑、删除科室

**实现思路**：
- 使用 `el-table` 展示科室列表
- 操作栏：编辑、删除按钮
- 新增/编辑使用 `el-dialog` 弹窗 + 表单
- CRUD 调用对应 API

**关键组件**：
- `el-table`
- `el-dialog`
- `el-form`
- `el-button`
- `el-popconfirm`

---

#### (6) 排班管理页 (`Schedule.vue`)

**功能**：
- 排班列表展示（可按科室、日期筛选）
- 新增、编辑、删除排班

**实现思路**：
- 顶部筛选区：科室选择器 + 日期选择器
- 表格展示排班信息（医生、日期、时段、号源数、费用、状态）
- 新增排班时需选择医生、日期、时段、号源数、费用

**关键组件**：
- `el-select`（科室筛选）
- `el-date-picker`（日期筛选）
- `el-table`
- `el-dialog`
- `el-form`

---

#### (7) 挂号统计页 (`Statistics.vue`)

**功能**：
- 展示挂号统计数据
- 可考虑简单的图表展示

**实现思路**：
- 调用 `GET /api/registration/statistics` 获取统计数据
- 使用 `el-statistic` 或 `el-card` 展示数字统计
- 可选：使用 ECharts 展示趋势图

**关键组件**：
- `el-card`
- `el-statistic`
- 可选：ECharts（按科室/日期统计图表）

---

## 八、组件复用设计

### 8.1 布局组件

**PatientLayout.vue**：
- 顶部导航栏：Logo、导航菜单（预约挂号、我的记录）、用户信息、退出
- 主内容区：`<router-view />`

**AdminLayout.vue**：
- 左侧菜单栏：科室管理、排班管理、挂号统计
- 顶部：面包屑、管理员信息、退出
- 主内容区：`<router-view />`

### 8.2 公共组件

| 组件 | 用途 |
|------|------|
| PageHeader.vue | 页面标题和操作按钮区 |
| EmptyState.vue | 空数据状态展示 |
| ConfirmDialog.vue | 通用确认弹窗 |

---

## 九、关键 Composables

### useAuth.ts

```typescript
export function useAuth() {
  const patientStore = usePatientStore()
  const router = useRouter()

  async function loginAsPatient(params: PatientLoginParams) {
    const patient = await patientLogin(params)
    patientStore.setPatient(patient)
    router.push('/patient/booking')
  }

  function logoutPatient() {
    patientStore.logout()
    router.push('/patient/login')
  }

  return { loginAsPatient, logoutPatient }
}
```

### useMessage.ts

```typescript
export function useMessage() {
  return {
    success: (msg: string) => ElMessage.success(msg),
    error: (msg: string) => ElMessage.error(msg),
    confirm: (msg: string) => ElMessageBox.confirm(msg, '提示', { type: 'warning' })
  }
}
```

---

## 十、环境配置

### .env.development

```env
VITE_API_BASE_URL=http://localhost:8080
```

### vite.config.ts 代理配置

```typescript
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

---

## 十一、依赖清单

```json
{
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.0",
    "pinia": "^2.1.0",
    "axios": "^1.6.0",
    "element-plus": "^2.4.0",
    "dayjs": "^1.11.0",
    "@vueuse/core": "^10.7.0"
  },
  "devDependencies": {
    "typescript": "^5.3.0",
    "vite": "^5.0.0",
    "@vitejs/plugin-vue": "^4.5.0",
    "unplugin-auto-import": "^0.17.0",
    "unplugin-vue-components": "^0.26.0"
  }
}
```

---

## 十二、开发脚本

### scripts/dev.sh

```bash
#!/bin/bash
cd "$(dirname "$0")/../frontend"
pnpm dev
```

### scripts/build.sh

```bash
#!/bin/bash
cd "$(dirname "$0")/../frontend"
pnpm build
```

---

## 十三、总结

### 技术亮点
1. **TypeScript 全覆盖**：所有 API 响应和实体均有类型定义，避免运行时错误
2. **Composition API + Pinia**：现代 Vue 3 开发范式，逻辑复用性强
3. **自动导入**：减少重复 import 语句，代码更简洁
4. **模块化 API 封装**：按业务模块拆分，便于维护
5. **路由守卫**：患者端和管理员端权限分离

### 页面数量确认
| 端 | 页面 | 路由 |
|----|------|------|
| 患者端 | 登录页 | /patient/login |
| 患者端 | 注册页 | /patient/register |
| 患者端 | 预约挂号页 | /patient/booking |
| 患者端 | 我的记录页 | /patient/records |
| 管理员端 | 登录页 | /admin/login |
| 管理员端 | 科室管理页 | /admin/department |
| 管理员端 | 排班管理页 | /admin/schedule |
| 管理员端 | 挂号统计页 | /admin/statistics |

共 8 个页面，符合需求。

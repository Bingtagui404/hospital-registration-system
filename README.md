# 门诊挂号管理系统

大学课程设计项目 - 医院门诊挂号管理模块

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.x |
| ORM | MyBatis |
| 数据库 | MySQL 8.0 |
| 前端框架 | Vue.js 3 + TypeScript |
| UI组件库 | Element Plus |
| 状态管理 | Pinia |
| HTTP请求 | Axios |

## 项目结构

```
Keshe_project/
├── src/main/java/com/hospital/registration/   # 后端代码
│   ├── controller/          # 控制器
│   ├── service/             # 业务层
│   ├── mapper/              # 数据层
│   ├── entity/              # 实体类
│   ├── vo/                  # 返回对象
│   └── config/              # 配置
├── frontend/                # 前端代码
│   ├── src/views/patient/   # 患者端页面
│   ├── src/views/admin/     # 管理员端页面
│   ├── src/api/             # API封装
│   └── src/stores/          # 状态管理
├── scripts/                 # 启动脚本
└── logs/                    # 日志目录
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0

### 数据库配置

1. 创建数据库 `hospital_registration`
2. 执行建表脚本（参见 CLAUDE.md）
3. 修改 `src/main/resources/application.yml` 中的数据库连接信息

### 启动项目

#### Windows 系统（推荐）

双击运行脚本即可：

| 脚本 | 功能 |
|------|------|
| `scripts/start-all.bat` | 一键启动前后端 |
| `scripts/start-backend.bat` | 单独启动后端 |
| `scripts/start-frontend.bat` | 单独启动前端 |
| `scripts/stop-all.bat` | 停止所有服务 |
| `scripts/build-backend.bat` | 编译后端 |

#### 命令行方式

```bash
# 启动后端
cd Keshe_project
./mvnw spring-boot:run

# 启动前端（另开终端）
cd frontend
npm install
npm run dev
```

### 访问地址

| 服务 | 地址 |
|------|------|
| 后端 API | http://localhost:8080 |
| 前端页面 | http://localhost:5173 |
| 患者登录 | http://localhost:5173/patient/login |
| 管理员登录 | http://localhost:5173/admin/login |

## 核心技术要点

### 并发安全机制

本系统在挂号/退号流程中实现了多层并发保护：

#### 1. 号源扣减 - 乐观锁

```sql
-- 原子扣减，防止超卖
UPDATE schedule SET remaining_quota = remaining_quota - 1
WHERE schedule_id = ? AND remaining_quota > 0
```

#### 2. 排队号生成 - MAX+1 + 重试机制

```java
// 使用 MAX(queue_no)+1 而非 totalQuota-remainingQuota
// 避免并发时生成重复排队号
Integer maxQueueNo = registrationMapper.selectMaxQueueNo(scheduleId);
int queueNo = (maxQueueNo == null) ? 1 : maxQueueNo + 1;
```

#### 3. 挂号单号生成 - MAX+1 + 重试机制

```java
// 格式: GH + yyyyMMdd + 6位序号（符合规范）
// 使用 MAX+1 查询当天最大序号，配合重试机制处理并发冲突
String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
Integer maxSeq = mapper.selectMaxSeqByDate(dateStr);
int seq = (maxSeq == null) ? 1 : maxSeq + 1;
return String.format("GH%s%06d", dateStr, seq);
```

#### 4. 退号 - 条件更新

```sql
-- 只有状态为 BOOKED 才能退号，防止并发重复退号
UPDATE registration SET status = 'CANCELLED'
WHERE reg_id = ? AND status = 'BOOKED'
```

### 防重复挂号约束 - active_flag 方案

采用生成列 + 唯一索引，利用 MySQL 唯一索引忽略 NULL 的特性：

```sql
-- 生成列：BOOKED 时为 1，其他状态为 NULL
ALTER TABLE registration
ADD COLUMN active_flag TINYINT
    GENERATED ALWAYS AS (CASE WHEN status = 'BOOKED' THEN 1 ELSE NULL END) STORED;

-- 唯一约束：只约束 BOOKED 状态
ALTER TABLE registration
ADD UNIQUE INDEX uk_patient_schedule_active (patient_id, schedule_id, active_flag);
```

**效果**：
- ✅ 同一患者 + 同一号源只能有一条 BOOKED 记录（防重复挂号）
- ✅ 允许多条 CANCELLED/FINISHED 历史记录（支持多次挂退）
- ✅ 数据库层面阻止并发连点

### 事务管理

所有写操作均使用 `@Transactional` 注解保证数据一致性。

### 并发冲突智能处理

挂号时的 `DuplicateKeyException` 根据冲突类型智能处理：

```java
// 判断冲突类型：queueNo冲突、reg_no冲突、还是真正的重复挂号
if (msg.contains("uk_schedule_queue") || msg.contains("reg_no")) {
    // queueNo 或 reg_no 冲突 → 重新计算并重试（最多3次）
} else {
    // 真正的重复挂号（uk_patient_schedule_active）→ 返回错误提示
}
```

### 排班管理异常处理

新增和编辑排班时捕获唯一约束冲突，返回友好提示：

```java
// add() 和 update() 方法都捕获 DuplicateKeyException
try {
    scheduleMapper.insert(schedule); // 或 update
} catch (DuplicateKeyException e) {
    return Result.error("该医生在此日期和时段已有排班");
}
```

## 数据库维护

### 初始化

1. 执行 `CLAUDE.md` 中的建表 SQL
2. 执行 `scripts/fix_constraints.sql` 添加 active_flag 约束

### 排查 SQL

当出现约束冲突时，使用以下 SQL 定位问题：

```sql
SELECT reg_id, patient_id, schedule_id, status, active_flag, reg_time
FROM registration
WHERE patient_id = ? AND schedule_id = ?
ORDER BY reg_time;
```

## 功能模块

### 患者端

- 注册/登录
- 个人信息管理
- 挂号预约（选科室 → 选医生 → 选日期时段 → 确认）
- 挂号记录查询
- 退号申请

### 管理员端

- 科室管理（增删改查）
- 医生排班管理（含号源同步校验）
- 挂号统计（支持标记已就诊）

## 测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | admin | 123456 |
| 患者 | 手机号注册 | 自定义 |

## API 接口

### 科室
- `GET /api/dept/list` - 科室列表
- `POST /api/dept` - 新增科室
- `PUT /api/dept` - 更新科室
- `DELETE /api/dept/{id}` - 删除科室

### 挂号
- `POST /api/registration` - 创建挂号
- `PUT /api/registration/cancel/{id}` - 退号
- `PUT /api/registration/finish/{id}` - 标记已就诊（管理员）
- `GET /api/registration/my?patientId=` - 我的挂号记录
- `GET /api/schedule/available?deptId=&workDate=` - 查询可预约号源

## 开发日志

### 2025-12-23 代码审查修复

#### 第一轮修复
| 问题 | 解决方案 |
|------|----------|
| 退号并发问题 | 条件更新 `WHERE status='BOOKED'` + 检查影响行数 |
| 挂号单号并发 | 改用时间戳+随机数（后改为 MAX+1 序号） |
| 防重复挂号绕过 | 捕获 `DuplicateKeyException` |
| Admin绕过Service | 创建 `AdminService` + `AdminServiceImpl` |
| 缺少@Transactional | 为所有 ServiceImpl 写操作添加事务注解 |

#### 第二轮修复
| 问题 | 解决方案 |
|------|----------|
| queueNo 并发重复 | 改用 `MAX(queue_no)+1` + 重试机制 |
| 多次挂退约束冲突 | `active_flag` 生成列方案（见上文） |
| 缺少患者信息页面 | 新增 `Profile.vue` + 路由配置 |
| 挂号单号格式不符规范 | 改为 `GH + yyyyMMdd + 6位序号` |
| 排班重复无友好提示 | `add()` 捕获异常返回提示 |

#### 第三轮修复
| 问题 | 解决方案 |
|------|----------|
| reg_no 并发冲突误判为重复挂号 | 异常判断增加 `msg.contains("reg_no")`，冲突时重试 |
| 编辑排班抛 DB 异常 | `update()` 方法添加 `try-catch` |

#### 脚本修复
| 问题 | 解决方案 |
|------|----------|
| .bat 中文乱码 | 改用英文避免 UTF-8/GBK 编码问题 |
| vite 命令找不到 | `start-frontend.bat` 改用 `npx vite` |

### 2025-12-24 功能优化

#### UI/UX 优化
| 问题 | 解决方案 |
|------|----------|
| 个人信息页年龄不显示数值 | `Profile.vue` 加载时 `age: res.data.age ?? 0` |
| "已预约"改为"待就诊" | `Statistics.vue` 状态文案统一修改 |

#### 排班号源同步
| 问题 | 解决方案 |
|------|----------|
| 调小总号源后剩余号源未变化 | 后端计算 `booked = total - remaining`，同步调整 |
| 总号源可设为小于已预约数 | 校验 `newTotal >= booked`，否则拒绝 |
| 前端无最小值限制 | `el-input-number :min="bookedCount"` 动态限制 |

#### 新增"标记已就诊"功能
| 改动位置 | 内容 |
|----------|------|
| `RegistrationService` | 新增 `finish(regId)` 方法 |
| `RegistrationController` | 新增 `PUT /api/registration/finish/{id}` |
| `Statistics.vue` | 表格增加"操作"列，待就诊记录显示"标记已就诊"按钮 |

#### 代码健壮性
| 问题 | 解决方案 |
|------|----------|
| RegistrationController 参数未校验 | 添加 `patientId/scheduleId` 空值检查 |
| user.ts JSON.parse 无异常处理 | 添加 try-catch，数据损坏时自动清理 |
| Statistics.vue deptName 可能 undefined | 添加条件判断防止报错 |

#### 号源统计优化
| 问题 | 解决方案 |
|------|----------|
| FINISHED 不计入已占用号源 | `countOccupiedBySchedule` 统计 `status != 'CANCELLED'` |
| 脏数据导致 booked 为负数 | 从挂号表统计真实占用数，而非 total-remaining |

#### 前端数据处理
| 问题 | 解决方案 |
|------|----------|
| 年龄清空后提交报错 | `handleSave` 中空字符串/undefined 转为 `null` |
| 科室占比分母含取消记录 | 新增 `validTotal`（非取消数）作为分母 |

### 2025-12-25 功能增强

#### 软删除恢复机制
| 问题 | 解决方案 |
|------|----------|
| 科室删除后无法重新添加同名科室 | `DepartmentMapper` 新增 `selectDeletedByName`、`restoreById`；`add()` 检测已删除记录并恢复 |
| 排班删除后无法重新添加相同排班 | `ScheduleMapper` 新增 `selectDeletedByDoctorDateSlot`、`restoreById`；`add()` 检测已删除记录并恢复 |
| 恢复排班时剩余号源计算错误 | 恢复前查询 `countOccupiedBySchedule`，校验 `totalQuota >= occupied`，计算 `remaining = total - occupied` |

#### 预约挂号界面优化
| 改动 | 说明 |
|------|------|
| 搜索功能 | 新增搜索框，一次搜索同时查询科室和医生，切换 Tab 保留结果 |
| 多表联合查询 | 医生搜索支持同时匹配医生姓名+科室名称 |
| 双入口模式 | Tab 切换"按科室预约"/"按医生预约" |
| 医生周历视图 | 选择医生后显示未来 7 天排班，有号/无号状态标识 |
| 后端搜索接口 | `GET /api/dept/search`、`GET /api/doctor/search`、`GET /api/schedule/doctor/{id}` |

#### 步骤流修复
| 问题 | 解决方案 |
|------|----------|
| 医生模式选日期后直接跳确认页 | `selectWeekDate` 不再改变 step，号源列表保持在周历下方显示 |
| 确认页返回定位错误 | `goBack` 根据模式返回正确步骤（医生模式→步骤1，科室模式→步骤2）|
| 周历日期时区偏移 | 新增 `formatLocalDate()` 使用本地时间，替代 `toISOString()` |

#### 其他修复
| 问题 | 解决方案 |
|------|----------|
| Login.vue 图标类型错误 | 添加 `@element-plus/icons-vue` 图标导入 |
| Profile.vue 年龄类型比较错误 | 修复 `age` 与空字符串比较的类型问题 |
| 患者端/管理端状态文案不一致 | MyRecords.vue "已预约" → "待就诊" |
| 恢复排班时 getTotalQuota() 可能 NPE | 添加 null 校验，返回友好错误提示 |

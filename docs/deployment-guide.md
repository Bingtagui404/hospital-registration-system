# 门诊挂号系统 - 部署指南

本文档帮助团队成员快速配置和运行项目。

---

## 一、环境要求

在开始之前，请确保已安装以下软件：

| 软件 | 版本要求 | 下载地址 |
|------|----------|----------|
| JDK | 17+ | https://adoptium.net/ |
| Node.js | 18+ | https://nodejs.org/ |
| MySQL | 8.0 | https://dev.mysql.com/downloads/mysql/ |
| Git | 任意 | https://git-scm.com/ |

### 验证安装

打开命令行，执行以下命令验证：

```bash
java -version    # 应显示 java version "17.x.x" 或更高
node -v          # 应显示 v18.x.x 或更高
npm -v           # 应显示 9.x.x 或更高
mysql --version  # 应显示 mysql Ver 8.x.x
```

---

## 二、数据库配置

### 2.1 创建数据库

打开 Navicat 或 MySQL 命令行，执行：

```sql
CREATE DATABASE hospital_registration
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;
```

### 2.2 执行建表脚本

1. 打开 `CLAUDE.md` 文件，找到"完整建表SQL"部分
2. 复制全部 SQL 语句
3. 在 Navicat 中执行

### 2.3 执行约束修复脚本

执行 `scripts/fix_constraints.sql` 添加 active_flag 约束：

```sql
USE hospital_registration;

ALTER TABLE registration DROP INDEX uk_patient_schedule_status;

ALTER TABLE registration
ADD COLUMN active_flag TINYINT
    GENERATED ALWAYS AS (CASE WHEN status = 'BOOKED' THEN 1 ELSE NULL END) STORED;

ALTER TABLE registration
ADD UNIQUE INDEX uk_patient_schedule_active (patient_id, schedule_id, active_flag);
```

---

## 三、后端配置

### 3.1 修改数据库连接信息

打开 `src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hospital_registration?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root           # ← 改成你的 MySQL 用户名
    password: XXX  # ← 改成你的 MySQL 密码
```

**重要**：只需修改 `username` 和 `password`，其他配置保持不变。

### 3.2 验证连接

可以先用 Navicat 测试能否连接数据库，确保用户名密码正确。

---

## 四、前端配置

**一般不需要修改**。

如果后端端口不是 8080，需要修改 `frontend/src/api/request.ts`：

```typescript
const request = axios.create({
  baseURL: 'http://localhost:8080/api',  // ← 如果后端端口变了，改这里
  timeout: 10000
})
```

---

## 五、安装依赖

### 5.1 后端依赖

后端使用 Maven，依赖会自动下载，**无需手动安装**。

### 5.2 前端依赖

打开命令行，进入 frontend 目录：

```bash
cd frontend
npm install
```

等待安装完成（首次可能需要几分钟）。

---

## 六、启动项目

### 方式一：使用脚本（推荐）

双击 `scripts/start-all.bat`，会自动启动前后端。

或者分别启动：
1. 双击 `scripts/start-backend.bat` → 等待看到 "Tomcat started on port 8080"
2. 双击 `scripts/start-frontend.bat` → 等待看到 "Local: http://localhost:5173"

### 方式二：命令行启动

**启动后端**（在项目根目录）：
```bash
./mvnw spring-boot:run
```

**启动前端**（在 frontend 目录）：
```bash
cd frontend
npx vite
```

---

## 七、访问系统

| 页面 | 地址 |
|------|------|
| 患者登录 | http://localhost:5173/patient/login |
| 管理员登录 | http://localhost:5173/admin/login |

### 测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | admin | 123456 |
| 患者 | 需要先注册 | 自定义 |

---

## 八、常见问题

### Q1: 后端启动报数据库连接失败

**原因**：MySQL 用户名或密码错误

**解决**：
1. 检查 `application.yml` 中的 username 和 password
2. 确保 MySQL 服务已启动
3. 用 Navicat 测试连接

### Q2: 前端启动报 'vite' 不是内部命令

**原因**：依赖未安装完整

**解决**：
```bash
cd frontend
npm install
npx vite
```

### Q3: 访问 http://localhost:8080 显示 404

**原因**：这是正常的，后端是纯 API 服务

**解决**：访问前端地址 http://localhost:5173

### Q4: 挂号时提示"号源不存在"

**原因**：数据库没有排班数据

**解决**：用管理员账号登录，在"排班管理"页面添加排班

### Q5: IDEA 导入项目后报错

**解决**：
1. 右键 `pom.xml` → Maven → Reload Project
2. 等待依赖下载完成
3. 设置 Project SDK 为 JDK 17+

### Q6: 端口被占用

**解决**：
```bash
# 查看占用 8080 端口的进程
netstat -ano | findstr 8080

# 杀掉进程（替换 PID）
taskkill /F /PID <PID>
```

---

## 九、项目结构速览

```
Keshe_project/
├── src/main/java/          # 后端 Java 代码
├── src/main/resources/     # 后端配置文件
│   └── application.yml     # ★ 数据库配置在这里
├── frontend/               # 前端 Vue 代码
│   └── src/api/request.ts  # ★ API 地址配置在这里
├── scripts/                # 启动脚本
│   ├── start-all.bat       # 一键启动
│   ├── start-backend.bat   # 启动后端
│   ├── start-frontend.bat  # 启动前端
│   └── fix_constraints.sql # 数据库约束脚本
└── docs/                   # 文档
```

---

## 十、快速检查清单

开始前确认以下事项：

- [ ] JDK 17+ 已安装
- [ ] Node.js 18+ 已安装
- [ ] MySQL 8.0 已安装并启动
- [ ] 数据库 `hospital_registration` 已创建
- [ ] 建表 SQL 已执行
- [ ] `fix_constraints.sql` 已执行
- [ ] `application.yml` 中的数据库密码已修改
- [ ] `npm install` 已执行（在 frontend 目录）

全部完成后，双击 `scripts/start-all.bat` 即可启动！

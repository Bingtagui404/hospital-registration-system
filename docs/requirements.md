# 门诊挂号管理系统 - 项目依赖清单

## 一、运行环境要求

### 操作系统

- **Windows 10/11**（推荐）
- macOS / Linux 也可运行

### 必装软件

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | 后端运行环境 |
| Node.js | 18+ | 前端运行环境 |
| MySQL | 8.0 | 数据库 |
| Git | 任意 | 版本控制 |

### 推荐开发工具

| 工具 | 用途 |
|------|------|
| IntelliJ IDEA | 后端开发 |
| VS Code | 前端开发 |
| Navicat | 数据库管理 |

---

## 二、后端依赖 (Maven)

### 核心框架

| 依赖 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.9 | 后端框架 |
| MyBatis Spring Boot Starter | 3.0.3 | ORM 框架 |
| Spring Boot Starter Web | - | Web 支持 |
| Spring Boot Starter Validation | - | 参数校验 |

### 数据库

| 依赖 | 版本 | 说明 |
|------|------|------|
| MySQL Connector/J | - | MySQL 驱动 |

### 开发工具

| 依赖 | 版本 | 说明 |
|------|------|------|
| Lombok | - | 简化代码（@Data 等注解） |
| Spring Boot DevTools | - | 热重载 |
| Spring Boot Starter Test | - | 单元测试 |

### pom.xml 依赖配置

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.9</version>
</parent>

<properties>
    <java.version>17</java.version>
</properties>

<dependencies>
    <!-- MyBatis -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>3.0.3</version>
    </dependency>

    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- 参数校验 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- MySQL 驱动 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- 热重载 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>

    <!-- 测试 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 三、前端依赖 (npm)

### 生产依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| vue | ^3.5.24 | 前端框架 |
| vue-router | ^4.6.4 | 路由管理 |
| pinia | ^3.0.4 | 状态管理 |
| axios | ^1.13.2 | HTTP 请求 |
| element-plus | ^2.13.0 | UI 组件库 |
| @element-plus/icons-vue | ^2.3.2 | Element Plus 图标 |

### 开发依赖

| 依赖 | 版本 | 说明 |
|------|------|------|
| vite | ^7.2.4 | 构建工具 |
| typescript | ~5.9.3 | TypeScript 支持 |
| vue-tsc | ^3.1.4 | Vue TypeScript 编译 |
| @vitejs/plugin-vue | ^6.0.1 | Vite Vue 插件 |
| @vue/tsconfig | ^0.8.1 | Vue TS 配置 |
| @types/node | ^24.10.1 | Node 类型定义 |

### package.json 依赖配置

```json
{
  "dependencies": {
    "@element-plus/icons-vue": "^2.3.2",
    "axios": "^1.13.2",
    "element-plus": "^2.13.0",
    "pinia": "^3.0.4",
    "vue": "^3.5.24",
    "vue-router": "^4.6.4"
  },
  "devDependencies": {
    "@types/node": "^24.10.1",
    "@vitejs/plugin-vue": "^6.0.1",
    "@vue/tsconfig": "^0.8.1",
    "typescript": "~5.9.3",
    "vite": "^7.2.4",
    "vue-tsc": "^3.1.4"
  }
}
```

---

## 四、数据库要求

### MySQL 配置

| 配置项 | 值 |
|--------|-----|
| 数据库名 | hospital_registration |
| 字符集 | utf8mb4 |
| 排序规则 | utf8mb4_general_ci |

### 数据表

| 表名 | 说明 |
|------|------|
| department | 科室表 |
| doctor | 医生表 |
| patient | 患者表 |
| schedule | 排班/号源表 |
| registration | 挂号记录表 |
| admin | 管理员表 |

---

## 五、端口要求

| 服务 | 端口 | 说明 |
|------|------|------|
| 后端 API | 8080 | Spring Boot |
| 前端页面 | 5173 | Vite 开发服务器 |
| MySQL | 3306 | 数据库 |

确保这些端口未被占用。

---

## 六、网络要求

- 首次运行需要联网下载 Maven 和 npm 依赖
- 后续可离线运行

---

## 七、版本兼容性说明

| 组件 | 最低版本 | 推荐版本 |
|------|----------|----------|
| JDK | 17 | 17 或 21 |
| Node.js | 18 | 20 LTS |
| MySQL | 8.0 | 8.0 |
| npm | 9.x | 10.x |

---

## 八、快速安装命令

### Windows

```powershell
# 检查 Java
java -version

# 检查 Node.js
node -v
npm -v

# 检查 MySQL
mysql --version

# 安装前端依赖
cd frontend
npm install
```

### 依赖下载慢？

配置国内镜像：

```bash
# npm 淘宝镜像
npm config set registry https://registry.npmmirror.com

# Maven 阿里云镜像（修改 ~/.m2/settings.xml）
```

---

## 九、总结

本项目技术栈：

```
┌─────────────────────────────────────────────────┐
│                    前端                          │
│  Vue 3 + TypeScript + Element Plus + Pinia      │
│  构建: Vite 7 | 请求: Axios | 路由: Vue Router  │
├─────────────────────────────────────────────────┤
│                    后端                          │
│  Spring Boot 3.5 + MyBatis 3 + Lombok           │
│  JDK 17 | Maven                                 │
├─────────────────────────────────────────────────┤
│                   数据库                         │
│  MySQL 8.0 (utf8mb4)                            │
└─────────────────────────────────────────────────┘
```

# 门诊挂号管理系统 - 开发需求文档

> **说明**：本文档为与用户讨论后整理的完整需求，请codex按此文档实现项目。

---

## 一、项目概述

### 1.1 项目背景
这是一个**大学课程设计项目**，周期3周，4人团队。用户是Java初学者，需要实现一个医院门诊挂号管理系统。

### 1.2 选题模块
**门诊挂号管理模块**，核心功能包括：
- 患者基础信息登记
- 号源查询与预约
- 挂号记录明细查询
- 医生出诊排班设置
- 挂号费用核算
- 退号申请处理

### 1.3 技术栈要求
| 层级 | 技术 |
|------|------|
| 后端框架 | SpringBoot 3.x |
| ORM | MyBatis |
| 数据库 | MySQL 8.0 |
| 前端框架 | Vue.js 3 |
| HTTP请求 | Axios |
| 版本控制 | Git |

---

## 二、数据库设计（已确定）

### 2.1 表关系
```
Department(科室) 1──N Doctor(医生) 1──N Schedule(号源) 1──N Registration(挂号) N──1 Patient(患者)
```

### 2.2 完整建表SQL

**请使用以下SQL创建数据库：**

```sql
-- ================================================================
-- 门诊挂号管理系统 - 完整建表脚本（改进版）
-- ================================================================

DROP DATABASE IF EXISTS hospital_registration;
CREATE DATABASE hospital_registration
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

USE hospital_registration;

-- 科室表 (Department)
CREATE TABLE department (
    dept_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '科室ID',
    dept_name VARCHAR(50) NOT NULL UNIQUE COMMENT '科室名称(唯一)',
    description VARCHAR(500) COMMENT '科室描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1有效 0无效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室表';

-- 医生表 (Doctor)
CREATE TABLE doctor (
    doctor_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '医生ID',
    dept_id INT NOT NULL COMMENT '所属科室ID',
    doctor_name VARCHAR(50) NOT NULL COMMENT '医生姓名',
    gender ENUM('M','F') DEFAULT 'M' COMMENT '性别：M男 F女',
    title VARCHAR(50) COMMENT '职称',
    specialty VARCHAR(200) COMMENT '擅长领域',
    phone VARCHAR(20) COMMENT '联系电话',
    status TINYINT DEFAULT 1 COMMENT '状态：1在职 0离职',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_doctor_dept FOREIGN KEY (dept_id) REFERENCES department(dept_id),
    INDEX idx_doctor_dept (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生表';

-- 患者表 (Patient)
CREATE TABLE patient (
    patient_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '患者ID',
    patient_name VARCHAR(50) NOT NULL COMMENT '患者姓名',
    id_card VARCHAR(18) NOT NULL UNIQUE COMMENT '身份证号（唯一）',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    gender ENUM('M','F') DEFAULT 'M' COMMENT '性别',
    age INT COMMENT '年龄',
    address VARCHAR(200) COMMENT '家庭住址',
    medical_history TEXT COMMENT '既往病史',
    password VARCHAR(100) NOT NULL DEFAULT '123456' COMMENT '登录密码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者表';

-- 号源/排班表 (Schedule)
CREATE TABLE schedule (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '排班/号源ID',
    doctor_id INT NOT NULL COMMENT '医生ID',
    work_date DATE NOT NULL COMMENT '出诊日期',
    time_slot ENUM('AM','PM') NOT NULL COMMENT '时段：AM上午 PM下午',
    total_quota INT NOT NULL DEFAULT 20 COMMENT '总号源数',
    remaining_quota INT NOT NULL DEFAULT 20 COMMENT '剩余号源数(实时更新)',
    fee DECIMAL(10,2) NOT NULL DEFAULT 15.00 COMMENT '挂号费',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常 0停诊',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES doctor(doctor_id),
    UNIQUE KEY uk_doctor_date_slot (doctor_id, work_date, time_slot),
    INDEX idx_schedule_date (work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='号源/排班表';

-- 挂号记录表 (Registration)
CREATE TABLE registration (
    reg_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '挂号ID',
    reg_no VARCHAR(20) NOT NULL UNIQUE COMMENT '挂号单号(唯一)',
    patient_id INT NOT NULL COMMENT '患者ID',
    schedule_id INT NOT NULL COMMENT '号源ID',
    doctor_id INT COMMENT '医生ID（冗余）',
    dept_id INT COMMENT '科室ID（冗余）',
    work_date DATE COMMENT '就诊日期（冗余）',
    time_slot ENUM('AM','PM') COMMENT '就诊时段（冗余）',
    queue_no INT COMMENT '排队序号',
    fee DECIMAL(10,2) NOT NULL COMMENT '挂号费',
    status ENUM('BOOKED','CANCELLED','FINISHED') NOT NULL DEFAULT 'BOOKED' COMMENT '状态',
    active_flag TINYINT GENERATED ALWAYS AS (CASE WHEN status = 'BOOKED' THEN 1 ELSE NULL END) STORED COMMENT '仅BOOKED=1，用于唯一约束',
    reg_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '挂号时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reg_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id),
    CONSTRAINT fk_reg_schedule FOREIGN KEY (schedule_id) REFERENCES schedule(schedule_id),
    UNIQUE KEY uk_patient_schedule_active (patient_id, schedule_id, active_flag),
    UNIQUE KEY uk_schedule_queue (schedule_id, queue_no),
    INDEX idx_reg_patient (patient_id),
    INDEX idx_reg_schedule (schedule_id),
    INDEX idx_reg_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='挂号记录表';

-- 管理员表 (Admin)
CREATE TABLE admin (
    admin_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';
```

---

## 三、核心业务规则（必须遵守）

### 3.1 挂号流程
```
1. 检查 schedule.remaining_quota > 0
2. 执行原子更新：UPDATE schedule SET remaining_quota = remaining_quota - 1 WHERE schedule_id = ? AND remaining_quota > 0
3. 检查影响行数，若为0则号源已满，返回失败
4. 生成挂号单号（格式：GH + yyyyMMdd + 6位序号）
5. 计算排队号：SELECT MAX(queue_no) + 1（必要时重试）
6. 插入 registration 记录（status = 'BOOKED'）
7. 冗余字段（doctor_id, dept_id, work_date, time_slot）从 schedule 和 doctor 表获取并填充
```

### 3.2 退号流程
```
1. 查询 registration 记录，检查 status = 'BOOKED'
2. 检查是否在允许退号时间内（就诊时间前1小时）
3. 更新 registration.status = 'CANCELLED'
4. 恢复号源：UPDATE schedule SET remaining_quota = remaining_quota + 1 WHERE schedule_id = ?
```

### 3.3 防重复挂号
- 同一患者 + 同一号源只允许 1 条 BOOKED（active_flag 生成列 + 唯一索引）
- 退号后（status='CANCELLED'）允许重新挂号

### 3.4 号源实时更新
- 挂号成功：remaining_quota - 1
- 退号成功：remaining_quota + 1
- 使用乐观锁防止超卖

---

## 四、功能需求（8个核心页面）

### 4.1 患者端功能

| 页面 | 功能描述 | 对应接口 |
|------|----------|----------|
| 患者注册/登录 | 注册账号、登录系统 | POST /api/patient/register, POST /api/patient/login |
| 患者信息管理 | 填写/修改个人信息 | GET/PUT /api/patient/info |
| 挂号预约 | 选科室→选医生→选日期时段→确认挂号 | GET /api/dept/list, GET /api/doctor/list, GET /api/schedule/available, POST /api/registration |
| 挂号记录查询 | 查看历史挂号记录 | GET /api/registration/my |
| 退号申请 | 取消已预约的挂号 | PUT /api/registration/cancel/{id} |

### 4.2 管理员端功能

| 页面 | 功能描述 | 对应接口 |
|------|----------|----------|
| 科室管理 | 增删改查科室 | CRUD /api/dept |
| 医生排班管理 | 设置医生出诊时间和号源 | CRUD /api/schedule |
| 挂号统计 | 统计挂号数据、费用汇总 | GET /api/registration/statistics |

---

## 五、接口设计

### 5.1 统一返回格式
```json
{
    "code": 200,
    "message": "操作成功",
    "data": { }
}
```

### 5.2 核心接口

#### 创建挂号（最重要）
```
POST /api/registration
Request Body: { "patientId": 1, "scheduleId": 5 }
Response: { "code": 200, "message": "挂号成功", "data": { "regNo": "GH20241223000001", "queueNo": 5, ... } }
```

#### 退号
```
PUT /api/registration/cancel/{id}
Response: { "code": 200, "message": "退号成功" }
```

#### 查询可预约号源
```
GET /api/schedule/available?deptId=1&workDate=2024-12-23
Response: 返回该科室该日期的可预约排班列表（remaining_quota > 0）
```

#### 查询患者挂号记录
```
GET /api/registration/my?patientId=1
Response: 返回该患者的所有挂号记录（关联医生名、科室名）
```

---

## 六、项目结构要求

### 6.1 后端目录结构
```
src/main/java/com/hospital/registration/
├── RegistrationApplication.java     # 启动类
├── controller/                      # 控制器
│   ├── DepartmentController.java
│   ├── DoctorController.java
│   ├── PatientController.java
│   ├── ScheduleController.java
│   └── RegistrationController.java
├── service/                         # 业务层
│   ├── DepartmentService.java
│   ├── RegistrationService.java
│   └── impl/
├── mapper/                          # 数据层
│   ├── DepartmentMapper.java
│   ├── ScheduleMapper.java
│   └── RegistrationMapper.java
├── entity/                          # 实体类
│   ├── Department.java
│   ├── Doctor.java
│   ├── Patient.java
│   ├── Schedule.java
│   └── Registration.java
├── vo/                              # 返回对象
│   └── Result.java
└── config/                          # 配置
    └── CorsConfig.java
```

### 6.2 前端目录结构
```
src/
├── views/
│   ├── patient/                     # 患者端页面
│   │   ├── Login.vue
│   │   ├── Appointment.vue          # 挂号预约
│   │   ├── MyRecords.vue            # 我的记录
│   │   └── Refund.vue               # 退号
│   └── admin/                       # 管理端页面
│       ├── Department.vue
│       ├── Schedule.vue
│       └── Statistics.vue
├── api/                             # API封装
└── router/                          # 路由
```

---

## 七、其他要求

### 7.1 代码规范
- 使用 Lombok 简化实体类（@Data 注解）
- Controller 使用 @RestController + @RequestMapping
- Service 层使用 @Service + @Transactional（事务）
- Mapper 可使用注解方式（@Select, @Insert, @Update）

### 7.2 必须实现的约束
- 患者身份证号唯一（id_card UNIQUE）
- 医生排班唯一（doctor_id + work_date + time_slot UNIQUE）
- 挂号单号唯一（reg_no UNIQUE）
- 同一患者 + 号源仅 1 条 BOOKED（uk_patient_schedule_active）
- 同一号源内排队号唯一（schedule_id + queue_no UNIQUE）
- 号源扣减使用乐观锁防止超卖

### 7.3 测试数据
数据库脚本已包含测试数据：
- 9个科室
- 10个医生
- 5个患者
- 24条排班记录
- 1个管理员（admin/123456）

---

## 八、开发优先级

**请按以下顺序实现：**

1. **第一优先**：后端基础架构（项目创建、数据库连接、实体类）
2. **第二优先**：科室管理 CRUD（练手，验证架构）
3. **第三优先**：挂号功能（核心！）
4. **第四优先**：退号功能
5. **第五优先**：查询功能（号源查询、记录查询）
6. **第六优先**：前端页面
7. **最后**：排班管理、统计功能

---

## 九、参考示例代码

### 挂号Service核心逻辑
```java
@Service
public class RegistrationServiceImpl implements RegistrationService {
    
    @Autowired
    private RegistrationMapper registrationMapper;
    @Autowired
    private ScheduleMapper scheduleMapper;
    
    @Override
    @Transactional
    public Result<Registration> createRegistration(Integer patientId, Integer scheduleId) {
        // 1. 查询号源
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null || schedule.getRemainingQuota() <= 0) {
            return Result.error("号源已满");
        }
        
        // 2. 检查重复挂号
        Registration existing = registrationMapper.findByPatientAndScheduleAndStatus(
            patientId, scheduleId, "BOOKED");
        if (existing != null) {
            return Result.error("您已预约过该号源");
        }
        
        // 3. 原子扣减号源（乐观锁）
        int affected = scheduleMapper.decreaseQuota(scheduleId);
        if (affected == 0) {
            return Result.error("号源已被抢完");
        }
        
        // 4. 生成挂号记录
        Registration reg = new Registration();
        reg.setRegNo(generateRegNo());
        reg.setPatientId(patientId);
        reg.setScheduleId(scheduleId);
        reg.setDoctorId(schedule.getDoctorId());
        reg.setDeptId(/* 从doctor表获取 */);
        reg.setWorkDate(schedule.getWorkDate());
        reg.setTimeSlot(schedule.getTimeSlot());
        reg.setQueueNo(schedule.getTotalQuota() - schedule.getRemainingQuota() + 1);
        reg.setFee(schedule.getFee());
        reg.setStatus("BOOKED");
        
        registrationMapper.insert(reg);
        return Result.success("挂号成功", reg);
    }
}
```

### 号源扣减SQL（乐观锁）
```sql
UPDATE schedule 
SET remaining_quota = remaining_quota - 1 
WHERE schedule_id = #{scheduleId} AND remaining_quota > 0
```

---

**以上为完整需求，请按此实现！**

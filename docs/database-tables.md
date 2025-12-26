# 数据库表说明

以下内容基于当前项目的数据库设计与代码实体/Mapper 对齐整理，便于查阅。

## 表清单（概要）

| 表名 | 表中文注释 | 核心功能作用 |
| --- | --- | --- |
| department | 科室表 | 维护科室基础信息，供医生归属与预约筛选 |
| doctor | 医生表 | 医生档案与科室归属，供排班与预约展示 |
| patient | 患者表 | 患者基础信息与登录账号 |
| schedule | 号源/排班表 | 医生出诊日期/时段、号源数量与费用 |
| registration | 挂号记录表 | 挂号明细、排队号、状态与费用流水 |
| admin | 管理员表 | 后台登录账号信息 |

## 字段明细

### department（科室表）

| 字段名 | 数据类型 | 是否主键 | 默认值 | 注释说明 |
| --- | --- | --- | --- | --- |
| dept_id | INT | 是 | auto_increment | 科室ID |
| dept_name | VARCHAR(50) | 否 | - | 科室名称（唯一） |
| description | VARCHAR(500) | 否 | NULL | 科室描述 |
| status | TINYINT | 否 | 1 | 状态：1有效 0无效 |
| create_time | DATETIME | 否 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | 否 | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

### doctor（医生表）

| 字段名 | 数据类型 | 是否主键 | 默认值 | 注释说明 |
| --- | --- | --- | --- | --- |
| doctor_id | INT | 是 | auto_increment | 医生ID |
| dept_id | INT | 否 | - | 所属科室ID（外键 department.dept_id） |
| doctor_name | VARCHAR(50) | 否 | - | 医生姓名 |
| gender | ENUM('M','F') | 否 | 'M' | 性别 |
| title | VARCHAR(50) | 否 | NULL | 职称 |
| specialty | VARCHAR(200) | 否 | NULL | 擅长领域 |
| phone | VARCHAR(20) | 否 | NULL | 联系电话 |
| status | TINYINT | 否 | 1 | 状态：1在职 0离职 |
| create_time | DATETIME | 否 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | 否 | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

### patient（患者表）

| 字段名 | 数据类型 | 是否主键 | 默认值 | 注释说明 |
| --- | --- | --- | --- | --- |
| patient_id | INT | 是 | auto_increment | 患者ID |
| patient_name | VARCHAR(50) | 否 | - | 患者姓名 |
| id_card | VARCHAR(18) | 否 | - | 身份证号（唯一） |
| phone | VARCHAR(20) | 否 | - | 手机号 |
| gender | ENUM('M','F') | 否 | 'M' | 性别 |
| age | INT | 否 | NULL | 年龄 |
| address | VARCHAR(200) | 否 | NULL | 家庭住址 |
| medical_history | TEXT | 否 | NULL | 既往病史 |
| password | VARCHAR(100) | 否 | '123456' | 登录密码 |
| create_time | DATETIME | 否 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | 否 | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

### schedule（号源/排班表）

| 字段名 | 数据类型 | 是否主键 | 默认值 | 注释说明 |
| --- | --- | --- | --- | --- |
| schedule_id | INT | 是 | auto_increment | 排班ID |
| doctor_id | INT | 否 | - | 医生ID（外键 doctor.doctor_id） |
| work_date | DATE | 否 | - | 出诊日期 |
| time_slot | ENUM('AM','PM') | 否 | - | 时段：AM/PM |
| total_quota | INT | 否 | 20 | 总号源数 |
| remaining_quota | INT | 否 | 20 | 剩余号源数 |
| fee | DECIMAL(10,2) | 否 | 15.00 | 挂号费 |
| status | TINYINT | 否 | 1 | 状态：1正常 0停诊 |
| create_time | DATETIME | 否 | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | 否 | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

### registration（挂号记录表）

| 字段名 | 数据类型 | 是否主键 | 默认值 | 注释说明 |
| --- | --- | --- | --- | --- |
| reg_id | INT | 是 | auto_increment | 挂号ID |
| reg_no | VARCHAR(20) | 否 | - | 挂号单号（唯一） |
| patient_id | INT | 否 | - | 患者ID（外键 patient.patient_id） |
| schedule_id | INT | 否 | - | 号源ID（外键 schedule.schedule_id） |
| doctor_id | INT | 否 | NULL | 医生ID（冗余） |
| dept_id | INT | 否 | NULL | 科室ID（冗余） |
| work_date | DATE | 否 | NULL | 就诊日期（冗余） |
| time_slot | ENUM('AM','PM') | 否 | NULL | 就诊时段（冗余） |
| queue_no | INT | 否 | NULL | 排队序号 |
| fee | DECIMAL(10,2) | 否 | - | 挂号费 |
| status | ENUM('BOOKED','CANCELLED','FINISHED') | 否 | 'BOOKED' | 状态 |
| active_flag | TINYINT | 否 | GENERATED | 仅 BOOKED 时为 1，用于唯一约束 |
| reg_time | DATETIME | 否 | CURRENT_TIMESTAMP | 挂号时间 |
| update_time | DATETIME | 否 | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

### admin（管理员表）

| 字段名 | 数据类型 | 是否主键 | 默认值 | 注释说明 |
| --- | --- | --- | --- | --- |
| admin_id | INT | 是 | auto_increment | 管理员ID |
| username | VARCHAR(50) | 否 | - | 用户名（唯一） |
| password | VARCHAR(100) | 否 | - | 密码 |
| real_name | VARCHAR(50) | 否 | NULL | 真实姓名 |
| status | TINYINT | 否 | 1 | 状态：1正常 0禁用 |
| create_time | DATETIME | 否 | CURRENT_TIMESTAMP | 创建时间 |

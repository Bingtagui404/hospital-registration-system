-- ================================================================
-- 修复唯一约束问题 - 采用 active_flag 方案
--
-- 问题：uk_patient_schedule_status (patient_id, schedule_id, status)
--       会导致同一患者多次挂退号时冲突
--       （第二次退号时会有两条 CANCELLED 记录，违反约束）
--
-- 方案：使用生成列 + 唯一索引
--       利用 MySQL 唯一索引忽略 NULL 的特性
--       - BOOKED 状态: active_flag = 1 → 参与唯一约束
--       - 其他状态:    active_flag = NULL → 不参与唯一约束
--
-- 效果：
--       ✓ 允许多条 CANCELLED/FINISHED 历史记录
--       ✓ 同一患者+号源只能有一条 BOOKED 记录（防重复挂号）
--       ✓ 保留数据库层面的防重复保护
--
-- 请在 Navicat 中执行此脚本
-- ================================================================

USE hospital_registration;

-- 步骤 1: 删除旧的有问题的唯一约束
ALTER TABLE registration DROP INDEX uk_patient_schedule_status;

-- 步骤 2: 添加生成列 active_flag
-- BOOKED 状态时为 1，其他状态时为 NULL
ALTER TABLE registration
ADD COLUMN active_flag TINYINT
    GENERATED ALWAYS AS (CASE WHEN status = 'BOOKED' THEN 1 ELSE NULL END) STORED;

-- 步骤 3: 添加新的唯一约束
-- 利用 MySQL 唯一索引忽略 NULL 的特性，只约束 BOOKED 状态的记录
ALTER TABLE registration
ADD UNIQUE INDEX uk_patient_schedule_active (patient_id, schedule_id, active_flag);

-- 验证
SHOW INDEX FROM registration WHERE Key_name = 'uk_patient_schedule_active';

SELECT '约束修复完成！' AS message;

-- ================================================================
-- 排查 SQL：当出现约束冲突时，用于定位问题记录
-- ================================================================
-- SELECT reg_id, patient_id, schedule_id, status, active_flag, reg_time
-- FROM registration
-- WHERE patient_id = ? AND schedule_id = ?
-- ORDER BY reg_time;

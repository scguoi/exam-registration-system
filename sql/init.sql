-- ============================================================
-- 在线考试报名系统 - 数据库初始化脚本
-- 数据库: exam_registration_system
-- 版本: v1.0
-- 日期: 2025-10-16
-- 说明: 此脚本由 docker-compose 在容器首次启动时自动执行
--       数据库已由 MYSQL_DATABASE 环境变量自动创建
-- ============================================================

-- ============================================================
-- 1. 用户表 (sys_user)
-- 说明: 存储系统所有用户信息,包括考生和管理员
-- ============================================================
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名(手机号)',
  `password` VARCHAR(128) NOT NULL COMMENT '密码(BCrypt加密)',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `id_card` VARCHAR(64) DEFAULT NULL COMMENT '身份证号(AES加密)',
  `phone` VARCHAR(64) DEFAULT NULL COMMENT '手机号(AES加密)',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `gender` TINYINT DEFAULT NULL COMMENT '性别(1-男 2-女)',
  `birthday` DATE DEFAULT NULL COMMENT '出生日期',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `education` VARCHAR(50) DEFAULT NULL COMMENT '学历(高中/大专/本科等)',
  `work_unit` VARCHAR(200) DEFAULT NULL COMMENT '工作单位',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '联系地址',
  `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色(user-考生/admin-管理员)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1-正常 2-禁用)',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `login_fail_count` INT NOT NULL DEFAULT 0 COMMENT '登录失败次数',
  `lock_until` DATETIME DEFAULT NULL COMMENT '锁定截止时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_id_card` (`id_card`),
  KEY `idx_phone` (`phone`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 考试表 (exam)
-- 说明: 存储考试基本信息
-- ============================================================
DROP TABLE IF EXISTS `exam`;

CREATE TABLE `exam` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_name` VARCHAR(200) NOT NULL COMMENT '考试名称',
  `exam_type` VARCHAR(50) NOT NULL COMMENT '考试类型(职业资格/学业水平等)',
  `exam_date` DATE NOT NULL COMMENT '考试日期',
  `exam_time` VARCHAR(50) NOT NULL COMMENT '考试时间段(如:9:00-11:00)',
  `registration_start` DATETIME NOT NULL COMMENT '报名开始时间',
  `registration_end` DATETIME NOT NULL COMMENT '报名结束时间',
  `fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '报名费用(元)',
  `description` TEXT COMMENT '考试简介',
  `notice` TEXT COMMENT '报名须知',
  `file_url` VARCHAR(255) DEFAULT NULL COMMENT '考试简章文件URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1-草稿 2-已发布 3-报名中 4-报名结束 5-已结束)',
  `total_quota` INT DEFAULT NULL COMMENT '总报名名额(NULL表示不限)',
  `current_count` INT NOT NULL DEFAULT 0 COMMENT '当前报名人数',
  `create_by` BIGINT NOT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_exam_type` (`exam_type`),
  KEY `idx_exam_date` (`exam_date`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试表';

-- ============================================================
-- 3. 考点表 (exam_site)
-- 说明: 存储考点信息
-- ============================================================
DROP TABLE IF EXISTS `exam_site`;

CREATE TABLE `exam_site` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_id` BIGINT NOT NULL COMMENT '考试ID',
  `site_name` VARCHAR(200) NOT NULL COMMENT '考点名称',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区/县',
  `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `capacity` INT NOT NULL DEFAULT 100 COMMENT '容纳人数',
  `current_count` INT NOT NULL DEFAULT 0 COMMENT '当前报名人数',
  `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '经度(用于地图展示)',
  `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度(用于地图展示)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1-启用 2-禁用)',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_exam_id` (`exam_id`),
  KEY `idx_province_city` (`province`, `city`),
  CONSTRAINT `fk_exam_id` FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考点表';

-- ============================================================
-- 4. 报名表 (registration)
-- 说明: 存储考生报名记录
-- ============================================================
DROP TABLE IF EXISTS `registration`;

CREATE TABLE `registration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_id` BIGINT NOT NULL COMMENT '考试ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `exam_site_id` BIGINT DEFAULT NULL COMMENT '考点ID',
  `admission_ticket_no` VARCHAR(50) DEFAULT NULL COMMENT '准考证号',
  `exam_room` VARCHAR(20) DEFAULT NULL COMMENT '考场号',
  `seat_no` VARCHAR(10) DEFAULT NULL COMMENT '座位号',
  `subject` VARCHAR(100) DEFAULT NULL COMMENT '报考科目',
  `id_card` VARCHAR(255) DEFAULT NULL COMMENT '身份证号(AES加密)',
  `phone` VARCHAR(255) DEFAULT NULL COMMENT '手机号(AES加密)',
  `materials` TEXT COMMENT '上传材料(JSON格式存储URL数组)',
  `audit_status` TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态(1-待审核 2-审核通过 3-审核驳回)',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注/驳回原因',
  `audit_by` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `payment_status` TINYINT NOT NULL DEFAULT 1 COMMENT '缴费状态(1-未缴费 2-已缴费)',
  `payment_time` DATETIME DEFAULT NULL COMMENT '缴费时间',
  `ticket_download_count` INT NOT NULL DEFAULT 0 COMMENT '准考证下载次数',
  `ticket_download_time` DATETIME DEFAULT NULL COMMENT '首次下载时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticket_no` (`admission_ticket_no`),
  UNIQUE KEY `uk_user_exam` (`user_id`, `exam_id`),
  KEY `idx_exam_id` (`exam_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_exam_audit` (`exam_id`, `audit_status`),
  KEY `idx_create_time` (`create_time` DESC),
  CONSTRAINT `fk_reg_exam_id` FOREIGN KEY (`exam_id`) REFERENCES `exam` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reg_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_reg_site_id` FOREIGN KEY (`exam_site_id`) REFERENCES `exam_site` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报名表';

-- ============================================================
-- 5. 缴费订单表 (payment_order)
-- 说明: 存储缴费订单信息
-- ============================================================
DROP TABLE IF EXISTS `payment_order`;

CREATE TABLE `payment_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `registration_id` BIGINT NOT NULL COMMENT '报名ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `exam_id` BIGINT NOT NULL COMMENT '考试ID(冗余字段,便于统计)',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额(元)',
  `payment_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式(alipay/wechat/union)',
  `transaction_id` VARCHAR(128) DEFAULT NULL COMMENT '第三方交易流水号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态(1-待支付 2-已支付 3-已关闭 4-已退款)',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `expire_time` DATETIME NOT NULL COMMENT '订单过期时间(创建后30分钟)',
  `callback_data` TEXT COMMENT '支付回调数据(JSON)',
  `refund_reason` VARCHAR(500) DEFAULT NULL COMMENT '退款原因',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_transaction_id` (`transaction_id`),
  KEY `idx_registration_id` (`registration_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_exam_id` (`exam_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time` DESC),
  CONSTRAINT `fk_pay_reg_id` FOREIGN KEY (`registration_id`) REFERENCES `registration` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pay_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='缴费订单表';

-- ============================================================
-- 6. 公告表 (notice)
-- 说明: 存储系统公告信息
-- ============================================================
DROP TABLE IF EXISTS `notice`;

CREATE TABLE `notice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容(富文本)',
  `type` VARCHAR(20) NOT NULL DEFAULT 'notice' COMMENT '公告类型(notice-通知/policy-政策/exam-考试安排)',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶(0-否 1-是)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1-已发布 2-已下架)',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `create_by` BIGINT NOT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_is_top_status` (`is_top`, `status`),
  KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 1. 插入默认管理员账号
-- 用户名: admin
-- 密码: admin123 (BCrypt加密后需要替换为实际加密值)
INSERT INTO `sys_user` (
  `username`,
  `password`,
  `real_name`,
  `role`,
  `status`,
  `create_time`
) VALUES (
  'admin',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', -- admin123
  '系统管理员',
  'admin',
  1,
  NOW()
);

-- 2. 插入测试考生账号
-- 用户名: 13800138000
-- 密码: 123456
INSERT INTO `sys_user` (
  `username`,
  `password`,
  `real_name`,
  `phone`,
  `role`,
  `status`,
  `create_time`
) VALUES (
  '13800138000',
  '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr/w0wTRaI5p2S5K.', -- 123456
  '张三',
  '13800138000',
  'user',
  1,
  NOW()
);

-- 3. 插入示例考试数据
INSERT INTO `exam` (
  `exam_name`,
  `exam_type`,
  `exam_date`,
  `exam_time`,
  `registration_start`,
  `registration_end`,
  `fee`,
  `description`,
  `notice`,
  `status`,
  `create_by`,
  `create_time`
) VALUES (
  '2025年春季全国计算机等级考试',
  '职业资格考试',
  '2025-03-15',
  '09:00-11:00',
  '2025-01-01 00:00:00',
  '2025-02-28 23:59:59',
  80.00,
  '全国计算机等级考试（National Computer Rank Examination，简称NCRE），是经原国家教育委员会（现教育部）批准，由教育部考试中心主办，面向社会，用于考查应试人员计算机应用知识与技能的全国性计算机水平考试体系。',
  '1. 考生须携带本人有效身份证件参加考试；\n2. 考试开始30分钟后禁止入场；\n3. 考试过程中不得携带手机等通讯工具；\n4. 请提前熟悉考点位置，合理安排出行时间。',
  2,
  1,
  NOW()
);

-- 4. 插入示例考点数据
INSERT INTO `exam_site` (
  `exam_id`,
  `site_name`,
  `province`,
  `city`,
  `address`,
  `capacity`,
  `status`,
  `create_time`
) VALUES
(1, '北京市教育考试院', '北京市', '海淀区', '海淀区志新东路9号', 500, 1, NOW()),
(1, '上海市教育考试院', '上海市', '黄浦区', '黄浦区钦州南路500号', 600, 1, NOW()),
(1, '广州市招生考试委员会办公室', '广东省', '广州市', '越秀区建设六马路16号', 400, 1, NOW());

-- 5. 插入示例公告
INSERT INTO `notice` (
  `title`,
  `content`,
  `type`,
  `is_top`,
  `status`,
  `publish_time`,
  `create_by`,
  `create_time`
) VALUES (
  '关于2025年春季考试报名的通知',
  '<p>各位考生：</p><p>2025年春季全国计算机等级考试报名工作即日起开始，请考生登录系统完成报名。</p><p><strong>报名时间：</strong>2025年1月1日至2月28日</p><p><strong>考试时间：</strong>2025年3月15日</p><p>请考生提前做好准备，祝考试顺利！</p>',
  'notice',
  1,
  1,
  NOW(),
  1,
  NOW()
);

-- ============================================================
-- 7. 创建MyBatis Plus自动填充处理器需要的配置
-- ============================================================

-- 创建自动填充配置表（可选，用于记录自动填充的配置）
CREATE TABLE IF NOT EXISTS `mybatis_plus_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(100) NOT NULL,
  `config_value` TEXT,
  `description` VARCHAR(255),
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MyBatis Plus配置表';

-- ============================================================
-- 8. 创建索引优化
-- ============================================================

-- 为经常查询的字段添加复合索引
ALTER TABLE `registration` ADD INDEX `idx_user_audit_status` (`user_id`, `audit_status`);
ALTER TABLE `registration` ADD INDEX `idx_exam_payment_status` (`exam_id`, `payment_status`);
ALTER TABLE `payment_order` ADD INDEX `idx_user_status` (`user_id`, `status`);
ALTER TABLE `payment_order` ADD INDEX `idx_exam_status` (`exam_id`, `status`);

-- ============================================================
-- 9. 创建视图（用于统计查询）
-- ============================================================

-- 考试报名统计视图
CREATE VIEW `v_exam_registration_stats` AS
SELECT 
    e.id as exam_id,
    e.exam_name,
    e.exam_type,
    e.exam_date,
    e.total_quota,
    e.current_count,
    COUNT(r.id) as total_registrations,
    SUM(CASE WHEN r.audit_status = 1 THEN 1 ELSE 0 END) as pending_audit,
    SUM(CASE WHEN r.audit_status = 2 THEN 1 ELSE 0 END) as approved,
    SUM(CASE WHEN r.audit_status = 3 THEN 1 ELSE 0 END) as rejected,
    SUM(CASE WHEN r.payment_status = 2 THEN 1 ELSE 0 END) as paid
FROM exam e
LEFT JOIN registration r ON e.id = r.exam_id
GROUP BY e.id, e.exam_name, e.exam_type, e.exam_date, e.total_quota, e.current_count;

-- 用户报名统计视图
CREATE VIEW `v_user_registration_stats` AS
SELECT 
    u.id as user_id,
    u.username,
    u.real_name,
    COUNT(r.id) as total_registrations,
    SUM(CASE WHEN r.audit_status = 2 THEN 1 ELSE 0 END) as approved_count,
    SUM(CASE WHEN r.payment_status = 2 THEN 1 ELSE 0 END) as paid_count
FROM sys_user u
LEFT JOIN registration r ON u.id = r.user_id
WHERE u.role = 'user'
GROUP BY u.id, u.username, u.real_name;

-- ============================================================
-- 10. 创建存储过程（用于生成准考证号）
-- ============================================================

DELIMITER $$

CREATE PROCEDURE `GenerateAdmissionTicketNo`(
    IN p_exam_id BIGINT,
    IN p_site_id BIGINT,
    OUT p_ticket_no VARCHAR(50)
)
BEGIN
    DECLARE v_exam_code VARCHAR(4);
    DECLARE v_site_code VARCHAR(4);
    DECLARE v_sequence INT;
    DECLARE v_ticket_no VARCHAR(50);
    
    -- 获取考试代码（取考试ID的后4位，不足4位前面补0）
    SET v_exam_code = LPAD(p_exam_id % 10000, 4, '0');
    
    -- 获取考点代码（取考点ID的后4位，不足4位前面补0）
    SET v_site_code = LPAD(p_site_id % 10000, 4, '0');
    
    -- 获取该考点该考试的下一个序号
    SELECT COALESCE(MAX(CAST(SUBSTRING(admission_ticket_no, 9, 6) AS UNSIGNED)), 0) + 1
    INTO v_sequence
    FROM registration 
    WHERE exam_id = p_exam_id 
    AND exam_site_id = p_site_id 
    AND admission_ticket_no IS NOT NULL;
    
    -- 生成准考证号：考试代码(4位) + 考点代码(4位) + 序号(6位)
    SET v_ticket_no = CONCAT(v_exam_code, v_site_code, LPAD(v_sequence, 6, '0'));
    
    -- 检查准考证号是否已存在，如果存在则递增序号
    WHILE EXISTS(SELECT 1 FROM registration WHERE admission_ticket_no = v_ticket_no) DO
        SET v_sequence = v_sequence + 1;
        SET v_ticket_no = CONCAT(v_exam_code, v_site_code, LPAD(v_sequence, 6, '0'));
    END WHILE;
    
    SET p_ticket_no = v_ticket_no;
END$$

DELIMITER ;

-- ============================================================
-- 11. 创建触发器（用于自动更新统计信息）
-- ============================================================

-- 报名表插入触发器：自动增加考试和考点的报名人数
DELIMITER $$

CREATE TRIGGER `tr_registration_after_insert` 
AFTER INSERT ON `registration`
FOR EACH ROW
BEGIN
    -- 增加考试报名人数
    UPDATE exam SET current_count = current_count + 1 WHERE id = NEW.exam_id;
    
    -- 增加考点报名人数
    IF NEW.exam_site_id IS NOT NULL THEN
        UPDATE exam_site SET current_count = current_count + 1 WHERE id = NEW.exam_site_id;
    END IF;
END$$

-- 报名表删除触发器：自动减少考试和考点的报名人数
CREATE TRIGGER `tr_registration_after_delete` 
AFTER DELETE ON `registration`
FOR EACH ROW
BEGIN
    -- 减少考试报名人数
    UPDATE exam SET current_count = current_count - 1 WHERE id = OLD.exam_id;
    
    -- 减少考点报名人数
    IF OLD.exam_site_id IS NOT NULL THEN
        UPDATE exam_site SET current_count = current_count - 1 WHERE id = OLD.exam_site_id;
    END IF;
END$$

DELIMITER ;

-- ============================================================
-- 12. 插入更多测试数据
-- ============================================================

-- 插入更多测试考生
INSERT INTO `sys_user` (
  `username`, `password`, `real_name`, `phone`, `role`, `status`, `create_time`
) VALUES 
('13800138001', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr/w0wTRaI5p2S5K.', '李四', '13800138001', 'user', 1, NOW()),
('13800138002', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr/w0wTRaI5p2S5K.', '王五', '13800138002', 'user', 1, NOW()),
('13800138003', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr/w0wTRaI5p2S5K.', '赵六', '13800138003', 'user', 1, NOW());

-- 插入更多考试数据
INSERT INTO `exam` (
  `exam_name`, `exam_type`, `exam_date`, `exam_time`, `registration_start`, `registration_end`, 
  `fee`, `description`, `notice`, `status`, `create_by`, `create_time`
) VALUES 
('2025年春季英语等级考试', '学业水平考试', '2025-04-20', '14:00-16:00', '2025-02-01 00:00:00', '2025-03-31 23:59:59', 
 120.00, '全国英语等级考试（PETS）', '请携带身份证和准考证参加考试', 2, 1, NOW()),
('2025年春季会计资格考试', '职业资格考试', '2025-05-15', '09:00-11:30', '2025-03-01 00:00:00', '2025-04-30 23:59:59', 
 150.00, '初级会计专业技术资格考试', '考试时间为2.5小时，请提前30分钟到达考场', 1, 1, NOW());

-- 插入更多考点数据
INSERT INTO `exam_site` (
  `exam_id`, `site_name`, `province`, `city`, `address`, `capacity`, `status`, `create_time`
) VALUES 
(2, '北京外国语大学', '北京市', '海淀区', '海淀区西三环北路2号', 300, 1, NOW()),
(2, '上海外国语大学', '上海市', '虹口区', '虹口区大连西路550号', 350, 1, NOW()),
(3, '中央财经大学', '北京市', '海淀区', '海淀区学院南路39号', 400, 1, NOW()),
(3, '上海财经大学', '上海市', '杨浦区', '杨浦区国定路777号', 450, 1, NOW());

-- 插入更多公告
INSERT INTO `notice` (
  `title`, `content`, `type`, `is_top`, `status`, `publish_time`, `create_by`, `create_time`
) VALUES 
('系统维护通知', '<p>系统将于本周六凌晨2:00-4:00进行维护，期间可能影响正常使用，请提前做好准备。</p>', 'notice', 0, 1, NOW(), 1, NOW()),
('考试政策更新', '<p>根据最新政策要求，部分考试科目和考试时间有所调整，请考生关注最新通知。</p>', 'policy', 0, 1, NOW(), 1, NOW());

-- ============================================================
-- 13. 创建定时任务清理过期数据（可选）
-- ============================================================

-- 创建事件调度器（需要开启事件调度器：SET GLOBAL event_scheduler = ON;）
-- 清理过期的支付订单（超过7天未支付的订单）
/*
CREATE EVENT IF NOT EXISTS `cleanup_expired_orders`
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
  UPDATE payment_order 
  SET status = 3 
  WHERE status = 1 
  AND expire_time < NOW() 
  AND create_time < DATE_SUB(NOW(), INTERVAL 7 DAY);
*/

-- ============================================================
-- 数据库初始化完成
-- ============================================================

-- 查看所有表
SHOW TABLES;

-- 查看表结构示例
-- DESC sys_user;
-- DESC exam;
-- DESC registration;

-- 查看视图
-- SHOW FULL TABLES WHERE Table_type = 'VIEW';

-- 查看存储过程
-- SHOW PROCEDURE STATUS WHERE Db = 'exam_registration_system';

-- 查看触发器
-- SHOW TRIGGERS;

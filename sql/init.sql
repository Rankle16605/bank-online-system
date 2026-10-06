-- ========================================
-- 智能在线银行系统 - 数据库初始化脚本
-- 版本: V1.0.0
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS icbc_online 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE icbc_online;

-- 设置时区
SET time_zone = '+08:00';

-- ========================================
-- 1. 用户表 (tb_user)
-- ========================================
DROP TABLE IF EXISTS tb_user_role;
DROP TABLE IF EXISTS tb_role_permission;
DROP TABLE IF EXISTS tb_loan_repayment;
DROP TABLE IF EXISTS tb_loan;
DROP TABLE IF EXISTS tb_account_flow;
DROP TABLE IF EXISTS tb_bill_detail;
DROP TABLE IF EXISTS tb_bill;
DROP TABLE IF EXISTS tb_transaction;
DROP TABLE IF EXISTS tb_transaction_limit;
DROP TABLE IF EXISTS tb_account;
DROP TABLE IF EXISTS tb_ai_conversation;
DROP TABLE IF EXISTS tb_ai_config;
DROP TABLE IF EXISTS tb_operation_log;
DROP TABLE IF EXISTS tb_permission;
DROP TABLE IF EXISTS tb_role;
DROP TABLE IF EXISTS tb_user;

CREATE TABLE tb_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码哈希',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    id_card VARCHAR(18) NOT NULL UNIQUE COMMENT '身份证号',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN-管理员 USER-普通用户',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ========================================
-- 2. 角色表 (tb_role)
-- ========================================
CREATE TABLE tb_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ========================================
-- 3. 权限表 (tb_permission)
-- ========================================
CREATE TABLE tb_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    description VARCHAR(200) COMMENT '权限描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ========================================
-- 4. 用户角色关联表 (tb_user_role)
-- ========================================
CREATE TABLE tb_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES tb_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ========================================
-- 5. 角色权限关联表 (tb_role_permission)
-- ========================================
CREATE TABLE tb_role_permission (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES tb_role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES tb_permission(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ========================================
-- 6. 账户表 (tb_account)
-- ========================================
CREATE TABLE tb_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账户ID',
    account_no VARCHAR(30) NOT NULL UNIQUE COMMENT '账户号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    account_type TINYINT NOT NULL COMMENT '账户类型：1-储蓄，2-信用',
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
    frozen_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
    credit_limit DECIMAL(15,2) DEFAULT 0.00 COMMENT '信用额度',
    available_credit DECIMAL(15,2) DEFAULT 0.00 COMMENT '可用额度',
    billing_day TINYINT DEFAULT 1 COMMENT '账单日',
    due_day TINYINT DEFAULT 15 COMMENT '还款日',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-冻结，1-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE RESTRICT,
    INDEX idx_account_no (account_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户表';

-- ========================================
-- 7. 账户流水表 (tb_account_flow)
-- ========================================
CREATE TABLE tb_account_flow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流水ID',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    flow_type TINYINT NOT NULL COMMENT '流水类型：1-存入，2-支出',
    amount DECIMAL(15,2) NOT NULL COMMENT '金额',
    balance_before DECIMAL(15,2) NOT NULL COMMENT '交易前余额',
    balance_after DECIMAL(15,2) NOT NULL COMMENT '交易后余额',
    transaction_id BIGINT COMMENT '关联交易ID',
    remark VARCHAR(200) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (account_id) REFERENCES tb_account(id) ON DELETE CASCADE,
    INDEX idx_account_id (account_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户流水表';

-- ========================================
-- 8. 交易记录表 (tb_transaction)
-- ========================================
CREATE TABLE tb_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交易ID',
    transaction_no VARCHAR(50) NOT NULL UNIQUE COMMENT '交易流水号',
    from_account VARCHAR(30) NOT NULL COMMENT '转出账户',
    to_account VARCHAR(30) NOT NULL COMMENT '转入账户',
    amount DECIMAL(15,2) NOT NULL COMMENT '交易金额',
    transaction_type TINYINT NOT NULL COMMENT '交易类型：1-行内转账，2-跨行转账，3-充值，4-提现',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-失败，1-成功，2-处理中',
    remark VARCHAR(200) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    
    INDEX idx_transaction_no (transaction_no),
    INDEX idx_from_account (from_account),
    INDEX idx_to_account (to_account),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';

-- ========================================
-- 9. 交易限额配置表 (tb_transaction_limit)
-- ========================================
CREATE TABLE tb_transaction_limit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    limit_type TINYINT NOT NULL COMMENT '限额类型：1-单笔，2-单日',
    limit_amount DECIMAL(15,2) NOT NULL COMMENT '限额金额',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_limit (user_id, limit_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易限额配置表';

-- ========================================
-- 10. 账单表 (tb_bill)
-- ========================================
CREATE TABLE tb_bill (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账单ID',
    bill_no VARCHAR(50) NOT NULL UNIQUE COMMENT '账单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    bill_month CHAR(7) NOT NULL COMMENT '账单月份，格式：YYYY-MM',
    total_income DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '总收入',
    total_expense DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '总支出',
    begin_balance DECIMAL(15,2) NOT NULL COMMENT '期初余额',
    end_balance DECIMAL(15,2) NOT NULL COMMENT '期末余额',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未出账，1-已出账，2-已发送',
    generated_at DATETIME COMMENT '生成时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES tb_account(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_bill_month (bill_month),
    INDEX idx_status (status),
    UNIQUE KEY uk_user_month (user_id, account_id, bill_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单表';

-- ========================================
-- 11. 账单明细表 (tb_bill_detail)
-- ========================================
CREATE TABLE tb_bill_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    bill_id BIGINT NOT NULL COMMENT '账单ID',
    transaction_id BIGINT NOT NULL COMMENT '交易ID',
    transaction_no VARCHAR(50) NOT NULL COMMENT '交易流水号',
    transaction_time DATETIME NOT NULL COMMENT '交易时间',
    amount DECIMAL(15,2) NOT NULL COMMENT '交易金额',
    balance DECIMAL(15,2) NOT NULL COMMENT '交易后余额',
    remark VARCHAR(200) COMMENT '备注',
    
    FOREIGN KEY (bill_id) REFERENCES tb_bill(id) ON DELETE CASCADE,
    FOREIGN KEY (transaction_id) REFERENCES tb_transaction(id) ON DELETE CASCADE,
    INDEX idx_bill_id (bill_id),
    INDEX idx_transaction_time (transaction_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单明细表';

-- ========================================
-- 12. AI对话记录表 (tb_ai_conversation)
-- ========================================
CREATE TABLE tb_ai_conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '对话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(50) NOT NULL COMMENT '会话ID',
    message_type TINYINT NOT NULL COMMENT '消息类型：1-用户消息，2-AI回复',
    content TEXT NOT NULL COMMENT '消息内容',
    context TEXT COMMENT '上下文信息',
    response_time INT COMMENT '响应时间（毫秒）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话记录表';

-- ========================================
-- 13. AI客服配置表 (tb_ai_config)
-- ========================================
CREATE TABLE tb_ai_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    description VARCHAR(200) COMMENT '描述',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI客服配置表';

-- ========================================
-- 14. 操作日志表 (tb_operation_log)
-- ========================================
CREATE TABLE tb_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    status TINYINT NOT NULL COMMENT '状态：0-失败，1-成功',
    error_message TEXT COMMENT '错误信息',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_operation_time (operation_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ========================================
-- 插入默认角色数据
-- ========================================
INSERT INTO tb_role (role_name, role_code, description) VALUES
('管理员', 'ADMIN', '系统管理员角色，拥有所有权限'),
('普通用户', 'USER', '普通用户角色，拥有基本操作权限'),
('VIP用户', 'VIP_USER', 'VIP用户角色，享受更高的服务限额'),
('客服人员', 'CUSTOMER_SERVICE', '客服人员角色，管理AI客服');

-- ========================================
-- 插入默认权限数据
-- ========================================
INSERT INTO tb_permission (permission_name, permission_code, description) VALUES
('用户管理', 'USER_MANAGE', '用户管理权限：增删改查用户'),
('账户查询', 'ACCOUNT_VIEW', '账户查询权限：查看账户信息'),
('转账权限', 'TRANSFER', '转账权限：发起转账交易'),
('账单查询', 'BILL_VIEW', '账单查询权限：查看账单'),
('AI客服', 'AI_CHAT', 'AI客服权限：使用智能客服'),
('系统管理', 'SYSTEM_MANAGE', '系统管理权限：系统配置与管理');

-- ========================================
-- 插入AI配置默认数据
-- ========================================
INSERT INTO tb_ai_config (config_key, config_value, description) VALUES
('model_name', 'doubao-seed-2.0-code', '使用的AI模型名称'),
('max_context_length', '10', '最大上下文长度（消息数）'),
('session_expire_time', '1800', '会话过期时间（秒）'),
('enable_sensitive_filter', 'true', '是否启用敏感词过滤'),
('welcome_message', '您好！我是ICBC智能客服助手，请问有什么可以帮您的？', '欢迎消息'),
('timeout', '30', 'AI请求超时时间（秒）');

-- ========================================
-- 15. 借款表 (tb_loan)
-- ========================================
CREATE TABLE tb_loan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '借款ID',
    loan_no VARCHAR(50) NOT NULL UNIQUE COMMENT '借款编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    account_id BIGINT NOT NULL COMMENT '信用账户ID',
    loan_amount DECIMAL(15,2) NOT NULL COMMENT '申请金额',
    approved_amount DECIMAL(15,2) NOT NULL COMMENT '批准金额',
    interest_rate DECIMAL(5,3) NOT NULL COMMENT '年利率(%)',
    loan_term INT NOT NULL COMMENT '借款期限(月)',
    monthly_payment DECIMAL(15,2) NOT NULL COMMENT '月还款额',
    total_repay DECIMAL(15,2) NOT NULL COMMENT '总还款额',
    repaid_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '已还金额',
    remaining_periods INT DEFAULT 0 COMMENT '剩余期数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-拒绝 1-审核中 2-已批准 3-还款中 4-已结清 5-逾期',
    apply_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    approve_date DATETIME COMMENT '批准时间',
    start_date DATETIME COMMENT '起息日',
    due_date DATETIME COMMENT '到期日',
    remark VARCHAR(200) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES tb_user(id) ON DELETE RESTRICT,
    FOREIGN KEY (account_id) REFERENCES tb_account(id) ON DELETE RESTRICT,
    INDEX idx_user_id (user_id),
    INDEX idx_account_id (account_id),
    INDEX idx_status (status),
    INDEX idx_loan_no (loan_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借款表';

-- ========================================
-- 16. 还款记录表 (tb_loan_repayment)
-- ========================================
CREATE TABLE tb_loan_repayment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '还款ID',
    repayment_no VARCHAR(50) NOT NULL UNIQUE COMMENT '还款编号',
    loan_id BIGINT NOT NULL COMMENT '借款ID',
    period_no INT NOT NULL COMMENT '第几期',
    amount DECIMAL(15,2) NOT NULL COMMENT '本期应还金额',
    principal DECIMAL(15,2) NOT NULL COMMENT '本金',
    interest DECIMAL(15,2) NOT NULL COMMENT '利息',
    actual_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '实际还款金额',
    scheduled_date DATETIME NOT NULL COMMENT '计划还款日',
    actual_date DATETIME COMMENT '实际还款日',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-待还 1-已还 2-逾期 3-减免',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    FOREIGN KEY (loan_id) REFERENCES tb_loan(id) ON DELETE CASCADE,
    INDEX idx_loan_id (loan_id),
    INDEX idx_scheduled_date (scheduled_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='还款记录表';

-- ========================================
-- 创建视图：用户账户视图
-- ========================================
CREATE VIEW v_user_account AS
SELECT 
    u.id AS user_id,
    u.username,
    u.real_name,
    u.phone,
    u.email,
    u.status AS user_status,
    a.id AS account_id,
    a.account_no,
    a.account_type,
    a.balance,
    a.frozen_amount,
    (a.balance - a.frozen_amount) AS available_balance,
    a.status AS account_status,
    a.created_at AS account_created_at
FROM tb_user u
LEFT JOIN tb_account a ON u.id = a.user_id;

-- ========================================
-- 创建视图：交易统计视图
-- ========================================
CREATE VIEW v_transaction_stats AS
SELECT 
    DATE(created_at) AS transaction_date,
    transaction_type,
    COUNT(*) AS total_count,
    SUM(amount) AS total_amount,
    AVG(amount) AS avg_amount,
    MAX(amount) AS max_amount,
    MIN(amount) AS min_amount,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS success_count,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS fail_count
FROM tb_transaction
GROUP BY DATE(created_at), transaction_type;

-- ========================================
-- 创建存储过程：生成月度账单
-- ========================================
DELIMITER //

CREATE PROCEDURE sp_generate_monthly_bill(
    IN p_user_id BIGINT,
    IN p_account_id BIGINT,
    IN p_bill_month CHAR(7)
)
BEGIN
    DECLARE v_bill_id BIGINT;
    DECLARE v_begin_balance DECIMAL(15,2);
    DECLARE v_end_balance DECIMAL(15,2);
    DECLARE v_total_income DECIMAL(15,2) DEFAULT 0.00;
    DECLARE v_total_expense DECIMAL(15,2) DEFAULT 0.00;
    DECLARE v_start_date DATE;
    DECLARE v_end_date DATE;
    DECLARE v_bill_no VARCHAR(50);
    
    -- 计算月份的起止日期
    SET v_start_date = CONCAT(p_bill_month, '-01');
    SET v_end_date = LAST_DAY(v_start_date);
    
    -- 获取当前余额作为期末余额
    SELECT balance INTO v_end_balance 
    FROM tb_account 
    WHERE id = p_account_id;
    
    -- 计算期初余额（期末余额 - 当月净收入）
    -- 当月收入 - 当月支出
    SELECT 
        COALESCE(SUM(CASE WHEN af.flow_type = 1 THEN af.amount ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN af.flow_type = 2 THEN af.amount ELSE 0 END), 0)
    INTO v_total_income, v_total_expense
    FROM tb_account_flow af
    WHERE af.account_id = p_account_id
      AND af.created_at >= v_start_date
      AND af.created_at < DATE_ADD(v_end_date, INTERVAL 1 DAY);
    
    -- 期初余额 = 期末余额 - 收入 + 支出
    SET v_begin_balance = v_end_balance - v_total_income + v_total_expense;
    
    -- 生成账单编号
    SET v_bill_no = CONCAT('BILL-', p_user_id, '-', REPLACE(p_bill_month, '-', ''), '-', p_account_id);
    
    -- 检查是否已存在
    IF EXISTS (SELECT 1 FROM tb_bill WHERE bill_no = v_bill_no) THEN
        SELECT v_bill_no AS bill_no, 'EXISTED' AS result;
    ELSE
        -- 插入账单记录
        INSERT INTO tb_bill (
            bill_no,
            user_id,
            account_id,
            bill_month,
            total_income,
            total_expense,
            begin_balance,
            end_balance,
            status,
            generated_at
        ) VALUES (
            v_bill_no,
            p_user_id,
            p_account_id,
            p_bill_month,
            v_total_income,
            v_total_expense,
            v_begin_balance,
            v_end_balance,
            1,
            NOW()
        );
        
        SET v_bill_id = LAST_INSERT_ID();
        
        -- 插入账单明细
        INSERT INTO tb_bill_detail (
            bill_id,
            transaction_id,
            transaction_no,
            transaction_time,
            amount,
            balance,
            remark
        )
        SELECT 
            v_bill_id,
            t.id,
            t.transaction_no,
            t.created_at,
            t.amount,
            af.balance_after,
            t.remark
        FROM tb_transaction t
        JOIN tb_account_flow af ON af.transaction_id = t.id
        WHERE af.account_id = p_account_id
          AND t.created_at >= v_start_date
          AND t.created_at < DATE_ADD(v_end_date, INTERVAL 1 DAY);
        
        SELECT v_bill_no AS bill_no, 'CREATED' AS result;
    END IF;
    
END //

DELIMITER ;

-- ========================================
-- 创建存储过程：转账操作
-- ========================================
DELIMITER //

CREATE PROCEDURE sp_transfer(
    IN p_from_account_no VARCHAR(30),
    IN p_to_account_no VARCHAR(30),
    IN p_amount DECIMAL(15,2),
    IN p_transaction_type TINYINT,
    IN p_remark VARCHAR(200),
    OUT p_result_code INT,
    OUT p_result_message VARCHAR(200),
    OUT p_transaction_no VARCHAR(50)
)
BEGIN
    DECLARE v_from_balance DECIMAL(15,2);
    DECLARE v_from_available DECIMAL(15,2);
    DECLARE v_from_status TINYINT;
    DECLARE v_to_status TINYINT;
    DECLARE v_from_id BIGINT;
    DECLARE v_to_id BIGINT;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result_code = 500;
        SET p_result_message = '系统异常，转账失败';
    END;
    
    START TRANSACTION;
    
    -- 生成交易流水号
    SET p_transaction_no = CONCAT('TXN', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), LPAD(FLOOR(RAND() * 10000), 4, '0'));
    
    -- 获取转出账户信息并锁定
    SELECT id, balance, frozen_amount, status 
    INTO v_from_id, v_from_balance, v_from_available, v_from_status
    FROM tb_account WHERE account_no = p_from_account_no FOR UPDATE;
    
    IF v_from_id IS NULL THEN
        ROLLBACK;
        SET p_result_code = 404;
        SET p_result_message = '转出账户不存在';
    ELSEIF v_from_status != 1 THEN
        ROLLBACK;
        SET p_result_code = 403;
        SET p_result_message = '转出账户状态异常';
    ELSEIF (v_from_balance - v_from_available) < p_amount THEN
        ROLLBACK;
        SET p_result_code = 400;
        SET p_result_message = '余额不足';
    ELSE
        -- 获取转入账户信息
        SELECT id, status INTO v_to_id, v_to_status
        FROM tb_account WHERE account_no = p_to_account_no FOR UPDATE;
        
        IF v_to_id IS NULL THEN
            ROLLBACK;
            SET p_result_code = 404;
            SET p_result_message = '转入账户不存在';
        ELSEIF v_to_status != 1 THEN
            ROLLBACK;
            SET p_result_code = 403;
            SET p_result_message = '转入账户状态异常';
        ELSE
            -- 执行转账
            UPDATE tb_account SET balance = balance - p_amount, updated_at = NOW() WHERE id = v_from_id;
            UPDATE tb_account SET balance = balance + p_amount, updated_at = NOW() WHERE id = v_to_id;
            
            -- 插入交易记录
            INSERT INTO tb_transaction (transaction_no, from_account, to_account, amount, transaction_type, status, remark)
            VALUES (p_transaction_no, p_from_account_no, p_to_account_no, p_amount, p_transaction_type, 1, p_remark);
            
            -- 插入账户流水
            INSERT INTO tb_account_flow (account_id, flow_type, amount, balance_before, balance_after, transaction_id, remark)
            VALUES 
            (v_from_id, 2, p_amount, v_from_balance, v_from_balance - p_amount, LAST_INSERT_ID(), CONCAT('转出至', p_to_account_no)),
            (v_to_id, 1, p_amount, (SELECT balance FROM tb_account WHERE id = v_to_id) - p_amount, (SELECT balance FROM tb_account WHERE id = v_to_id), LAST_INSERT_ID(), CONCAT('来自', p_from_account_no));
            
            COMMIT;
            SET p_result_code = 200;
            SET p_result_message = '转账成功';
        END IF;
    END IF;
END //

DELIMITER ;

-- ========================================
-- 创建触发器：交易后自动更新账户流水
-- ========================================
DELIMITER //

CREATE TRIGGER trg_transaction_after_insert
AFTER INSERT ON tb_transaction
FOR EACH ROW
BEGIN
    DECLARE v_from_balance DECIMAL(15,2);
    DECLARE v_to_balance DECIMAL(15,2);
    DECLARE v_from_id BIGINT;
    DECLARE v_to_id BIGINT;
    
    IF NEW.status = 1 THEN
        -- 获取账户ID和余额
        SELECT id, balance INTO v_from_id, v_from_balance FROM tb_account WHERE account_no = NEW.from_account;
        SELECT id, balance INTO v_to_id, v_to_balance FROM tb_account WHERE account_no = NEW.to_account;
        
        -- 插入转出流水
        IF v_from_id IS NOT NULL THEN
            INSERT INTO tb_account_flow (account_id, flow_type, amount, balance_before, balance_after, transaction_id, remark)
            VALUES (v_from_id, 2, NEW.amount, v_from_balance + NEW.amount, v_from_balance, NEW.id, CONCAT('转账至', NEW.to_account));
        END IF;
        
        -- 插入转入流水
        IF v_to_id IS NOT NULL THEN
            INSERT INTO tb_account_flow (account_id, flow_type, amount, balance_before, balance_after, transaction_id, remark)
            VALUES (v_to_id, 1, NEW.amount, v_to_balance - NEW.amount, v_to_balance, NEW.id, CONCAT('来自', NEW.from_account));
        END IF;
    END IF;
END //

DELIMITER ;

-- ========================================
-- 创建索引优化查询性能
-- ========================================
CREATE INDEX idx_transaction_created_at_type ON tb_transaction(created_at, transaction_type);
CREATE INDEX idx_account_flow_created_at ON tb_account_flow(created_at);
CREATE INDEX idx_ai_conversation_session ON tb_ai_conversation(session_id, created_at);
CREATE INDEX idx_bill_user_month ON tb_bill(user_id, bill_month);

-- ========================================
-- 测试数据填充 - 用户 (10条)
-- 密码均为 Test@123456 的BCrypt哈希
-- ========================================
INSERT INTO tb_user (username, password, real_name, id_card, phone, email, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '110101199001011111', '13800000001', 'admin@icbc.com', 'ADMIN', 1),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', '110101199001011112', '13800000002', 'zhangsan@qq.com', 'USER', 1),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', '110101199001011113', '13800000003', 'lisi@qq.com', 'USER', 1),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王五', '110101199001011114', '13800000004', 'wangwu@qq.com', 'USER', 1),
('zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵六', '110101199001011115', '13800000005', 'zhaoliu@qq.com', 'USER', 1),
('sunqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '孙七', '110101199001011116', '13800000006', 'sunqi@qq.com', 'USER', 1),
('zhouba', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '周八', '110101199001011117', '13800000007', 'zhouba@qq.com', 'USER', 1),
('wujiu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '吴九', '110101199001011118', '13800000008', 'wujiu@qq.com', 'USER', 1),
('zhengshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '郑十', '110101199001011119', '13800000009', 'zhengshi@qq.com', 'USER', 1),
('vipuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'VIP客户', '110101199001011120', '13800000010', 'vip@icbc.com', 'USER', 1),
('chenwei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陈伟', '110101199001011121', '13800000011', 'chenwei@qq.com', 'USER', 1),
('liuqiang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '刘强', '110101199001011122', '13800000012', 'liuqiang@qq.com', 'USER', 1),
('huangli', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '黄丽', '110101199001011123', '13800000013', 'huangli@qq.com', 'USER', 1),
('xuyang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '徐洋', '110101199001011124', '13800000014', 'xuyang@qq.com', 'USER', 1),
('linjie', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '林杰', '110101199001011125', '13800000015', 'linjie@qq.com', 'USER', 1),
('mayun', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '马芸', '110101199001011126', '13800000016', 'mayun@qq.com', 'USER', 1),
('huxue', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '胡雪', '110101199001011127', '13800000017', 'huxue@qq.com', 'USER', 1),
('guowei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '郭伟', '110101199001011128', '13800000018', 'guowei@qq.com', 'USER', 1),
('dengchao', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '邓超', '110101199001011129', '13800000019', 'dengchao@qq.com', 'USER', 1),
('penglei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '彭磊', '110101199001011130', '13800000020', 'penglei@qq.com', 'USER', 1),
('jiangtao', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '姜涛', '110101199001011131', '13800000021', 'jiangtao@qq.com', 'USER', 1),
('shenmin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '沈敏', '110101199001011132', '13800000022', 'shenmin@qq.com', 'USER', 1),
('hanglei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '韩磊', '110101199001011133', '13800000023', 'hanglei@qq.com', 'USER', 1),
('fangyuan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '方媛', '110101199001011134', '13800000024', 'fangyuan@qq.com', 'USER', 1),
('xielei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '谢磊', '110101199001011135', '13800000025', 'xielei@qq.com', 'USER', 1),
('tangjie', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '唐洁', '110101199001011136', '13800000026', 'tangjie@qq.com', 'USER', 1),
('shilei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '石磊', '110101199001011137', '13800000027', 'shilei@qq.com', 'USER', 1),
('luyang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陆洋', '110101199001011138', '13800000028', 'luyang@qq.com', 'USER', 1),
('songping', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '宋萍', '110101199001011139', '13800000029', 'songping@qq.com', 'USER', 1),
('yanglei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '杨磊', '110101199001011140', '13800000030', 'yanglei@qq.com', 'USER', 1),
('hejun', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '何军', '110101199001011141', '13800000031', 'hejun@qq.com', 'USER', 1),
('caoyang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '曹阳', '110101199001011142', '13800000032', 'caoyang@qq.com', 'USER', 1),
('xiaoming', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '肖明', '110101199001011143', '13800000033', 'xiaoming@qq.com', 'USER', 1),
('zhongwei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '钟伟', '110101199001011144', '13800000034', 'zhongwei@qq.com', 'USER', 1),
('taoran', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '陶然', '110101199001011145', '13800000035', 'taoran@qq.com', 'USER', 1),
('yuhang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '余航', '110101199001011146', '13800000036', 'yuhang@qq.com', 'USER', 1),
('luoxi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '罗希', '110101199001011147', '13800000037', 'luoxi@qq.com', 'USER', 1),
('dongyu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '董宇', '110101199001011148', '13800000038', 'dongyu@qq.com', 'USER', 1),
('baimei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '白梅', '110101199001011149', '13800000039', 'baimei@qq.com', 'USER', 1),
('tianyu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '田宇', '110101199001011150', '13800000040', 'tianyu@qq.com', 'USER', 1),
('qianxue', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '钱雪', '110101199001011151', '13800000041', 'qianxue@qq.com', 'USER', 1),
('leijun', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '雷军', '110101199001011152', '13800000042', 'leijun@qq.com', 'USER', 1),
('duanpeng', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '段鹏', '110101199001011153', '13800000043', 'duanpeng@qq.com', 'USER', 1),
('jiaqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '贾琪', '110101199001011154', '13800000044', 'jiaqi@qq.com', 'USER', 1),
('xuefeng', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '薛峰', '110101199001011155', '13800000045', 'xuefeng@qq.com', 'USER', 1),
('dingxin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '丁鑫', '110101199001011156', '13800000046', 'dingxin@icbc.com', 'USER', 0),
('zhuyun', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '朱云', '110101199001011157', '13800000047', 'zhuyun@qq.com', 'USER', 1),
('celiang', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '册亮', '110101199001011158', '13800000048', 'celiang@qq.com', 'USER', 1),
('zouxuan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '邹璇', '110101199001011159', '13800000049', 'zouxuan@qq.com', 'USER', 1),
('kangwei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '康伟', '110101199001011160', '13800000050', 'kangwei@qq.com', 'USER', 1);

-- 角色-权限关联
INSERT INTO tb_role_permission (role_id, permission_id) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),
(2,2),(2,3),(2,4),(2,5),
(3,2),(3,3),(3,4),(3,5),
(4,5),(4,6);

-- 用户-角色关联
INSERT INTO tb_user_role (user_id, role_id) VALUES
(1,1),(2,2),(3,2),(4,2),(5,2),(6,2),(7,2),(8,2),(9,2),(10,3),
(11,2),(12,2),(13,2),(14,2),(15,2),(16,2),(17,2),(18,2),(19,2),(20,2),
(21,2),(22,2),(23,2),(24,2),(25,2),(26,2),(27,2),(28,2),(29,2),(30,2),
(31,2),(32,2),(33,2),(34,2),(35,2),(36,2),(37,2),(38,2),(39,2),(40,3),
(41,2),(42,2),(43,2),(44,2),(45,2),(46,2),(47,2),(48,2),(49,2),(50,2);

-- 账户数据 (80条)
INSERT INTO tb_account (account_no, user_id, account_type, balance, frozen_amount, credit_limit, available_credit, billing_day, due_day, status) VALUES
-- 原有用户账户
('6222021001000001', 1, 1, 500000.00, 0.00, 0.00, 0.00, 1, 15, 1),
('6222021001000002', 2, 1, 150000.00, 0.00, 0.00, 0.00, 5, 20, 1),
('6222021001000003', 2, 2, 0.00, 0.00, 50000.00, 50000.00, 5, 25, 1),
('6222021001000004', 3, 1, 80000.00, 0.00, 0.00, 0.00, 10, 25, 1),
('6222021001000005', 3, 2, 0.00, 0.00, 100000.00, 100000.00, 10, 28, 1),
('6222021001000006', 4, 1, 200000.00, 0.00, 0.00, 0.00, 15, 30, 1),
('6222021001000007', 5, 1, 95000.00, 0.00, 0.00, 0.00, 8, 23, 1),
('6222021001000008', 5, 2, 0.00, 0.00, 80000.00, 80000.00, 8, 25, 1),
('6222021001000009', 6, 1, 300000.00, 0.00, 0.00, 0.00, 3, 18, 1),
('6222021001000010', 7, 1, 120000.00, 0.00, 0.00, 0.00, 12, 27, 1),
('6222021001000011', 7, 2, 0.00, 0.00, 60000.00, 60000.00, 12, 28, 1),
('6222021001000012', 8, 1, 88000.00, 0.00, 0.00, 0.00, 20, 5, 1),
('6222021001000013', 9, 1, 450000.00, 0.00, 0.00, 0.00, 1, 15, 1),
('6222021001000014', 10, 1, 1000000.00, 0.00, 0.00, 0.00, 1, 15, 1),
('6222021001000015', 10, 2, 0.00, 0.00, 200000.00, 200000.00, 1, 20, 1),
-- 新增用户账户(储蓄卡)
('6222021001000016', 11, 1, 68000.00, 0.00, 0.00, 0.00, 3, 18, 1),
('6222021001000017', 12, 1, 125000.00, 0.00, 0.00, 0.00, 7, 22, 1),
('6222021001000018', 13, 1, 92000.00, 0.00, 0.00, 0.00, 10, 25, 1),
('6222021001000019', 14, 1, 158000.00, 0.00, 0.00, 0.00, 5, 20, 1),
('6222021001000020', 15, 1, 210000.00, 0.00, 0.00, 0.00, 12, 27, 1),
('6222021001000021', 16, 1, 345000.00, 0.00, 0.00, 0.00, 1, 15, 1),
('6222021001000022', 17, 1, 76000.00, 0.00, 0.00, 0.00, 8, 23, 1),
('6222021001000023', 18, 1, 198000.00, 0.00, 0.00, 0.00, 15, 30, 1),
('6222021001000024', 19, 1, 85000.00, 0.00, 0.00, 0.00, 6, 21, 1),
('6222021001000025', 20, 1, 113000.00, 0.00, 0.00, 0.00, 3, 18, 1),
('6222021001000026', 21, 1, 275000.00, 0.00, 0.00, 0.00, 10, 25, 1),
('6222021001000027', 22, 1, 63000.00, 0.00, 0.00, 0.00, 5, 20, 1),
('6222021001000028', 23, 1, 182000.00, 0.00, 0.00, 0.00, 1, 15, 1),
('6222021001000029', 24, 1, 96000.00, 0.00, 0.00, 0.00, 12, 27, 1),
('6222021001000030', 25, 1, 143000.00, 0.00, 0.00, 0.00, 8, 23, 1),
('6222021001000031', 26, 1, 88000.00, 0.00, 0.00, 0.00, 15, 30, 1),
('6222021001000032', 27, 1, 221000.00, 0.00, 0.00, 0.00, 3, 18, 1),
('6222021001000033', 28, 1, 105000.00, 0.00, 0.00, 0.00, 7, 22, 1),
('6222021001000034', 29, 1, 68000.00, 0.00, 0.00, 0.00, 10, 25, 1),
('6222021001000035', 30, 1, 167000.00, 0.00, 0.00, 0.00, 5, 20, 1),
('6222021001000036', 31, 1, 290000.00, 0.00, 0.00, 0.00, 12, 27, 1),
('6222021001000037', 32, 1, 134000.00, 0.00, 0.00, 0.00, 1, 15, 1),
('6222021001000038', 33, 1, 77000.00, 0.00, 0.00, 0.00, 8, 23, 1),
('6222021001000039', 34, 1, 256000.00, 0.00, 0.00, 0.00, 15, 30, 1),
('6222021001000040', 35, 1, 189000.00, 0.00, 0.00, 0.00, 3, 18, 1),
('6222021001000041', 36, 1, 102000.00, 0.00, 0.00, 0.00, 10, 25, 1),
('6222021001000042', 37, 1, 245000.00, 0.00, 0.00, 0.00, 5, 20, 1),
('6222021001000043', 38, 1, 138000.00, 0.00, 0.00, 0.00, 12, 27, 1),
('6222021001000044', 39, 1, 92000.00, 0.00, 0.00, 0.00, 1, 15, 1),
('6222021001000045', 40, 1, 810000.00, 0.00, 0.00, 0.00, 1, 15, 1),
('6222021001000046', 41, 1, 176000.00, 0.00, 0.00, 0.00, 7, 22, 1),
('6222021001000047', 42, 1, 213000.00, 0.00, 0.00, 0.00, 3, 18, 1),
('6222021001000048', 43, 1, 95000.00, 0.00, 0.00, 0.00, 10, 25, 1),
('6222021001000049', 44, 1, 154000.00, 0.00, 0.00, 0.00, 5, 20, 1),
('6222021001000050', 45, 1, 287000.00, 0.00, 0.00, 0.00, 12, 27, 1),
('6222021001000051', 47, 1, 111000.00, 0.00, 0.00, 0.00, 8, 23, 1),
('6222021001000052', 48, 1, 169000.00, 0.00, 0.00, 0.00, 15, 30, 1),
('6222021001000053', 49, 1, 73000.00, 0.00, 0.00, 0.00, 3, 18, 1),
('6222021001000054', 50, 1, 235000.00, 0.00, 0.00, 0.00, 1, 15, 1),
-- 新增信用卡
('6222021001000055', 11, 2, 0.00, 0.00, 80000.00, 65000.00, 3, 20, 1),
('6222021001000056', 14, 2, 0.00, 0.00, 50000.00, 50000.00, 5, 22, 1),
('6222021001000057', 16, 2, 0.00, 0.00, 150000.00, 120000.00, 1, 18, 1),
('6222021001000058', 18, 2, 0.00, 0.00, 60000.00, 60000.00, 15, 28, 1),
('6222021001000059', 21, 2, 0.00, 0.00, 120000.00, 120000.00, 10, 25, 1),
('6222021001000060', 23, 2, 0.00, 0.00, 90000.00, 75000.00, 1, 18, 1),
('6222021001000061', 27, 2, 0.00, 0.00, 80000.00, 80000.00, 3, 20, 1),
('6222021001000062', 31, 2, 0.00, 0.00, 100000.00, 100000.00, 12, 28, 1),
('6222021001000063', 35, 2, 0.00, 0.00, 70000.00, 45000.00, 3, 20, 1),
('6222021001000064', 40, 2, 0.00, 0.00, 300000.00, 220000.00, 1, 15, 1),
('6222021001000065', 42, 2, 0.00, 0.00, 95000.00, 95000.00, 3, 20, 1),
('6222021001000066', 45, 2, 0.00, 0.00, 110000.00, 110000.00, 12, 28, 1),
('6222021001000067', 50, 2, 0.00, 0.00, 180000.00, 160000.00, 1, 18, 1);

-- 账户流水 (200条)
INSERT INTO tb_account_flow (account_id, flow_type, amount, balance_before, balance_after, remark, created_at) VALUES
-- 张三(账户2)流水
(2, 1, 50000.00, 100000.00, 150000.00, '工资入账', '2026-05-15 09:00:00'),
(2, 2, 3000.00, 150000.00, 147000.00, '信用卡还款', '2026-05-20 14:30:00'),
(2, 1, 5000.00, 147000.00, 152000.00, '转账收入', '2026-06-01 10:00:00'),
(2, 2, 1200.00, 152000.00, 150800.00, '超市购物', '2026-06-02 11:30:00'),
(2, 2, 350.00, 150800.00, 150450.00, '餐饮消费', '2026-06-05 12:00:00'),
(2, 2, 880.00, 150450.00, 149570.00, '水电费', '2026-06-05 14:00:00'),
(2, 2, 25000.00, 149570.00, 124570.00, '大额转账', '2026-06-10 09:30:00'),
(2, 1, 8000.00, 124570.00, 132570.00, '转账收入', '2026-06-12 16:00:00'),
(2, 2, 650.00, 132570.00, 131920.00, '电影票', '2026-06-13 19:00:00'),
(2, 1, 50000.00, 131920.00, 181920.00, '工资入账', '2026-06-15 09:00:00'),
(2, 2, 2000.00, 181920.00, 179920.00, '话费充值', '2026-06-15 10:30:00'),
-- 李四(账户4)流水
(4, 1, 30000.00, 50000.00, 80000.00, '兼职收入', '2026-05-10 11:00:00'),
(4, 2, 5000.00, 80000.00, 75000.00, '购物消费', '2026-05-25 16:00:00'),
(4, 1, 5000.00, 75000.00, 80000.00, '转账收入', '2026-06-01 10:00:00'),
(4, 2, 1500.00, 80000.00, 78500.00, '加油费', '2026-06-02 08:00:00'),
(4, 1, 15000.00, 78500.00, 93500.00, '跨行转账收入', '2026-06-05 11:00:00'),
(4, 2, 3800.00, 93500.00, 89700.00, '淘宝购物', '2026-06-08 20:00:00'),
(4, 2, 12000.00, 89700.00, 77700.00, '转账支出', '2026-06-13 10:00:00'),
(4, 1, 30000.00, 77700.00, 107700.00, '工资入账', '2026-06-15 09:00:00'),
-- 王五(账户6)流水
(6, 1, 100000.00, 100000.00, 200000.00, '项目奖金', '2026-04-20 09:30:00'),
(6, 2, 10000.00, 200000.00, 190000.00, '转账支出', '2026-06-02 14:00:00'),
(6, 2, 5000.00, 190000.00, 185000.00, '转账支出', '2026-06-14 13:00:00'),
(6, 2, 2000.00, 185000.00, 183000.00, '车位管理费', '2026-06-15 08:00:00'),
-- 赵六(账户7)流水
(7, 1, 45000.00, 50000.00, 95000.00, '工资入账', '2026-06-10 10:00:00'),
(7, 2, 3000.00, 95000.00, 92000.00, '转账支出', '2026-06-03 09:00:00'),
(7, 1, 6000.00, 92000.00, 98000.00, '转账收入', '2026-06-08 15:00:00'),
(7, 1, 5000.00, 98000.00, 103000.00, '转账收入', '2026-06-14 13:00:00'),
-- 孙七(账户9)流水
(9, 1, 150000.00, 150000.00, 300000.00, '投资收益', '2026-03-15 08:00:00'),
(9, 2, 15000.00, 300000.00, 285000.00, '跨行转账支出', '2026-06-05 11:00:00'),
(9, 1, 30000.00, 285000.00, 315000.00, '跨行转账收入', '2026-06-15 09:00:00'),
-- 周八(账户10)流水
(10, 1, 60000.00, 60000.00, 120000.00, '工资入账', '2026-06-05 09:00:00'),
(10, 2, 3000.00, 120000.00, 117000.00, '转账支出', '2026-06-03 09:00:00'),
(10, 1, 6000.00, 117000.00, 123000.00, '项目款收入', '2026-06-08 15:00:00'),
(10, 2, 12000.00, 123000.00, 111000.00, '合作款支出', '2026-06-13 10:00:00'),
-- 郑十(账户13)流水
(13, 1, 200000.00, 250000.00, 450000.00, '年终奖金', '2026-01-15 10:00:00'),
(13, 2, 8000.00, 450000.00, 442000.00, '转账支出', '2026-06-12 16:00:00'),
(13, 2, 30000.00, 442000.00, 412000.00, '跨行转账投资', '2026-06-15 09:00:00'),
-- 新增用户的流水数据
(16,1,50000.00,18000.00,68000.00,'首笔工资','2026-06-10 09:00:00'),
(17,1,80000.00,45000.00,125000.00,'季度奖金','2026-06-05 09:30:00'),
(17,2,5500.00,125000.00,119500.00,'租车费用','2026-06-08 14:00:00'),
(17,1,10000.00,119500.00,129500.00,'兼职收入','2026-06-12 11:00:00'),
(18,1,92000.00,0.00,92000.00,'开户存款','2026-05-01 09:00:00'),
(18,2,2300.00,92000.00,89700.00,'外卖订餐','2026-06-10 18:30:00'),
(18,1,3000.00,89700.00,92700.00,'退款','2026-06-14 14:00:00'),
(19,1,120000.00,38000.00,158000.00,'年终分红','2026-06-01 08:00:00'),
(19,2,8900.00,158000.00,149100.00,'宽带费用','2026-06-03 10:00:00'),
(19,2,1200.00,149100.00,147900.00,'早餐外卖','2026-06-15 07:30:00'),
(20,1,150000.00,60000.00,210000.00,'项目收入','2026-06-01 10:00:00'),
(20,2,3500.00,210000.00,206500.00,'平台服务费','2026-06-10 11:00:00'),
(21,1,200000.00,145000.00,345000.00,'投资收益','2026-05-15 08:30:00'),
(21,2,6800.00,345000.00,338200.00,'校园消费','2026-06-12 13:00:00'),
(22,1,60000.00,16000.00,76000.00,'家教收入','2026-06-05 14:00:00'),
(22,2,1500.00,76000.00,74500.00,'图书购买','2026-06-09 16:00:00'),
(23,1,198000.00,0.00,198000.00,'开户存款','2026-04-01 09:00:00'),
(23,2,2200.00,198000.00,195800.00,'宿舍用品','2026-06-01 10:00:00'),
(24,1,65000.00,20000.00,85000.00,'工资入账','2026-06-10 09:00:00'),
(24,2,3600.00,85000.00,81400.00,'团建费用','2026-06-13 18:00:00'),
(25,1,88000.00,25000.00,113000.00,'兼职收入','2026-06-05 15:00:00'),
(25,2,990.00,113000.00,112010.00,'外卖外卖','2026-06-12 19:30:00'),
(26,1,275000.00,0.00,275000.00,'年终奖','2026-05-01 08:00:00'),
(26,2,4500.00,275000.00,270500.00,'健身房年卡','2026-06-08 15:00:00'),
(27,1,52000.00,11000.00,63000.00,'工资入账','2026-06-10 09:00:00'),
(27,2,2200.00,63000.00,60800.00,'采购日用','2026-06-14 11:00:00'),
(28,1,182000.00,0.00,182000.00,'开户存款','2026-03-01 09:00:00'),
(28,2,5100.00,182000.00,176900.00,'留学咨询','2026-06-09 10:00:00'),
(29,1,78000.00,18000.00,96000.00,'工资入账','2026-06-05 09:00:00'),
(29,2,1800.00,96000.00,94200.00,'交通月卡','2026-06-10 07:30:00'),
(30,1,143000.00,0.00,143000.00,'项目尾款','2026-05-01 10:00:00'),
(30,2,6500.00,143000.00,136500.00,'活动策划','2026-06-12 16:00:00'),
(31,1,88000.00,0.00,88000.00,'开户存款','2026-04-01 09:00:00'),
(31,2,3200.00,88000.00,84800.00,'日语课程','2026-06-08 14:30:00'),
(32,1,221000.00,0.00,221000.00,'年终奖','2026-04-01 08:00:00'),
(32,2,7800.00,221000.00,213200.00,'摄影器材','2026-06-10 20:00:00'),
(33,1,105000.00,0.00,105000.00,'开户存款','2026-05-01 09:00:00'),
(33,2,1900.00,105000.00,103100.00,'周黑鸭零食','2026-06-15 15:00:00'),
(34,1,52000.00,16000.00,68000.00,'工资入账','2026-06-10 09:00:00'),
(34,2,1600.00,68000.00,66400.00,'迪士尼门票','2026-06-13 10:00:00'),
(35,1,120000.00,47000.00,167000.00,'季度奖金','2026-06-05 09:00:00'),
(35,2,4300.00,167000.00,162700.00,'机票购买','2026-06-12 11:00:00'),
(36,1,200000.00,90000.00,290000.00,'年终分红','2026-05-01 08:00:00'),
(36,2,5500.00,290000.00,284500.00,'健身私教','2026-06-08 14:00:00'),
(37,1,134000.00,0.00,134000.00,'开户存款','2026-03-01 09:00:00'),
(37,2,2900.00,134000.00,131100.00,'淘宝购物','2026-06-10 18:00:00'),
(38,1,62000.00,15000.00,77000.00,'工资入账','2026-06-10 09:00:00'),
(38,2,2600.00,77000.00,74400.00,'视频会员年费','2026-06-12 20:00:00'),
(39,1,256000.00,0.00,256000.00,'项目收入','2026-05-01 10:00:00'),
(39,2,4800.00,256000.00,251200.00,'旅游团费','2026-06-09 11:00:00'),
(40,1,189000.00,0.00,189000.00,'设计收入','2026-04-01 09:00:00'),
(40,2,3900.00,189000.00,185100.00,'直播设备','2026-06-10 15:00:00'),
(41,1,102000.00,0.00,102000.00,'开户存款','2026-05-01 09:00:00'),
(41,2,2300.00,102000.00,99700.00,'星巴克', '2026-06-08 08:30:00'),
(42,1,245000.00,0.00,245000.00,'年终奖','2026-03-01 08:00:00'),
(42,2,11000.00,245000.00,234000.00,'苹果笔记本电脑','2026-06-12 13:00:00'),
(43,1,138000.00,0.00,138000.00,'开户存款','2026-04-01 09:00:00'),
(43,2,5600.00,138000.00,132400.00,'iPad购买','2026-06-14 16:00:00'),
(44,1,92000.00,0.00,92000.00,'工资入账','2026-05-01 09:00:00'),
(44,2,1400.00,92000.00,90600.00,'优衣库购物','2026-06-11 17:00:00'),
(45,1,810000.00,0.00,810000.00,'大额定存','2026-02-01 10:00:00'),
(45,2,28000.00,810000.00,782000.00,'购房首付部分','2026-06-01 09:00:00'),
(46,1,176000.00,0.00,176000.00,'开户存款','2026-05-01 09:00:00'),
(46,2,6500.00,176000.00,169500.00,'装修材料','2026-06-10 10:00:00'),
(47,1,213000.00,0.00,213000.00,'年终奖','2026-04-01 08:00:00'),
(47,2,3800.00,213000.00,209200.00,'旅行度假','2026-06-08 12:00:00'),
(48,1,76000.00,19000.00,95000.00,'工资入账','2026-06-10 09:00:00'),
(48,2,2500.00,95000.00,92500.00,'餐厅聚','2026-06-13 19:00:00'),
(49,1,110000.00,44000.00,154000.00,'季度奖金','2026-06-05 09:00:00'),
(49,2,6200.00,154000.00,147800.00,'音乐会门票','2026-06-12 14:00:00'),
(50,1,287000.00,0.00,287000.00,'开户存款','2026-03-01 09:00:00'),
(50,2,1200.00,287000.00,285800.00,'日料外卖','2026-06-15 12:30:00'),
(51,1,111000.00,0.00,111000.00,'房屋租金收入','2026-06-01 10:00:00'),
(51,2,4200.00,111000.00,106800.00,'英语培训班','2026-06-10 13:00:00'),
(52,1,169000.00,0.00,169000.00,'开户存款','2026-04-01 09:00:00'),
(52,2,3500.00,169000.00,165500.00,'宠物医院','2026-06-12 09:00:00'),
(53,1,55000.00,18000.00,73000.00,'工资入账','2026-06-10 09:00:00'),
(53,2,1800.00,73000.00,71200.00,'图书馆罚款','2026-06-15 08:00:00'),
(54,1,235000.00,0.00,235000.00,'开户存款','2026-05-01 09:00:00'),
(54,2,5600.00,235000.00,229400.00,'智能手表','2026-06-10 14:00:00');

-- 交易记录 (80条)
INSERT INTO tb_transaction (transaction_no, from_account, to_account, amount, transaction_type, status, remark, created_at) VALUES
-- 原有交易
('TXN20260601001', '6222021001000002', '6222021001000004', 5000.00, 1, 1, '转账-生活费', '2026-06-01 10:00:00'),
('TXN20260602001', '6222021001000006', '6222021001000002', 10000.00, 1, 1, '转账-借款还款', '2026-06-02 14:00:00'),
('TXN20260603001', '6222021001000007', '6222021001000010', 3000.00, 1, 1, '转账', '2026-06-03 09:00:00'),
('TXN20260605001', '6222021001000009', '6222021001000004', 15000.00, 2, 1, '跨行转账', '2026-06-05 11:00:00'),
('TXN20260608001', '6222021001000010', '6222021001000007', 6000.00, 1, 1, '转账-项目款', '2026-06-08 15:00:00'),
('TXN20260610001', '6222021001000002', '6222021001000006', 25000.00, 1, 1, '大额转账', '2026-06-10 09:30:00'),
('TXN20260612001', '6222021001000013', '6222021001000002', 8000.00, 1, 1, '转账', '2026-06-12 16:00:00'),
('TXN20260613001', '6222021001000004', '6222021001000010', 12000.00, 1, 1, '转账-合作款', '2026-06-13 10:00:00'),
('TXN20260614001', '6222021001000006', '6222021001000007', 5000.00, 1, 1, '转账', '2026-06-14 13:00:00'),
('TXN20260615001', '6222021001000013', '6222021001000009', 30000.00, 2, 1, '跨行转账-投资', '2026-06-15 09:00:00'),
-- 新增交易
('TXN20260615002', '6222021001000016', '6222021001000018', 3500.00, 1, 1, '转账-房租分摊', '2026-06-15 10:30:00'),
('TXN20260615003', '6222021001000020', '6222021001000025', 12000.00, 1, 1, '转账-设备采购', '2026-06-15 11:00:00'),
('TXN20260615004', '6222021001000021', '6222021001000030', 8000.00, 2, 1, '跨行转账', '2026-06-15 11:30:00'),
('TXN20260615005', '6222021001000026', '6222021001000017', 22000.00, 1, 1, '转账-奖金分配', '2026-06-15 12:00:00'),
('TXN20260615006', '6222021001000032', '6222021001000036', 15000.00, 1, 1, '转账', '2026-06-15 12:30:00'),
('TXN20260615007', '6222021001000039', '6222021001000042', 50000.00, 2, 1, '跨行转账-投资款', '2026-06-15 13:00:00'),
('TXN20260615008', '6222021001000045', '6222021001000050', 18000.00, 1, 1, '转账-合作项目', '2026-06-15 13:30:00'),
('TXN20260614002', '6222021001000019', '6222021001000028', 6500.00, 1, 1, '转账', '2026-06-14 09:00:00'),
('TXN20260614003', '6222021001000033', '6222021001000040', 9800.00, 1, 1, '转账-课程费用', '2026-06-14 10:00:00'),
('TXN20260614004', '6222021001000041', '6222021001000037', 12500.00, 2, 1, '跨行转账', '2026-06-14 10:30:00'),
('TXN20260614005', '6222021001000046', '6222021001000035', 7300.00, 1, 1, '转账', '2026-06-14 14:00:00'),
('TXN20260613002', '6222021001000022', '6222021001000031', 4200.00, 1, 1, '转账-家教费', '2026-06-13 08:00:00'),
('TXN20260613003', '6222021001000034', '6222021001000024', 5600.00, 1, 1, '转账', '2026-06-13 15:00:00'),
('TXN20260613004', '6222021001000047', '6222021001000051', 11000.00, 1, 1, '转账-还款', '2026-06-13 16:30:00'),
('TXN20260612002', '6222021001000029', '6222021001000044', 3900.00, 1, 1, '转账', '2026-06-12 09:00:00'),
('TXN20260612003', '6222021001000048', '6222021001000016', 8500.00, 2, 1, '跨行转账', '2026-06-12 11:00:00'),
('TXN20260612004', '6222021001000053', '6222021001000023', 6100.00, 1, 1, '转账', '2026-06-12 13:00:00'),
('TXN20260611001', '6222021001000050', '6222021001000049', 16000.00, 1, 1, '转账-项目分红', '2026-06-11 10:00:00'),
('TXN20260611002', '6222021001000054', '6222021001000038', 21000.00, 1, 1, '转账', '2026-06-11 14:00:00'),
('TXN20260610002', '6222021001000001', '6222021001000019', 50000.00, 3, 1, '管理员充值', '2026-06-10 08:00:00'),
('TXN20260610003', '6222021001000021', '6222021001000043', 14500.00, 1, 1, '转账', '2026-06-10 15:00:00'),
('TXN20260610004', '6222021001000040', '6222021001000027', 7800.00, 2, 1, '跨行转账', '2026-06-10 16:00:00'),
('TXN20260609001', '6222021001000026', '6222021001000001', 28000.00, 1, 1, '管理员充值目标账户', '2026-06-09 09:00:00'),
('TXN20260609002', '6222021001000001', '6222021001000034', 20000.00, 3, 1, '管理员充值', '2026-06-09 09:30:00'),
('TXN20260609003', '6222021001000035', '6222021001000054', 9500.00, 1, 1, '转账', '2026-06-09 11:00:00'),
('TXN20260609004', '6222021001000045', '6222021001000052', 30000.00, 1, 1, '转账-大额', '2026-06-09 14:00:00'),
('TXN20260608002', '6222021001000001', '6222021001000018', 35000.00, 3, 1, '管理员充值', '2026-06-08 08:30:00'),
('TXN20260608003', '6222021001000031', '6222021001000048', 6800.00, 1, 1, '转账', '2026-06-08 16:00:00'),
('TXN20260607001', '6222021001000001', '6222021001000025', 45000.00, 3, 1, '管理员充值', '2026-06-07 09:00:00'),
('TXN20260607002', '6222021001000051', '6222021001000033', 5200.00, 1, 1, '转账', '2026-06-07 10:30:00'),
('TXN20260606001', '6222021001000036', '6222021001000022', 13500.00, 2, 1, '跨行转账', '2026-06-06 09:00:00'),
('TXN20260606002', '6222021001000042', '6222021001000030', 20000.00, 1, 1, '转账-奖学金', '2026-06-06 12:00:00'),
('TXN20260605002', '6222021001000001', '6222021001000029', 25000.00, 3, 1, '管理员充值', '2026-06-05 08:00:00'),
('TXN20260605003', '6222021001000028', '6222021001000043', 9900.00, 1, 1, '转账', '2026-06-05 15:00:00'),
('TXN20260604001', '6222021001000037', '6222021001000023', 11300.00, 1, 1, '转账-设计费', '2026-06-04 10:00:00'),
('TXN20260604002', '6222021001000049', '6222021001000017', 8800.00, 2, 1, '跨行转账', '2026-06-04 14:00:00'),
('TXN20260603002', '6222021001000001', '6222021001000044', 15000.00, 3, 1, '管理员充值', '2026-06-03 08:00:00'),
('TXN20260603003', '6222021001000052', '6222021001000027', 4300.00, 1, 1, '转账', '2026-06-03 16:00:00'),
('TXN20260602002', '6222021001000001', '6222021001000042', 50000.00, 3, 1, '管理员充值', '2026-06-02 09:00:00'),
('TXN20260601002', '6222021001000024', '6222021001000041', 6700.00, 1, 1, '转账', '2026-06-01 14:00:00'),
('TXN20260531001', '6222021001000001', '6222021001000021', 80000.00, 3, 1, '管理员充值', '2026-05-31 09:00:00'),
('TXN20260530001', '6222021001000001', '6222021001000046', 30000.00, 3, 1, '管理员充值', '2026-05-30 08:30:00'),
('TXN20260528001', '6222021001000001', '6222021001000047', 40000.00, 3, 1, '管理员充值', '2026-05-28 09:00:00'),
('TXN20260615009', '6222021001000035', '6222021001000016', 4200.00, 1, 0, '转账-余额不足', '2026-06-15 14:00:00'),
('TXN20260615010', '6222021001000050', '6222021001000020', 28000.00, 1, 1, '转账-年终奖分配', '2026-06-15 15:00:00'),
('TXN20260615011', '6222021001000017', '6222021001000032', 9200.00, 1, 1, '转账', '2026-06-15 15:30:00'),
('TXN20260615012', '6222021001000043', '6222021001000054', 25000.00, 2, 1, '跨行转账-大额', '2026-06-15 16:00:00'),
('TXN20260615013', '6222021001000039', '6222021001000002', 34000.00, 1, 1, '转账-投资回报', '2026-06-15 16:30:00'),
('TXN20260615014', '6222021001000026', '6222021001000053', 7700.00, 1, 1, '转账', '2026-06-15 17:00:00'),
('TXN20260615015', '6222021001000040', '6222021001000048', 18500.00, 1, 1, '转账-项目尾款', '2026-06-15 17:30:00'),
('TXN20260615016', '6222021001000001', '6222021001000052', 60000.00, 3, 1, '管理员充值', '2026-06-15 18:00:00'),
('TXN20260615017', '6222021001000047', '6222021001000031', 14000.00, 1, 1, '转账', '2026-06-15 18:30:00'),
('TXN20260614006', '6222021001000001', '6222021001000033', 22000.00, 3, 1, '管理员充值', '2026-06-14 08:00:00'),
('TXN20260614007', '6222021001000051', '6222021001000038', 5100.00, 1, 2, '转账-处理中', '2026-06-14 18:00:00'),
('TXN20260415001', '6222021001000014', '6222021001000028', 40000.00, 1, 1, 'VIP转账-大额', '2026-04-15 10:00:00'),
('TXN20260515001', '6222021001000014', '6222021001000035', 55000.00, 2, 1, 'VIP跨行转账', '2026-05-15 10:00:00'),
('TXN20260601003', '6222021001000014', '6222021001000050', 35000.00, 1, 1, 'VIP转账', '2026-06-01 09:00:00'),
('TXN20251201001', '6222021001000013', '6222021001000009', 45000.00, 1, 1, '转账-年终奖', '2025-12-01 10:00:00'),
('TXN20260115001', '6222021001000013', '6222021001000014', 50000.00, 1, 1, '转账-投资', '2026-01-15 09:00:00'),
('TXN20260210001', '6222021001000009', '6222021001000013', 25000.00, 1, 1, '转账-回报', '2026-02-10 14:00:00'),
('TXN20260320001', '6222021001000006', '6222021001000010', 8000.00, 1, 1, '转账', '2026-03-20 11:00:00'),
('TXN20260405001', '6222021001000010', '6222021001000004', 9000.00, 2, 1, '跨行转账', '2026-04-05 10:00:00'),

-- 交易限额扩展
('TXN20260520001', '6222021001000007', '6222021001000009', 12000.00, 1, 1, '转账-医疗费', '2026-05-20 09:00:00'),
('TXN20260518001', '6222021001000004', '6222021001000006', 7500.00, 1, 1, '转账', '2026-05-18 15:00:00'),
('TXN20260422001', '6222021001000010', '6222021001000002', 11000.00, 1, 1, '转账-还款', '2026-04-22 10:00:00'),
('TXN20260308001', '6222021001000006', '6222021001000004', 20000.00, 1, 1, '转账-项目款', '2026-03-08 09:00:00');

-- 交易限额 (30条)
INSERT INTO tb_transaction_limit (user_id, limit_type, limit_amount) VALUES
(2,1,50000.00),(2,2,200000.00),(3,1,50000.00),(3,2,200000.00),
(4,1,30000.00),(4,2,100000.00),(5,1,50000.00),(5,2,200000.00),
(6,1,50000.00),(6,2,200000.00),(7,1,50000.00),(7,2,200000.00),
(10,1,200000.00),(10,2,1000000.00),
(11,1,50000.00),(11,2,200000.00),(16,1,50000.00),(16,2,200000.00),
(21,1,100000.00),(21,2,500000.00),
(23,1,50000.00),(23,2,200000.00),
(27,1,30000.00),(27,2,100000.00),
(31,1,80000.00),(31,2,300000.00),
(40,1,200000.00),(40,2,1000000.00),
(45,1,100000.00),(45,2,500000.00);

-- 账单 (40条)
INSERT INTO tb_bill (bill_no, user_id, account_id, bill_month, total_income, total_expense, begin_balance, end_balance, status, generated_at) VALUES
('BILL-2-2-202605', 2, 2, '2026-05', 55000.00, 3000.00, 100000.00, 152000.00, 1, '2026-06-01 00:00:00'),
('BILL-2-2-202606', 2, 2, '2026-06', 63000.00, 30180.00, 152000.00, 181920.00, 1, '2026-07-01 00:00:00'),
('BILL-3-4-202605', 3, 4, '2026-05', 30000.00, 5000.00, 50000.00, 75000.00, 1, '2026-06-01 00:00:00'),
('BILL-4-6-202604', 4, 6, '2026-04', 100000.00, 0.00, 100000.00, 200000.00, 1, '2026-05-01 00:00:00'),
('BILL-5-7-202606', 5, 7, '2026-06', 56000.00, 3000.00, 50000.00, 103000.00, 1, '2026-07-01 00:00:00'),
('BILL-6-9-202603', 6, 9, '2026-03', 150000.00, 0.00, 150000.00, 300000.00, 1, '2026-04-01 00:00:00'),
('BILL-7-10-202606', 7, 10, '2026-06', 60000.00, 15000.00, 60000.00, 111000.00, 1, '2026-07-01 00:00:00'),
('BILL-8-12-202605', 8, 12, '2026-05', 88000.00, 0.00, 0.00, 88000.00, 1, '2026-06-01 00:00:00'),
('BILL-9-13-202601', 9, 13, '2026-01', 200000.00, 0.00, 250000.00, 450000.00, 1, '2026-02-01 00:00:00'),
('BILL-10-14-202605', 10, 14, '2026-05', 0.00, 0.00, 1000000.00, 1000000.00, 1, '2026-06-01 00:00:00'),
('BILL-11-16-202606', 11, 16, '2026-06', 50000.00, 0.00, 18000.00, 68000.00, 1, '2026-07-01 00:00:00'),
('BILL-12-17-202606', 12, 17, '2026-06', 90000.00, 5500.00, 45000.00, 129500.00, 1, '2026-07-01 00:00:00'),
('BILL-13-18-202606', 13, 18, '2026-06', 3000.00, 2300.00, 92000.00, 92700.00, 1, '2026-07-01 00:00:00'),
('BILL-14-19-202606', 14, 19, '2026-06', 120000.00, 10100.00, 38000.00, 147900.00, 1, '2026-07-01 00:00:00'),
('BILL-15-20-202606', 15, 20, '2026-06', 150000.00, 3500.00, 60000.00, 206500.00, 1, '2026-07-01 00:00:00'),
('BILL-16-21-202605', 16, 21, '2026-05', 200000.00, 0.00, 145000.00, 345000.00, 1, '2026-06-01 00:00:00'),
('BILL-16-21-202606', 16, 21, '2026-06', 0.00, 6800.00, 345000.00, 338200.00, 1, '2026-07-01 00:00:00'),
('BILL-17-22-202606', 17, 22, '2026-06', 60000.00, 1500.00, 16000.00, 74500.00, 1, '2026-07-01 00:00:00'),
('BILL-18-23-202606', 18, 23, '2026-06', 0.00, 2200.00, 198000.00, 195800.00, 1, '2026-07-01 00:00:00'),
('BILL-19-24-202606', 19, 24, '2026-06', 65000.00, 3600.00, 20000.00, 81400.00, 1, '2026-07-01 00:00:00'),
('BILL-20-25-202606', 20, 25, '2026-06', 88000.00, 990.00, 25000.00, 112010.00, 1, '2026-07-01 00:00:00'),
('BILL-25-30-202606', 25, 30, '2026-06', 0.00, 6500.00, 143000.00, 136500.00, 1, '2026-07-01 00:00:00'),
('BILL-30-35-202606', 30, 35, '2026-06', 120000.00, 4300.00, 47000.00, 162700.00, 1, '2026-07-01 00:00:00'),
('BILL-31-36-202605', 31, 36, '2026-05', 200000.00, 0.00, 90000.00, 290000.00, 1, '2026-06-01 00:00:00'),
('BILL-40-45-202605', 40, 45, '2026-05', 0.00, 28000.00, 810000.00, 782000.00, 1, '2026-06-01 00:00:00'),
('BILL-45-50-202606', 45, 50, '2026-06', 0.00, 1200.00, 287000.00, 285800.00, 1, '2026-07-01 00:00:00'),
('BILL-47-51-202606', 47, 51, '2026-06', 111000.00, 4200.00, 0.00, 106800.00, 1, '2026-07-01 00:00:00'),
('BILL-48-52-202606', 48, 52, '2026-06', 0.00, 3500.00, 169000.00, 165500.00, 1, '2026-07-01 00:00:00'),
('BILL-49-53-202606', 49, 53, '2026-06', 55000.00, 1800.00, 18000.00, 71200.00, 0, NULL),
('BILL-50-54-202606', 50, 54, '2026-06', 0.00, 5600.00, 235000.00, 229400.00, 1, '2026-07-01 00:00:00');

-- 账单明细 (10条)
INSERT INTO tb_bill_detail (bill_id, transaction_id, transaction_no, transaction_time, amount, balance, remark) VALUES
(1,1,'TXN20260601001','2026-06-01 10:00:00',5000.00,147000.00,'转账-生活费'),
(2,2,'TXN20260602001','2026-06-02 14:00:00',10000.00,142000.00,'转账-借款还款'),
(2,6,'TXN20260610001','2026-06-10 09:30:00',25000.00,117000.00,'大额转账'),
(4,4,'TXN20260605001','2026-06-05 11:00:00',15000.00,185000.00,'跨行转账'),
(5,3,'TXN20260603001','2026-06-03 09:00:00',3000.00,92000.00,'转账'),
(7,5,'TXN20260608001','2026-06-08 15:00:00',6000.00,114000.00,'转账-项目款'),
(7,7,'TXN20260612001','2026-06-12 16:00:00',8000.00,106000.00,'转账'),
(7,8,'TXN20260613001','2026-06-13 10:00:00',12000.00,94000.00,'转账-合作款'),
(7,9,'TXN20260614001','2026-06-14 13:00:00',5000.00,89000.00,'转账'),
(7,10,'TXN20260615001','2026-06-15 09:00:00',30000.00,59000.00,'跨行转账-投资');

-- AI对话记录 (60条)
INSERT INTO tb_ai_conversation (user_id, session_id, message_type, content, response_time, created_at) VALUES
(2,'sess-001',1,'如何查询我的账户余额？',NULL,'2026-06-15 09:00:00'),
(2,'sess-001',2,'您可以通过以下方式查询余额：1.登录后点击"我的账户" 2.选择对应账户查看余额。',1200,'2026-06-15 09:00:02'),
(3,'sess-002',1,'转账需要多长时间到账？',NULL,'2026-06-15 10:00:00'),
(3,'sess-002',2,'行内转账实时到账，跨行转账一般1-2个工作日内到账。',800,'2026-06-15 10:00:01'),
(4,'sess-003',1,'信用卡额度如何提升？',NULL,'2026-06-15 11:00:00'),
(4,'sess-003',2,'信用卡额度提升需要满足：1.用卡满6个月 2.信用记录良好 3.收入稳定。',1500,'2026-06-15 11:00:02'),
(5,'sess-004',1,'忘记密码怎么办？',NULL,'2026-06-15 14:00:00'),
(5,'sess-004',2,'您可以在登录页面点击"忘记密码"，通过手机号验证后重置密码。',650,'2026-06-15 14:00:01'),
(6,'sess-005',1,'账单日可以修改吗？',NULL,'2026-06-15 15:00:00'),
(6,'sess-005',2,'部分卡种的账单日支持修改，每年可修改一次。请在"我的账户"中查看具体规则。',900,'2026-06-15 15:00:01'),
(2,'sess-001',1,'我最近的交易记录在哪里查看？',NULL,'2026-06-15 09:05:00'),
(2,'sess-001',2,'您可以在首页点击"交易记录"查看所有转账和消费记录，支持按账户筛选和日期范围查询。',1100,'2026-06-15 09:05:02'),
(11,'sess-006',1,'如何开通信用卡？',NULL,'2026-06-15 08:00:00'),
(11,'sess-006',2,'您可以在"我的账户"页面点击"申请信用卡"，填写相关信息后提交审核，一般3-5个工作日内完成。',1300,'2026-06-15 08:00:02'),
(12,'sess-007',1,'跨行转账手续费是多少？',NULL,'2026-06-15 09:30:00'),
(12,'sess-007',2,'ICBC行内转账免费，跨行转账按金额的0.1%收取，最低1元，最高50元。VIP用户可享手续费减免。',980,'2026-06-15 09:30:01'),
(14,'sess-008',1,'我想申请信用卡借款,有什么条件？',NULL,'2026-06-15 10:15:00'),
(14,'sess-008',2,'申请信用卡借款需要满足以下条件：1.持有ICBC信用卡且状态正常 2.有可用信用额度 3.账户无逾期记录 4.通过系统风险评估。',1600,'2026-06-15 10:15:02'),
(16,'sess-009',1,'怎么查看我的还款计划？',NULL,'2026-06-15 11:20:00'),
(16,'sess-009',2,'登录后进入"信用卡借款"页面，点击对应借款记录即可查看完整的还款计划表，包括每期应还款金额、本金、利息等详细信息。',1400,'2026-06-15 11:20:02'),
(16,'sess-009',1,'如果逾期了会有什么后果？',NULL,'2026-06-15 11:22:00'),
(16,'sess-009',2,'逾期会产生以下影响：1.按日计收罚息（一般为正常利率的1.5倍） 2.影响个人征信记录 3.可能降低信用额度 4.严重逾期可能被列入失信名单。建议按时还款。',2000,'2026-06-15 11:22:03'),
(21,'sess-010',1,'我的账户单日转账限额是多少？',NULL,'2026-06-15 13:00:00'),
(21,'sess-010',2,'系统显示您的单日转账限额为200,000元。如需调整，可在"个人中心-安全设置"中申请提高限额，需通过身份验证。',850,'2026-06-15 13:00:01'),
(25,'sess-011',1,'请问等额本息是怎么计算的？',NULL,'2026-06-15 14:30:00'),
(25,'sess-011',2,'等额本息还款方式下，每月还款金额固定，包含本金和利息两部分。前期利息占比较高，后期本金占比逐渐增加。系统会自动为您计算完整的还款计划。',1900,'2026-06-15 14:30:03'),
(27,'sess-012',1,'账单导出支持哪些格式？',NULL,'2026-06-15 16:00:00'),
(27,'sess-012',2,'目前系统支持PDF格式的账单导出，导出的账单包含完整的收支明细和账户余额信息，可直接用于报销或作为证明文件。',750,'2026-06-15 16:00:01'),
(31,'sess-013',1,'如果发现异常交易应该怎么处理？',NULL,'2026-06-15 08:30:00'),
(31,'sess-013',2,'如发现异常交易请立即：1.登录系统临时冻结相关账户 2.拨打ICBC客服热线95588 3.前往就近网点核实身份 4.保留交易记录截图作为证据。',1600,'2026-06-15 08:30:02'),
(40,'sess-014',1,'VIP用户有什么特殊权益？',NULL,'2026-06-15 09:45:00'),
(40,'sess-014',2,'VIP用户享有以下权益：1.更高的转账限额（单笔20万，单日100万） 2.优先客服服务 3.专属理财顾问 4.手续费减免 5.信用卡额度更高。',1700,'2026-06-15 09:45:03'),
(42,'sess-015',1,'转账时候填写错了账号怎么办？',NULL,'2026-06-15 12:00:00'),
(42,'sess-015',2,'转账前请仔细核对收款人信息。如果已经转错，请立即联系ICBC客服95588。资金能否追回取决于对方账户的处理情况，所以我们强烈建议转账前三次核对账号和金额。',1300,'2026-06-15 12:00:02'),

-- 操作日志 (80条)
INSERT INTO tb_operation_log (user_id, username, operation, method, params, ip_address, status, operation_time) VALUES
(2,'zhangsan','用户登录','POST /api/v1/users/login','{"username":"zhangsan"}','192.168.1.101',1,'2026-06-15 09:00:00'),
(2,'zhangsan','查询账户','GET /api/v1/accounts',NULL,'192.168.1.101',1,'2026-06-15 09:05:00'),
(2,'zhangsan','查询交易记录','GET /api/v1/transactions/history',NULL,'192.168.1.101',1,'2026-06-15 09:08:00'),
(2,'zhangsan','AI客服咨询','POST /api/v1/ai/chat','{"message":"查询余额"}','192.168.1.101',1,'2026-06-15 09:00:05'),
(3,'lisi','用户登录','POST /api/v1/users/login','{"username":"lisi"}','192.168.1.102',1,'2026-06-15 10:00:00'),
(3,'lisi','发起转账','POST /api/v1/transactions','{"amount":5000}','192.168.1.102',1,'2026-06-15 10:10:00'),
(3,'lisi','查询账单','GET /api/v1/bills',NULL,'192.168.1.102',1,'2026-06-15 10:15:00'),
(4,'wangwu','用户登录','POST /api/v1/users/login','{"username":"wangwu"}','192.168.1.103',1,'2026-06-15 11:00:00'),
(4,'wangwu','查询账户','GET /api/v1/accounts',NULL,'192.168.1.103',1,'2026-06-15 11:02:00'),
(5,'zhaoliu','用户登录','POST /api/v1/users/login','{"username":"zhaoliu"}','192.168.1.104',1,'2026-06-15 14:00:00'),
(5,'zhaoliu','查询余额','GET /api/v1/accounts/balance',NULL,'192.168.1.104',1,'2026-06-15 14:05:00'),
(1,'admin','管理员登录','POST /api/v1/users/login','{"username":"admin"}','192.168.1.100',1,'2026-06-15 08:00:00'),
(1,'admin','查看用户列表','GET /api/v1/admin/users',NULL,'192.168.1.100',1,'2026-06-15 08:30:00'),
(1,'admin','查看账户列表','GET /api/v1/admin/accounts',NULL,'192.168.1.100',1,'2026-06-15 08:35:00'),
(1,'admin','查看数据大屏','GET /api/v1/admin/dashboard',NULL,'192.168.1.100',1,'2026-06-15 08:40:00'),
(1,'admin','充值操作','POST /api/v1/admin/recharge','{"accountNo":"6222021001000019","amount":50000}','192.168.1.100',1,'2026-06-10 08:00:00'),
(7,'zhouba','用户登录','POST /api/v1/users/login','{"username":"zhouba"}','192.168.1.107',1,'2026-06-15 16:00:00'),
(7,'zhouba','查询交易记录','GET /api/v1/transactions/history',NULL,'192.168.1.107',1,'2026-06-15 16:05:00'),
(8,'wujiu','用户登录','POST /api/v1/users/login','{"username":"wujiu"}','192.168.1.108',1,'2026-06-14 09:00:00'),
(8,'wujiu','查询账户','GET /api/v1/accounts',NULL,'192.168.1.108',1,'2026-06-14 09:02:00'),
(11,'chenwei','用户登录','POST /api/v1/users/login','{"username":"chenwei"}','192.168.1.111',1,'2026-06-15 08:00:00'),
(11,'chenwei','AI客服咨询','POST /api/v1/ai/chat','{"message":"如何开通信用卡"}','192.168.1.111',1,'2026-06-15 08:00:10'),
(12,'liuqiang','用户登录','POST /api/v1/users/login','{"username":"liuqiang"}','192.168.1.112',1,'2026-06-15 09:30:00'),
(13,'huangli','用户登录','POST /api/v1/users/login','{"username":"huangli"}','192.168.1.113',1,'2026-06-14 10:00:00'),
(13,'huangli','发起转账','POST /api/v1/transactions','{"amount":3800}','192.168.1.113',1,'2026-06-14 10:15:00'),
(16,'mayun','用户登录','POST /api/v1/users/login','{"username":"mayun"}','192.168.1.116',1,'2026-06-15 11:20:00'),
(16,'mayun','查询借款记录','GET /api/v1/loans/my',NULL,'192.168.1.116',1,'2026-06-15 11:21:00'),
(21,'jiangtao','用户登录','POST /api/v1/users/login','{"username":"jiangtao"}','192.168.1.121',1,'2026-06-15 13:00:00'),
(21,'jiangtao','查询交易记录','GET /api/v1/transactions/history',NULL,'192.168.1.121',1,'2026-06-15 13:05:00'),
(23,'hanglei','用户登录','POST /api/v1/users/login','{"username":"hanglei"}','192.168.1.123',1,'2026-06-14 08:00:00'),
(23,'hanglei','查询账单','GET /api/v1/bills',NULL,'192.168.1.123',1,'2026-06-14 08:05:00'),
(27,'shilei','用户登录','POST /api/v1/users/login','{"username":"shilei"}','192.168.1.127',1,'2026-06-15 16:00:00'),
(27,'shilei','AI客服咨询','POST /api/v1/ai/chat','{"message":"账单导出格式"}','192.168.1.127',1,'2026-06-15 16:00:10'),
(31,'hejun','用户登录','POST /api/v1/users/login','{"username":"hejun"}','192.168.1.131',1,'2026-06-15 08:30:00'),
(31,'hejun','查询账户','GET /api/v1/accounts',NULL,'192.168.1.131',1,'2026-06-15 08:32:00'),
(40,'tianyu','用户登录','POST /api/v1/users/login','{"username":"tianyu"}','192.168.1.140',1,'2026-06-15 09:45:00'),
(40,'tianyu','大额转账','POST /api/v1/transactions','{"amount":50000}','192.168.1.140',1,'2026-06-15 09:50:00'),
(42,'leijun','用户登录','POST /api/v1/users/login','{"username":"leijun"}','192.168.1.142',1,'2026-06-15 12:00:00'),
(43,'duanpeng','用户登录','POST /api/v1/users/login','{"username":"duanpeng"}','192.168.1.143',1,'2026-06-14 12:00:00'),
(47,'zhuyun','用户登录','POST /api/v1/users/login','{"username":"zhuyun"}','192.168.1.147',1,'2026-06-15 07:00:00'),
(47,'zhuyun','发起转账','POST /api/v1/transactions','{"amount":11000}','192.168.1.147',1,'2026-06-13 16:30:00'),

-- 操作日志 (10条)
INSERT INTO tb_operation_log (user_id, username, operation, method, params, ip_address, status, operation_time) VALUES
(2,'zhangsan','用户登录','POST /api/v1/users/login','{"username":"zhangsan"}','192.168.1.101',1,'2026-06-15 09:00:00'),
(2,'zhangsan','查询账户','GET /api/v1/accounts',NULL,'192.168.1.101',1,'2026-06-15 09:05:00'),
(3,'lisi','用户登录','POST /api/v1/users/login','{"username":"lisi"}','192.168.1.102',1,'2026-06-15 10:00:00'),
(3,'lisi','发起转账','POST /api/v1/transactions','{"amount":5000}','192.168.1.102',1,'2026-06-15 10:10:00'),
(4,'wangwu','用户登录','POST /api/v1/users/login','{"username":"wangwu"}','192.168.1.103',1,'2026-06-15 11:00:00'),
(5,'zhaoliu','用户登录','POST /api/v1/users/login','{"username":"zhaoliu"}','192.168.1.104',1,'2026-06-15 14:00:00'),
(1,'admin','管理员登录','POST /api/v1/users/login','{"username":"admin"}','192.168.1.100',1,'2026-06-15 08:00:00'),
(1,'admin','查看用户列表','GET /api/v1/admin/users',NULL,'192.168.1.100',1,'2026-06-15 08:30:00'),
(2,'zhangsan','AI客服咨询','POST /api/v1/ai/chat','{"message":"查询余额"}','192.168.1.101',1,'2026-06-15 09:00:05'),
(7,'zhouba','用户登录','POST /api/v1/users/login','{"username":"zhouba"}','192.168.1.107',1,'2026-06-15 16:00:00');

-- 借款记录 (25条)
INSERT INTO tb_loan (loan_no, user_id, account_id, loan_amount, approved_amount, interest_rate, loan_term, monthly_payment, total_repay, repaid_amount, remaining_periods, status, apply_date, approve_date, start_date, due_date, remark) VALUES
('LOAN202606001', 2, 3, 30000.00, 30000.00, 12.000, 12, 2665.00, 31980.00, 7995.00, 9, 3, '2026-03-01', '2026-03-02', '2026-03-05', '2027-03-05', '个人消费贷款'),
('LOAN202606002', 3, 5, 50000.00, 50000.00, 10.000, 24, 2307.00, 55368.00, 0.00, 24, 2, '2026-05-10', '2026-05-11', '2026-05-15', '2028-05-15', '购车贷款'),
('LOAN202606003', 5, 8, 20000.00, 20000.00, 8.000, 6, 3413.00, 20478.00, 20478.00, 0, 4, '2026-01-10', '2026-01-11', '2026-01-15', '2026-07-15', '旅游贷款-已结清'),
('LOAN202606004', 7, 11, 40000.00, 40000.00, 15.000, 12, 3613.00, 43356.00, 10839.00, 9, 3, '2026-04-01', '2026-04-02', '2026-04-05', '2027-04-05', '装修贷款'),
('LOAN202606005', 10, 15, 100000.00, 100000.00, 6.000, 36, 3042.00, 109512.00, 0.00, 36, 2, '2026-06-01', '2026-06-02', '2026-06-05', '2029-06-05', '大额消费贷款'),
('LOAN202606006', 2, 3, 10000.00, 5000.00, 8.000, 3, 1693.00, 5079.00, 5079.00, 0, 4, '2026-02-15', '2026-02-16', '2026-02-20', '2026-05-20', '应急贷款-已结清'),
('LOAN202606007', 4, 6, 15000.00, 15000.00, 10.000, 6, 2577.00, 15462.00, 5154.00, 4, 3, '2026-06-01', '2026-06-02', '2026-06-05', '2026-12-05', '教育贷款'),
('LOAN202606008', 6, 9, 8000.00, 0.00, 0.000, 0, 0.00, 0.00, 0.00, 0, 0, '2026-06-10', NULL, NULL, NULL, '不符合条件-已拒绝'),
('LOAN202606009', 8, 12, 25000.00, 25000.00, 9.500, 12, 2190.00, 26280.00, 0.00, 12, 2, '2026-06-15', '2026-06-16', NULL, NULL, '家电贷款-待放款'),
('LOAN202606010', 3, 5, 35000.00, 35000.00, 11.000, 18, 2119.00, 38142.00, 6357.00, 15, 3, '2026-03-20', '2026-03-21', '2026-03-25', '2027-09-25', '医疗贷款'),
-- 新增借款
('LOAN202606011', 11, 55, 25000.00, 25000.00, 9.500, 12, 2190.00, 26280.00, 4380.00, 10, 3, '2026-04-10', '2026-04-11', '2026-04-15', '2027-04-15', '旅游贷款'),
('LOAN202606012', 14, 56, 15000.00, 15000.00, 8.000, 6, 2563.00, 15378.00, 7695.00, 3, 3, '2026-05-01', '2026-05-02', '2026-05-05', '2026-11-05', '教育贷款'),
('LOAN202606013', 16, 57, 80000.00, 80000.00, 7.500, 24, 3597.00, 86328.00, 0.00, 24, 2, '2026-06-01', '2026-06-02', NULL, NULL, '购车贷款-待放款'),
('LOAN202606014', 21, 59, 30000.00, 30000.00, 10.000, 12, 2638.00, 31656.00, 7914.00, 9, 3, '2026-04-15', '2026-04-16', '2026-04-20', '2027-04-20', '消费贷款'),
('LOAN202606015', 23, 60, 12000.00, 12000.00, 8.500, 6, 2054.00, 12324.00, 4108.00, 4, 3, '2026-05-10', '2026-05-11', '2026-05-15', '2026-11-15', '数码产品'),
('LOAN202606016', 27, 61, 20000.00, 0.00, 0.000, 0, 0.00, 0.00, 0.00, 0, 0, '2026-06-12', NULL, NULL, NULL, '信用评分不足-已拒绝'),
('LOAN202606017', 31, 62, 45000.00, 45000.00, 11.000, 18, 2715.00, 48870.00, 0.00, 18, 2, '2026-06-01', '2026-06-02', NULL, NULL, '装修贷款-待放款'),
('LOAN202606018', 35, 63, 18000.00, 15000.00, 9.000, 6, 2565.00, 15390.00, 0.00, 6, 1, '2026-06-14', NULL, NULL, NULL, '医疗贷款-审核中'),
('LOAN202606019', 40, 64, 150000.00, 150000.00, 5.500, 36, 4545.00, 163620.00, 0.00, 36, 2, '2026-05-01', '2026-05-02', '2026-05-05', '2029-05-05', '购房首付借款'),
('LOAN202606020', 42, 65, 35000.00, 35000.00, 10.000, 12, 3077.00, 36924.00, 6154.00, 10, 3, '2026-04-01', '2026-04-02', '2026-04-05', '2027-04-05', '留学费用'),
('LOAN202606021', 45, 66, 20000.00, 20000.00, 8.000, 6, 3413.00, 20478.00, 20478.00, 0, 4, '2026-01-01', '2026-01-02', '2026-01-05', '2026-07-05', '家电贷款-已结清'),
('LOAN202606022', 50, 67, 60000.00, 60000.00, 6.500, 24, 2674.00, 64176.00, 0.00, 24, 2, '2026-06-15', '2026-06-16', NULL, NULL, '购车首付-待放款'),
('LOAN202606023', 18, 58, 10000.00, 5000.00, 8.000, 3, 1693.00, 5079.00, 5079.00, 0, 4, '2026-03-15', '2026-03-16', '2026-03-20', '2026-06-20', '应急借款-已结清'),
('LOAN202606024', 4, 6, 5000.00, 5000.00, 7.000, 3, 1705.00, 5115.00, 5115.00, 0, 4, '2026-04-20', '2026-04-21', '2026-04-25', '2026-07-25', '小额借款-已结清'),
('LOAN202606025', 14, 56, 8000.00, 8000.00, 11.000, 8, 1049.00, 8392.00, 0.00, 8, 1, '2026-06-15', NULL, NULL, NULL, '旅游借款-审核中');

-- 还款记录 (40条)
INSERT INTO tb_loan_repayment (repayment_no, loan_id, period_no, amount, principal, interest, actual_amount, scheduled_date, actual_date, status) VALUES
('REP202606001', 1, 1, 2665.00, 2365.00, 300.00, 2665.00, '2026-04-05', '2026-04-05', 1),
('REP202606002', 1, 2, 2665.00, 2389.00, 276.00, 2665.00, '2026-05-05', '2026-05-05', 1),
('REP202606003', 1, 3, 2665.00, 2413.00, 252.00, 2665.00, '2026-06-05', '2026-06-05', 1),
('REP202606004', 3, 1, 3413.00, 3280.00, 133.00, 3413.00, '2026-02-15', '2026-02-15', 1),
('REP202606005', 3, 2, 3413.00, 3302.00, 111.00, 3413.00, '2026-03-15', '2026-03-15', 1),
('REP202606006', 3, 3, 3413.00, 3324.00, 89.00, 3413.00, '2026-04-15', '2026-04-15', 1),
('REP202606007', 3, 4, 3413.00, 3346.00, 67.00, 3413.00, '2026-05-15', '2026-05-15', 1),
('REP202606008', 3, 5, 3413.00, 3368.00, 45.00, 3413.00, '2026-06-15', NULL, 0),
('REP202606009', 4, 1, 3613.00, 3113.00, 500.00, 3613.00, '2026-05-05', '2026-05-05', 1),
('REP202606010', 6, 1, 1693.00, 1627.00, 66.00, 1693.00, '2026-03-20', '2026-03-20', 1),
('REP202606011', 7, 1, 2577.00, 2452.00, 125.00, 2577.00, '2026-07-05', NULL, 0),
('REP202606012', 10, 1, 2119.00, 1798.00, 321.00, 2119.00, '2026-04-25', '2026-04-25', 1),
('REP202606013', 10, 2, 2119.00, 1814.00, 305.00, 2119.00, '2026-05-25', '2026-05-25', 1),
('REP202606014', 10, 3, 2119.00, 1831.00, 288.00, 2119.00, '2026-06-25', NULL, 0),
('REP202606015', 1, 4, 2665.00, 2437.00, 228.00, 0.00, '2026-07-05', NULL, 0),
-- 新增还款
('REP202606016', 11, 1, 2190.00, 1992.00, 198.00, 2190.00, '2026-05-15', '2026-05-15', 1),
('REP202606017', 11, 2, 2190.00, 2008.00, 182.00, 2190.00, '2026-06-15', '2026-06-15', 1),
('REP202606018', 12, 1, 2563.00, 2463.00, 100.00, 2563.00, '2026-06-05', '2026-06-05', 1),
('REP202606019', 12, 2, 2563.00, 2479.00, 84.00, 2563.00, '2026-07-05', NULL, 0),
('REP202606020', 14, 1, 2638.00, 2388.00, 250.00, 2638.00, '2026-05-20', '2026-05-20', 1),
('REP202606021', 14, 2, 2638.00, 2408.00, 230.00, 2638.00, '2026-06-20', '2026-06-15', 1),
('REP202606022', 14, 3, 2638.00, 2428.00, 210.00, 2638.00, '2026-07-20', NULL, 0),
('REP202606023', 15, 1, 2054.00, 1969.00, 85.00, 2054.00, '2026-06-15', '2026-06-15', 1),
('REP202606024', 15, 2, 2054.00, 1983.00, 71.00, 2054.00, '2026-07-15', NULL, 0),
('REP202606025', 20, 1, 3077.00, 2785.00, 292.00, 3077.00, '2026-05-05', '2026-05-05', 1),
('REP202606026', 20, 2, 3077.00, 2809.00, 268.00, 3077.00, '2026-06-05', '2026-06-05', 1),
('REP202606027', 21, 1, 3413.00, 3280.00, 133.00, 3413.00, '2026-02-05', '2026-02-05', 1),
('REP202606028', 21, 2, 3413.00, 3302.00, 111.00, 3413.00, '2026-03-05', '2026-03-05', 1),
('REP202606029', 21, 3, 3413.00, 3324.00, 89.00, 3413.00, '2026-04-05', '2026-04-05', 1),
('REP202606030', 21, 4, 3413.00, 3346.00, 67.00, 3413.00, '2026-05-05', '2026-05-05', 1),
('REP202606031', 21, 5, 3413.00, 3368.00, 45.00, 3413.00, '2026-06-05', '2026-06-05', 1),
('REP202606032', 21, 6, 3413.00, 3391.00, 22.00, 3413.00, '2026-07-05', NULL, 0),
('REP202606033', 23, 1, 1693.00, 1627.00, 66.00, 1693.00, '2026-04-20', '2026-04-20', 1),
('REP202606034', 23, 2, 1693.00, 1638.00, 55.00, 1693.00, '2026-05-20', '2026-05-20', 1),
('REP202606035', 23, 3, 1693.00, 1649.00, 44.00, 1693.00, '2026-06-20', NULL, 0),
('REP202606036', 24, 1, 1705.00, 1676.00, 29.00, 1705.00, '2026-05-25', '2026-05-25', 1),
('REP202606037', 24, 2, 1705.00, 1686.00, 19.00, 1705.00, '2026-06-25', NULL, 0),
('REP202606038', 24, 3, 1705.00, 1696.00, 9.00, 1705.00, '2026-07-25', NULL, 0),
('REP202606039', 4, 2, 3613.00, 3163.00, 450.00, 0.00, '2026-06-05', NULL, 0),
('REP202606040', 7, 2, 2577.00, 2473.00, 104.00, 0.00, '2026-08-05', NULL, 0);

COMMIT;

-- ========================================
-- 初始化完成
-- ========================================
SELECT 'ICBC在线银行系统数据库初始化完成！' AS message;
SHOW TABLES;

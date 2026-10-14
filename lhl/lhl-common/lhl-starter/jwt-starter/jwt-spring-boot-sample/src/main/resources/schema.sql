-- ============================================================================
-- jwt-spring-boot-sample 演示表结构（MySQL 8.x）
--
-- 启动时由 spring.sql.init.mode=always 自动执行，全部语句幂等（IF NOT EXISTS）。
-- 演示数据不在这里插入：口令需要 BCrypt 散列，由 SampleDataInitializer 用
-- PasswordEncoder 编码后幂等写入，见 SampleDataInitializer.kt。
-- ============================================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(64)  NOT NULL COMMENT '登录账号',
    password    VARCHAR(100) NOT NULL COMMENT '口令散列（BCrypt，不可逆）',
    mobile      VARCHAR(20)           DEFAULT NULL COMMENT '手机号，手机验证码登录按此列查询',
    nickname    VARCHAR(64)           DEFAULT NULL COMMENT '昵称',
    wx_open_id  VARCHAR(64)           DEFAULT NULL COMMENT '微信 openId，公众号/小程序登录按此列查询',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：1=正常，0=禁用',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常，1=已删除',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_mobile (mobile),
    UNIQUE KEY uk_sys_user_wx_open_id (wx_open_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 角色 / 权限表
-- type=1 为角色（role_code 以 ROLE_ 开头，供 hasRole 使用）
-- type=2 为权限（role_code 直接是权限标识，供 hasAuthority 使用）
CREATE TABLE IF NOT EXISTS sys_role
(
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_code   VARCHAR(64) NOT NULL COMMENT '角色标识 ROLE_ADMIN / 权限标识 USER_READ',
    role_name   VARCHAR(64) NOT NULL COMMENT '显示名称',
    type        TINYINT     NOT NULL DEFAULT 1 COMMENT '类型：1=角色，2=权限',
    remark      VARCHAR(255)         DEFAULT NULL COMMENT '备注',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色与权限表';

-- 用户与角色/权限的关联表
CREATE TABLE IF NOT EXISTS sys_user_role
(
    id      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    role_id BIGINT NOT NULL COMMENT '角色/权限 ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户与角色关联表';
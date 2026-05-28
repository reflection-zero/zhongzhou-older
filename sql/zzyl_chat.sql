-- 客服智能体聊天表
-- 执行前确保数据库 zzyl 已存在

CREATE TABLE IF NOT EXISTS chat_session (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  user_type TINYINT NOT NULL COMMENT '0=admin,1=family_member',
  title VARCHAR(100) COMMENT '会话标题',
  summary TEXT COMMENT '对话历史摘要(压缩长历史用)',
  status TINYINT DEFAULT 1 COMMENT '1=active,0=closed',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user (user_id, user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

CREATE TABLE IF NOT EXISTS chat_message (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id BIGINT NOT NULL COMMENT 'FK chat_session.id',
  role VARCHAR(10) NOT NULL COMMENT 'user|assistant',
  content TEXT NOT NULL COMMENT '消息内容',
  context_data TEXT COMMENT '注入的养老数据JSON(调试用)',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

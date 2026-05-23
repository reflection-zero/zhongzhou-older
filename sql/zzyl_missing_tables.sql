-- 缺失业务表 DDL，从 Java 实体类反查生成

DROP TABLE IF EXISTS `alert_data`;
CREATE TABLE `alert_data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `iot_id` varchar(255) DEFAULT NULL COMMENT '物联网设备id',
  `device_name` varchar(255) DEFAULT NULL COMMENT '设备名称',
  `product_key` varchar(255) DEFAULT NULL COMMENT '所属产品key',
  `product_name` varchar(255) DEFAULT NULL COMMENT '产品名称',
  `function_id` varchar(255) DEFAULT NULL COMMENT '功能标识符',
  `access_location` varchar(255) DEFAULT NULL COMMENT '接入位置',
  `location_type` int DEFAULT NULL COMMENT '位置类型 0：随身设备 1：固定设备',
  `physical_location_type` int DEFAULT NULL COMMENT '物理位置类型 0楼层 1房间 2床位',
  `device_description` varchar(500) DEFAULT NULL COMMENT '位置备注',
  `data_value` varchar(255) DEFAULT NULL COMMENT '数据值',
  `alert_rule_id` bigint DEFAULT NULL COMMENT '报警规则id',
  `alert_reason` varchar(500) DEFAULT NULL COMMENT '报警原因',
  `processing_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `processor_id` bigint DEFAULT NULL COMMENT '处理人id',
  `processor_name` varchar(64) DEFAULT NULL COMMENT '处理人名称',
  `processing_time` datetime DEFAULT NULL COMMENT '处理时间',
  `type` int DEFAULT NULL COMMENT '报警数据类型 0：老人异常数据 1：设备异常数据',
  `status` int DEFAULT NULL COMMENT '状态 0：待处理 1：已处理',
  `user_id` bigint DEFAULT NULL COMMENT '接收人id',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报警数据';

DROP TABLE IF EXISTS `alert_rule`;
CREATE TABLE `alert_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_key` varchar(255) DEFAULT NULL COMMENT '所属产品的key',
  `product_name` varchar(255) DEFAULT NULL COMMENT '产品名称',
  `module_id` varchar(255) DEFAULT NULL COMMENT '模块的key',
  `module_name` varchar(255) DEFAULT NULL COMMENT '模块名称',
  `function_name` varchar(255) DEFAULT NULL COMMENT '功能名称',
  `function_id` varchar(255) DEFAULT NULL COMMENT '功能标识',
  `iot_id` varchar(255) DEFAULT NULL COMMENT '物联网设备id',
  `device_name` varchar(255) DEFAULT NULL COMMENT '设备名称',
  `alert_data_type` int DEFAULT NULL COMMENT '报警数据类型 0：老人异常数据 1：设备异常数据',
  `alert_rule_name` varchar(255) DEFAULT NULL COMMENT '告警规则名称',
  `operator` varchar(64) DEFAULT NULL COMMENT '运算符',
  `value` double DEFAULT NULL COMMENT '阈值',
  `duration` int DEFAULT NULL COMMENT '持续周期',
  `alert_effective_period` varchar(255) DEFAULT NULL COMMENT '报警生效时段',
  `alert_silent_period` int DEFAULT NULL COMMENT '报警沉默周期',
  `status` int DEFAULT NULL COMMENT '0 禁用 1启用',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报警规则';

DROP TABLE IF EXISTS `check_in`;
CREATE TABLE `check_in` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elder_name` varchar(64) DEFAULT NULL COMMENT '老人姓名',
  `elder_id` bigint DEFAULT NULL COMMENT '老人ID',
  `id_card_no` varchar(32) DEFAULT NULL COMMENT '身份证号',
  `start_date` datetime DEFAULT NULL COMMENT '入住开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '入住结束时间',
  `nursing_level_name` varchar(64) DEFAULT NULL COMMENT '护理等级名称',
  `bed_number` varchar(64) DEFAULT NULL COMMENT '入住床位',
  `status` int DEFAULT NULL COMMENT '状态 0:已入住 1:已退住',
  `sort_order` int DEFAULT NULL COMMENT '排序编号',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='入住';

DROP TABLE IF EXISTS `check_in_config`;
CREATE TABLE `check_in_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `check_in_id` bigint DEFAULT NULL COMMENT '入住表ID',
  `nursing_level_id` bigint DEFAULT NULL COMMENT '护理等级ID',
  `nursing_level_name` varchar(64) DEFAULT NULL COMMENT '护理等级名称',
  `fee_start_date` datetime DEFAULT NULL COMMENT '费用开始时间',
  `fee_end_date` datetime DEFAULT NULL COMMENT '费用结束时间',
  `deposit` decimal(10,2) DEFAULT NULL COMMENT '押金（元）',
  `nursing_fee` decimal(10,2) DEFAULT NULL COMMENT '护理费用（元/月）',
  `bed_fee` decimal(10,2) DEFAULT NULL COMMENT '床位费用（元/月）',
  `insurance_payment` decimal(10,2) DEFAULT NULL COMMENT '医保支付（元/月）',
  `government_subsidy` decimal(10,2) DEFAULT NULL COMMENT '政府补贴（元/月）',
  `other_fees` decimal(10,2) DEFAULT NULL COMMENT '其他费用（元/月）',
  `sort_order` int DEFAULT NULL COMMENT '排序编号',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='入住配置';

DROP TABLE IF EXISTS `contract`;
CREATE TABLE `contract` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elder_id` bigint DEFAULT NULL COMMENT '老人ID',
  `contract_name` varchar(255) DEFAULT NULL COMMENT '合同名称',
  `contract_number` varchar(64) DEFAULT NULL COMMENT '合同编号',
  `agreement_path` varchar(500) DEFAULT NULL COMMENT '协议地址（文件路径或URL）',
  `third_party_phone` varchar(32) DEFAULT NULL COMMENT '丙方手机号',
  `third_party_name` varchar(64) DEFAULT NULL COMMENT '丙方姓名',
  `elder_name` varchar(64) DEFAULT NULL COMMENT '老人姓名',
  `start_date` datetime DEFAULT NULL COMMENT '开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '结束时间',
  `status` int DEFAULT NULL COMMENT '状态 0:未生效 1:已生效 2:已过期 3:已失效',
  `sign_date` datetime DEFAULT NULL COMMENT '签约日期',
  `termination_submitter` varchar(64) DEFAULT NULL COMMENT '解除提交人',
  `termination_date` datetime DEFAULT NULL COMMENT '解除日期',
  `termination_agreement_path` varchar(500) DEFAULT NULL COMMENT '解除协议地址',
  `sort_order` int DEFAULT NULL COMMENT '排序编号',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='合同';

DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `iot_id` varchar(255) DEFAULT NULL COMMENT '物联网设备ID',
  `secret` varchar(255) DEFAULT NULL COMMENT '设备秘钥',
  `binding_location` varchar(255) DEFAULT NULL COMMENT '绑定位置',
  `location_type` int DEFAULT NULL COMMENT '位置类型 0：随身设备 1：固定设备',
  `physical_location_type` int DEFAULT NULL COMMENT '物理位置类型 0楼层 1房间 2床位',
  `device_name` varchar(255) DEFAULT NULL COMMENT '设备名称',
  `product_key` varchar(255) DEFAULT NULL COMMENT '产品key',
  `product_name` varchar(255) DEFAULT NULL COMMENT '产品名称',
  `device_description` varchar(500) DEFAULT NULL COMMENT '位置备注',
  `have_entrance_guard` int DEFAULT 0 COMMENT '产品是否包含门禁 0：否 1：是',
  `node_id` varchar(255) DEFAULT NULL COMMENT '节点id',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='设备';

DROP TABLE IF EXISTS `device_data`;
CREATE TABLE `device_data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_name` varchar(255) DEFAULT NULL COMMENT '设备名称',
  `iot_id` varchar(255) DEFAULT NULL COMMENT '设备ID',
  `product_key` varchar(255) DEFAULT NULL COMMENT '所属产品的key',
  `product_name` varchar(255) DEFAULT NULL COMMENT '产品名称',
  `function_id` varchar(255) DEFAULT NULL COMMENT '功能名称',
  `access_location` varchar(255) DEFAULT NULL COMMENT '接入位置',
  `location_type` int DEFAULT NULL COMMENT '位置类型 0：随身设备 1：固定设备',
  `physical_location_type` int DEFAULT NULL COMMENT '物理位置类型 0楼层 1房间 2床位',
  `device_description` varchar(500) DEFAULT NULL COMMENT '位置备注',
  `data_value` varchar(255) DEFAULT NULL COMMENT '数据值',
  `alarm_time` datetime DEFAULT NULL COMMENT '数据上报时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='设备数据';

DROP TABLE IF EXISTS `family_member`;
CREATE TABLE `family_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
  `name` varchar(64) DEFAULT NULL COMMENT '名称',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `open_id` varchar(255) DEFAULT NULL COMMENT 'OpenID',
  `gender` int DEFAULT NULL COMMENT '性别 0:男 1:女',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='老人家属';

DROP TABLE IF EXISTS `family_member_elder`;
CREATE TABLE `family_member_elder` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `family_member_id` bigint DEFAULT NULL COMMENT '家属id',
  `elder_id` bigint DEFAULT NULL COMMENT '老人id',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='客户老人关联';

DROP TABLE IF EXISTS `health_assessment`;
CREATE TABLE `health_assessment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elder_name` varchar(64) DEFAULT NULL COMMENT '老人姓名',
  `id_card` varchar(32) DEFAULT NULL COMMENT '身份证号',
  `birth_date` datetime DEFAULT NULL COMMENT '出生日期',
  `age` int DEFAULT NULL COMMENT '年龄',
  `gender` int DEFAULT NULL COMMENT '性别 0:男 1:女',
  `health_score` varchar(64) DEFAULT NULL COMMENT '健康评分',
  `risk_level` varchar(64) DEFAULT NULL COMMENT '危险等级',
  `suggestion_for_admission` int DEFAULT NULL COMMENT '是否建议入住 0:建议 1:不建议',
  `nursing_level_name` varchar(64) DEFAULT NULL COMMENT '推荐护理等级',
  `admission_status` int DEFAULT NULL COMMENT '入住情况 0:已入住 1:未入住',
  `total_check_date` varchar(64) DEFAULT NULL COMMENT '总检日期',
  `physical_exam_institution` varchar(255) DEFAULT NULL COMMENT '体检机构',
  `physical_report_url` varchar(500) DEFAULT NULL COMMENT '体检报告URL链接',
  `assessment_time` datetime DEFAULT NULL COMMENT '评估时间',
  `report_summary` text DEFAULT NULL COMMENT '报告总结',
  `disease_risk` text DEFAULT NULL COMMENT '疾病风险',
  `abnormal_analysis` text DEFAULT NULL COMMENT '异常分析',
  `system_score` varchar(255) DEFAULT NULL COMMENT '健康系统分值',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='健康评估';

DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL COMMENT '预约人姓名',
  `mobile` varchar(32) DEFAULT NULL COMMENT '预约人手机号',
  `time` datetime DEFAULT NULL COMMENT '预约时间',
  `visitor` varchar(64) DEFAULT NULL COMMENT '探访人',
  `type` int DEFAULT NULL COMMENT '预约类型 0：参观预约 1：探访预约',
  `status` int DEFAULT NULL COMMENT '预约状态 0：待报道 1：已完成 2：取消 3：过期',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预约信息';

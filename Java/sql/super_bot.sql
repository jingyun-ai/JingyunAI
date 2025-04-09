/*
 SuperBot Database Schema (Optimized)
 Version: 2024.03
 Character Set: utf8mb4
 Collation: utf8mb4_unicode_ci
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for drawing
-- ----------------------------
DROP TABLE IF EXISTS `drawing`;
CREATE TABLE `drawing` (
  `drawing_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '绘图ID',
  `user_id` bigint(20) NOT NULL COMMENT '所属用户',
  `prompt` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提示词',
  `original_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上传图',
  `generate_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生成图',
  `is_public` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否公开',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `env` tinyint(4) NOT NULL DEFAULT 0,
  PRIMARY KEY (`drawing_id`) USING BTREE,
  INDEX `idx_env` (`env`),
  INDEX `idx_generate_url` (`generate_url`),
  INDEX `idx_created_time` (`created_time`),
  INDEX `idx_is_public` (`is_public`),
  INDEX `idx_update_time` (`update_time`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for drawingsimple
-- ----------------------------
DROP TABLE IF EXISTS `drawingsimple`;
CREATE TABLE `drawingsimple` (
  `taskid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `userid` bigint(20) NOT NULL,
  `frequency` bigint(20) NOT NULL,
  `createdtime` datetime DEFAULT CURRENT_TIMESTAMP,
  `updatetime` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`taskid`) USING BTREE,
  INDEX `idx_userid` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for exchange
-- ----------------------------
DROP TABLE IF EXISTS `exchange`;
CREATE TABLE `exchange` (
  `exchange_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '兑换码ID',
  `exchange_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '兑换码',
  `frequency` bigint(20) NOT NULL COMMENT '兑换码所含Ai币',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`exchange_id`) USING BTREE,
  UNIQUE INDEX `uidx_exchange_code` (`exchange_code`),
  INDEX `idx_created_time` (`created_time`),
  INDEX `idx_frequency` (`frequency`),
  INDEX `idx_update_time` (`update_time`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `orders_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `product_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_price` double NOT NULL,
  `state` tinyint(4) NOT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `pay_time` datetime DEFAULT NULL,
  `frequency` bigint(20) NOT NULL,
  `reason_failure` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`orders_id`) USING BTREE,
  INDEX `idx_product_id` (`product_id`),
  INDEX `idx_state` (`state`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for personality
-- ----------------------------
DROP TABLE IF EXISTS `personality`;
CREATE TABLE `personality` (
  `personality_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '所属用户',
  `model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型名称',
  `top_p` tinyint(4) NOT NULL,
  `max_tokens` bigint(20) NOT NULL COMMENT '最大Token数',
  `temperature` tinyint(4) NOT NULL COMMENT '温度参数',
  `open_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'chatgpt密钥',
  `open_ai_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'chatgpt请求地址',
  `questions` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题',
  `answer` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回答',
  `speed` bigint(20) NOT NULL COMMENT '回复速率',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`personality_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `product_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `frequency` bigint(20) NOT NULL,
  `product_price` double NOT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`) USING BTREE,
  INDEX `idx_product_name` (`product_name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for sd_model
-- ----------------------------
DROP TABLE IF EXISTS `sd_model`;
CREATE TABLE `sd_model` (
  `sd_model_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `text_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sd_model_id`) USING BTREE,
  INDEX `idx_model_text` (`model_name`, `text_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for star
-- ----------------------------
DROP TABLE IF EXISTS `star`;
CREATE TABLE `star` (
  `star_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '所属用户',
  `issue` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '答案',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`star_id`) USING BTREE,
  INDEX `idx_created_time` (`created_time`),
  INDEX `idx_update_time` (`update_time`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for star_preset
-- ----------------------------
DROP TABLE IF EXISTS `star_preset`;
CREATE TABLE `star_preset` (
  `star_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '所属用户',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`star_id`) USING BTREE,
  INDEX `idx_created_time` (`created_time`),
  INDEX `idx_update_time` (`update_time`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `open_id` varchar(180) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信用户标识',
  `avatar` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户头像',
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户微信昵称',
  `email` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` enum('ADMIN','USER') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USER',
  `frequency` bigint(20) NOT NULL DEFAULT 0 COMMENT 'Ai币',
  `is_sign_in` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否签到',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE,
  INDEX `idx_created_time` (`created_time`),
  INDEX `idx_frequency` (`frequency`),
  INDEX `idx_open_id` (`open_id`),
  INDEX `idx_update_time` (`update_time`),
  INDEX `idx_email_password` (`email`, `password`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
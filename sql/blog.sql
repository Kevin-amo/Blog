/*
 Navicat Premium Dump SQL

 Source Server         : 20240618
 Source Server Type    : MySQL
 Source Server Version : 80012 (8.0.12)
 Source Host           : localhost:3306
 Source Schema         : blog

 Target Server Type    : MySQL
 Target Server Version : 80012 (8.0.12)
 File Encoding         : 65001

 Date: 16/04/2026 14:48:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章标题',
  `summary` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文章摘要',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章内容',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面地址',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布',
  `is_top` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `view_count` int(11) NOT NULL DEFAULT 0 COMMENT '浏览量',
  `comment_count` int(11) NOT NULL DEFAULT 0 COMMENT '评论数',
  `audit_status` tinyint(4) NOT NULL COMMENT '审核状态 0-未审核 1-审核通过 2-审核拒绝',
  `create_by` bigint(20) NOT NULL COMMENT '创建人/作者ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_create_by`(`create_by` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_is_top`(`is_top` ASC) USING BTREE,
  INDEX `idx_is_deleted`(`is_deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of article
-- ----------------------------
INSERT INTO `article` VALUES (1, '测试文章1', '', '## 测试\n\n> 这是一篇测试文章', '', 1, 1, 0, 0, 2, 1, 2, '2026-03-29 14:48:59', 1, '2026-04-15 11:28:31', 0);
INSERT INTO `article` VALUES (2, 'testQPS', '', '测试3s自动保存草稿撒撒你的阿斯利康士大夫老大是客服会尽量快点撒辣椒粉客户大师复活节卡的萨芬和框架十大是 发 撒 阿斯 是sdfdsafdsawho you are\ndnishifcddddf', '', 2, 0, 0, 0, 0, 0, 2, '2026-03-29 18:41:01', 2, '2026-04-16 11:00:09', 0);
INSERT INTO `article` VALUES (3, '测试驳回', '', '## 搓泥娘的撇', '', 2, 1, 0, 0, 0, 2, 2, '2026-03-29 18:49:19', 1, '2026-03-29 18:50:54', 0);

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '生活', 0, 1, '2026-03-27 17:54:44', '2026-03-27 17:54:44', 0);
INSERT INTO `category` VALUES (2, '技术', 1, 1, '2026-03-28 10:33:43', '2026-03-28 10:33:43', 0);

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父评论ID，0表示一级评论',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '昵称',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论内容',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
  `is_deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '评论作者ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_comment_article_id`(`article_id` ASC) USING BTREE,
  INDEX `idx_comment_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_comment_create_by`(`create_by` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 1, 0, '测试用户', '测试评论123', 1, 0, 2, '2026-04-15 21:45:28');
INSERT INTO `comment` VALUES (2, 1, 0, '测试用户', '****', 1, 0, 2, '2026-04-15 21:46:01');
INSERT INTO `comment` VALUES (3, 1, 0, '测试用户', '测试', 1, 0, 2, '2026-04-15 21:46:30');
INSERT INTO `comment` VALUES (4, 1, 0, '测试用户', '****是不是违禁词', 1, 0, 2, '2026-04-15 21:46:48');
INSERT INTO `comment` VALUES (5, 1, 0, '测试用户', '走一梦是**', 1, 0, 2, '2026-04-15 21:47:05');
INSERT INTO `comment` VALUES (6, 1, 0, '测试用户', '死了没', 1, 0, 2, '2026-04-15 21:49:31');
INSERT INTO `comment` VALUES (7, 1, 6, '普通用户-测试1', '**妈', 1, 0, 2, '2026-04-16 10:47:05');
INSERT INTO `comment` VALUES (8, 1, 6, '普通用户-测试1', '123', 1, 0, 2, '2026-04-16 10:47:15');
INSERT INTO `comment` VALUES (9, 1, 0, '普通用户-测试1', '****', 1, 0, 2, '2026-04-16 14:29:03');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像url',
  `role` tinyint(4) NOT NULL COMMENT '0普通用户，1管理员',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '1 启用，0 禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$FMtg1Jp1zCykhEDyRNJd8OhH8UBXDhP6UbSONfbkh.cKXgzoDp4ma', 'admin', NULL, 1, 1, '2026-03-26 11:13:47', '2026-03-29 14:32:27');
INSERT INTO `user` VALUES (2, 'admin1', '$2a$10$GTAsMV5eJSV3rnjWMoa/H.fst2tiny5Py2FBe/GZp7lsR6yoXTTuq', '普通用户-测试1', 'https://lqr-blog.oss-cn-hangzhou.aliyuncs.com/avatar/2/202603/10f9c1db18954bb8a1b7cb8c396f9ab7.jpg', 0, 1, '2026-03-28 22:27:08', '2026-03-29 19:29:43');

SET FOREIGN_KEY_CHECKS = 1;
